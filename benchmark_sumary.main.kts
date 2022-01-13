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
    try {
        val filename = f.toString()
        if (filename != "simulator_output") {
            val x = filename.substring(filename.lastIndexOf("/") + 2).split("_")
            var headers: List<String>? = null
            val baseRow = mutableMapOf<String, String>()
            baseRow["platform"] = x[0]
            baseRow["benchmark_case"] = x[1]
            baseRow["multicast"] = x[2]
            baseRow["routing"] = x[3]
            baseRow["scenario"] = x[4]
            baseRow["topology"] = x[5]
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
                    try {
                        row["system time (Seconds)"] = (baseRow["system time (Seconds)"]!!.toDouble() / lines.size.toDouble()).toString()
                    } catch (e: Throwable) {
                    }
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
//                        row[h[i] + "Min"] = valueMin.toString()
//                        row[h[i] + "Max"] = valueMax.toString()
//                        row[h[i] + "Q1"] = valueQ1.toString()
//                        row[h[i] + "Q3"] = valueQ3.toString()
                    }
                    addRow(row)
                }
            } catch (e: Throwable) {
            }
        }
    } catch (e: Throwable) {
    }
}
val plot_scalability = Array(6) { mutableMapOf<Int, Double>() }
val platformID = argumentNames.indexOf("platform")
val routingID = argumentNames.indexOf("routing")
val packageCountID = argumentNames.indexOf("number of sent packages")
val topologyID = argumentNames.indexOf("topology")
val scenarioID = argumentNames.indexOf("scenario")
val durationID = argumentNames.indexOf("simulation total duration real (Seconds)")
val trafficID = argumentNames.indexOf("network traffic total (Bytes)")
val benchmarkID = argumentNames.indexOf("benchmark_case")
val routingMap = arrayOf("ASP", "RPLFast")
val scenarioMap = arrayOf("iotconfigurationmulticast", "iotconfigurationmulticastStateOfTheArt", "iotconfigurationunicast", "iotconfigurationbroadcast")
val topologyMap = arrayOf("Full128", "Uniform128", "Random128", "Ring128")
val plot_routing = DoubleArray(scenarioMap.size * topologyMap.size * routingMap.size)
val plot_routing_ctr = IntArray(scenarioMap.size * topologyMap.size * routingMap.size)
var plot_routing_max = 1.0
File("summary.csv").printWriter().use { out ->
    out.println(argumentNames.joinToString(","))
    for (r in rows) {
        out.println(r.joinToString(",") + ",".repeat(argumentNames.size - r.size))
        when (r[benchmarkID]) {
            "routing" -> {
                var x = routingMap.indexOf(r[routingID])
                var y = scenarioMap.indexOf(r[scenarioID])
                var z = topologyMap.indexOf(r[topologyID])
                var v = r[trafficID].toDouble()
                var idx = (z * scenarioMap.size + y) * routingMap.size + x
                plot_routing[idx] = v
                plot_routing_ctr[idx] = r[packageCountID].toDouble().toInt()
                if (plot_routing_max < v) {
                    plot_routing_max = v
                }
            }
            "scalability" -> {
                val x = r[topologyID].substring("Strong".length).toInt()
                val y = r[durationID].toDouble()
                if (r[platformID] == "linux") {
                    if (r[routingID] == "RPLFastLate") {
                        plot_scalability[2][x] = y
                    }
                    if (r[routingID] == "ASP") {
                        plot_scalability[3][x] = y
                    }
                } else if (r[platformID] == "js") {
                    if (r[routingID] == "RPLFastLate") {
                        plot_scalability[4][x] = y
                    }
                    if (r[routingID] == "ASP") {
                        plot_scalability[5][x] = y
                    }
                } else {
                    if (r[routingID] == "RPLFastLate") {
                        plot_scalability[0][x] = y
                    }
                    if (r[routingID] == "ASP") {
                        plot_scalability[1][x] = y
                    }
                }
            }
        }
    }
}
for (i in 0 until plot_scalability.size) {
    File("plot_scalability${i + 1}.csv").printWriter().use { out ->
        for (k in plot_scalability[i].keys.sorted()) {
            out.println("$k,${plot_scalability[i][k]}")
        }
    }
}
File("plot_routing.csv").printWriter().use { out ->
    val x1 = routingMap.indexOf("ASP")
    val x2 = routingMap.indexOf("RPLFast")
    out.println("," + topologyMap.joinToString().replace("128", ""))
    for (y in 0 until scenarioMap.size) {
        var row = scenarioMap[y].replace("iotconfiguration", "").replaceFirstChar { it.uppercaseChar() }
        for (z in 0 until topologyMap.size) {
            val idx1 = (z * scenarioMap.size + y) * routingMap.size + x1
            val idx2 = (z * scenarioMap.size + y) * routingMap.size + x2
            row = "$row,${(plot_routing[idx1] + plot_routing[idx2]) / 2048.0}"
        }
        out.println(row)
    }
}
File("plot_routing_abs.map").printWriter().use { outMap ->
    File("plot_routing_abs.csv").printWriter().use { out ->
        var ctr = 1000000
        val x1 = routingMap.indexOf("ASP")
        val x2 = routingMap.indexOf("RPLFast")
        out.println("," + topologyMap.joinToString().replace("128", ""))
        for (y in 0 until scenarioMap.size) {
            var row = scenarioMap[y].replace("iotconfiguration", "").replaceFirstChar { it.uppercaseChar() }
            for (z in 0 until topologyMap.size) {
                val idx1 = (z * scenarioMap.size + y) * routingMap.size + x1
                val idx2 = (z * scenarioMap.size + y) * routingMap.size + x2
                val v1 = (plot_routing[idx1] / 1024.0).toInt()
                val v2 = (plot_routing[idx2] / 1024.0).toInt()
                val v1b = plot_routing_ctr[idx1] / 1000
                val v2b = plot_routing_ctr[idx2] / 1000
                val c = ctr++
                outMap.println("$c.00:\\\\shortstack{$v1 ($v1b)\\\\\\\\$v2 ($v2b)}")
                row = "$row,$c.00"
            }
            out.println(row)
        }
    }
}
File("plot_routing_asp.csv").printWriter().use { out ->
    val x = routingMap.indexOf("ASP")
    out.println("," + topologyMap.joinToString().replace("128", ""))
    for (y in 0 until scenarioMap.size) {
        var row = scenarioMap[y].replace("iotconfiguration", "")
        for (z in 0 until topologyMap.size) {
            val idx = (z * scenarioMap.size + y) * routingMap.size + x
            row = "$row,${plot_routing[idx] / plot_routing_max * 100.0}"
        }
        out.println(row)
    }
}
File("plot_routing_rpl.csv").printWriter().use { out ->
    val x = routingMap.indexOf("RPLFast")
    out.println("," + topologyMap.joinToString().replace("128", ""))
    for (y in 0 until scenarioMap.size) {
        var row = scenarioMap[y].replace("iotconfiguration", "")
        for (z in 0 until topologyMap.size) {
            val idx = (z * scenarioMap.size + y) * routingMap.size + x
            row = "$row,${plot_routing[idx] / plot_routing_max * 100.0}"
        }
        out.println(row)
    }
}
File("plot_routing_asp_abs.csv").printWriter().use { out ->
    val x = routingMap.indexOf("ASP")
    out.println("," + topologyMap.joinToString().replace("128", ""))
    for (y in 0 until scenarioMap.size) {
        var row = scenarioMap[y].replace("iotconfiguration", "")
        for (z in 0 until topologyMap.size) {
            val idx = (z * scenarioMap.size + y) * routingMap.size + x
            row = "$row,${plot_routing[idx]}"
        }
        out.println(row)
    }
}
File("plot_routing_rpl_abs.csv").printWriter().use { out ->
    val x = routingMap.indexOf("RPLFast")
    out.println("," + topologyMap.joinToString().replace("128", ""))
    for (y in 0 until scenarioMap.size) {
        var row = scenarioMap[y].replace("iotconfiguration", "")
        for (z in 0 until topologyMap.size) {
            val idx = (z * scenarioMap.size + y) * routingMap.size + x
            row = "$row,${plot_routing[idx]}"
        }
        out.println(row)
    }
}
