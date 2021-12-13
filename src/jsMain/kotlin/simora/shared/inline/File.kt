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

import simora.shared.IMyInputStream
import simora.shared.IMyOutputStream
import simora.shared.js.ExternalModule_fs

internal actual class File {
    internal val filename: String

    actual constructor(filename: String) {
        this.filename = filename
    }

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun exists(): Boolean = ExternalModule_fs.exists(filename)

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun mkdirs(): Boolean = ExternalModule_fs.mkdirs(filename)

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun length(): Long = ExternalModule_fs.length(filename)

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun readAsString(): String {
        var res = StringBuilder()
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

    internal actual inline fun withInputStream(crossinline action: (IMyInputStream) -> Unit) {
        val stream = MyInputStream(filename)
        action(stream)
        stream.close()
    }

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun openOutputStream(append: Boolean): IMyOutputStream {
        return object : IMyOutputStream {
            val tmp = ExternalModule_fs.openOutputStream(filename, append)
            override fun write(buf: ByteArray): Unit = tmp.write(buf)
            override fun write(buf: ByteArray, len: Int): Unit = tmp.write(buf, len)
            override fun close(): Unit = tmp.close()
            override fun flush(): Unit = tmp.flush()
            override fun println(x: String) = tmp.println(x)
            override fun print(x: String) = tmp.print(x)
            override fun print(x: Boolean) = tmp.print(x)
            override fun print(x: Int) = tmp.print(x)
            override fun print(x: Double) = tmp.print(x)
            override fun println() = tmp.println()
        }
    }
}
