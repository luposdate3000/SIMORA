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
import simora.simulator_iot.ILogger
import simora.simulator_iot.IPayload
import simora.simulator_iot.config.Configuration
import simora.simulator_iot.models.Device
import simora.simulator_iot.models.net.NetworkPackage

internal class ApplicationStack_AllShortestPath(
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
    private var routingTableFeatureHops = Array(config.features.size) { intArrayOf() }
    override fun setDevice(device: Device) {
        parent = device
    }

    override fun setRoot() {
        isRoot = true
    }

    override fun getNextFeatureHops(destinationAddresses: IntArray, flag: Int): IntArray = IntArray(destinationAddresses.size) { routingTableFeatureHops[flag][destinationAddresses[it]] }
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
    private fun calculateConfigRoutingHelper() {
        if (config.routingHelper == null) {
            val size = config.devices.size
            val matrix = DoubleArray(size * size) { -1.0 }
            val matrixNext = IntArray(size * size) { -1 }
// matrix self links
            for (i in 0 until size) {
                val idx = i * size + i
                matrix[idx] = 0.0
                matrixNext[idx] = i
            }
// matrix direct connections
            for (device in config.devices) {
                val addrSrc = device.address
                for (addrDest in device.linkManager.getNeighbours()) {
                    val idx = addrDest * size + addrSrc
                    val cost = device.location.getDistanceInMeters(config.devices[addrDest].location) + 0.0001
                    if (cost < matrix[idx] || matrix[idx] <0.0) {
                        matrix[idx] = cost
                        matrixNext[idx] = addrDest
                    }
                }
            }
// floydWarshal
            for (k in 0 until size) {
                for (i in 0 until size) {
                    for (j in 0 until size) {
                        val idx = j * size + i
                        val idx1 = k * size + i
                        val idx2 = j * size + k
                        if (matrix[idx] <0.0) {
                            matrix[idx] = matrix[idx1] + matrix[idx2]
                            matrixNext[idx] = matrixNext[idx1]
                        } else if (matrix[idx] > matrix[idx1] + matrix[idx2]) {
                            matrix[idx] = matrix[idx1] + matrix[idx2]
                            matrixNext[idx] = matrixNext[idx1]
                        }
                    }
                }
            }
            config.routingHelper = matrixNext
        }
    }

    override fun startUpRouting() {
        calculateConfigRoutingHelper()
        val address = parent.address
        val size = config.devices.size
        val helper = config.routingHelper as IntArray
        routingTable = IntArray(size) { helper[it * size + address] }
        routingTableFeatureHops = Array(config.features.size) { feature ->
            val devicesWithFeature = config.getAllDevicesForFeature(feature).map { it.address }
            if (devicesWithFeature.size == size) {
                routingTable
            } else {
                IntArray(size) {
                    if (devicesWithFeature.contains(it)) {
                        var next = helper[it * size + address]
                        while (next != -1 && next != it && !devicesWithFeature.contains(next)) {
                            next = helper[it * size + next]
                        }
                        if (next == -1) {
                            it
                        } else {
                            next
                        }
                    } else {
                        -1
                    }
                }
            }
        }
    }
    override fun startUp() {
        child.startUp()
    }
}
