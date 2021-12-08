/*
 * This file is part of the Luposdate3000 distribution (https://github.com/luposdate3000/luposdate3000).
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
package lupos.simulator_iot.applications

import lupos.simulator_core.ITimer
import lupos.simulator_iot.IPayload

public class ApplicationStack_Sequence(
    private val ownAddress: Int,
    private val child: IApplicationStack_Actuator,
) : IApplicationStack_BothDirections {
    private val outgoingNum = mutableListOf<Int>() // index is the dest-address
    private val incomingNum = mutableListOf<Int>() // index is the src-address
    private val caches = mutableListOf<MutableList<Package_ApplicationStack_Sequence>>() // index is the src-address
    private lateinit var parent: IApplicationStack_Middleware

    init {
        child.setRouter(this)
    }

    override fun startUp(): Unit = child.startUp()
    override fun shutDown(): Unit = child.shutDown()
    override fun getAllChildApplications(): Set<IApplicationStack_Actuator> {
        var res = mutableSetOf<IApplicationStack_Actuator>()
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

    override fun receive(pck: IPayload): IPayload? {
        if (pck is Package_ApplicationStack_Sequence) {
            while (incomingNum.size <= pck.src) {
                incomingNum.add(0)
            }
            while (caches.size <= pck.src) {
                caches.add(mutableListOf())
            }
            if (pck.num == incomingNum[pck.src]) {
                child.receive(pck.data)
                incomingNum[pck.src]++
                var changed = true
                loop@ while (changed) {
                    changed = false
                    for (c in caches[pck.src]) {
                        if (c.num == incomingNum[pck.src]) {
                            var p = child.receive(c.data)
                            incomingNum[pck.src]++
                            caches[pck.src].remove(c)
                            changed = true
                            if (p != null) {
                                TODO("$p")
                            }
                            continue@loop
                        }
                    }
                }
            } else {
                caches[pck.src].add(pck)
            }
            return null
        } else {
            return child.receive(pck) // unsequenced packages ...
        }
    }

    override fun send(destinationAddress: Int, pck: IPayload) {
        while (outgoingNum.size <= destinationAddress) {
            outgoingNum.add(0)
        }
        val num = outgoingNum[destinationAddress]++
        val pck2 = Package_ApplicationStack_Sequence(pck, num, ownAddress)
        parent.send(destinationAddress, pck2)
    }

    override fun getNextFeatureHops(destinationAddresses: IntArray, flag: Int): IntArray = parent.getNextFeatureHops(destinationAddresses, flag)
    override fun registerTimer(durationInNanoSeconds: Long, entity: ITimer): Unit = parent.registerTimer(durationInNanoSeconds, entity)
    override fun flush(): Unit = parent.flush()
    override fun closestDeviceWithFeature(name: String): Int = parent.closestDeviceWithFeature(name)
    override fun resolveHostName(name: String): Int = parent.resolveHostName(name)
    override fun addChildApplication(child: IApplicationStack_Actuator): Unit = (this.child as IApplicationStack_Middleware).addChildApplication(child)
}
