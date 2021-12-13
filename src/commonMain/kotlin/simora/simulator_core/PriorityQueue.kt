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

package simora.simulator_core

internal class PriorityQueue<E>(private val comparator: Comparator<E>) {

    private val queue = mutableListOf<E>()

    internal fun enqueue(newElement: E) {
        var insertionIndex = queue.binarySearch(newElement, comparator)
        if (insertionIndex < 0) {
            insertionIndex = insertionIndex.inv()
        }
        queue.add(insertionIndex, newElement)
    }

    internal fun dequeue(): E =
        queue.removeAt(0)

    internal fun peek(): E =
        queue[0]

    internal fun hasNext(): Boolean = queue.isNotEmpty()
}
