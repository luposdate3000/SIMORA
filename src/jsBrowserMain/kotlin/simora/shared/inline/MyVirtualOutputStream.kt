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
internal class MyOutputStream(private val filename: String, append: Boolean) : IMyOutputStream {
    private var buffer: ByteArray
    private var bufferSize: Int

    init {
        if (append) {
            val v = File.inmemoryFs[filename]
            if (v != null) {
                buffer = v
                bufferSize = v.size
            } else {
                buffer = ByteArray(1024)
                bufferSize = 0
            }
        } else {
            buffer = ByteArray(1024)
            bufferSize = 0
        }
    }

    private fun reserveSpace(size: Int) {
        if (bufferSize + size > buffer.size) {
            var destSize = 1024
            while (destSize < size + bufferSize) {
                destSize *= 2
            }
            val b = ByteArray(destSize)
            buffer.copyInto(b)
            buffer = b
        }
    }

    override fun write(buf: ByteArray) {
        write(buf, buf.size)
    }

    override fun write(buf: ByteArray, len: Int) {
        reserveSpace(len)
        buf.copyInto(buffer, bufferSize, 0, len)
        bufferSize += len
    }

    override fun close() {
        val b = ByteArray(bufferSize)
        buffer.copyInto(b, 0, 0, bufferSize)
        File.inmemoryFs[filename] = b
    }

    override fun flush() {
    }

    override fun println(x: String) {
        print("$x\n")
    }

    override fun print(x: String) {
        write(x.encodeToByteArray())
    }

    override fun print(x: Boolean) {
        print("$x")
    }

    override fun print(x: Int) {
        print("$x")
    }

    override fun print(x: Double) {
        print("$x")
    }

    override fun println() {
        print("\n")
    }
}
