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

package simora.applications

import simora.ILogger
import simora.IPayload
import simora.ITimer
import simora.SimulationRun
import simora.models.Device
import simora.models.net.LinkManagerList
import simora.models.net.NetworkPackage

public class ApplicationStack_RPL(
    private val child: IApplicationStack_Actuator,
    private val logger: ILogger,
    private val config: SimulationRun,
) : IApplicationStack_Rooter {
    init {
        child.setRouter(this)
    }

    private lateinit var parent: Device
    private lateinit var routingTable: ApplicationStack_RPL_RoutingTable
    private val notInitializedAddress = -1
    private var isRoot: Boolean = false
    private var rank: Int = INFINITE_RANK
    private var preferredParent: ApplicationStack_RPL_Parent = ApplicationStack_RPL_Parent(notInitializedAddress)
    private var isDelayPackage_ApplicationStack_RPL_DAOTimerRunning = false

    override fun setDevice(device: Device) {
        parent = device
    }

    override fun setRoot() {
        isRoot = true
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendUnRoutedPackage(hop: Int, data: IPayload) {
        val pck = NetworkPackage(parent.address, hop, data)
        val delay = parent.getNetworkDelay(hop, pck)
        parent.assignToSimulation(hop, hop, pck, delay)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun broadcastPackage_ApplicationStack_RPL_DIO() {
        val linkManager = config.linkManager as LinkManagerList
        for (potentialChild in linkManager.getNeighbours(parent.address))
            if (potentialChild != preferredParent.address) {
                sendPackage_ApplicationStack_RPL_DIO(potentialChild)
            }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendPackage_ApplicationStack_RPL_DIO(destinationAddress: Int) {
        val dio = Package_ApplicationStack_RPL_DIO(rank)
        sendUnRoutedPackage(destinationAddress, dio)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun hasDatabase(): Boolean {
        for (f in 0 until config.features.size) {
            if (config.features[f].getName().contains("Database") && config.hasFeature(parent, f)) {
                return true
            }
        }
        return false
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendPackage_ApplicationStack_RPL_DAO(destinationAddress: Int) {
        val destinations = routingTable.getDestinations()
        val nextDatabaseHops = routingTable.getNextFeatureHops(destinations)
        val dao = Package_ApplicationStack_RPL_DAO(true, destinations, hasDatabase(), nextDatabaseHops)
        sendUnRoutedPackage(destinationAddress, dao)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun updateParent(newParent: ApplicationStack_RPL_Parent) {
        if (hasParent()) {
            if (newParent.address == preferredParent.address) {
                return
            }
        }
        if (hasParent()) {
            val dao = Package_ApplicationStack_RPL_DAO(false, IntArray(0), false, IntArray(0))
            sendUnRoutedPackage(preferredParent.address, dao)
        }
        preferredParent = newParent
        routingTable.fallbackHop = preferredParent.address
        sendPackage_ApplicationStack_RPL_DAO(preferredParent.address)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun updateApplicationStack_RPL_RoutingTable(hopAddress: Int, dao: Package_ApplicationStack_RPL_DAO): Boolean {
        return if (dao.isPath) {
            if (dao.hopHasDatabase) {
                routingTable.setDestinationsByDatabaseHop(hopAddress, dao.destinations)
            } else {
                routingTable.setDestinationsByHop(hopAddress, dao.destinations, dao.existingDatabaseHops)
            }
        } else {
            routingTable.removeDestinationsByHop(hopAddress)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun objectiveFunction(pck: NetworkPackage): Int {
        val otherRank = (pck.payload as Package_ApplicationStack_RPL_DIO).rank
        return otherRank + MinHopRankIncrease
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun hasParent(): Boolean =
        preferredParent.address != notInitializedAddress

    override fun startUpRouting() {
        val numberOfDevices = config.getNumberOfDevices()
        routingTable = ApplicationStack_RPL_RoutingTable(parent.address, numberOfDevices, hasDatabase())
        if (isRoot) {
            rank = ROOT_RANK
            broadcastPackage_ApplicationStack_RPL_DIO()
        }
    }
    override fun startUp() {
        child.startUp()
    }

    override fun receive(pck: IPayload): IPayload? {
        pck as NetworkPackage
        val payload = pck.payload
        if (pck.destinationAddress == parent.address) {
            when (payload) {
                is Package_ApplicationStack_RPL_DIO -> {
                    if (objectiveFunction(pck) < rank) {
                        rank = objectiveFunction(pck)
                        updateParent(ApplicationStack_RPL_Parent(pck.sourceAddress))
                        broadcastPackage_ApplicationStack_RPL_DIO()
                    }
                }
                is Package_ApplicationStack_RPL_DAO -> {
                    val hasApplicationStack_RPL_RoutingTableChanged = updateApplicationStack_RPL_RoutingTable(pck.sourceAddress, payload)
                    if (hasParent() && hasApplicationStack_RPL_RoutingTableChanged) {
                        if (!isDelayPackage_ApplicationStack_RPL_DAOTimerRunning) {
                            val daoDelay = DEFAULT_Package_ApplicationStack_RPL_DAO_DELAY * 1000L * 1000L * 1000L
                            parent.setTimer(
                                daoDelay,
                                object : ITimer {
                                    override fun onTimerExpired(clock: Long) {
                                        isDelayPackage_ApplicationStack_RPL_DAOTimerRunning = false
                                        sendPackage_ApplicationStack_RPL_DAO(preferredParent.address)
                                    }
                                }
                            )
                            isDelayPackage_ApplicationStack_RPL_DAOTimerRunning = true
                        }
                    }
                }
                else -> {
                    child.receive(payload)
                }
            }
        } else {
            val hop = getNextHop(pck.destinationAddress)
            val delay = parent.getNetworkDelay(hop, pck)
            parent.assignToSimulation(pck.destinationAddress, hop, pck, delay)
        }
        return null
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun getNextHop(destinationAddress: Int): Int = routingTable.getNextHop(destinationAddress)
    override fun getNextFeatureHops(destinationAddresses: IntArray, flag: Int): IntArray {
        return routingTable.getNextFeatureHops(destinationAddresses)
    }

    internal companion object {

        internal const val DEFAULT_Package_ApplicationStack_RPL_DAO_DELAY: Int = 1

        internal const val MinHopRankIncrease: Int = 1

        internal const val ROOT_RANK: Int = MinHopRankIncrease

        internal const val INFINITE_RANK: Int = Int.MAX_VALUE
    }

    override fun getAllChildApplications(): Set<IApplicationStack_Actuator> {
        val res = mutableSetOf(child)
        if (child is IApplicationStack_Middleware) {
            res.addAll(child.getAllChildApplications())
        }
        return res
    }

    override fun flush() {}
    override fun registerTimer(durationInNanoSeconds: Long, entity: ITimer): Unit = parent.registerTimer(durationInNanoSeconds, entity)
    override fun closestDeviceWithFeature(name: String): Int = parent.closestDeviceWithFeature(name)
    override fun resolveHostName(name: String): Int = parent.resolveHostName(name)
    override fun send(destinationAddress: Int, pck: IPayload) {
        val pck2 = NetworkPackage(parent.address, destinationAddress, pck)
        val hop = getNextHop(destinationAddress)
        val delay = parent.getNetworkDelay(hop, pck2)
        parent.assignToSimulation(destinationAddress, hop, pck2, delay)
    }

    override fun shutDown() {
        for (dest in 0 until config.devices.size) {
            try {
                val hop = getNextHop(dest)
                if (hop != -1) {
                    logger.addConnectionTable(parent.address, dest, hop)
                }
            } catch (e: Throwable) {
            }
        }
        child.shutDown()
    }

    override fun addChildApplication(child: IApplicationStack_Actuator): Unit = (this.child as IApplicationStack_Middleware).addChildApplication(child)
}
