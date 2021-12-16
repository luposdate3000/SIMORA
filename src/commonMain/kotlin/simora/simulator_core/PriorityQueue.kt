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
internal class PriorityQueueNode(var value: Any, var key: Long) {
    var parent: PriorityQueueNode? = null
    var child: PriorityQueueNode? = null
    var prev: PriorityQueueNode? = null
    var next: PriorityQueueNode? = null
    var rank = 0
    var mark = false

    fun meld1(node: PriorityQueueNode) {
        this.prev?.next = node
        node.prev = this.prev
        node.next = this
        this.prev = node
    }

    fun meld2(node: PriorityQueueNode) {
        this.prev?.next = node
        node.prev?.next = this
        val temp = this.prev
        this.prev = node.prev
        node.prev = temp
    }
}

internal class PriorityQueue(var node: PriorityQueueNode? = null) {

    fun insert(v: Any, k: Long): PriorityQueueNode {
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

    fun union(other: PriorityQueue) {
        if (this.node == null) {
            this.node = other.node
        } else if (other.node != null) {
            this.node!!.meld2(other.node!!)
            if (other.node!!.key < this.node!!.key) this.node = other.node
        }
        other.node = null
    }

    fun minimum(): PriorityQueueNode? = this.node

    fun extractMin(): Any? {
        val min = this.node
        if (min == null) return null
        val roots = mutableMapOf<Int, PriorityQueueNode>()

        fun add(r: PriorityQueueNode) {
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
            return min.value
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
        return min.value
    }

    fun decreaseKey(n: PriorityQueueNode, value: Any, key: Long) {
        require(n.key> key) {
            "In 'decreaseKey' new value greater than existing value"
        }
        n.value = value
        n.key = key
        if (n == this.node) return
        val p = n.parent
        if (p == null) {
            if (key < this.node!!.key) this.node = n
            return
        }
        cutAndMeld(n)
    }

    private fun cut(x: PriorityQueueNode) {
        val p = x.parent
        if (p == null) return
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

    private fun cutAndMeld(x: PriorityQueueNode) {
        cut(x)
        x.parent = null
        this.node?.meld1(x)
    }

    fun delete(n: PriorityQueueNode) {
        val p = n.parent
        if (p == null) {
            if (n == this.node) {
                extractMin()
                return
            }
            n.prev?.next = n.next
            n.next?.prev = n.prev
        } else {
            cut(n)
        }
        var c = n.child
        if (c == null) return
        while (true) {
            c!!.parent = null
            c = c.next
            if (c == n.child) break
        }
        this.node?.meld2(c!!)
    }
}
