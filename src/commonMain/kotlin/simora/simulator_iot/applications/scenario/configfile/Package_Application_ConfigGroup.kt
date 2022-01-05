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

internal typealias Package_Application_ConfigGroup_IndividualParts = Map<Int, String> //mapping of individual address -> individual text
internal typealias Package_Application_ConfigGroup_GroupPart = Pair<String,Package_Application_ConfigGroup_IndividualParts> //group shared text + individuals
internal class Package_Application_ConfigGroup(
    internal val text_global: String,
    internal val groups: List<Package_Application_ConfigGroup_GroupPart>,
) : IPayload {
    override fun getSizeInBytes(): Int {
var res=text_global.length+1 //the global text (zero terminated)
res+=4 //variable to specify length of list
for (g in groups){
res+=g.first.length+1 //the group shared text (zero terminated)
res+=4//variable to specify size of individuals
for((k,v) in g.second){
res+=4//size of individual address
res+=v.length+1 // individual text (zero terminated)
}
}
return res
}
    override fun getTopic(): String = "ConfigGroup"
}
