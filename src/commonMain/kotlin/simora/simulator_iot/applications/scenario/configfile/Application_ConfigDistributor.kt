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
package simora.simulator_iot.applications.scenario.configfile

import simora.simulator_iot.IPayload
import simora.simulator_iot.applications.IApplicationStack_Actuator
import simora.simulator_iot.applications.IApplicationStack_Middleware

internal class Application_ConfigDistributor(
    private val mailDistributorFlag: Int,
) : IApplicationStack_Actuator {
    private lateinit var parent: IApplicationStack_Middleware
    override fun setRouter(router: IApplicationStack_Middleware) {
        parent = router
    }

    override fun startUp() {
    }

    override fun shutDown() {
    }

    override fun receive(pck: IPayload): IPayload? {
        when (pck) {
            is Package_Application_ConfigGroup -> {
                val destinations = pck.replacements.keys.toSet().toIntArray()
                val hops = parent.getNextFeatureHops(destinations, mailDistributorFlag)
                for (i in hops.indices) {
                    if (hops[i] == -1) {
                        hops[i] = destinations[i]
                    }
                }
                val packets = mutableMapOf<Int, MutableMap<Int, String>>()
                for ((target, name) in pck.replacements) {
                    val hop = hops[destinations.indexOf(target)]
                    var p = packets[hop]
                    if (p == null) {
                        p = mutableMapOf()
                        packets[hop] = p
                    }
                    p[target] = name
                }
                for ((target, mapping) in packets) {
                    if (mapping.size == 1) {
                        val x = mapping.toList().first()
                        parent.send(x.first, Package_Application_Config(x.second + pck.text))
                    } else {
                        parent.send(target, Package_Application_ConfigGroup(pck.text, mapping))
                    }
                }
                return null
            }
            is Package_Application_ConfigGroupIdentical -> {
                val destinations = pck.targets.toIntArray()
                val hops = parent.getNextFeatureHops(destinations, mailDistributorFlag)
                for (i in hops.indices) {
                    if (hops[i] == -1) {
                        hops[i] = destinations[i]
                    }
                }
                val packets = mutableMapOf<Int, MutableSet<Int>>()
                for (target in pck.targets) {
                    val hop = hops[destinations.indexOf(target)]
                    var p = packets[hop]
                    if (p == null) {
                        p = mutableSetOf()
                        packets[hop] = p
                    }
                    p.add(target)
                }
                for ((target, mapping) in packets) {
                    if (mapping.size == 1) {
                        val x = mapping.toList().first()
                        parent.send(x, Package_Application_Config(pck.text))
                    } else {
                        parent.send(target, Package_Application_ConfigGroupIdentical(pck.text, mapping))
                    }
                }
                return null
            }
            else -> {
                return pck
            }
        }
    }
}
