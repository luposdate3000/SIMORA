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
package simora.simulator_iot.applications.scenario.mailinglist

import simora.simulator_iot.applications.IApplicationStack_Middleware
import simora.simulator_iot.applications.IApplicationStack_Actuator
import simora.simulator_iot.applications.IApplication_Factory
import simora.simulator_iot.IPayload
import simora.simulator_iot.Package_Query

public class Application_MailDistributor(private val ownAddress: Int,
private val mailDistributorFlag:Int,
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
        if (pck is Package_Application_MailGroup) {
val destinations=pck.replacements.keys.toSet().toIntArray()
val hops=parent.getNextFeatureHops(destinations,mailDistributorFlag)
val packets=mutableMapOf<Int,MutableMap<Int,String>>()
for((target,name) in pck.replacements){
val hop=hops[destinations.indexOf(target)]
var p=packets[hop]
if(p==null){
p=mutableMapOf()
packets[hop]=p
}
p[target]=name
}
for((target,mapping) in packets){
if(mapping.size==1){
            parent.send(target, Package_Application_Mail(pck.text.replace("§",mapping[target]!!)))
}else{
            parent.send(target, Package_Application_MailGroup(pck.text,mapping))
}
}
            return null
        } else {
            return pck
        }
    }
}
