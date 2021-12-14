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
import simora.simulator_core.Entity
import simora.simulator_core.Event
import simora.simulator_core.PriorityQueue
import simora.simulator_iot.config.Configuration
public class SimulationRun {

    internal val randGenerator = RandomGenerator()
    public val config: Configuration = Configuration(this)

    internal val logger: Loggers = Loggers(mutableListOf())

    public var notInitializedClock: Long = -1

    public var simSteadyClock: Long = notInitializedClock

    public var simMaxClock: Long = notInitializedClock

    public fun parseConfig(json: IJsonParserValue, fileName: String, autocorrect: Boolean = true): Configuration {
        return parseConfig(json as JsonParserObject, fileName, autocorrect)
    }

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
    private lateinit var entities: List<Entity>
    public fun startSimulation(configuration: Configuration) {
        entities = configuration.getEntities()
        maxClock = if (simMaxClock == notInitializedClock) maxClock else simMaxClock
        steadyClock = if (simSteadyClock == notInitializedClock) steadyClock else simSteadyClock
        logger.onStartSimulation()
        startSimulation()
        logger.onStopSimulation()
    }
    private var futureEvents: PriorityQueue<Event> = PriorityQueue(compareBy<Event> { it.occurrenceTime }.thenBy { it.eventNumber })

    public var maxClock: Long = Long.MAX_VALUE

    public var steadyClock: Long = Long.MAX_VALUE

    public var clock: Long = 0

    private var addedEventCounter: Int = 0

    internal fun startSimulation() {
        startUp()
        run()
        shutDown()
    }

    private fun startUpAllEntities() {
        for (entity: Entity in entities) {
            entity.simulation = this
            entity.onStartUp()
        }
    }

    public fun run() {
        var isFinished = false
        while (!isFinished)
            isFinished = runNextTimeStep()
    }

    public fun runNextTimeStep(): Boolean {
        if (!futureEvents.hasNext()) {
            return true
        }

        if (isSteadyStateReached()) {
            transferToSteadyState()
        }

        if (isMaxClockReached()) {
            return true
        }

        processEvent()
        return false
    }
    private fun processEvent() {
        val nextEvent = futureEvents.dequeue()
        clock = nextEvent.occurrenceTime
        val entity = nextEvent.destination
        entity.processIncomingEvent(nextEvent)
    }

    private fun transferToSteadyState() {
        clock = steadyClock
        notifyAboutSteadyState()
    }

    private fun getTimeOfNextTimeStep() = futureEvents.peek().occurrenceTime

    private fun isSteadyStateReached() = getTimeOfNextTimeStep() > steadyClock

    private fun isMaxClockReached() = getTimeOfNextTimeStep() > maxClock

    private fun notifyAboutSteadyState() {
        for (entity in entities) {
            entity.onSteadyState()
        }
        logger.onSteadyState() // call this last due to time measurement
    }

    internal fun addEvent(delay: Long, src: Entity, dest: Entity, data: Any) {
        addedEventCounter++
        val occurringTime = clock + delay
        val ev = Event(addedEventCounter, occurringTime, src, dest, data)
        futureEvents.enqueue(ev)
    }

    public fun startUp() {
        startUpAllEntities()
        logger.onStartUp() // call this last due to time measurement
    }

    public fun shutDown() {
        logger.onShutDown() // call this first due to time measurement
        for (ent: Entity in entities) {
            ent.onShutDown()
        }
    }
}
