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
import simora.shared.IMyJSInputStream

internal class MyInputStream constructor(filename: String) : IMyJSInputStream {
    val fd = openSync(filename, "r")
    var pos = 0
    private fun read(buf: ByteArray, off: Int, len: Int): Int {
        return readSync(fd, buf, off, len, pos)
    }

    override fun read(buf: ByteArray, len: Int): Int {
        val l = read(buf, 0, len)
        pos += l
        return l
    }

    override fun close() {
        closeSync(fd)
    }
}
