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

import kotlinx.cinterop.*
import platform.posix.*
import simora.shared.IMyOutputStream
import simora.shared.SanityCheck

internal actual class MyOutputStream : IMyOutputStream {
    val buffer: ByteArray = ByteArray(8192)
    var bufferPos = 0
    private var stream: CPointer<FILE>?

    private var closedBy: MutableList<Throwable>? = null

    internal constructor(file: CPointer<FILE>) {
        stream = file
    }

    actual override fun close() {
        SanityCheck(
            { /*SOURCE_FILE_START*/"/src/simora/src/desktopMain/kotlin/simora/shared/inline/MyOutputStream.kt:36"/*SOURCE_FILE_END*/ },
            {
                try {
                    throw Exception()
                } catch (e: Throwable) {
                    if (closedBy == null) {
                        closedBy = mutableListOf(e)
                    } else {
                        closedBy!!.add(e)
                    }
                }
                if (stream == null) {
                    for (e in closedBy!!) {
                        e.printStackTrace()
                    }
                }
            }
        )
        // kotlin.io.println("MyOutputStream.close $this")
        flush()
        fclose(stream)
        stream = null
    }

    private fun localFlush() {
        // kotlin.io.println("MyOutputStream.localFlush $this $bufferPos")
        if (bufferPos > 0) {
            fwrite(buffer.refTo(0), bufferPos.toULong(), 1, stream)
            bufferPos = 0
        }
    }

    actual override fun flush() {
        // kotlin.io.println("MyOutputStream.flush $this")
        localFlush()
        fflush(stream)
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun _write(buf: ByteArray, off: Int, len: Int) {
        // kotlin.io.println("MyOutputStream._write $this")
        if (bufferPos + len > buffer.size) {
            localFlush()
        }
        if (len > buffer.size) {
            fwrite(buf.refTo(off), len.toULong(), 1, stream)
        } else {
            buf.copyInto(buffer, bufferPos, off, off + len)
            bufferPos += len
        }
    }

    actual override fun write(buf: ByteArray) {
        _write(buf, 0, buf.size)
    }

    actual override fun write(buf: ByteArray, len: Int) {
        _write(buf, 0, len)
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun _print(x: String) {
        val buf = x.encodeToByteArray()
        _write(buf, 0, buf.size)
    }

    actual override fun println(x: String) = _print("$x\n")
    actual override fun print(x: String) = _print(x)
    actual override fun print(x: Boolean) = _print("$x")
    actual override fun print(x: Int) = _print("$x")
    actual override fun print(x: Double) = _print("$x")
    actual override fun println() = _print("\n")
}
