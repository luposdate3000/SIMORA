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

public class JsonParserObject(private val map: MutableMap<String, IJsonParserValue>) : Iterable<Pair<String, IJsonParserValue>>, IJsonParserValue {
    private var accessed0 = false
    override fun setAccessed() {
        accessed0 = true
    }

    override fun isAccessed(): Boolean = accessed0
    override fun cloneJson(): JsonParserObject {
        val res = JsonParserObject(mutableMapOf())
        for ((k, v) in map) {
            res.map[k] = v.cloneJson()
        }
        return res
    }

    internal fun mergeWith(other: JsonParserObject) {
        for ((k, other_v) in other.map) {
            val my_v = map[k]
            if (my_v == null) {
                map[k] = other_v
            } else {
                when (my_v) {
                    is JsonParserObject -> my_v.mergeWith(other_v as JsonParserObject)
                    is JsonParserArray -> my_v.mergeWith(other_v as JsonParserArray)
                    is JsonParserInt, is JsonParserLong, is JsonParserBoolean, is JsonParserDouble, is JsonParserString -> map[k] = other_v
                    else -> TODO("$my_v - $other_v")
                }
            }
        }
    }
    override operator fun iterator(): Iterator<Pair<String, IJsonParserValue>> {
        return object : Iterator<Pair<String, IJsonParserValue>> {
            var iter = map.iterator()
            override fun hasNext(): Boolean {
                return iter.hasNext()
            }

            override fun next(): Pair<String, IJsonParserValue> {
                val (k, v) = iter.next()
                v.setAccessed()
                return k to v
            }
        }
    }

    internal fun putAll(d: Map<String, Any>) {
        for ((k, v) in d) {
            map[k] = when (v) {
                is Int -> JsonParserInt(v)
                is Long -> JsonParserLong(v)
                is Double -> JsonParserDouble(v)
                is Boolean -> JsonParserBoolean(v)
                is String -> JsonParserString(v)
                else -> TODO("$v")
            }
        }
    }

    internal fun isEmpty(): Boolean = map.isEmpty()
    internal operator fun set(k: String, v: IJsonParserValue) {
        setAccessed()
        v.setAccessed()
        map[k] = v
    }

    internal operator fun set(k: String, v: String) {
        setAccessed()
        val tmp = JsonParserString(v)
        tmp.setAccessed()
        map[k] = tmp
    }

    internal operator fun set(k: String, v: Int) {
        setAccessed()
        val tmp = JsonParserInt(v)
        tmp.setAccessed()
        map[k] = tmp
    }
    internal operator fun set(k: String, v: Long) {
        setAccessed()
        val tmp = JsonParserLong(v)
        tmp.setAccessed()
        map[k] = tmp
    }

    internal operator fun set(k: String, v: Boolean) {
        setAccessed()
        val tmp = JsonParserBoolean(v)
        tmp.setAccessed()
        map[k] = tmp
    }

    internal operator fun set(k: String, v: Double) {
        setAccessed()
        val tmp = JsonParserDouble(v)
        tmp.setAccessed()
        map[k] = tmp
    }

    internal operator fun get(k: String): IJsonParserValue? {
        setAccessed()
        val tmp = map[k]
        tmp?.setAccessed()
        return tmp
    }

    internal fun getOrDefault(k: String, v: IJsonParserValue): IJsonParserValue {
        setAccessed()
        val res = map[k]
        val r = if (res == null) {
            map[k] = v
            v
        } else {
            res
        }
        r.setAccessed()
        try {
            when (r) {
                is JsonParserString -> r.setDefault((v as JsonParserString).value)
                is JsonParserInt -> r.setDefault((v as JsonParserInt).value)
                is JsonParserLong -> r.setDefault((v as JsonParserLong).value)
                is JsonParserBoolean -> r.setDefault((v as JsonParserBoolean).value)
                is JsonParserDouble -> r.setDefault((v as JsonParserDouble).value)
            }
        } catch (e: Throwable) {
            throw e
        }
        return r
    }

    internal fun getOrEmptyObject(k: String): JsonParserObject {
        setAccessed()
        val res = getOrDefault(k, JsonParserObject(mutableMapOf())) as JsonParserObject
        res.setAccessed()
        return res
    }

    internal fun getOrEmptyArray(k: String): JsonParserArray {
        setAccessed()
        val res = getOrDefault(k, JsonParserArray(mutableListOf())) as JsonParserArray
        res.setAccessed()
        return res
    }
    internal fun getOrDefault(k: String, v: String): String {
        setAccessed()
        val tmp = getOrDefault(k, JsonParserString(v))
        val res = when (tmp) {
            is JsonParserString -> tmp.value
            is JsonParserBoolean -> "${tmp.value}"
            is JsonParserInt -> "${tmp.value}"
            is JsonParserLong -> "${tmp.value}"
            is JsonParserDouble -> "${tmp.value}"
            else -> TODO("$tmp")
        }
        val tmp2 = JsonParserString(res)
        tmp2.setAccessed()
        tmp2.setDefault(v)
        map[k] = tmp2
        return res
    }

    internal fun getOrDefault(k: String, v: Boolean): Boolean {
        setAccessed()
        val tmp = getOrDefault(k, JsonParserBoolean(v))
        val res = when (tmp) {
            is JsonParserString -> tmp.value.lowercase() == "true"
            is JsonParserBoolean -> tmp.value
            is JsonParserInt -> tmp.value != 0
            is JsonParserLong -> tmp.value != 0L
            is JsonParserDouble -> tmp.value != 0.0
            else -> TODO("$tmp")
        }
        val tmp2 = JsonParserBoolean(res)
        tmp2.setAccessed()
        tmp2.setDefault(v)
        map[k] = tmp2
        return res
    }

    internal fun getOrDefault(k: String, v: Int): Int {
        setAccessed()
        val tmp = getOrDefault(k, JsonParserInt(v))
        val res = when (tmp) {
            is JsonParserString -> tmp.value.toInt()
            is JsonParserBoolean -> if (tmp.value) 1 else 0
            is JsonParserInt -> tmp.value
            is JsonParserLong -> tmp.value.toInt()
            else -> TODO("$tmp")
        }
        val tmp2 = JsonParserInt(res)
        tmp2.setAccessed()
        tmp2.setDefault(v)
        map[k] = tmp2
        return res
    }

    internal fun getOrDefault(k: String, v: Long): Long {
        setAccessed()
        val tmp = getOrDefault(k, JsonParserLong(v))
        val res = when (tmp) {
            is JsonParserString -> tmp.value.toLong()
            is JsonParserBoolean -> if (tmp.value) 1 else 0
            is JsonParserInt -> tmp.value.toLong()
            is JsonParserLong -> tmp.value
            is JsonParserDouble -> tmp.value.toLong()
            else -> TODO("$tmp")
        }
        val tmp2 = JsonParserLong(res)
        tmp2.setAccessed()
        tmp2.setDefault(v)
        map[k] = tmp2
        return res
    }

    internal fun getOrDefault(k: String, v: Double): Double {
        setAccessed()
        val tmp = getOrDefault(k, JsonParserDouble(v))
        val res = when (tmp) {
            is JsonParserString -> tmp.value.toDouble()
            is JsonParserBoolean -> if (tmp.value) 1.0 else 0.0
            is JsonParserInt -> tmp.value.toDouble()
            is JsonParserLong -> tmp.value.toDouble()
            is JsonParserDouble -> tmp.value
            else -> TODO("$tmp")
        }
        val tmp2 = JsonParserDouble(res)
        tmp2.setAccessed()
        tmp2.setDefault(v)
        map[k] = tmp2
        return res
    }
}
