/*
 * This file is part of the Luposdate3000 distribution (https://github.com/luposdate3000/SIMORA).
 * Copyright (c) 2020-2021, Institute of Information Systems (Benjamin Warnke and contributors of LUPOSDATE3000), University of Luebeck
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package simora.simulator_iot

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import simora.parser.IJsonParserValue
import simora.parser.JsonParser
import simora.parser.JsonParserArray
import simora.parser.JsonParserObject
import simora.parser.JsonParserString
import simora.shared.inline.File
import simora.simulator_core.Event
import simora.simulator_core.PriorityQueue
import simora.simulator_iot.applications.ApplicationStack_AllShortestPath
import simora.simulator_iot.applications.ApplicationStack_CatchSelfMessages
import simora.simulator_iot.applications.ApplicationStack_Logger
import simora.simulator_iot.applications.ApplicationStack_MergeMessages
import simora.simulator_iot.applications.ApplicationStack_MulticastNone
import simora.simulator_iot.applications.ApplicationStack_MulticastRouting
import simora.simulator_iot.applications.ApplicationStack_MulticastSimple
import simora.simulator_iot.applications.ApplicationStack_MultipleChilds
import simora.simulator_iot.applications.ApplicationStack_RPL
import simora.simulator_iot.applications.ApplicationStack_RPL_Fast
import simora.simulator_iot.applications.ApplicationStack_Sequence
import simora.simulator_iot.applications.IApplicationFeature
import simora.simulator_iot.applications.IApplicationStack_Actuator
import simora.simulator_iot.applications.IApplication_Factory
import simora.simulator_iot.models.Device
import simora.simulator_iot.models.geo.LatLngTool
import simora.simulator_iot.models.net.LinkManager
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt
public class SimulationRun {
    internal companion object {
        internal const val defaultOutputDirectory: String = "simulator_output/"
    }
    internal var startConfigurationStamp: Instant = Clock.System.now()
    public var notInitializedClock: Long = -1
    public var simMaxClock: Long = notInitializedClock
    public var maxClock: Long = Long.MAX_VALUE
    public var clock: Long = 0
    private val randGenerator = RandomGenerator()
    internal val logger: Loggers = Loggers(mutableListOf())
    private var addedEventCounter: Int = 0
    private var futureEvents: PriorityQueue<Event> = PriorityQueue()
    internal var routingHelper: Any? = null
    private val factories = mutableMapOf<String, IApplication_Factory>()
    internal val features: MutableList<IApplicationFeature> = mutableListOf(RoutingFeature())
    private val featureIDRouting = 0
    public var devices: MutableList<Device> = mutableListOf()
    private var namedAddresses: MutableMap<String, Int> = mutableMapOf()
    public var outputDirectory: String = defaultOutputDirectory
    private var json: JsonParserObject? = null
    public var rootRouterAddress: Int = -1
    private var sortedLinkTypes: Array<LinkType> = emptyArray()

    internal fun getAllDevicesForFeature(feature: Int): List<Device> = devices.filter { hasFeature(it, feature) }

    public fun featureIdForName2(name: String): Int {
        for (i in 0 until features.size) {
            if (features[i].getName() == name) {
                return i
            }
        }
        return -1
    }

    public fun hasFeature(device: Device, feature: Int): Boolean {
        if (feature < 0) {
            return false
        }
        val f = features[feature]
        for (app in device.applicationStack.getAllChildApplications()) {
            if (f.hasFeature(app)) {
                return true
            }
        }
        return false
    }

    private fun parse(json: JsonParserObject, fileName: String, autocorrect: Boolean = true) {
        this.json = json
        outputDirectory = json.getOrDefault("outputDirectory", defaultOutputDirectory) + "/"
        if (outputDirectory == "") {
            outputDirectory = defaultOutputDirectory
            json["outputDirectory"] = defaultOutputDirectory
        }
        val jsonLoggers = json.getOrEmptyObject("logging")
        for ((loggerName, loggerJson) in jsonLoggers) {
            loggerJson as JsonParserObject
            val enabled = loggerJson.getOrDefault("enabled", false)
            if (enabled) {
                val log = ReflectionHelper.create(loggerName) as ILogger
                log.initialize(this)
                logger.loggers.add(log)
            }
        }
// load all link types --->>>
        setLinkTypes(
            json.getOrEmptyObject("linkType").iterator().asSequence().map {
                val v = it.second
                v as JsonParserObject
                LinkType(
                    it.first,
                    v.getOrDefault("rangeInMeters", 0),
                    v.getOrDefault("dataRateInKbps", 0),
                )
            }.toList().toTypedArray()
        )
// load all link types <<<---
        for ((name, fixedDevice) in json.getOrEmptyObject("fixedDevice")) {
            fixedDevice as JsonParserObject
            val created = createDevice(
                fixedDevice.getOrDefault("deviceType", ""),
                fixedDevice,
                JsonParserObject(mutableMapOf()),
            )
            namedAddresses[name] = created.address
        }
        val rootRouterName = json.getOrDefault("rootRouter", "")
        if (rootRouterName.isNotEmpty()) {
            val device = getDeviceByName(rootRouterName)
            device.applicationStack.setRoot()
            rootRouterAddress = device.address
        }
// assign all static links --->>>
        for (l in json.getOrEmptyArray("fixedLink")) {
            l as JsonParserObject
            val a = getDeviceByName(l.getOrDefault("fixedDeviceA", ""))
            val b = getDeviceByName(l.getOrDefault("fixedDeviceB", ""))
            link(a, b, l.getOrDefault("dataRateInKbps", 0))
        }
// assign all static links <<<---
        createPattern(
            json.getOrEmptyArray("patterns"),
            null,
            null,
            JsonParserObject(mutableMapOf()),
            null,
        )
// assign all dynamic links --->>>
        createAvailableLinks(devices)
// assign all dynamic links <<<---
        if (autocorrect) {
            File(fileName).withOutputStream { out ->
                out.println(JsonParser().jsonToString(json, false))
            }
        }
    }

    public fun getDeviceByName(name: String): Device {
        val index = namedAddresses[name]!!
        return devices[index]
    }

    private fun createDevice(deviceTypeName: String, jsonDeviceParam: JsonParserObject, valuesPassThrough: JsonParserObject): Device {
        val ownAddress = devices.size
// device json-->>
        val deviceTypes2 = json!!.getOrEmptyObject("deviceType")
        val deviceType2 = deviceTypes2.getOrEmptyObject(deviceTypeName)
        val jsonDevice = valuesPassThrough.cloneJson()
        jsonDevice.mergeWith(deviceType2.cloneJson())
        jsonDevice.mergeWith(jsonDeviceParam.cloneJson())
// device json<<--
        val latitude = jsonDevice.getOrDefault("latitude", 0.0)
        val longitude = jsonDevice.getOrDefault("longitude", 0.0)
// applications-->>
        val applications = mutableListOf<IApplicationStack_Actuator>()
        val jsonApplicationsEffective = json!!.getOrEmptyObject("applications").cloneJson()
        jsonApplicationsEffective.mergeWith(jsonDevice.getOrEmptyObject("applications").cloneJson())
        for ((applicationName, applicationJsonTmp) in jsonApplicationsEffective) {
            var applicationJson: IJsonParserValue = jsonDevice.cloneJson()
            if (applicationJsonTmp is JsonParserObject) {
                applicationJson as JsonParserObject
                applicationJson.mergeWith(applicationJsonTmp.cloneJson())
            } else {
                applicationJson = applicationJsonTmp
            }
            var factory = factories[applicationName]
            if (factory == null) {
                factory = ReflectionHelper.create(applicationName) as IApplication_Factory
                factory.registerFeatures(features)
                factories[applicationName] = factory
            }
            applications.addAll(
                factory.create(
                    applicationJson,
                    ownAddress,
                    logger,
                    outputDirectory,
                    randGenerator,
                    factories,
                )
            )
        }
        val applicationStack = ApplicationStack_CatchSelfMessages(
            ownAddress,
            ApplicationStack_MultipleChilds(applications.map { ApplicationStack_Logger(ownAddress, logger, it) }.toTypedArray()),
        )
// applications<<--
        val jsonRouting = json!!.getOrEmptyObject("routing")
        val multicastLayer = ApplicationStack_MergeMessages(
            ApplicationStack_Sequence(
                ownAddress,
                when (jsonRouting.getOrDefault("multicast", "None")) {
                    "None" -> ApplicationStack_MulticastNone(applicationStack)
                    "ApplicationSide", "Simple" -> ApplicationStack_MulticastSimple(applicationStack)
                    "StateOfTheArt", "Routing" -> ApplicationStack_MulticastRouting(false, featureIDRouting, ownAddress, applicationStack)
                    "RoutingAndApplication" -> ApplicationStack_MulticastRouting(true, featureIDRouting, ownAddress, applicationStack)
                    else -> TODO("unknown multicast implementation '${jsonRouting.getOrDefault("multicast", "None")}'")
                }
            )
        )
        val router = when (jsonRouting.getOrDefault("protocol", "RPL")) {
            "AllShortestPath" -> ApplicationStack_AllShortestPath(
                multicastLayer,
                this,
            )
            "RPL_Fast" -> ApplicationStack_RPL_Fast(
                multicastLayer,
                this,
                jsonRouting.getOrDefault("lateInitRoutingTable", false),
                jsonRouting.getOrDefault("usePriorityQueue", true),
            )
            "RPL" -> ApplicationStack_RPL(
                multicastLayer,
                logger,
                this,
            )
            else -> TODO("unknown routing.protocol '${jsonRouting.getOrDefault("protocol", "RPL")}'")
        }
        val linkTypes = getSortedLinkTypeIndices(jsonDevice.getOrEmptyArray("supportedLinkTypes").map { (it as JsonParserString).value }.toMutableList())
        val device = Device(
            this,
            latitude, longitude,
            ownAddress,
            jsonDevice.getOrDefault("performance", 100.0),
            LinkManager(linkTypes),
            json!!.getOrDefault("deterministic", true),
            router,
            namedAddresses,
        )
        devices.add(device)
        logger.addDevice(ownAddress, longitude, latitude)
        createPattern(jsonDevice.getOrEmptyArray("patterns"), latitude, longitude, valuesPassThrough, device)
        return device
    }

    public fun getNumberOfDevices(): Int {
        return devices.size
    }

    public fun getDeviceByAddress(address: Int): Device {
        return devices[address]
    }

    private fun createPattern(
        patterns: JsonParserArray,
        latitude: Double?,
        longitude: Double?,
        valuesPassThrough: JsonParserObject,
        parent: Device?
    ) {
        for (rand in patterns) {
            rand as JsonParserObject
            val posLong = longitude ?: rand.getOrDefault("longitude", 0.0)
            val posLat = latitude ?: rand.getOrDefault("latitude", 0.0)
            when (rand.getOrDefault("type", "random_fill")) {
                "random_fill" -> {
                    val radius = rand.getOrDefault("radius", 0.1)
                    val count = when (rand.getOrDefault("mode", "count")) {
                        "count" -> rand.getOrDefault("count", 1)
                        "density" -> (2 * PI * radius * radius / rand.getOrDefault("density", 0.01)).toInt() + 1
                        else -> TODO()
                    }
                    val deviceTypeName = rand.getOrDefault("deviceType", "")
                    for (i in 0 until count) {
                        val p = randomCoords(radius)
                        rand["latitude"] = posLat + p.first
                        rand["longitude"] = posLong + p.second
                        val name = rand.getOrDefault("provideCounterAs", "")
                        val values = valuesPassThrough.cloneJson()
                        if (name != "") {
                            values[name] = i
                        }
                        createDevice(deviceTypeName, rand, values)
                    }
                }
                "ring" -> {
                    val radius = rand.getOrDefault("radius", 0.1)
                    val count = rand.getOrDefault("count", 1)
                    val deviceTypeName = rand.getOrDefault("deviceType", "")
                    var firstDevice: Device? = parent
                    var lastDevice: Device? = parent
                    for (i in 0 until count) {
                        val alpha = 2 * PI * i.toDouble() / count.toDouble()
                        rand["latitude"] = posLat + sin(alpha) * radius
                        rand["longitude"] = posLong + cos(alpha) * radius
                        val name = rand.getOrDefault("provideCounterAs", "")
                        val values = valuesPassThrough.cloneJson()
                        if (name != "") {
                            values[name] = i
                        }
                        val d = createDevice(deviceTypeName, rand, values)
                        if (firstDevice == null) {
                            firstDevice = d
                        } else {
                            link(d, lastDevice!!, rand.getOrDefault("dataRateInKbps", 1000))
                        }
                        lastDevice = d
                    }
                    link(firstDevice!!, lastDevice!!, rand.getOrDefault("dataRateInKbps", 1000))
                }
                "full" -> {
                    val radius = rand.getOrDefault("radius", 0.1)
                    val count = rand.getOrDefault("count", 1)
                    val deviceTypeName = rand.getOrDefault("deviceType", "")
                    val localDevices = mutableListOf<Device>()
                    if (parent != null) {
                        localDevices.add(parent)
                    }
                    for (i in 0 until count) {
                        val alpha = 2 * PI * i.toDouble() / count.toDouble()
                        rand["latitude"] = posLat + sin(alpha) * radius
                        rand["longitude"] = posLong + cos(alpha) * radius
                        val name = rand.getOrDefault("provideCounterAs", "")
                        val values = valuesPassThrough.cloneJson()
                        if (name != "") {
                            values[name] = i
                        }
                        val d = createDevice(deviceTypeName, rand, values)
                        localDevices.add(d)
                    }
                    for (i in 0 until localDevices.size) {
                        for (j in i + 1 until localDevices.size) {
                            link(localDevices[i], localDevices[j], rand.getOrDefault("dataRateInKbps", 1000))
                        }
                    }
                }
                "uniform" -> {
                    fun sunflower(n: Double, alpha: Double, action: (Int, Double, Double) -> Unit) {
                        val b = round(alpha * sqrt(n))
                        val phi = (sqrt(5.0) + 1.0) / 2.0
                        for (i in 1 until 1 + n.toInt()) {
                            val k = i.toDouble()
                            val r = if (k > n - b) {
                                1.0
                            } else {
                                sqrt(k - 1.0 / 2.0) / sqrt(n - (b + 1.0) / 2.0)
                            }
                            val theta = 2 * PI * k / (phi * phi)
                            action(i, r * cos(theta), r * sin(theta))
                        }
                    }

                    val radius = rand.getOrDefault("radius", 0.1)
                    val count = when (rand.getOrDefault("mode", "count")) {
                        "count" -> rand.getOrDefault("count", 1)
                        "density" -> (2 * PI * radius * radius / rand.getOrDefault("density", 0.01)).toInt() + 1
                        else -> TODO()
                    }
                    val deviceTypeName = rand.getOrDefault("deviceType", "")
                    sunflower(count.toDouble(), 1.0) { i, x, y ->
                        rand["latitude"] = posLat + x * radius
                        rand["longitude"] = posLong + y * radius
                        val name = rand.getOrDefault("provideCounterAs", "")
                        val values = valuesPassThrough.cloneJson()
                        if (name != "") {
                            values[name] = i
                        }
                        createDevice(deviceTypeName, rand, values)
                    }
                }
                else -> TODO()
            }
        }
    }

    private fun randomCoords(r: Double): Pair<Double, Double> {
        var a = randGenerator.getDouble(0.0, 1.0)
        var b = randGenerator.getDouble(0.0, 1.0)
        if (b < a) {
            val c = b
            b = a
            a = c
        }
        return b * r * cos(2 * PI * a / b) to b * r * sin(2 * PI * a / b)
    }

    public fun parseConfig(json: JsonParserObject, fileName: String, autocorrect: Boolean = true) {
        parse(json, fileName, autocorrect)
    }

    public fun parseConfig(fileName: String, autocorrect: Boolean = true, modifyJson: (JsonParserObject) -> Unit = {}) {
        val fileStr = File(fileName).readAsString()
        val json = JsonParser().stringToJson(fileStr) as JsonParserObject
        modifyJson(json)
        parseConfig(json, fileName, autocorrect)
    }

    public fun startSimulation() {
        maxClock = if (simMaxClock == notInitializedClock) maxClock else simMaxClock
        logger.onStartSimulation()
        startUp()
        run()
        shutDown()
        logger.onStopSimulation()
    }

    public fun run() {
        var nextEvent = futureEvents.extractMinValue()
        while (nextEvent != null) {
            if (nextEvent.occurrenceTime > maxClock) {
                break
            }
            clock = nextEvent.occurrenceTime
            val entity = nextEvent.destination
            entity.processIncomingEvent(nextEvent)
            nextEvent = futureEvents.extractMinValue()
        }
    }

    internal fun addEvent(delay: Long, src: Device, dest: Device, data: Any) {
        addedEventCounter++
        futureEvents.insert(Event(addedEventCounter, clock + delay, src, dest, data), clock + delay)
    }
    public fun startUp() {
        for (entity: Device in devices) {
            entity.simulation = this
            entity.onStartUpRouting()
        }
        var nextEvent = futureEvents.extractMinValue()
        while (nextEvent != null) {
            clock = nextEvent.occurrenceTime
            val entity = nextEvent.destination
            entity.processIncomingEvent(nextEvent)
            nextEvent = futureEvents.extractMinValue()
        }
        logger.onStartUpRouting()
        for (entity in devices) {
            entity.simulation = this
            entity.onStartUp()
        }
        logger.onStartUp()
    }

    public fun shutDown() {
        logger.onShutDown()
        for (ent in devices) {
            ent.onShutDown()
        }
    }
    private fun getLinkByName(name: String): LinkType = sortedLinkTypes.first { it.name == name }
    private fun getSortedLinkTypeIndices(linkTypeNames: List<String>): IntArray = linkTypeNames.map { getLinkByName(it) }.map { sortedLinkTypes.indexOf(it) }.sorted().toIntArray()

    private fun setLinkTypes(types: Array<LinkType>) {
        sortedLinkTypes = types.sortedByDescending { it.dataRateInKbps }.toTypedArray()
    }

    private fun createAvailableLinks(devices: MutableList<Device>) {
        for (one in devices) {
            for (two in devices) {
                if (!one.isStarNetworkChild && !two.isStarNetworkChild) {
                    linkIfPossible(one, two)
                }
            }
        }
    }

    private fun linkIfPossible(one: Device, two: Device) {
        if (one != two && !one.linkManager.hasLink(two)) {
            val distance = getDistanceInMeters(one, two)
            val oneIndices = one.linkManager.supportedLinkTypes
            val twoIndices = two.linkManager.supportedLinkTypes
            loop@ for (i in oneIndices) {
                for (i2 in twoIndices) {
                    if (i == i2) {
                        if (distance <= sortedLinkTypes[i].rangeInMeters) {
                            one.linkManager.addLink(two.address, sortedLinkTypes[i].dataRateInKbps)
                            two.linkManager.addLink(one.address, sortedLinkTypes[i].dataRateInKbps)
                            return
                        }
                    }
                }
            }
        }
    }

    internal fun getDistanceInMeters(one: Device, two: Device): Double = LatLngTool.getDistanceInMeters(one.latitude, one.longitude, two.latitude, two.longitude)

    private fun link(one: Device, two: Device, dataRate: Int) {
        one.linkManager.addLink(two.address, dataRate)
        two.linkManager.addLink(one.address, dataRate)
    }
}
