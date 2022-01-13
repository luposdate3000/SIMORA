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
@file:JsModule("fs")
@file:JsNonModule
package simora.shared.inline

import kotlin.js.JsName

@JsName("openSync")
internal external fun openSync(filename: String, flags: String): Int

@JsName("readSync")
internal external fun readSync(fd: Int, buffer: ByteArray, offset: Int, length: Int, position: Int): Int

@JsName("writeSync")
internal external fun writeSync(fd: Int, buffer: ByteArray, offset: Int, length: Int): Int

@JsName("closeSync")
internal external fun closeSync(fd: Int)

@JsName("mkdirSync")
internal external fun mkdirSync(filename: String)

@JsName("existsSync")
internal external fun existsSync(filename: String): Boolean
