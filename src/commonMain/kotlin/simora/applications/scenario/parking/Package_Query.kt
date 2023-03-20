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

package simora.applications.scenario.parking

import simora.shared.UUID_Counter

public class Package_Query(
    public val sourceAddress: Int,
    public val query: ByteArray,
public val order:ByteArray?=null,
    public val attributes: Map<String, Any> = mapOf()
) : IPackage_Database {
    private val hops = mutableListOf<Int>()
    override fun addHop(address: Int) { hops.add(address) }
    override fun getAllHops(): List<Int> = hops

    public val queryID: Int = UUID_Counter.getNextUUID().toInt()
    private val pckID: Long = UUID_Counter.getNextUUID()
    override fun getPackageID(): Long = pckID

    override fun getSizeInBytes(): Int {
        return query.size + 4
    }

    override fun getTopic(): String = "SPARQL-Query"
}
