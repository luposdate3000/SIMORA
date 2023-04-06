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
import py4j.GatewayServer
import simora.parser.JsonParser
import simora.parser.JsonParserObject
import simora.shared.inline.File

@OptIn(kotlin.time.ExperimentalTime::class)
public actual class EvaluationJavaBridge actual constructor () : IEvaluationJavaBridge {
    internal var json: JsonParserObject? = null
    public override fun evalConfigFileMerge(configFileNames: List<String>) {
        val stamp = Clock.System.now()
        json = JsonParser().fileMergeToJson(configFileNames)
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
                "_$t"
            }
        }
        val outputdirectory = json!!.getOrDefault("outputDirectory", outputdirectoryTmp.replace("luposdate3000", "")) + "/"
        println("outputdirectory=$outputdirectory")
        File(outputdirectory).mkdirs()
        File("$outputdirectory.generated.parsed.json").withOutputStream { out -> // this reformats the json file, such that all files are structurally equal
            out.println(JsonParser().jsonToString(json!!))
        }
        json!!.getOrEmptyObject("logging").getOrEmptyObject("simora.LoggerMeasure")["enabled"] = true
        val outputDirectory = json!!.getOrDefault("outputDirectory", "simulator_output") + "/"
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

        val numberOfRepetitions: Int = json!!.getOrDefault("repeatSimulationCount", 1)
        val measurementsm = mutableMapOf<String, MutableList<DoubleArray>>()
        var headerLine = ""

        GatewayServer(this).start()
    }
    public override fun getIntermediateResultsFor(sparql: String, order: String): Long {
        try {
            val simRun = SimulationRun()
            simRun.startConfigurationStamp = Clock.System.now()
            val a = json!!.getOrEmptyObject("deviceType")
            val b = a.getOrEmptyObject("Central Tower")
            val c = b.getOrEmptyObject("applications")
            val d = c.getOrEmptyArray("simora.applications.scenario.parking.ApplicationFactory_QuerySender")
            val e = d[0]
            val f = e as JsonParserObject
            f.set("query", sparql)
            f.set("order", order)
            simRun.parseConfig(json!!, "", false)
            simRun.startSimulation2()
            for (logger in simRun.logger.loggers) {
                if (logger is LoggerMeasure) {
                    val res = logger.data.last()[LoggerMeasureBase.StatNetworkTraffic]
                    logger.clear()
                    return res.toLong()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -1
    }
    public override fun getIntermediateResultsFor(sparql: String): Long {
        try {
            val simRun = SimulationRun()
            simRun.startConfigurationStamp = Clock.System.now()
            (json!!.getOrEmptyObject("deviceType").getOrEmptyObject("Central Tower").getOrEmptyObject("applications").getOrEmptyArray("simora.applications.scenario.parking.ApplicationFactory_QuerySender")[0] as JsonParserObject).set("query", sparql)
            simRun.parseConfig(json!!, "", false)
            simRun.startSimulation()
            for (logger in simRun.logger.loggers) {
                if (logger is LoggerMeasure) {
                    val res = logger.data.last()[LoggerMeasureBase.StatNetworkTraffic]
                    logger.clear()
                    return res.toLong()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -1
    }
}
