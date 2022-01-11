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
package simora.applications

import simora.IPayload
import simora.simulator_core.ITimer

internal class ApplicationStack_MultipleChilds(
    private var childs: Array<IApplicationStack_Actuator>,
) : IApplicationStack_BothDirections {
    private var hadStartUp = false
    private lateinit var parent: IApplicationStack_Middleware

    init {
        for (child in childs) {
            child.setRouter(this)
        }
    }

    override fun startUp() {
        for (child in childs) {
            child.startUp()
        }
        hadStartUp = true
    }

    override fun shutDown() {
        for (child in childs) {
            child.shutDown()
        }
    }

    override fun getAllChildApplications(): Set<IApplicationStack_Actuator> {
        val res = mutableSetOf<IApplicationStack_Actuator>()
        for (child in childs) {
            res.add(child)
            if (child is IApplicationStack_Middleware) {
                res.addAll(child.getAllChildApplications())
            }
        }
        return res
    }

    override fun setRouter(router: IApplicationStack_Middleware) {
        parent = router
    }

    override fun receive(pck: IPayload): IPayload? {
        for (child in childs) {
            child.receive(pck) ?: return null
        }
        return pck
    }

    override fun send(destinationAddress: Int, pck: IPayload): Unit = parent.send(destinationAddress, pck)
    override fun closestDeviceWithFeature(name: String): Int = parent.closestDeviceWithFeature(name)
    override fun getNextFeatureHops(destinationAddresses: IntArray, flag: Int): IntArray = parent.getNextFeatureHops(destinationAddresses, flag)
    override fun registerTimer(durationInNanoSeconds: Long, entity: ITimer): Unit = parent.registerTimer(durationInNanoSeconds, entity)
    override fun flush(): Unit = parent.flush()
    override fun resolveHostName(name: String): Int = parent.resolveHostName(name)
    override fun addChildApplication(child: IApplicationStack_Actuator) {
        val res = Array(childs.size + 1) {
            if (it < childs.size) {
                childs[it]
            } else {
                child
            }
        }
        childs = res
        child.setRouter(this)
        if (hadStartUp) {
            child.startUp()
        }
    }
}
