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

package simora.models

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import simora.simulator_core.Event
import simora.simulator_core.ITimer
import simora.SimulationRun
import simora.applications.IApplicationStack_Actuator
import simora.applications.IApplicationStack_Rooter
import simora.models.net.ILinkManagerWrite
import simora.models.net.NetworkPackage

public class Device(
    private val simRun: SimulationRun,
    internal var latitude: Double,
    internal var longitude: Double,
    public val address: Int,
    private val performance: Double,
    private val isDeterministic: Boolean,
    public val applicationStack: IApplicationStack_Rooter,
    private val hostNameLookUpTable: MutableMap<String, Int>,
) {
    internal lateinit var simulation: SimulationRun
    internal var isStarNetworkChild: Boolean = false
    private lateinit var deviceStart: Instant
    private var isTerminated = false

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun processIncomingEvent(event: Event) {
        if (isTerminated) {
            return
        }
        val data = event.data
        if (data is ITimer) {
            data.onTimerExpired(simulation.clock)
        } else {
            onEvent(data)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun scheduleEvent(destination: Device, data: Any, delay: Long) {
        simulation.addEvent(delay, this, destination, data)
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun setTimer(time: Long, callback: ITimer) {
        scheduleEvent(this, callback, time)
    }

    init {
        applicationStack.setDevice(this)
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    @Suppress("NOTHING_TO_INLINE")
    private inline fun getProcessingDelay(): Long {
        if (isDeterministic) {
            return 1
        }
        val now = Clock.System.now()
        val microDif = (now - deviceStart).inWholeNanoseconds
        val scaled = microDif * 100 / performance
        return scaled.toLong()
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun getNetworkDelay(destinationAddress: Int, pck: NetworkPackage): Long {
        val linkManager = simRun.linkManager as ILinkManagerWrite
        val processingDelay = getProcessingDelay()
        return if (destinationAddress == address) {
            processingDelay
        } else {
            val transmissionDelay = linkManager.getTransmissionDelay(address, destinationAddress, pck.getSizeInBytes())
            transmissionDelay + processingDelay
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun onStartUp() {
        deviceStart = Clock.System.now()
        applicationStack.startUp()
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun onStartUpRouting() {
        deviceStart = Clock.System.now()
        applicationStack.startUpRouting()
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun onEvent(data: Any) {
        deviceStart = Clock.System.now()
        val pck = data as NetworkPackage
        simRun.logger.onReceiveNetworkPackage(address, pck.payload)
        applicationStack.receive(pck)
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun onShutDown() {
        applicationStack.shutDown()
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun closestDeviceWithFeature(name: String): Int {
        val devicesWithFeature = simRun.getAllDevicesForFeature(simRun.featureIdForName2(name)).toMutableList()
        if (devicesWithFeature.size == 0) {
            return -1
        }
        var closestDevice: Device? = null
        var closestDistance = 0.0
        for (d in devicesWithFeature) {
            val dist = simRun.getDistanceInMeters(this, d)
            if (closestDevice == null) {
                closestDevice = d
                closestDistance = dist
            } else {
                if (dist < closestDistance) {
                    closestDevice = d
                    closestDistance = dist
                }
            }
        }
        return closestDevice!!.address
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun assignToSimulation(dest: Int, hop: Int, pck: NetworkPackage, delay: Long) {
        val entity = simRun.getDeviceByAddress(hop)
        scheduleEvent(entity, pck, delay)
        simRun.logger.onSendNetworkPackage(address, dest, hop, pck.payload, delay)
    }

    override fun equals(other: Any?): Boolean = other is Device && address == other.address
    override fun hashCode(): Int = address

    override fun toString(): String = "Device(addr $address)"

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun registerTimer(durationInNanoSeconds: Long, entity: ITimer): Unit = setTimer(durationInNanoSeconds, entity)

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun resolveHostName(name: String): Int = hostNameLookUpTable[name]!!

    @Suppress("NOTHING_TO_INLINE")
    public inline fun getAllChildApplications(): Set<IApplicationStack_Actuator> = applicationStack.getAllChildApplications()
}
