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

package simora.simulator_iot

import kotlin.random.Random

public class RandomGenerator {
    private var seed: Int = 1
        set(value) {
            field = value
            random = Random(value)
        }
    private var random: Random = Random(seed)

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun getDouble(minInclusive: Double, maxInclusive: Double): Double {
        if (minInclusive == maxInclusive) {
            return minInclusive
        }
        val maxExclusive = maxInclusive + Double.MIN_VALUE
        return random.nextDouble(minInclusive, maxExclusive)
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun getLong(minInclusive: Long, maxInclusive: Long): Long =
        getDouble(minInclusive.toDouble(), maxInclusive.toDouble()).toLong()

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun getBoolean(probabilityOfTrue: Float): Boolean =
        random.nextFloat() < probabilityOfTrue
}
