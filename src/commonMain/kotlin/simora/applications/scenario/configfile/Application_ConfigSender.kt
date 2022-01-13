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
package simora.applications.scenario.configfile

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import simora.IPayload
import simora.ITimer
import simora.applications.IApplicationStack_Actuator
import simora.applications.IApplicationStack_Middleware
import kotlin.random.Random

internal class Application_ConfigSender(
    private val startClockInSec: Int,
    private val sendRateInSec: Int,
    private val maxNumber: Int,
    private val ownAddress: Int,
    private val random: Random,
    private val allReveivers: List<Int>,
    private val useApplicationSideUnicast: Boolean,
    private val useApplicationSideMulticastStateOfTheArt: Boolean,
    private val useApplicationSideMulticast: Boolean,
    private val useApplicationSideBroadcast: Boolean,
) : IApplicationStack_Actuator, ITimer {
    private lateinit var parent: IApplicationStack_Middleware
    private lateinit var startUpTimeStamp: Instant
    private var sendingVarianceInSec = 10
    private var eventCounter = 0
    private val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')

    override fun setRouter(router: IApplicationStack_Middleware) {
        parent = router
    }

    override fun startUp() {
        startUpTimeStamp = Clock.System.now()
        parent.registerTimer(startClockInSec.toLong() * 1000000000L + random.nextLong(0L, sendingVarianceInSec.toLong() * 1000000000L), this)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun getRandomString(length: Int): String = Array(length) { allowedChars.random(random) }.joinToString("")

    override fun shutDown() {}
    override fun receive(pck: IPayload): IPayload = pck
    override fun onTimerExpired(clock: Long) {
        try {
            if (eventCounter < maxNumber || maxNumber == -1) {
                eventCounter++
// 1. constants
                val sizeGlobal = 128
                val sizeGroup = 64
                val sizeDevice = 32
                val targetGroups = 4
                val targetDevicesPerGroup = 8

// 2. generate list of receiver devices
                val targetsTmp = allReveivers.toSet().toIntArray()
                targetsTmp.shuffle(random)
                val targets = targetsTmp.copyOfRange(0, targetGroups * targetDevicesPerGroup)

// 3. generate data
                val data = Package_Application_ConfigMulticast(
                    getRandomString(sizeGlobal),
                    List(targetGroups) { groupID ->
                        getRandomString(sizeGroup) to List(targetDevicesPerGroup) { deviceID ->
                            targets[groupID * targetDevicesPerGroup + deviceID] to getRandomString(sizeDevice)
                        }.toMap()
                    }
                )
// 4. send it
                if (useApplicationSideUnicast) {
                    for (g in data.groups) {
                        for ((k, v) in g.second) {
                            parent.send(k, Package_Application_ConfigUnicast(data.text_global + g.first + v))
                        }
                    }
                } else if (useApplicationSideMulticastStateOfTheArt) {
                    parent.send(ownAddress, Package_Application_ConfigBroadcast(targets, data.text_global))
                    for (g in data.groups) {
                        parent.send(ownAddress, Package_Application_ConfigBroadcast(g.second.keys.toIntArray(), g.first))
                        for ((k, v) in g.second) {
                            parent.send(k, Package_Application_ConfigUnicast(v))
                        }
                    }
                } else if (useApplicationSideMulticast) {
                    parent.send(ownAddress, data)
                } else if (useApplicationSideBroadcast) {
                    parent.send(
                        ownAddress,
                        Package_Application_ConfigBroadcast(
                            targets,
                            data.text_global + data.groups.joinToString("") {
                                it.first + it.second.values.joinToString(
                                    ""
                                )
                            }
                        )
                    )
                } else {
                    TODO()
                }
                parent.flush()
                parent.registerTimer(sendRateInSec.toLong() * 1000000000L + random.nextLong(0L, sendingVarianceInSec.toLong() * 1000000000L), this)
            }
        } catch (e: Throwable) {
        }
    }
}
