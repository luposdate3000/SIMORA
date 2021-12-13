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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class LoggerMeasure : ILogger {
    private lateinit var simRun: SimulationRun
    override fun initialize(simRun: SimulationRun) {
        this.simRun = simRun
    }

    internal companion object {
        internal var StatCounter: Int = 0
        private val StatNumberOfDevices: Int = StatCounter++
        private val StatNetworkLinkCounter: Int = StatCounter++

        private val StatSimulationStartupDurationReal: Int = StatCounter++
        private val StatSimulationShutdownDurationReal: Int = StatCounter++
        private val StatSimulationDurationReal: Int = StatCounter++
        private val StatSimulationDurationVirtual: Int = StatCounter++

        private val StatNetworkCounterForwarded: Int = StatCounter++
        private val StatNetworkCounter: Int = StatCounter++

        private val StatNetworkTrafficForwarded: Int = StatCounter++
        private val StatNetworkTraffic: Int = StatCounter++
        private val StatNetworkTrafficIncludingLocalMessages: Int = StatCounter++
    }

    private val data: DoubleArray = DoubleArray(StatCounter)
    private val headers: Array<String> = Array(StatCounter) {
        when (it) {
            StatNumberOfDevices -> "number of devices"
            StatNetworkLinkCounter -> "number of links"

            StatSimulationStartupDurationReal -> "simulation startup duration real (Seconds)"
            StatSimulationShutdownDurationReal -> "simulation shutdown duration real (Seconds)"
            StatSimulationDurationReal -> "simulation duration real (Seconds)"
            StatSimulationDurationVirtual -> "simulation duration virtual (Seconds)"

            StatNetworkCounterForwarded -> "number of forwarded packages"
            StatNetworkCounter -> "number of sent packages"

            StatNetworkTrafficForwarded -> "network traffic forwarded(Bytes)"
            StatNetworkTraffic -> "network traffic total (Bytes)"
            StatNetworkTrafficIncludingLocalMessages -> "package size aggregated (Bytes)"
            else -> TODO("$it")
        }
    }
    private var startSimulationTimeStamp: Instant = Clock.System.now()
    private var startUpTimeStamp: Instant = Clock.System.now()
    private var shutDownTimeStamp: Instant = Clock.System.now()
    private val packageByTopic = mutableMapOf<String, Int>()
    private val packageCounter = mutableListOf<Double>()
    private val packageSize = mutableListOf<Double>()
    private val packageSizeAggregated = mutableListOf<Double>()
    private val packageSizeSelfMessage = mutableListOf<Double>()
    internal fun getDataAggregated(): DoubleArray {
        val res = mutableListOf<Double>()
        for (d in data) {
            res.add(d)
        }
        for (feature in 0 until simRun.config.features.size) {
            var counter = 0.0
            for (d in simRun.config.devices) {
                if (simRun.config.hasFeature(d, feature)) {
                    counter++
                }
            }
            res.add(counter)
        }
        for (topicId in 0 until packageByTopic.size) {
            res.add(packageCounter[topicId])
            res.add(packageSize[topicId])
            res.add(packageSizeAggregated[topicId])
            res.add(packageSizeSelfMessage[topicId])
        }
        return res.toDoubleArray()
    }

    internal fun getHeadersAggregated(): Array<String> {
        val res = mutableListOf<String>()
        for (h in headers) {
            res.add(h)
        }
        for (feature in simRun.config.features) {
            res.add("number of devices having '" + feature.getName() + "'")
        }
        val packageByTopicReverse = mutableMapOf<Int, String>()
        for ((k, v) in packageByTopic) {
            packageByTopicReverse[v] = k
        }
        for (topicId in 0 until packageByTopic.size) {
            val topic = packageByTopicReverse[topicId]
            res.add("package count for '$topic'")
            res.add("package size for '$topic'")
            res.add("package size aggregated for '$topic'")
            res.add("package size self Messages for '$topic'")
        }
        return res.toTypedArray()
    }

    override fun onSendNetworkPackage(src: Int, dest: Int, hop: Int, pck: IPayload, delay: Long) {
        data[StatNetworkTraffic] += pck.getSizeInBytes().toDouble()
        data[StatNetworkCounter]++
        if (dest != hop) {
            data[StatNetworkCounterForwarded]++
            data[StatNetworkTrafficForwarded] += pck.getSizeInBytes().toDouble()
        }
        if (pck is IPayloadLayer) {
            for (p in pck.getApplicationPayload()) {
                onSendNetworkPackageInternal(src, dest, hop, p)
            }
        } else {
            onSendNetworkPackageInternal(src, dest, hop, pck)
        }
    }

    private fun onSendNetworkPackageInternal(src: Int, dest: Int, hop: Int, pck: IPayload) {
        val topic = pck.getTopic()
        var id = packageByTopic[topic]
        val size = pck.getSizeInBytes().toDouble()
        if (id == null) {
            id = packageByTopic.size
            packageByTopic[topic] = id
            packageCounter.add(0.0)
            packageSize.add(0.0)
            packageSizeAggregated.add(0.0)
            packageSizeSelfMessage.add(0.0)
        }
        if (src == dest) {
            packageSizeSelfMessage[id] += size
        }
        if (dest == hop) {
            packageCounter[id]++
            packageSize[id] += size
        }
        packageSizeAggregated[id] += size
        data[StatNetworkTrafficIncludingLocalMessages] += size
    }

    override fun onReceiveNetworkPackage(address: Int, pck: IPayload) {}
    override fun onSendPackage(src: Int, dest: Int, pck: IPayload) {
        if (src == dest) {
            // self messages never produce network packages .. therefore catch them here
            onSendNetworkPackageInternal(src, dest, dest, pck)
        }
    }

    override fun onReceivePackage(address: Int, pck: IPayload) {}
    override fun addConnectionTable(src: Int, dest: Int, hop: Int) {}

    override fun onStartSimulation() { // phase 1
        startSimulationTimeStamp = Clock.System.now()
    }

    override fun onStartUp() { // phase 2
        startUpTimeStamp = Clock.System.now()
        data[StatSimulationStartupDurationReal] = (startUpTimeStamp - startSimulationTimeStamp).inWholeNanoseconds.toDouble() / 1000000000.0
        data[StatNetworkLinkCounter] = simRun.config.devices.sumOf { d ->
            d.linkManager.getNeighbours().filter { it > d.address }.size
        }.toDouble()
    }

    override fun onSteadyState() { // phase 3
    }

    override fun onShutDown() { // phase 4
        val shutDownTimeStampVirtual = startSimulationTimeStamp.plus(simRun.sim.clock, DateTimeUnit.NANOSECOND, TimeZone.UTC)
        shutDownTimeStamp = Clock.System.now()
        data[StatSimulationDurationReal] = (shutDownTimeStamp - startSimulationTimeStamp).inWholeNanoseconds.toDouble() / 1000000000.0
        data[StatSimulationDurationVirtual] = (shutDownTimeStampVirtual - startSimulationTimeStamp).inWholeNanoseconds.toDouble() / 1000000000.0
    }

    override fun onStopSimulation() { // phase 5
        val stopSimulationTimeStamp = Clock.System.now()
        data[StatSimulationShutdownDurationReal] = (stopSimulationTimeStamp - shutDownTimeStamp).inWholeNanoseconds.toDouble() / 1000000000.0
    }

    override fun addDevice(address: Int, x: Double, y: Double) {
        data[StatNumberOfDevices]++
    }
}
