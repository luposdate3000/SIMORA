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

package simora.simulator_iot

import simora.parser.JsonParser
import simora.parser.JsonParserObject
import simora.shared.inline.File
import simora.simulator_iot.config.Configuration

public class Evaluation {

    private fun simulate(configFileName: String) {
        val simRun = SimulationRun()
        val config = simRun.parseConfig(configFileName)
        simRun.startSimulation(config)
    }

    private fun evalConfigFile(configFileName: String) {
        val json = JsonParser().fileToJson(configFileName) as JsonParserObject
        json.getOrDefault("outputDirectory", Configuration.defaultOutputDirectory + "/" + configFileName.substring(configFileName.lastIndexOf("/") + 1, configFileName.lastIndexOf(".")))
        val runs = MultipleSimulationRuns(json)
        runs.startSimulationRuns()
        File(configFileName).withOutputStream { out -> // this reformats the json file, such that all files are structurally equal
            out.println(JsonParser().jsonToString(json, true))
        }
    }

    public fun evalConfigFileMerge(configFileNames: List<String>) {
        val json = JsonParser().fileMergeToJson(configFileNames)
        var outputdirectoryTmp = Configuration.defaultOutputDirectory + "/"
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
        val outputdirectory = json.getOrDefault("outputDirectory", outputdirectoryTmp) + "/"
        println("outputdirectory=$outputdirectory")
        File(outputdirectory).mkdirs()
        File("$outputdirectory.generated.parsed.json").withOutputStream { out -> // this reformats the json file, such that all files are structurally equal
            out.println(JsonParser().jsonToString(json, false))
        }
        val runs = MultipleSimulationRuns(json)
        runs.startSimulationRuns()
        File("$outputdirectory.generated.used.json").withOutputStream { out -> // this reformats the json file, such that all files are structurally equal
            out.println(JsonParser().jsonToString(json, true))
        }
    }

    private fun evalConfigFiles(configFileNames: Set<String>) {
        for ((index, configFileName) in configFileNames.withIndex()) {
            evalConfigFile(configFileName)
            println("evalQueryProcessingCentralizedCase: Run ${index + 1} finished. ${configFileNames.size - index - 1} runs left..")
        }
    }

    private fun evalConfigFilesMerge(configFileNames: Set<List<String>>) {
        for ((index, configFileName) in configFileNames.withIndex()) {
            evalConfigFileMerge(configFileName)
            println("evalQueryProcessingCentralizedCase: Run ${index + 1} finished. ${configFileNames.size - index - 1} runs left..")
        }
    }
}
