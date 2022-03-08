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

package simora

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.native.concurrent.ThreadLocal
@OptIn(kotlin.time.ExperimentalTime::class)
internal class LoggerMeasure : ILogger {
    private lateinit var simRun: SimulationRun
    override fun initialize(simRun: SimulationRun) {
        this.simRun = simRun
    }

    @ThreadLocal
    internal companion object {
        internal var StatCounter: Int = 0
        private val StatNumberOfDevices: Int = StatCounter++
        private val StatNetworkLinkCounter: Int = StatCounter++

        private val StatSimulationStartupConfigDurationReal: Int = StatCounter++
        private val StatSimulationStartupRoutingDurationReal: Int = StatCounter++
        private val StatSimulationStartupDurationReal: Int = StatCounter++
        private val StatSimulationShutdownDurationReal: Int = StatCounter++
        private val StatSimulationDurationReal: Int = StatCounter++
        private val StatSimulationTotalDurationReal: Int = StatCounter++

        private val StatSimulationStartupRoutingDurationVirtual: Int = StatCounter++
        private val StatSimulationDurationVirtual: Int = StatCounter++

        private val StatNetworkCounterForwarded: Int = StatCounter++
        private val StatNetworkCounter: Int = StatCounter++

        private val StatNetworkTrafficForwarded: Int = StatCounter++
        private val StatNetworkTraffic: Int = StatCounter++
        private val StatNetworkTrafficIncludingLocalMessages: Int = StatCounter++
    }

    private val data: MutableList<DoubleArray> = mutableListOf(DoubleArray(StatCounter))
    private val dataLabels = mutableListOf<String>()
    private val headers: Array<String> = Array(StatCounter) {
        when (it) {
            StatNumberOfDevices -> "number of devices"
            StatNetworkLinkCounter -> "number of links"

            StatSimulationStartupConfigDurationReal -> "simulation startup duration config real (Seconds)"
            StatSimulationStartupRoutingDurationReal -> "simulation startup duration routing real (Seconds)"
            StatSimulationStartupDurationReal -> "simulation startup duration real (Seconds)"
            StatSimulationShutdownDurationReal -> "simulation shutdown duration real (Seconds)"
            StatSimulationDurationReal -> "simulation duration real (Seconds)"
            StatSimulationTotalDurationReal -> "simulation total duration real (Seconds)"

            StatSimulationStartupRoutingDurationVirtual -> "simulation startup duration routing virtual (Seconds)"
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
    private var startSimulationTimeStampVirtual: Long = 0
    private val packageByTopic = mutableMapOf<String, Int>()
    private val packageCounter = mutableListOf(mutableListOf<Double>())
    private val packageSize = mutableListOf(mutableListOf<Double>())
    private val packageSizeAggregated = mutableListOf(mutableListOf<Double>())
    private val packageSizeSelfMessage = mutableListOf(mutableListOf<Double>())

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun getDataAggregated2(): Pair<Array<DoubleArray>, Array<String>> {
        val res = mutableListOf<MutableList<Double>>()
        for (dd in data) {
            val t = mutableListOf<Double>()
            res.add(t)
            for (d in dd) {
                t.add(d)
            }
        }
        for (feature in 0 until simRun.features.size) {
            var counter = 0.0
            for (d in simRun.devices) {
                if (simRun.hasFeature(d, feature)) {
                    counter++
                }
            }
            for (t in res) {
                t.add(counter)
            }
        }
        for (topicId in 0 until packageByTopic.size) {
            for (i in 0 until res.size) {
                res[i].add(packageCounter[i][topicId])
                res[i].add(packageSize[i][topicId])
                res[i].add(packageSizeAggregated[i][topicId])
                res[i].add(packageSizeSelfMessage[i][topicId])
            }
        }
        return res.map { it.toDoubleArray() }.toTypedArray() to dataLabels.toTypedArray()
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun getHeadersAggregated(): Array<String> {
        val res = mutableListOf<String>()
        for (h in headers) {
            res.add(h)
        }
        for (feature in simRun.features) {
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
        data.last()[StatNetworkTraffic] += pck.getSizeInBytes().toDouble()
        data.last()[StatNetworkCounter]++
        if (dest != hop) {
            data.last()[StatNetworkCounterForwarded]++
            data.last()[StatNetworkTrafficForwarded] += pck.getSizeInBytes().toDouble()
        }
        if (pck is IPayloadLayer) {
            for (p in pck.getApplicationPayload()) {
                onSendNetworkPackageInternal(src, dest, hop, p)
            }
        } else {
            onSendNetworkPackageInternal(src, dest, hop, pck)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun onSendNetworkPackageInternal(src: Int, dest: Int, hop: Int, pck: IPayload) {
        val topic = pck.getTopic()
        var id = packageByTopic[topic]
        val size = pck.getSizeInBytes().toDouble()
        if (id == null) {
            id = packageByTopic.size
            packageByTopic[topic] = id
            packageCounter.last().add(0.0)
            packageSize.last().add(0.0)
            packageSizeAggregated.last().add(0.0)
            packageSizeSelfMessage.last().add(0.0)
        }
        if (src == dest) {
            packageSizeSelfMessage.last()[id] += size
        }
        if (dest == hop) {
            packageCounter.last()[id]++
            packageSize.last()[id] += size
        }
        packageSizeAggregated.last()[id] += size
        data.last()[StatNetworkTrafficIncludingLocalMessages] += size
    }

    override fun onReceiveNetworkPackage(address: Int, pck: IPayload) {}
    override fun onSendPackage(src: Int, dest: Int, pck: IPayload) {
        if (src == dest) {
            // self messages does not count as network packages
            onSendNetworkPackageInternal(src, dest, dest, pck)
        }
    }

    override fun onReceivePackage(address: Int, pck: IPayload) {}
    override fun addConnectionTable(src: Int, dest: Int, hop: Int) {}

    override fun onStartSimulation() { // phase 1
        val stamp = Clock.System.now()
        data.last()[StatSimulationStartupConfigDurationReal] = (stamp - simRun.startConfigurationStamp).inWholeNanoseconds.toDouble() / 1000000000.0
        startSimulationTimeStamp = stamp
        startSimulationTimeStampVirtual = simRun.clock
    }

    override fun onStartUpRouting() { // phase 2
        val stamp = Clock.System.now()
        data.last()[StatSimulationStartupRoutingDurationReal] = (stamp - startSimulationTimeStamp).inWholeNanoseconds.toDouble() / 1000000000.0
        data.last()[StatSimulationStartupRoutingDurationVirtual] = (simRun.clock - startSimulationTimeStampVirtual).toDouble() / 1000000000.0
    }

    override fun onStartUp() { // phase 3
        val stamp = Clock.System.now()
        data.last()[StatSimulationStartupDurationReal] = ((stamp - startSimulationTimeStamp).inWholeNanoseconds.toDouble() / 1000000000.0) - data.last()[StatSimulationStartupRoutingDurationReal]
        val linkCount = simRun.linkManager.getLinkCount().toDouble()
        data.last()[StatNetworkLinkCounter] = linkCount
    }
    override fun onShutDown() { // phase 4
        val stamp = Clock.System.now()
        data.last()[StatSimulationDurationReal] = ((stamp - startSimulationTimeStamp).inWholeNanoseconds.toDouble() / 1000000000.0) - data.last()[StatSimulationStartupRoutingDurationReal] - data.last()[StatSimulationStartupDurationReal]
        data.last()[StatSimulationDurationVirtual] = ((simRun.clock - startSimulationTimeStampVirtual).toDouble() / 1000000000.0) - data.last()[StatSimulationStartupRoutingDurationVirtual]
    }

    override fun onStopSimulation() { // phase 5
        val stamp = Clock.System.now()
        data.last()[StatSimulationShutdownDurationReal] = ((stamp - startSimulationTimeStamp).inWholeNanoseconds.toDouble() / 1000000000.0) - data.last()[StatSimulationDurationReal] - data.last()[StatSimulationStartupDurationReal] - data.last()[StatSimulationStartupRoutingDurationReal]
        data.last()[StatSimulationTotalDurationReal] = ((stamp - startSimulationTimeStamp).inWholeNanoseconds.toDouble() / 1000000000.0) + data.last()[StatSimulationStartupConfigDurationReal]
    }

    override fun addDevice(address: Int, x: Double, y: Double) {
        data.last()[StatNumberOfDevices]++
    }
    override fun reset(label: String, finish: Boolean) {
        dataLabels.add(label)
        if (!finish) {
            onShutDown()
            onStopSimulation()
            data.add(DoubleArray(StatCounter))
            packageCounter.add(mutableListOf<Double>())
            packageSize.add(mutableListOf<Double>())
            packageSizeAggregated.add(mutableListOf<Double>())
            packageSizeSelfMessage.add(mutableListOf<Double>())
            onStartSimulation()
            onStartUpRouting()
            onStartUp()
        }
    }
}
