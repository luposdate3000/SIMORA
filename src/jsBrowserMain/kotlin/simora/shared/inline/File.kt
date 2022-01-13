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

internal actual class File actual constructor(internal val filename: String) {
    internal companion object {
        internal val inmemoryFs = mutableMapOf<String, ByteArray>()
    }

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun exists(): Boolean =        inmemoryFs[filename] != null
    

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun mkdirs(): Boolean =true

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun readAsString(): String =inmemoryFs[filename]!!.decodeToString()

    internal actual inline fun withOutputStream(crossinline action: (IMyOutputStream) -> Unit) {
        val stream = openOutputStream(false)
        try {
            action(stream)
        } finally {
            stream.close()
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    internal actual inline fun openOutputStream(append: Boolean): IMyOutputStream =MyOutputStream(filename, append)
}
