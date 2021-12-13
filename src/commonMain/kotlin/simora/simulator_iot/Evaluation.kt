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
}
