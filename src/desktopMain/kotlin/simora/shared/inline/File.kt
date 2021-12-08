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
package simora.shared.inline

import kotlinx.cinterop.*
import platform.posix.*
import simora.shared.IMyInputStream
import simora.shared.IMyOutputStream
internal actual class File {
    internal val filename: String

    actual constructor(filename: String) {
        this.filename = filename.replace("\\", "/").replace("/./", "/").replace("//", "/")
    }

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun exists() = access(filename, F_OK) == 0

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun mkdirs() = mkdir(filename, S_IRWXU) == 0

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun length(): Long {
        val stream = fopen(filename, "rb")
        fseek(stream, 0, SEEK_END)
        val size = ftell(stream).toLong()
        fclose(stream)
        return size
    }

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun readAsString(): String {
        val stream = fopen(filename, "rb")
        fseek(stream, 0, SEEK_END)
        val size = ftell(stream).toInt()
        fseek(stream, 0, SEEK_SET)
        val buf = ByteArray(size)
        var o = 0
        var s = size
        while (s > 0) {
            val tmp = fread(buf.refTo(o), s.toULong(), 1, stream).toInt()
            if (tmp <= 0) {
                break
            }
            s -= tmp
            o += tmp
        }
        fclose(stream)
        return buf.decodeToString()
    }

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun openOutputStream(append: Boolean): IMyOutputStream {
        if (append) {
            return MyOutputStream(fopen(filename, "ab"))
        } else {
            return MyOutputStream(fopen(filename, "wb"))
        }
    }

    internal actual inline fun withOutputStream(crossinline action: (IMyOutputStream) -> Unit) {
        val printer = MyOutputStream(fopen(filename, "wb"))
        try {
            action(printer)
        } finally {
            printer.close()
        }
    }

    internal actual inline fun withInputStream(crossinline action: (IMyInputStream) -> Unit) {
        val printer = MyInputStream(fopen(filename, "rb"))
        try {
            action(printer)
        } finally {
            printer.close()
        }
    }
}
