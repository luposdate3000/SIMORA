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
import kotlinx.datetime.Clock
import simora.parser.JsonParser
import simora.shared.inline.File
import kotlin.math.sqrt

@OptIn(kotlin.time.ExperimentalTime::class)
public class Evaluation {

    public fun evalConfigFileMerge(configFileNames: List<String>) {
        val stamp = Clock.System.now()
        val json = JsonParser().fileMergeToJson(configFileNames)
        var outputdirectoryTmp = Config.defaultOutputDirectory + "/"
        for (n in configFileNames) {
            val a = n.lastIndexOf("/") + 1
            val b = n.lastIndexOf(".")
            val t = if (a >= 0) {
                if (b >= 0) {
                    n.substring(a, b)
                } else {
                    n.substring(a, n.length)
                }
            } else {
                if (b >= 0) {
                    n.substring(0, b)
                } else {
                    n
                }
            }
            outputdirectoryTmp += if (outputdirectoryTmp == "") {
                t
            } else {
                "/$t"
            }
        }
        val outputdirectory = json.getOrDefault("outputDirectory", outputdirectoryTmp.replace("luposdate3000","")) + "/"
        println("outputdirectory=$outputdirectory")
        File(outputdirectory).mkdirs()
        File("$outputdirectory.generated.parsed.json").withOutputStream { out -> // this reformats the json file, such that all files are structurally equal
            out.println(JsonParser().jsonToString(json))
        }
        json.getOrEmptyObject("logging").getOrEmptyObject("simora.LoggerMeasure")["enabled"] = true
        val outputDirectory = json.getOrDefault("outputDirectory", "simulator_output") + "/"
        File(outputDirectory).mkdirs()
        fun appendLineToFile(name: String, header: () -> String, line: String) {
            val f = File(outputDirectory + name)
            val flag = f.exists()
            val stream = f.openOutputStream(flag)
            if (!flag) {
                stream.println(header())
            }
            stream.println(line)
            stream.close()
        }

        val numberOfRepetitions: Int = json.getOrDefault("repeatSimulationCount", 1)
        val initTime = Clock.System.now() - stamp
        val measurementsm = mutableMapOf<String, MutableList<DoubleArray>>()
        var headerLine = ""
        for (repetition in 0 until numberOfRepetitions) {
            val simRun = SimulationRun()
            simRun.startConfigurationStamp = Clock.System.now() - initTime
            simRun.parseConfig(json, "", false)
            simRun.startSimulation()
            for (logger in simRun.logger.loggers) {
                if (logger is LoggerMeasure) {
                    val (datas, labels) = logger.getDataAggregated2()
                    for (i in 0 until datas.size) {
                        val data = datas[i]
                        val label = labels[i]
                        var t = measurementsm[label]
                        if (t == null) {
                            t = mutableListOf()
                            measurementsm[label] = t
                        }
                        t.add(data)
                        headerLine = "phase," + logger.getHeadersAggregated().toList().joinToString(",")
                        appendLineToFile("measurement.csv", { headerLine }, label + "," + data.toList().joinToString(","))
                    }
                }
            }
        }
        for ((label, measurements) in measurementsm) {
            if (measurements.size > 0) {
                val size = measurements[0].size
                val dataAvg = DoubleArray(size)
                val dataDev = DoubleArray(size)
                val dataDevp = DoubleArray(size)
                for (i in 0 until size) {
                    var sum = 0.0
                    for (m in measurements) {
                        sum += m[i]
                    }
                    val avg = sum / measurements.size
                    var dev = 0.0
                    for (m in measurements) {
                        dev += (m[i] - avg) * (m[i] - avg)
                    }
                    val devPercent = if (avg == 0.0) {
                        0.0
                    } else {
                        sqrt(dev / measurements.size) * 100 / avg
                    }
                    dataAvg[i] = avg
                    dataDev[i] = dev
                    dataDevp[i] = devPercent
                }
                appendLineToFile("average.csv", { headerLine }, label + "," + dataAvg.toList().joinToString(","))
                appendLineToFile("deviation.csv", { headerLine }, label + "," + dataDev.toList().joinToString(","))
                appendLineToFile("deviationPercent.csv", { headerLine }, label + "," + dataDevp.toList().joinToString(","))
            }
        }
        File("$outputdirectory.generated.used.json").withOutputStream { out -> // this reformats the json file, such that all files are structurally equal
            out.println(JsonParser().jsonToString(json))
        }
    }
}
