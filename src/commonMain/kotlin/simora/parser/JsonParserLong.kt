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

package simora.parser

internal class JsonParserLong(internal var value: Long) : IJsonParserValue {
    private var accessed0 = false
    override fun setAccessed() {
        accessed0 = true
    }

    override fun isAccessed(): Boolean = accessed0
    private var default0: Long? = null
    internal fun setDefault(v: Long) {
        default0 = v
    }

    internal fun getDefault() = default0
    override fun cloneJson(): JsonParserLong = this
}
