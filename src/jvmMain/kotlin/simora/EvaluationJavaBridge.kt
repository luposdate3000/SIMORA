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
import simora.applications.scenario.parking.Package_Query
import simora.models.net.NetworkPackage
import simora.parser.JsonParser
import simora.parser.JsonParserObject
import simora.shared.inline.File

@OptIn(kotlin.time.ExperimentalTime::class)
public actual class EvaluationJavaBridge actual constructor () : IEvaluationJavaBridge {
    internal val simRun = SimulationRun()
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
        File(outputdirectory).mkdirs()
        File("$outputdirectory.generated.parsed.json").withOutputStream { out ->
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
        simRun.startConfigurationStamp = Clock.System.now()
        simRun.parseConfig(json!!, "", false)
        simRun.startSimulation2()
        println("waiting for connections")
        GatewayServer(this).start()
    }
    public override fun getIntermediateResultsFor(sparql: String, order: String): Long {
        val d = simRun.getAllDevicesForFeature(simRun.featureIdForName2("DatabaseQuery")).first()
        val pck = Package_Query(d.address, sparql.encodeToByteArray(), if (order != "") { order.encodeToByteArray() } else { null })
        val netpck = NetworkPackage(d.address, d.address, pck)
        simRun.addEvent(0L, d, d, netpck)
        simRun.run()
        try {
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
        return getIntermediateResultsFor(sparql, "")
    }
}
