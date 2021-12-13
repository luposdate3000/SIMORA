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
package simora.simulator_iot.applications.scenario.parking

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import simora.simulator_core.ITimer
import simora.simulator_iot.IPayload
import simora.simulator_iot.RandomGenerator
import simora.simulator_iot.applications.IApplicationStack_Actuator
import simora.simulator_iot.applications.IApplicationStack_Middleware

internal class Application_ParkingSensor(
    private val startClockInSec: Int,
    private val sendRateInSec: Int,
    private val maxNumber: Int,
    private val ownAddress: Int,
    private val random: RandomGenerator,
    private val area: Int,
    private val spotInArea: Int,
) : IApplicationStack_Actuator, ITimer {
    private lateinit var parent: IApplicationStack_Middleware
    private lateinit var startUpTimeStamp: Instant
    private var sendingVarianceInSec = 10
    private var receiver: Int = -1
    private var eventCounter = 0
    override fun setRouter(router: IApplicationStack_Middleware) {
        parent = router
    }

    override fun startUp() {
        startUpTimeStamp = Clock.System.now()
        parent.registerTimer(startClockInSec.toLong() * 1000000000L + random.getLong(0L, sendingVarianceInSec.toLong() * 1000000000L), this)
        receiver = parent.closestDeviceWithFeature("Database")
    }

    override fun shutDown() {}
    override fun receive(pck: IPayload): IPayload = pck
    override fun onTimerExpired(clock: Long) {
        if (eventCounter < maxNumber || maxNumber == -1) {
            val sampleTimeObj = startUpTimeStamp.plus(clock, DateTimeUnit.NANOSECOND, TimeZone.UTC).toLocalDateTime(TimeZone.currentSystemDefault())
            val sampleTime = "${sampleTimeObj.year}-${sampleTimeObj.monthNumber.toString().padStart(2, '0')}-${sampleTimeObj.dayOfMonth.toString().padStart(2, '0')}T${sampleTimeObj.hour.toString().padStart(2, '0')}:${sampleTimeObj.minute.toString().padStart(2, '0')}:${sampleTimeObj.second.toString().padStart(2, '0')}.${(sampleTimeObj.nanosecond / 1000000).toString().padStart(3, '0')}"
            eventCounter++
            parent.send(
                receiver,
                Package_Application_ParkingSample(
                    sensorID = ownAddress,
                    sampleTime = sampleTime,
                    isOccupied = random.getBoolean(0.5f),
                    area = area,
                    spotInArea = spotInArea,
                )
            )
            parent.flush()
            parent.registerTimer(sendRateInSec.toLong() * 1000000000L + random.getLong(0L, sendingVarianceInSec.toLong() * 1000000000L), this)
        }
    }
}
