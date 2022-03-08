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
package simora.applications.scenario.configfile

import simora.IPayload
import simora.applications.IApplicationStack_Actuator
import simora.applications.IApplicationStack_Middleware

internal class Application_ConfigDistributor(
    private val mailDistributorFlag: Int,
    private val address: Int,
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
            is Package_Application_ConfigBroadcast -> {
// calculate next hops
                val destinations = pck.targets
                val hops = parent.getNextFeatureHops(destinations, mailDistributorFlag)
                for (i in hops.indices) {
                    if (hops[i] == -1) {
                        hops[i] = destinations[i]
                    }
                }
// send broadcast towards next hops
                for (h in hops.toSet()) {
                    val targets = mutableListOf<Int>()
                    for (i in destinations.indices) {
                        if (hops[i] == h) {
                            targets.add(destinations[i])
                        }
                    }
                    if (h == address) {
                        if (targets.size != 1) {
                            TODO("error somewhere")
                        }
                        parent.send(h, Package_Application_ConfigUnicast(pck.text_global))
                    } else {
                        parent.send(h, Package_Application_ConfigBroadcast(targets.toIntArray(), pck.text_global))
                    }
                }
                return null
            }
            is Package_Application_ConfigMulticast -> {
                val destinations = pck.groups.map { it.second.keys }.flatten().toSet().toIntArray()
                val hops = parent.getNextFeatureHops(destinations, mailDistributorFlag)
                for (i in hops.indices) {
                    if (hops[i] == -1) {
                        hops[i] = destinations[i]
                    }
                }
                val groups = mutableMapOf<Int, MutableList<Pair<String, Map<Int, String>>>>()
                for (g in pck.groups) {
                    val parts = mutableMapOf<Int, MutableMap<Int, String>>()
// group members by hop
                    for ((k, v) in g.second) {
                        val h = hops[destinations.indexOf(k)]
                        var p = parts[h]
                        if (p == null) {
                            p = mutableMapOf()
                            parts[h] = p
                        }
                        p[k] = v
                    }
// append group to global list ... grouped by hop.
                    for ((h, p) in parts) {
                        var g2 = groups[h]
                        if (g2 == null) {
                            g2 = mutableListOf()
                            groups[h] = g2
                        }
                        g2.add(g.first to p)
                    }
                }
// iterate over the message grouped by next hop, and send it
                for ((hop, group) in groups) {
                    if (group.sumOf { it.second.size } == 1) {
                        for (g in group) {
                            for (v in g.second.values) {
                                parent.send(hop, Package_Application_ConfigUnicast(pck.text_global + g.first + v))
                            }
                        }
                    } else {
                        parent.send(hop, Package_Application_ConfigMulticast(pck.text_global, group))
                    }
                }
                return null
            }
            else -> {
                return pck
            }
        }
    }
    override fun emptyEventQueue(): String? = null
}
