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
    val fd: Int
    init {
        if (append) {
            fd = openSync(filename, "a")
        } else {
            fd = openSync(filename, "w")
        }
    }

    override fun write(buf: ByteArray) {
        write(buf, buf.size)
    }
    private fun write(buf: ByteArray, off: Int, len: Int) {
        writeSync(fd, buf, off, len)
    }
    override fun write(buf: ByteArray, len: Int) {
        write(buf, 0, len)
    }

    override fun close() {
        closeSync(fd)
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
