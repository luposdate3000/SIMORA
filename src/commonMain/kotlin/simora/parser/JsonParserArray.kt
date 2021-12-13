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

package simora.parser

private class JsonParserArray(private val array: MutableList<IJsonParserValue>) : Iterable<IJsonParserValue>, IJsonParserValue {
    private var accessed0 = false
    override fun setAccessed() {
        accessed0 = true
    }

    override fun cloneJson(): JsonParserArray {
        val res = JsonParserArray(mutableListOf())
        for (a in array) {
            res.array.add(a.cloneJson())
        }
        return res
    }

    override fun isAccessed(): Boolean = accessed0
    private fun mergeWith(other: JsonParserArray) {
        var k = 0
        while (k < array.size && k < other.array.size) {
            val other_v = other.array[k]
            val my_v = array[k]
            when (my_v) {
                is JsonParserObject -> my_v.mergeWith(other_v as JsonParserObject)
                is JsonParserArray -> my_v.mergeWith(other_v as JsonParserArray)
                is JsonParserInt, is JsonParserLong, is JsonParserBoolean, is JsonParserDouble, is JsonParserString -> array[k] = other_v
                else -> TODO("$my_v - $other_v")
            }
            k++
        }
        while (k < other.array.size) {
            array.add(other.array[k])
            k++
        }
    }

    private fun isEmpty(): Boolean = array.isEmpty()
    private fun <T> map(action: (IJsonParserValue) -> T): MutableList<T> {
        val res: List<T> = array.map { action(it) }
        return res.toMutableList()
    }
    private fun firstOrEmptyObject(): JsonParserObject {
        setAccessed()
        val res =
            if (array.size > 0) {
                array[0] as JsonParserObject
            } else {
                val tmp = JsonParserObject(mutableMapOf())
                array.add(tmp)
                tmp
            }
        res.setAccessed()
        return res
    }

    internal operator fun get(i: Int): IJsonParserValue {
        setAccessed()
        val res = array[i]
        res.setAccessed()
        return res
    }

    override operator fun iterator(): Iterator<IJsonParserValue> {
        setAccessed()
        for (a in array) {
            a.setAccessed()
        }
        return array.iterator()
    }

    private fun add(v: IJsonParserValue) {
        setAccessed()
        v.setAccessed()
        array.add(v)
    }
}
