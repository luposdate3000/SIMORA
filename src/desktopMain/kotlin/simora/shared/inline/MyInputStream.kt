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

import kotlinx.cinterop.refTo
import kotlinx.cinterop.CPointer
import platform.posix.*
import simora.shared.IMyInputStream

internal actual class MyInputStream(file: CPointer<FILE>) : IMyInputStream {
    private var stream: CPointer<FILE>? = file
    private val buf8: ByteArray = ByteArray(8)
    private var buffer = ByteArray(1)

    actual override fun read(buf: ByteArray): Int {
        return read(buf, buf.size)
    }

    actual override fun read(buf: ByteArray, len: Int): Int {
        var o = 0
        var s = len
        while (s > 0) {
            val tmp = fread(buf.refTo(o), s.toULong(), 1, stream).toInt()
            if (tmp <= 0) {
                return len - s
            }
            s -= tmp
            o += tmp
        }
        return len
    }

    actual override fun read(buf: ByteArray, off: Int, len: Int): Int {
        var o = off
        var s = len
        while (s > 0) {
            val tmp = fread(buf.refTo(o), s.toULong(), 1, stream).toInt()
            if (tmp <= 0) {
                return len - s
            }
            s -= tmp
            o += tmp
        }
        return len
    }

    actual override fun readByte(): Byte {
        read(buf8, 1)
        return buf8[0]
    }

    actual override fun close() {
        // kotlin.io.println("MyInputStream.close $this")
        fclose(stream)
        stream = null
    }

    actual override fun readLine(): String? {
// TODO this may break on utf-8 if '\r' or '\0' is part of another char
        var len = 0
        try {
            var b = readByte()
            while (true) {
                when (b) {
                    '\n'.code.toByte() -> break
                    '\r'.code.toByte() -> {
                    }
                    0.toByte() -> throw Exception("zero Bytes not allowed within utf8-string")
                    else -> {
                        if (len >= buffer.size) {
                            val bb = ByteArray(len * 2)
                            buffer.copyInto(bb)
                            buffer = bb
                        }
                        buffer[len++] = b
                    }
                }
                b = readByte()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            if (len == 0) {
                return null
            }
        }
        return buffer.decodeToString(0, len)
    }
}
