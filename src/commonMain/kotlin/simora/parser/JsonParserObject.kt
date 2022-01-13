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
                return k to v
            }
        }
    }

    public fun putAll(d: Map<String, Any>) {
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

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun isEmpty(): Boolean = map.isEmpty()

    @Suppress("NOTHING_TO_INLINE")
    internal inline operator fun set(k: String, v: IJsonParserValue) {
        map[k] = v
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline operator fun set(k: String, v: String) {
        val tmp = JsonParserString(v)
        map[k] = tmp
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline operator fun set(k: String, v: Int) {
        val tmp = JsonParserInt(v)
        map[k] = tmp
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline operator fun set(k: String, v: Long) {
        val tmp = JsonParserLong(v)
        map[k] = tmp
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline operator fun set(k: String, v: Boolean) {
        val tmp = JsonParserBoolean(v)
        map[k] = tmp
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline operator fun set(k: String, v: Double) {
        val tmp = JsonParserDouble(v)
        map[k] = tmp
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline operator fun get(k: String): IJsonParserValue? {
        return map[k]
    }

    public fun getOrDefault(k: String, v: IJsonParserValue): IJsonParserValue {
        val res = map[k]
        val r = if (res == null) {
            map[k] = v
            v
        } else {
            res
        }
        return r
    }

    @Suppress("NOTHING_TO_INLINE")
    public inline fun getOrEmptyObject(k: String): JsonParserObject {
        return getOrDefault(k, JsonParserObject(mutableMapOf())) as JsonParserObject
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun getOrEmptyArray(k: String): JsonParserArray {
        return getOrDefault(k, JsonParserArray(mutableListOf())) as JsonParserArray
    }

    public fun getOrDefault(k: String, v: String): String {
        val res = when (val tmp = getOrDefault(k, JsonParserString(v))) {
            is JsonParserString -> tmp.value
            is JsonParserBoolean -> "${tmp.value}"
            is JsonParserInt -> "${tmp.value}"
            is JsonParserLong -> "${tmp.value}"
            is JsonParserDouble -> "${tmp.value}"
            else -> TODO("$tmp")
        }
        val tmp2 = JsonParserString(res)
        map[k] = tmp2
        return res
    }

    public fun getOrDefault(k: String, v: Boolean): Boolean {
        val res = when (val tmp = getOrDefault(k, JsonParserBoolean(v))) {
            is JsonParserString -> tmp.value.lowercase() == "true"
            is JsonParserBoolean -> tmp.value
            is JsonParserInt -> tmp.value != 0
            is JsonParserLong -> tmp.value != 0L
            is JsonParserDouble -> tmp.value != 0.0
            else -> TODO("$tmp")
        }
        val tmp2 = JsonParserBoolean(res)
        map[k] = tmp2
        return res
    }

    public fun getOrDefault(k: String, v: Int): Int {
        val res = when (val tmp = getOrDefault(k, JsonParserInt(v))) {
            is JsonParserString -> tmp.value.toInt()
            is JsonParserBoolean -> if (tmp.value) 1 else 0
            is JsonParserInt -> tmp.value
            is JsonParserLong -> tmp.value.toInt()
            else -> TODO("$tmp")
        }
        val tmp2 = JsonParserInt(res)
        map[k] = tmp2
        return res
    }

    public fun getOrDefault(k: String, v: Long): Long {
        val res = when (val tmp = getOrDefault(k, JsonParserLong(v))) {
            is JsonParserString -> tmp.value.toLong()
            is JsonParserBoolean -> if (tmp.value) 1 else 0
            is JsonParserInt -> tmp.value.toLong()
            is JsonParserLong -> tmp.value
            is JsonParserDouble -> tmp.value.toLong()
            else -> TODO("$tmp")
        }
        val tmp2 = JsonParserLong(res)
        map[k] = tmp2
        return res
    }

    public fun getOrDefault(k: String, v: Double): Double {
        val res = when (val tmp = getOrDefault(k, JsonParserDouble(v))) {
            is JsonParserString -> tmp.value.toDouble()
            is JsonParserBoolean -> if (tmp.value) 1.0 else 0.0
            is JsonParserInt -> tmp.value.toDouble()
            is JsonParserLong -> tmp.value.toDouble()
            is JsonParserDouble -> tmp.value
            else -> TODO("$tmp")
        }
        val tmp2 = JsonParserDouble(res)
        map[k] = tmp2
        return res
    }
}
