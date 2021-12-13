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

import simora.parser.IJsonParserValue
import simora.parser.JsonParser
import simora.parser.JsonParserObject
import simora.shared.inline.File
import simora.simulator_core.Simulation
import simora.simulator_iot.config.Configuration

public class SimulationRun {

    internal lateinit var sim: Simulation

    internal val randGenerator = RandomGenerator()
    internal val config: Configuration = Configuration(this)

    internal val logger: Loggers = Loggers(mutableListOf())

    internal var notInitializedClock: Long = -1

    internal var simSteadyClock: Long = notInitializedClock

    internal var simMaxClock: Long = notInitializedClock

    internal fun parseConfig(json: IJsonParserValue, fileName: String, autocorrect: Boolean = true): Configuration {
        return parseConfig(json as JsonParserObject, fileName, autocorrect)
    }

    internal fun parseConfig(json: JsonParserObject, fileName: String, autocorrect: Boolean = true): Configuration {
        config.parse(json, fileName, autocorrect)
        return config
    }

    internal fun parseConfig(fileName: String, autocorrect: Boolean = true, modifyJson: (JsonParserObject) -> Unit = {}): Configuration {
        val fileStr = File(fileName).readAsString()
        val json = JsonParser().stringToJson(fileStr) as JsonParserObject
        modifyJson(json)
        return parseConfig(json, fileName, autocorrect)
    }

    internal fun startSimulation(configuration: Configuration) {
        sim = Simulation(configuration.getEntities())
        sim.logger = logger
        sim.maxClock = if (simMaxClock == notInitializedClock) sim.maxClock else simMaxClock
        sim.steadyClock = if (simSteadyClock == notInitializedClock) sim.steadyClock else simSteadyClock
        logger.onStartSimulation()
        sim.startSimulation()
        logger.onStopSimulation()
    }
}
