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

import simora.shared.SanityCheck
import simora.simulator_core.ITimer
import simora.simulator_iot.ILogger
import simora.simulator_iot.IPayload
import simora.simulator_iot.config.Configuration
import simora.simulator_iot.models.Device
import simora.simulator_iot.models.net.NetworkPackage

internal class ApplicationStack_RPL_Fast(
    private val child: IApplicationStack_Actuator,
    private val logger: ILogger,
    private val config: Configuration,
) : IApplicationStack_Rooter {
    init {
        child.setRouter(this)
    }

    private lateinit var parent: Device
    private var isRoot = false
    private var routingTable = intArrayOf()
    private var routingTableDatabaseHops = Array(config.features.size) { intArrayOf() }
    override fun setDevice(device: Device) {
        parent = device
    }

    override fun setRoot() {
        isRoot = true
    }

    override fun getNextFeatureHops(destinationAddresses: IntArray, flag: Int): IntArray = IntArray(destinationAddresses.size) { routingTableDatabaseHops[flag][destinationAddresses[it]] }
    override fun send(destinationAddress: Int, pck: IPayload) {
        val pck2 = NetworkPackage(parent.address, destinationAddress, pck)
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
    private fun generateRoutingTableUsingGlobalParentTable(globalParentTable: IntArray) {
        routingTable = IntArray(config.devices.size) { -1 }
        routingTable[parent.address] = parent.address // myself
        for (i in 0 until config.devices.size) {
            if (globalParentTable[i] == parent.address) {
                routingTable[i] = i // the next hops write down their own address
            }
        }
        var changed2 = true
        while (changed2) {
            changed2 = false
            for (i in 0 until config.devices.size) {
                if (routingTable[i] == -1 && routingTable[globalParentTable[i]] != -1) {
                    routingTable[i] = routingTable[globalParentTable[i]] // their next hop is known, so use it
                    changed2 = true
                }
            }
        }
        for (i in 0 until config.devices.size) {
            if (routingTable[i] == -1) {
                SanityCheck.check(
                    { /*SOURCE_FILE_START*/"/src/simora/src/commonMain/kotlin/simora/simulator_iot/applications/ApplicationStack_RPL_Fast.kt:104"/*SOURCE_FILE_END*/ },
                    { !isRoot }, // no route possible
                )
                routingTable[i] = globalParentTable[parent.address] // everything else goes to my own parent
            }
        }
        routingTableDatabaseHops = Array(config.features.size) { IntArray(config.devices.size) { -1 } }

        for (flag in 0 until config.features.size) {
            val devicesWithDatabase = config.getAllDevicesForFeature(flag).map { it.address }
            routingTableDatabaseHops[flag][parent.address] = parent.address // myself
            if (devicesWithDatabase.contains(parent.address)) {
                val localParentTable = IntArray(globalParentTable.size) { globalParentTable[it] }
                var changed = true
                while (changed) {
                    changed = false
                    for (d in 0 until globalParentTable.size) {
                        if (devicesWithDatabase.contains(localParentTable[d])) {
// done, my parent is a database
                        } else if (localParentTable[d] == localParentTable[localParentTable[d]]) {
// done, endless loop
                        } else {
                            localParentTable[d] = localParentTable[localParentTable[d]]
                            changed = true
                        }
                    }
                }
                changed = true
                while (changed) {
                    changed = false
                    for (d in 0 until globalParentTable.size) {
                        if (localParentTable[d] == parent.address) {
// done, i am a direct neighbor
                        } else if (localParentTable[localParentTable[d]] == parent.address) {
// done, i am a indirect neighbor
                        } else if (localParentTable[d] == localParentTable[localParentTable[d]]) {
// done, endless loop
                        } else {
                            localParentTable[d] = localParentTable[localParentTable[d]]
                            changed = true
                        }
                    }
                }
                for (i in 0 until config.devices.size) {
                    if (localParentTable[i] == parent.address) {
                        routingTableDatabaseHops[flag][i] = i
                    } else {
                        routingTableDatabaseHops[flag][i] = localParentTable[i]
                    }
                }
            }
        }
        for (dest in 0 until config.devices.size) {
            val hop = routingTable[dest]
            logger.addConnectionTable(parent.address, dest, hop)
        }
    }

    override fun startUp() {
        val globalParentTable = IntArray(config.devices.size) { -1 }
        val globalParentCosts = DoubleArray(config.devices.size) { Double.MAX_VALUE }
        if (isRoot) {
            globalParentCosts[parent.address] = 0.0
            globalParentTable[parent.address] = parent.address
            val queue = mutableListOf(parent)
            while (queue.size > 0) {
                val a = queue.removeAt(0)
                for (b in a.linkManager.getNeighbours().map { config.devices[it] }) {
                    val distance = a.location.getDistanceInMeters(b.location) + 0.0001
                    val p = if (globalParentCosts[a.address] < globalParentCosts[b.address]) {
                        a to b
                    } else {
                        b to a
                    }
                    if (globalParentCosts[p.second.address] > globalParentCosts[p.first.address] + distance) {
                        globalParentCosts[p.second.address] = globalParentCosts[p.first.address] + distance
                        globalParentTable[p.second.address] = p.first.address
                        queue.add(p.second)
                    }
                }
            }
            for (d in config.devices) {
                (d.applicationStack as ApplicationStack_RPL_Fast).generateRoutingTableUsingGlobalParentTable(globalParentTable)
            }
        }
        child.startUp()
    }
}
