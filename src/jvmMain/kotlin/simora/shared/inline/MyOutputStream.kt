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
import java.io.OutputStream

internal class MyOutputStream
internal constructor(it: OutputStream) : IMyOutputStream {
    private val buffer: ByteArray = ByteArray(8192)
    private var bufferPos = 0

    private var stream: OutputStream? = it

    override fun close() {
        flush()
        stream!!.close()
        stream = null
    }

    private fun localFlush() {
        if (bufferPos > 0) {
            stream!!.write(buffer, 0, bufferPos)
            bufferPos = 0
        }
    }

    override fun flush() {
        localFlush()
        stream!!.flush()
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun _write(buf: ByteArray, off: Int, len: Int) {
        if (bufferPos + len > buffer.size) {
            localFlush()
        }
        if (len > buffer.size) {
            stream!!.write(buf, off, len)
        } else {
            buf.copyInto(buffer, bufferPos, off, off + len)
            bufferPos += len
        }
    }

    override fun write(buf: ByteArray) {
        _write(buf, 0, buf.size)
    }

    override fun write(buf: ByteArray, len: Int) {
        _write(buf, 0, len)
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun _print(x: String) {
        val buf = x.encodeToByteArray()
        _write(buf, 0, buf.size)
    }

    override fun println(x: String) = _print("$x\n")
    override fun print(x: String) = _print(x)
    override fun print(x: Boolean) = _print("$x")
    override fun print(x: Int) = _print("$x")
    override fun print(x: Double) = _print("$x")
    override fun println() = _print("\n")
}
