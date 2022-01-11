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

package simora.simulator_iot.applications
import simora.simulator_core.ITimer
import simora.simulator_core.PriorityQueue
import kotlin.native.concurrent.ThreadLocal
import simora.simulator_iot.Config
import simora.simulator_iot.IPayload
import simora.simulator_iot.SimulationRun
import simora.simulator_iot.models.Device
import simora.simulator_iot.models.net.LinkManagerList
import simora.simulator_iot.models.net.NetworkPackage
import kotlin.jvm.JvmField
internal class ApplicationStack_RPL_Fast(
    private val child: IApplicationStack_Actuator,
    private val config: SimulationRun,
    private val lateInitRoutingTable: Boolean,
) : IApplicationStack_Rooter {
    init {
        child.setRouter(this)
    }

    private lateinit var parent: Device
    private var isRoot = false
    private var isRoutingTableInitialized = false
    private var routingTable = intArrayOf()
    private var routingTableFeatureHops = Array(config.features.size) { intArrayOf() }
    override fun setDevice(device: Device) {
        parent = device
    }

    override fun setRoot() {
        isRoot = true
    }

    override fun getNextFeatureHops(destinationAddresses: IntArray, flag: Int): IntArray {
        initRoutingTable()
        return IntArray(destinationAddresses.size) { routingTableFeatureHops[flag][destinationAddresses[it]] }
    }
    override fun send(destinationAddress: Int, pck: IPayload) {
        val pck2 = NetworkPackage(parent.address, destinationAddress, pck)
        initRoutingTable()
        val hop = routingTable[destinationAddress]
        val delay = parent.getNetworkDelay(hop, pck2)
        parent.assignToSimulation(destinationAddress, hop, pck2, delay)
    }

    override fun receive(pck: IPayload): IPayload? {
        pck as NetworkPackage
        val payload = pck.payload
        if (pck.destinationAddress == parent.address) {
            child.receive(payload)
        } else {
            initRoutingTable()
            val hop = routingTable[pck.destinationAddress]
            val delay = parent.getNetworkDelay(hop, pck)
            parent.assignToSimulation(pck.destinationAddress, hop, pck, delay)
        }
        return null
    }

    override fun getAllChildApplications(): Set<IApplicationStack_Actuator> {
        val res = mutableSetOf(child)
        if (child is IApplicationStack_Middleware) {
            res.addAll(child.getAllChildApplications())
        }
        return res
    }

    override fun flush() {}
    override fun registerTimer(durationInNanoSeconds: Long, entity: ITimer): Unit = parent.registerTimer(durationInNanoSeconds, entity)
    override fun closestDeviceWithFeature(name: String): Int = parent.closestDeviceWithFeature(name)
    override fun resolveHostName(name: String): Int = parent.resolveHostName(name)
    override fun shutDown() = child.shutDown()
    override fun addChildApplication(child: IApplicationStack_Actuator): Unit = (this.child as IApplicationStack_Middleware).addChildApplication(child)

    @Suppress("NOTHING_TO_INLINE")
    private inline fun calculateConfigRoutingHelper() {
        if (config.routingHelper == null) {
            val linkManager = config.linkManager as LinkManagerList
            val size = config.devices.size
            val tinyMatrix = DoubleArray(size) { Double.MAX_VALUE }
            val tinyMatrixNext = IntArray(size) { -1 }
            tinyMatrix[config.rootRouterAddress] = 0.0
            tinyMatrixNext[config.rootRouterAddress] = config.rootRouterAddress
// dijkstra
            if (Config.usePriorityQueue) {
// the runtime is theoretically faster, but this allocates a lot of temporary objects
                val q = PriorityQueue<Int>()
                q.insert(config.rootRouterAddress, 0)
                var addrSrc = q.extractMinValue()
                while (addrSrc != null) {
                    val device = config.devices[addrSrc]
                    for (addrDest in linkManager.getNeighbours(addrSrc)) {
                        val cost = config.getDistanceInMeters(device, config.devices[addrDest]) + 0.0001 + tinyMatrix[addrSrc]
                        if (cost < tinyMatrix[addrDest]) {
                            tinyMatrix[addrDest] = cost
                            tinyMatrixNext[addrDest] = addrSrc
                            q.insert(addrDest, (cost * 1000.0).toLong())
                        }
                    }
                    addrSrc = q.extractMinValue()
                }
            } else {
// due to the array, here is NO garbage collection required. Array is sorted using binary search insert
                val canAddToQueue = IntArray(size) { 1 }
                val queue = IntArray(size) { -1 }
                queue[0] = config.rootRouterAddress
                canAddToQueue[config.rootRouterAddress] = 0
                var queueLeft = 0 // inclusive
                var queueRight = 0 // inclusive
                while (queueLeft <= queueRight) {
                    var addrSrc = queue[queueLeft++]
                    val device = config.devices[addrSrc]
                    for (addrDest in linkManager.getNeighbours(addrSrc)) {
                        val cost = config.getDistanceInMeters(device, config.devices[addrDest]) + 0.0001 + tinyMatrix[addrSrc]
                        if (cost < tinyMatrix[addrDest]) {
                            tinyMatrix[addrDest] = cost
                            tinyMatrixNext[addrDest] = addrSrc
                            if (canAddToQueue[addrDest] == 1) {
                                var l = queueLeft
                                var r = queueRight
                                while (l < r) {
                                    val m = (l + r) / 2
                                    val c = tinyMatrix[queue[m]]
                                    if (c <cost) {
                                        l = m + 1
                                    } else if (c> cost) {
                                        r = m - 1
                                    } else {
                                        l = m
                                        r = m
                                    }
                                }
                                var j = queueRight
                                while (j >= l) {
                                    queue[j + 1] = queue[j]
                                    j--
                                }
                                queueRight++
                                queue[l] = addrDest
                                canAddToQueue[addrDest] = 0
                            }
                        }
                    }
                }
            }
            config.routingHelper = tinyMatrixNext
            featuredDevices = Array(config.features.size) { feature -> config.getAllDevicesForFeature(feature).map { it.address }.toIntArray() }
        }
    }

    override fun startUpRouting() {
        calculateConfigRoutingHelper()
        if (!lateInitRoutingTable) {
            initRoutingTable()
        }
    }

    private fun initRoutingTable() {
        if (!isRoutingTableInitialized) {
            val address = parent.address
            val size = config.devices.size
            val helper = config.routingHelper as IntArray
            val helperAddress = helper[address]
            routingTable = IntArray(size) { helperAddress }
            routingTableFeatureHops = Array(config.features.size) { IntArray(size) { address } }
            fun treeDown(hop: Int, node: Int) {
                routingTable[hop] = node
                for (i in 0 until size) {
                    if (helper[i] == hop) {
                        val newNode = if (node == address) {
                            i
                        } else {
                            node
                        }
                        if (i != hop) {
                            treeDown(i, newNode)
                        }
                    }
                }
            }
            treeDown(address, address)
            fun treeDown2(hop: Int, node: Int, table: IntArray, devices: IntArray) {
                table[hop] = node
                for (i in 0 until size) {
                    if (helper[i] == hop) {
                        val newNode = if (node == -1 && i != address && devices.contains(i)) {
                            i
                        } else {
                            node
                        }
                        if (i != hop) {
                            treeDown2(i, newNode, table, devices)
                        }
                    }
                }
            }
            for (f in 0 until config.features.size) {
                treeDown2(address, -1, routingTableFeatureHops[f], featuredDevices[f])
            }
            var p = helper[address]
            val treeUp = IntArray(config.features.size) { -1 }
            while (true) {
                for (i in 0 until config.features.size) {
                    if (treeUp[i] == -1 && p != address && featuredDevices[i].contains(p)) {
                        treeUp[i] = p
                    }
                    routingTableFeatureHops[i][p] = treeUp[i]
                }
                if (p == config.rootRouterAddress) {
                    break
                }
                p = helper[p]
            }
            routingTable[address] = address
            for (feature in 0 until config.features.size) {
                for (i in 0 until size) {
                    if (routingTableFeatureHops[feature][i] == address) {
                        routingTableFeatureHops[feature][i] = treeUp[feature]
                    }
                }
            }
            isRoutingTableInitialized = true
        }
    }
@ThreadLocal
    internal companion object {
        @JvmField internal var featuredDevices: Array<IntArray> = Array(0) { IntArray(0) }
    }
    override fun startUp() {
        child.startUp()
    }
}
