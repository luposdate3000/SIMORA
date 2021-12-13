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

import simora.shared.inline.File

internal class JsonParser {
    private fun readValueAt(data: String, off: Int): Pair<Int, IJsonParserValue> {
        val i = readSpacesAt(data, off)
        while (i < data.length) {
            return when (data[i]) {
                '{' -> readMapAt(data, i)
                '[' -> readArrayAt(data, i)
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '+', '.' -> readNumberAt(data, i)
                '"' -> readStringAt(data, i)
                't', 'f', 'T', 'F' -> readBooleanAt(data, i)
                else -> throw Exception("unknown char at A $i '${data[i]}' $data")
            }
        }
        return data.length to JsonParserObject(mutableMapOf())
    }

    private fun readMapAt(data: String, off: Int): Pair<Int, JsonParserObject> {
        var i = off + 1
        val res = mutableMapOf<String, IJsonParserValue>()
        loop@ while (i < data.length) {
            i = readSpacesAt(data, i)
            when (data[i]) {
                '}' -> return (i + 1) to JsonParserObject(res)
                '"' -> {
                    val (j, key) = readStringAt(data, i)
                    i = readSpacesAt(data, j)
                    if (data[i] != ':') {
                        throw Exception("unknown char at B $i '${data[i]}' $data")
                    }
                    i = readSpacesAt(data, i + 1)
                    val (k, value) = readValueAt(data, i)
                    res[key.value] = value
                    i = readSpacesAt(data, k)
                    when (data[i]) {
                        ',' -> i++
                        '}' -> return (i + 1) to JsonParserObject(res)
                        else -> throw Exception("unknown char at C $i '${data[i]}' $data")
                    }
                }
            }
        }
        throw Exception("object not closed I $off '${data[off]}' $data")
    }

    private fun readSpacesAt(data: String, off: Int): Int {
        var i = off
        while (i < data.length) {
            when (data[i]) {
                ' ', '\t', '\n', '\r' -> {
                    i++
                }
                else -> return i
            }
        }
        return i
    }

    private fun readArrayAt(data: String, off: Int): Pair<Int, JsonParserArray> {
        var i = off + 1
        val res = mutableListOf<IJsonParserValue>()
        loop@ while (i < data.length) {
            i = readSpacesAt(data, i)
            when (data[i]) {
                ']' -> return (i + 1) to JsonParserArray(res)
                else -> {
                    val (k, value) = readValueAt(data, i)
                    res.add(value)
                    i = readSpacesAt(data, k)
                    when (data[i]) {
                        ',' -> i++
                        ']' -> return (i + 1) to JsonParserArray(res)
                        else -> throw Exception("unknown char at D $i '${data[i]}' $data")
                    }
                }
            }
        }
        throw Exception("array not closed H $off '${data[off]}' $data")
    }

    private fun readNumberAt(data: String, off: Int): Pair<Int, IJsonParserValue> {
        var i = off
        while (i < data.length) {
            when (data[i]) {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '+', '.', 'e', 'E' -> i++
                else -> break
            }
        }
        if (i != off) {
            var num = data.substring(off, i)
            if (num.startsWith("+")) {
                num = num.substring(1)
            }
            try {
                val res = num.toInt()
                if (num == res.toString()) {
                    return i to JsonParserInt(res)
                }
            } catch (e: Throwable) {
            }
            try {
                val res = num.toLong()
                if (num == res.toString()) {
                    return i to JsonParserLong(res)
                }
            } catch (e: Throwable) {
            }
            try {
                val res = num.toDouble()
                return i to JsonParserDouble(res)
            } catch (e: Throwable) {
            }
        }
        throw Exception("unknown numberformat at E $off '${data.substring(off, i)}' $data")
    }

    private fun readStringAt(data: String, off: Int): Pair<Int, JsonParserString> {
        var i = off + 1
        var backslashOpen = false
        while (i < data.length) {
            when (data[i]) {
                '"' -> {
                    if (backslashOpen) {
                        backslashOpen = false
                        i++
                    } else {
                        return (i + 1) to JsonParserString(decodeString(data.substring(off + 1, i)))
                    }
                }
                '\\' -> {
                    backslashOpen = !backslashOpen
                    i++
                }
                else -> {
                    backslashOpen = false
                    i++
                }
            }
        }
        throw Exception("string not closed $off '${data[off]}' $data")
    }

    private fun readBooleanAt(data: String, off: Int): Pair<Int, IJsonParserValue> {
        when (data[off]) {
            't', 'T' -> {
                if (data[off + 1] == 'r' || data[off + 1] == 'R') {
                    if (data[off + 2] == 'u' || data[off + 2] == 'U') {
                        if (data[off + 3] == 'e' || data[off + 3] == 'E') {
                            return (off + 4) to JsonParserBoolean(true)
                        }
                    }
                }
            }
            'f', 'F' -> {
                if (data[off + 1] == 'a' || data[off + 1] == 'A') {
                    if (data[off + 2] == 'l' || data[off + 2] == 'L') {
                        if (data[off + 3] == 's' || data[off + 3] == 'S') {
                            if (data[off + 4] == 'e' || data[off + 4] == 'E') {
                                return (off + 5) to JsonParserBoolean(false)
                            }
                        }
                    }
                }
            }
        }
        throw Exception("unknown char at F $off '${data[off]}' $data")
    }

    internal fun jsonToString(data: IJsonParserValue, printDefaults: Boolean): String {
        return jsonToString(data, "", printDefaults)
    }

    private fun jsonToString(data: IJsonParserValue, indention: String, printDefaults: Boolean): String {
        val r = when (data) {
            is JsonParserObject -> {
                if (data.isEmpty()) {
                    "{}"
                } else {
                    var res = "{\n"
                    for ((k, v) in data.toList().sortedBy { it.first }) {
                        res += "$indention    \"$k\": ${jsonToString(v, "$indention    ", printDefaults)},\n"
                    }
                    "$res$indention}"
                }
            }
            is JsonParserArray -> {
                if (data.isEmpty()) {
                    "[]"
                } else {
                    var res = "[\n"
                    for (e in data) {
                        res += "$indention    ${jsonToString(e, "$indention    ", printDefaults)},\n"
                    }
                    "$res$indention]"
                }
            }
            is JsonParserBoolean -> {
                if (data.getDefault() == null || !printDefaults) {
                    "${data.value}"
                } else {
                    "${data.value} /* '${data.getDefault()}' */"
                }
            }
            is JsonParserInt -> {
                if (data.getDefault() == null || !printDefaults) {
                    "${data.value}"
                } else {
                    "${data.value} /* '${data.getDefault()}' */"
                }
            }
            is JsonParserLong -> {
                if (data.getDefault() == null || !printDefaults) {
                    "${data.value}"
                } else {
                    "${data.value} /* '${data.getDefault()}' */"
                }
            }
            is JsonParserDouble -> {
                if (data.getDefault() == null || !printDefaults) {
                    "${data.value}"
                } else {
                    "${data.value} /* '${data.getDefault()}' */"
                }
            }
            is JsonParserString -> {
                if (data.getDefault() == null || !printDefaults) {
                    "\"${encodeString(data.value)}\""
                } else {
                    "\"${encodeString(data.value)}\" /* '${data.getDefault()}' */"
                }
            }
            else -> throw Exception("unknown JSON element G $data")
        }
        return if (data.isAccessed() || !printDefaults) {
            r
        } else {
            "$r /* unused */"
        }
    }

    internal fun stringToJson(data: String): IJsonParserValue {
        return readValueAt(data, 0).second
    }

    internal fun fileToJson(fileName: String, autoformat: Boolean = true): IJsonParserValue {
        try {
            val fileStr = File(fileName).readAsString()
            val json = JsonParser().stringToJson(fileStr)
            if (autoformat) {
                File(fileName).withOutputStream { out ->
                    out.println(JsonParser().jsonToString(json, false))
                }
            }
            return json
        } catch (e: Throwable) {
            println(fileName)
            throw e
        }
    }

    internal fun fileMergeToJson(fileNames: List<String>, autoformat: Boolean = true): JsonParserObject {
        val res = JsonParserObject(mutableMapOf())
        for (fileName in fileNames) {
            try {
                val json = fileToJson(fileName, autoformat) as JsonParserObject
                res.mergeWith(json)
            } catch (e: Throwable) {
            }
        }
        return res
    }

    private fun encodeString(s: String): String {
        return s
            .replace("\\", "\\\\")
            .replace("\t", "\\t")
            .replace("\r", "\\r")
            .replace("\n", "\\n")
            .replace("\"", "\\\"")
    }

    private fun decodeString(s: String): String {
        return s
            .replace("\\\"", "\"")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\\\", "\\")
    }
}
