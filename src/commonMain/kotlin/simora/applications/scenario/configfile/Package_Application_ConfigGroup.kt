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

internal typealias Package_Application_ConfigMulticast_IndividualParts = Map<Int, String> // mapping of individual address -> individual text
internal typealias Package_Application_ConfigMulticast_GroupPart = Pair<String, Package_Application_ConfigMulticast_IndividualParts> // group shared text + individuals
internal typealias Package_Application_ConfigMulticast_Group = List<Package_Application_ConfigMulticast_GroupPart>
internal class Package_Application_ConfigMulticast(
    internal val text_global: String,
    internal val groups: Package_Application_ConfigMulticast_Group,
) : IPayload {
    override fun getSizeInBytes(): Int {
        var res = text_global.length // the global text
        res += 4 // variable to specify length of list
        for (g in groups) {
            res += g.first.length // the group shared text
            res += 4 // variable to specify size of individuals
            for (v in g.second.values) {
                res += 4 // size of individual address
                res += v.length // individual text
            }
        }
        return res
    }
    override fun getTopic(): String = "ConfigMulticast"
}
