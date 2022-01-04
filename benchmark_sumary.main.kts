#!/usr/bin/env kotlin
import java.io.File

val fileToUse = "measurement.csv"
// val fileToUse="average.csv"

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
        baseRow["platform"] = x[0]
        baseRow["benchmark_case"] = x[1]
        baseRow["routing"] = x[2]
        baseRow["scenario"] = x[3]
        baseRow["topology"] = x[4]
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
            var lines = mutableListOf<List<String>>()
            File(filename + "/$fileToUse").forEachLine { line ->
                val l = line.split(",")
                val h = headers
                if (h == null) {
                    headers = l
                } else {
                    lines.add(l)
                }
            }
            val h = headers
            if (h != null) {
                val row = mutableMapOf<String, String>()
                row.putAll(baseRow)
                val timeUsed = (baseRow["system time (Seconds)"]!!.toDouble() / lines.size.toDouble()).toString()
                row["system time (Seconds)"] = timeUsed
                val s = if (h.size < lines[0].size) h.size else lines[0].size
                for (i in 0 until s) {
                    val values = mutableListOf<Double>()
                    for (l in lines) {
                        values.add(l[i].toDouble())
                    }
                    val valueAvg = values.sum() / values.size
                    val valueMin = values.first()
                    val valueMax = values.last()
                    val valueMedian = values[values.size / 2]
                    val valueQ1 = values[values.size / 4]
                    val valueQ3 = values[values.size * 3 / 4]
// https://www.khanacademy.org/math/statistics-probability/summarizing-quantitative-data/box-whisker-plots/a/box-plot-review
                    row[h[i]] = valueAvg.toString()
                    row[h[i] + "Min"] = valueMin.toString()
                    row[h[i] + "Max"] = valueMax.toString()
                    row[h[i] + "Q1"] = valueQ1.toString()
                    row[h[i] + "Q3"] = valueQ3.toString()
                }
                addRow(row)
            }
        } catch (e: Throwable) {
        }
    }
}

println(argumentNames.joinToString(","))
for (r in rows) {
    println(r.joinToString(",") + ",".repeat(argumentNames.size - r.size))
}
