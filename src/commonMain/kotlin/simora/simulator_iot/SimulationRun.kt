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
import simora.simulator_core.Event
import simora.simulator_core.PriorityQueue
import simora.simulator_iot.config.Configuration
import simora.simulator_iot.models.Device

public class SimulationRun {
    public val config: Configuration = Configuration(this)
    public var notInitializedClock: Long = -1
    public var simMaxClock: Long = notInitializedClock
    public var maxClock: Long = Long.MAX_VALUE
    public var clock: Long = 0
    internal val randGenerator = RandomGenerator()
    internal val logger: Loggers = Loggers(mutableListOf())
    private var addedEventCounter: Int = 0
    public lateinit var entities: List<Device>
    private var futureEvents: PriorityQueue<Event> = PriorityQueue(compareBy<Event> { it.occurrenceTime }.thenBy { it.eventNumber })

    public fun parseConfig(json: JsonParserObject, fileName: String, autocorrect: Boolean = true): Configuration {
        config.parse(json, fileName, autocorrect)
        return config
    }

    public fun parseConfig(fileName: String, autocorrect: Boolean = true, modifyJson: (JsonParserObject) -> Unit = {}): Configuration {
        val fileStr = File(fileName).readAsString()
        val json = JsonParser().stringToJson(fileStr) as JsonParserObject
        modifyJson(json)
        return parseConfig(json, fileName, autocorrect)
    }

    public fun startSimulation(configuration: Configuration) {
        entities = configuration.getEntities()
        maxClock = if (simMaxClock == notInitializedClock) maxClock else simMaxClock
        logger.onStartSimulation()
        startUp()
        run()
        shutDown()
        logger.onStopSimulation()
    }

    public fun run() {
        while (futureEvents.hasNext()) {
            if (futureEvents.peek().occurrenceTime > maxClock) {
                break
            }
            val nextEvent = futureEvents.dequeue()
            clock = nextEvent.occurrenceTime
            val entity = nextEvent.destination
            entity.processIncomingEvent(nextEvent)
        }
    }

    internal fun addEvent(delay: Long, src: Device, dest: Device, data: Any) {
        addedEventCounter++
        futureEvents.enqueue(Event(addedEventCounter, clock + delay, src, dest, data))
    }

    public fun startUp() {
        for (entity: Device in entities) {
            entity.simulation = this
            entity.onStartUpRouting()
        }
        while (futureEvents.hasNext()) {
            val nextEvent = futureEvents.dequeue()
            clock = nextEvent.occurrenceTime
            val entity = nextEvent.destination
            entity.processIncomingEvent(nextEvent)
        }
        logger.onStartUpRouting()
        for (entity in entities) {
            entity.simulation = this
            entity.onStartUp()
        }
        logger.onStartUp()
    }

    public fun shutDown() {
        logger.onShutDown()
        for (ent in entities) {
            ent.onShutDown()
        }
    }
}
