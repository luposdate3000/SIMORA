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
import simora.IPayloadLayer

internal class Package_ApplicationStack_Sequence(
    internal val data: IPayload,
    internal val num: Int,
    internal val src: Int,
) : IPayloadLayer {
    private val hops = mutableListOf<Int>()
    override fun addHop(address: Int) { hops.add(address) }
    override fun getAllHops(): List<Int> = hops

    override fun getSizeInBytes(): Int = data.getSizeInBytes() + 8
    override fun toString(): String = "Package_ApplicationStack_Sequence($data)"
    override fun getApplicationPayload(): List<IPayload> {
        return if (data is IPayloadLayer) {
            data.getApplicationPayload()
        } else {
            listOf(data)
        }
    }

    override fun getTopic(): String = TODO()
}
