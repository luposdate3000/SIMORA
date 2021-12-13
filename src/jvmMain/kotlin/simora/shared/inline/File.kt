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
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.jvm.JvmField

internal actual class File actual constructor(filename: String) {
    @JvmField
    internal val filename: String

    init {
        this.filename = filename.replace("\\", "/").replace("/./", "/").replace("//", "/")
    }

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun exists() = java.io.File(filename).exists()

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun mkdirs() = java.io.File(filename).mkdirs()

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun readAsString() = java.io.File(filename).readText()

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun openOutputStream(append: Boolean): IMyOutputStream = MyOutputStream(BufferedOutputStream(FileOutputStream(filename, append)))

    internal actual inline fun withOutputStream(crossinline action: (IMyOutputStream) -> Unit) {
        val printer = MyOutputStream(BufferedOutputStream(FileOutputStream(java.io.File(filename))))
        try {
            action(printer)
        } finally {
            printer.close()
        }
    }

}
