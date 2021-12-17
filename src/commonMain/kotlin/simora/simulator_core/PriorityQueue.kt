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
// https://rosettacode.org/wiki/Fibonacci_heap#Kotlin

internal class PriorityQueue<V>(private var node: PriorityQueueNode<V>? = null) {

    fun insert(v: V, k: Long): PriorityQueueNode<V> {
        val x = PriorityQueueNode(v, k)
        if (this.node == null) {
            x.next = x
            x.prev = x
            this.node = x
        } else {
            this.node!!.meld1(x)
            if (x.key < this.node!!.key) this.node = x
        }
        return x
    }

    fun extractMinValue(): V? = extractMin()?.value
    private fun extractMin(): PriorityQueueNode<V>? {
        val min = this.node ?: return null
        val roots = mutableMapOf<Int, PriorityQueueNode<V>>()

        fun add(r: PriorityQueueNode<V>) {
            r.prev = r
            r.next = r
            var rr = r
            while (true) {
                var x = roots[rr.rank] ?: break
                roots.remove(rr.rank)
                if (x.key < rr.key) {
                    val t = rr
                    rr = x
                    x = t
                }
                x.parent = rr
                x.mark = false
                if (rr.child == null) {
                    x.next = x
                    x.prev = x
                    rr.child = x
                } else {
                    rr.child!!.meld1(x)
                }
                rr.rank++
            }
            roots[rr.rank] = rr
        }

        var r = this.node!!.next
        while (r != this.node) {
            val n = r!!.next
            add(r)
            r = n
        }
        val c = this.node!!.child
        if (c != null) {
            c.parent = null
            var rr = c.next!!
            add(c)
            while (rr != c) {
                val n = rr.next!!
                rr.parent = null
                add(rr)
                rr = n
            }
        }
        if (roots.isEmpty()) {
            this.node = null
            return min
        }
        val d = roots.keys.first()
        var mv = roots[d]!!
        roots.remove(d)
        mv.next = mv
        mv.prev = mv
        for ((_, rr) in roots) {
            rr.prev = mv
            rr.next = mv.next
            mv.next!!.prev = rr
            mv.next = rr
            if (rr.key < mv.key) mv = rr
        }
        this.node = mv
        return min
    }

    private fun cut(x: PriorityQueueNode<V>) {
        val p = x.parent ?: return
        p.rank--
        if (p.rank == 0) {
            p.child = null
        } else {
            p.child = x.next
            x.prev?.next = x.next
            x.next?.prev = x.prev
        }
        if (p.parent == null) return
        if (!p.mark) {
            p.mark = true
            return
        }
        cutAndMeld(p)
    }

    private fun cutAndMeld(x: PriorityQueueNode<V>) {
        cut(x)
        x.parent = null
        this.node?.meld1(x)
    }
}
