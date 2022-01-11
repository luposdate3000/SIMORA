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

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.refTo
import platform.posix.FILE
import platform.posix.F_OK
import platform.posix.SEEK_END
import platform.posix.SEEK_SET
import platform.posix.S_IRWXU
import platform.posix.access
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.mkdir
import simora.shared.IMyOutputStream

internal actual class File actual constructor(filename: String) {
    internal val filename: String

    init {
        this.filename = filename.replace("\\", "/").replace("/./", "/").replace("//", "/")
    }

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun exists() = access(filename, F_OK) == 0

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun mkdirs(): Boolean {
        var arr = filename.split("/").filterNot { it == "" || it == "." }
        if (filename.startsWith("/")) {
            arr = listOf("") + arr
        }
        var i = arr.size
        while (i >= 0) {
            if (mkdir(arr.subList(0, i).joinToString("/"), S_IRWXU) == 0) {
                break
            }
            i--
        }
        i++
        while (i <= arr.size) {
            if (mkdir(arr.subList(0, i).joinToString("/"), S_IRWXU) != 0) {
                return false
            }
            i++
        }
        return true
    }

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun readAsString(): String {
        val stream = myOpen(filename, "rb")
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
        return if (append) {
            MyOutputStream(myOpen(filename, "ab"))
        } else {
            MyOutputStream(myOpen(filename, "wb"))
        }
    }

    internal actual inline fun withOutputStream(crossinline action: (IMyOutputStream) -> Unit) {
        val printer = MyOutputStream(myOpen(filename, "wb"))
        try {
            action(printer)
        } finally {
            printer.close()
        }
    }

    private inline fun myOpen(name: String, mode: String): CPointer<FILE> {
        return fopen(name, mode) ?: throw Exception("File '$name' error for mode '$mode'")
    }
}
