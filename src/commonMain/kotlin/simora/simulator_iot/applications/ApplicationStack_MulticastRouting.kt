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
import simora.simulator_iot.IPayload
import simora.simulator_iot.IPayloadBinary

internal class ApplicationStack_MulticastRouting(
    private val enableApplciationSideMulticast: Boolean,
    private val featureFlag: Int,
    private val ownAddress: Int,
    private val child: IApplicationStack_Actuator,
) : IApplicationStack_BothDirections {
    private lateinit var parent: IApplicationStack_Middleware
    private var myQueue = mutableSetOf<Package_ApplicationStack_Multicast>()

    init {
        child.setRouter(this)
    }

    override fun startUp(): Unit = child.startUp()
    override fun shutDown(): Unit = child.shutDown()
    override fun getAllChildApplications(): Set<IApplicationStack_Actuator> {
        val res = mutableSetOf<IApplicationStack_Actuator>()
        res.add(child)
        val c = child
        if (c is IApplicationStack_Middleware) {
            res.addAll(c.getAllChildApplications())
        }
        return res
    }

    override fun setRouter(router: IApplicationStack_Middleware) {
        parent = router
    }

    private fun myFlush() {
// indentify duplicates
        val queueCopy = myQueue
        myQueue = mutableSetOf()
        for (p in queueCopy) {
            if (p.targets.size == 1) {
                parent.send(p.targets[0], p.pck)
            } else {
                val targets = p.targets.toIntArray()
                val hops = parent.getNextFeatureHops(targets, featureFlag)
                for (h in hops.indices) {
                    if (hops[h] == -1) {
                        hops[h] = targets[h]
                    }
                }
                for (hop in hops.toSet()) {
                    val filteredTargets = mutableListOf<Int>()
                    for (h in hops.indices) {
                        if (hops[h] == hop) {
                            filteredTargets.add(targets[h])
                        }
                    }
                    if (filteredTargets.size == 1) {
                        parent.send(filteredTargets[0], p.pck)
                    } else {
                        parent.send(hop, Package_ApplicationStack_Multicast(filteredTargets, p.pck))
                    }
                }
            }
        }
    }

    override fun receive(pck: IPayload): IPayload? {
        val res = if (pck is Package_ApplicationStack_Multicast) {
// detect self targets
            val newTargets = mutableListOf<Int>()
            for (t in pck.targets) {
                if (t == ownAddress) {
                    child.receive(pck.pck)
                } else {
                    newTargets.add(t)
                }
            }
// add all remaining to send queue
            val element = myQueue.find { it == pck }
            if (element == null) {
                myQueue.add(pck)
            } else {
                element.targets.addAll(newTargets)
            }
            null
        } else {
            child.receive(pck)
        }
        myFlush()
        return res
    }

    override fun send(destinationAddress: Int, pck: IPayload) {
        if (pck is IPayloadBinary) {
// add all possible elements to send queue
            val p = Package_ApplicationStack_Multicast(mutableListOf(destinationAddress), pck)
            val element = myQueue.find { it == p }
            if (element == null) {
                myQueue.add(p)
            } else {
                element.targets.add(destinationAddress)
            }
        } else {
            parent.send(destinationAddress, pck)
        }
    }

    override fun getNextFeatureHops(destinationAddresses: IntArray, flag: Int): IntArray {
        return if (enableApplciationSideMulticast) {
            parent.getNextFeatureHops(destinationAddresses, flag)
        } else {
            IntArray(destinationAddresses.size) { -1 }
        }
    }

    override fun registerTimer(durationInNanoSeconds: Long, entity: ITimer): Unit = parent.registerTimer(durationInNanoSeconds, entity)
    override fun flush() {
        myFlush()
        parent.flush()
    }

    override fun resolveHostName(name: String): Int = parent.resolveHostName(name)
    override fun closestDeviceWithFeature(name: String): Int = parent.closestDeviceWithFeature(name)
    override fun addChildApplication(child: IApplicationStack_Actuator): Unit = (this.child as IApplicationStack_Middleware).addChildApplication(child)
}
