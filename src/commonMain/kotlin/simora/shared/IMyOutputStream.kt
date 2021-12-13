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
package simora.shared

internal interface IMyOutputStream {
    internal fun write(buf: ByteArray): Unit
    internal fun write(buf: ByteArray, len: Int): Unit
    internal fun close(): Unit
    internal fun flush(): Unit
    internal fun println(x: String)
    internal fun print(x: String)
    internal fun print(x: Boolean)
    internal fun print(x: Int)
    internal fun print(x: Double)
    internal fun println()
}
