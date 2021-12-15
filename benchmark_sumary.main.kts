#!/usr/bin/env kotlin
import java.io.File

val argumentNames = mutableListOf<String>()
val rows = mutableListOf<MutableList<String>>()

fun addRow(row: Map<String, String>) {
    for (k in row.keys) {
        if (!argumentNames.contains(k)) {
            argumentNames.add(k)
        }
    }
    var r = MutableList<String>(argumentNames.size) { "" }
    for ((k, v) in row) {
        r[argumentNames.indexOf(k)] = v.trim()
    }
    rows.add(r)
}

for (f in File("simulator_output").walk().maxDepth(1)) {
    val filename = f.toString()
    if (filename != "simulator_output") {
        val x = filename.substring(filename.lastIndexOf("/") + 2).split("_")
        var headers: List<String>? = null
        val baseRow = mutableMapOf<String, String>()
        baseRow["routing"] = x[0]
        baseRow["scenario"] = x[1]
        baseRow["topology"] = x[2]
        baseRow["platform"] = x[3]
        try {
            File(filename + "/time").forEachLine { line ->
                if (line.contains("User time (seconds)")) {
                    baseRow["system time (Seconds)"] = line.substring(line.lastIndexOf(":") + 2)
                } else if (line.contains("Maximum resident set size (kbytes)")) {
                    baseRow["system memory (Bytes)"] = (line.substring(line.lastIndexOf(":") + 2).toLong() * 1024L).toString()
                } else if (line.contains("Minor (reclaiming a frame) page faults")) {
                    baseRow["system page faults"] = line.substring(line.lastIndexOf(":") + 2)
                } else if (line.contains("Voluntary context switches")) {
                    baseRow["system context switches"] = line.substring(line.lastIndexOf(":") + 2)
                }
            }
        } catch (e: Throwable) {
        }
        try {
            File(filename + "/measurement.csv").forEachLine { line ->
                val l = line.split(",")
                val h = headers
                if (h == null) {
                    headers = l
                } else {
                    val row = mutableMapOf<String, String>()
                    row.putAll(baseRow)
                    val s = if (h.size < l.size) h.size else l.size
                    for (i in 0 until s) {
                        row[h[i]] = l[i]
                    }
                    addRow(row)
                }
            }
        } catch (e: Throwable) {
        }
    }
}

println(argumentNames.joinToString(","))
for (r in rows) {
    println(r.joinToString(",") + ",".repeat(argumentNames.size - r.size))
}
