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

package simora

/*
import com.google.monitoring.runtime.instrumentation.AllocationRecorder
import com.google.monitoring.runtime.instrumentation.Sampler
private class SamplerImpl : Sampler {
    data class MappingKey(internal val s: String)
    data class MappingValue(internal var counter: Int, internal var size: Long)
    val mapping = mutableMapOf<MappingKey, MappingValue>()
    override fun sampleAllocation(count: Int, desc: String, newObj: Any, size: Long) {
        if (desc == "java/util/LinkedHashMap\$Entry") {
            println(newObj)
        }
        if (!desc.contains("runtime/instrumentation/asm")) {
            val key = MappingKey(desc)
            val value = mapping[key]
            if (value == null) {
                mapping[key] = MappingValue(1, size)
            } else {
                value.counter++
                value.size += size
            }
        }
    }
    fun finish() {
        for ((name, v) in mapping) {
            val (count, size) = v
            println("$count,$size,${name.s}")
        }
    }
}
public fun main(args: Array<String>) {
    val impl = SamplerImpl()
    AllocationRecorder.addSampler(impl)
    mainfunc(args.toList())
    impl.finish()
}
*/
public fun main(args: Array<String>) {
    mainfunc(args.toList())
}
