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

import simora.parser.JsonParserObject
import simora.shared.inline.File
import kotlin.math.sqrt

internal class MultipleSimulationRuns(
    private val json: JsonParserObject,
) {

    internal fun startSimulationRuns() {
        val measurements = mutableListOf<LoggerMeasure>()
        json.getOrEmptyObject("logging").getOrEmptyObject("simora.simulator_iot.LoggerMeasure")["enabled"] = true
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
        for (repetition in 0 until numberOfRepetitions) {
            val simRun = SimulationRun()
            val config = simRun.parseConfig(json, "", false)
            simRun.startSimulation(config)
            for (logger in simRun.logger.loggers) {
                if (logger is LoggerMeasure) {
                    measurements.add(logger)
                    appendLineToFile("measurement.csv", { logger.getHeadersAggregated().toList().joinToString(",") }, logger.getDataAggregated().toList().joinToString(","))
                }
            }
        }
        if (measurements.size > 0) {
val size=measurements[0].getDataAggregated().size
            val firstLogger = measurements.first()
            val dataAvg = DoubleArray(size)
            val dataDev = DoubleArray(size)
            val dataDevp = DoubleArray(size)
            for (i in 0 until size) {
                var sum = 0.0
                for (m in measurements) {
                    sum += m.getDataAggregated()[i]
                }
                val avg = sum / measurements.size
                var dev = 0.0
                for (m in measurements) {
                    dev += (m.getDataAggregated()[i] - avg) * (m.getDataAggregated()[i] - avg)
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
            appendLineToFile("average.csv", { firstLogger.getHeadersAggregated().toList().joinToString(",") }, dataAvg.toList().joinToString(","))
            appendLineToFile("deviation.csv", { firstLogger.getHeadersAggregated().toList().joinToString(",") }, dataDev.toList().joinToString(","))
            appendLineToFile("deviationPercent.csv", { firstLogger.getHeadersAggregated().toList().joinToString(",") }, dataDevp.toList().joinToString(","))
        }
    }
}
