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

package simora.simulator_core

import simora.shared.SanityCheck

public class Simulation(
    private val entities: List<Entity>,
) {
    internal var logger: ILoggerCore = LoggerCoreNone()

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

    private fun endSimulation() {
        maxClock = clock
    }

    private fun startUpAllEntities() {
        for (entity: Entity in entities) {
            entity.simulation = this
            entity.onStartUp()
        }
    }

    private fun run() {
        var isFinished = false
        while (!isFinished)
            isFinished = runNextTimeStep()
    }

    private fun runNextTimeStep(): Boolean {
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
        SanityCheck.check(
            { /*SOURCE_FILE_START*/"/src/simora/src/commonMain/kotlin/simora/simulator_core/Simulation.kt:103"/*SOURCE_FILE_END*/ },
            { delay >= 0 },
            { "Clock cannot go backwards." }
        )
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

    private fun numberOfEntities(): Int = entities.size
}
