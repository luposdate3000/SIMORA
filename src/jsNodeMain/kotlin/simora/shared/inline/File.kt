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
package simora.shared.inline

import simora.shared.IMyOutputStream

internal actual class File actual constructor(internal val filename: String) {
    internal companion object {
    }

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun exists(): Boolean {
        return existsSync(filename)
    }

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun mkdirs(): Boolean {
        var arr = filename.split("/").filterNot { it == "" || it == "." }
        if (filename.startsWith("/")) {
            arr = listOf("") + arr
        }
        var i = 1
        while (i <= arr.size) {
            try {
                mkdirSync(arr.subList(0, i).joinToString("/"))
            } catch (e: Throwable) {}
            i++
        }
        return true
    }

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun readAsString(): String {
        val res = StringBuilder()
        val stream = MyInputStream(filename)
        val buffer = ByteArray(8192)
        var pos = 0
        val s = mutableListOf<Byte>()
        while (true) {
            val len = stream.read(buffer, buffer.size)
            if (len == 0) {
                break
            }
            for (i in 0 until len) {
                val b = buffer[i]
                if (b == '\r'.code.toByte() || b == '\n'.code.toByte()) {
                    res.appendLine(s.toByteArray().decodeToString())
                    s.clear()
                } else {
                    s.add(b)
                }
            }
            pos += len
        }
        res.appendLine(s.toByteArray().decodeToString())
        stream.close()
        return res.toString()
    }

    internal actual inline fun withOutputStream(crossinline action: (IMyOutputStream) -> Unit) {
        val stream = openOutputStream(false)
        try {
            action(stream)
        } finally {
            stream.close()
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun openOutputStream(append: Boolean): IMyOutputStream {
        return MyOutputStream(filename, append)
    }
}
