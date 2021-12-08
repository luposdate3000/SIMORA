/*
 * This file is part of the Luposdate3000 distribution (https://github.com/simoradate3000/simoradate3000).
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

public object ExternalModule_fs {
    internal val inmemoryFs = mutableMapOf<String, ByteArray>()
    private var tmpCounter = 0
    public fun createTempDirectory(): String {
        return "tmp${tmpCounter++}"
    }

    public fun exists(filename: String): Boolean {
        return inmemoryFs[filename] != null
    }

    public fun mkdirs(filename: String): Boolean {
        return true
    }

    public fun length(filename: String): Long {
        val f = inmemoryFs[filename]
        return if (f == null) {
            0
        } else {
            f.size.toLong()
        }
    }

    public fun openOutputStream(filename: String, append: Boolean): JSOutputStream {
        return JSOutputStream(filename, append)
    }
}

public class JSOutputStream(private val filename: String, append: Boolean) {
    private var buffer: ByteArray
    private var bufferSize: Int

    init {
        if (append) {
            val v = ExternalModule_fs.inmemoryFs[filename]
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

    public fun write(buf: ByteArray) {
        write(buf, buf.size)
    }

    public fun write(buf: ByteArray, len: Int) {
        reserveSpace(len)
        buf.copyInto(buffer, bufferSize, 0, len)
        bufferSize += len
    }

    public fun close() {
        val b = ByteArray(bufferSize)
        buffer.copyInto(b, 0, 0, bufferSize)
        ExternalModule_fs.inmemoryFs[filename] = b
    }

    public fun flush() {
    }

    public fun println(x: String) {
        print("$x\n")
    }

    public fun print(x: String) {
        write(x.encodeToByteArray())
    }

    public fun print(x: Boolean) {
        print("$x")
    }

    public fun print(x: Int) {
        print("$x")
    }

    public fun print(x: Double) {
        print("$x")
    }

    public fun println() {
        print("\n")
    }
}

public class JSInputStream {
    internal var pos = 0
    internal lateinit var buffer: ByteArray

    public constructor(filename: String) {
        buffer = ExternalModule_fs.inmemoryFs[filename]!!
    }

    public constructor(fd: Int) {
        buffer = ByteArray(0)
        TODO()
    }

    public fun readByte(): Byte {
        return buffer[pos++]
    }

    public fun read(buf: ByteArray, off: Int, len: Int): Int {
        var l = len + off
        if (len + off > buffer.size) {
            l = buffer.size
        }
        if (l > off) {
            buffer.copyInto(buf, 0, off, l)
        }
        return l - off
    }

    public fun read(buf: ByteArray, len: Int): Int {
        val l = read(buf, pos, len)
        pos += l
        return l
    }

    public fun read(buf: ByteArray): Int {
        val l = read(buf, pos, buf.size)
        pos += l
        return l
    }

    public fun close() {
    }

    public fun readLine(): String? {
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
