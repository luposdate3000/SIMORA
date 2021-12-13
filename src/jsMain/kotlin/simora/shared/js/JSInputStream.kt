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
package simora.shared.js
internal class JSInputStream {
    private var pos = 0
    private lateinit var buffer: ByteArray

    internal constructor(filename: String) {
        buffer = ExternalModule_fs.inmemoryFs[filename]!!
    }

    internal fun readByte(): Byte {
        return buffer[pos++]
    }

    internal fun read(buf: ByteArray, off: Int, len: Int): Int {
        var l = len + off
        if (len + off > buffer.size) {
            l = buffer.size
        }
        if (l > off) {
            buffer.copyInto(buf, 0, off, l)
        }
        return l - off
    }

    internal fun read(buf: ByteArray, len: Int): Int {
        val l = read(buf, pos, len)
        pos += l
        return l
    }

    internal fun read(buf: ByteArray): Int {
        val l = read(buf, pos, buf.size)
        pos += l
        return l
    }

    internal fun close() {
    }
    internal fun readLine(): String? {
// TODO this may break on utf-8
        val buf = mutableListOf<Byte>()
        try {
            var b = readByte()
            while (b != '\n'.code.toByte()) {
                if (b != '\r'.code.toByte()) {
                    buf.add(b)
                }
                b = readByte()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            if (buf.size == 0) {
                return null
            }
        }
        return buf.toByteArray().decodeToString()
    }
}
