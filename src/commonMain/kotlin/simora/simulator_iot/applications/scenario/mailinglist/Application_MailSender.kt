/*
 * This file is part of the Luposdate3000 distribution (https://github.com/luposdate3000/luposdate3000).
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
package simora.simulator_iot.applications.scenario.mailinglist

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.plus
import simora.simulator_core.ITimer
import simora.simulator_iot.IPayload
import simora.simulator_iot.RandomGenerator
import simora.simulator_iot.applications.IApplicationStack_Actuator
import simora.simulator_iot.applications.IApplicationStack_Middleware

public class Application_MailSender(
    internal val startClockInSec: Int,
    internal val sendRateInSec: Int,
    internal val maxNumber: Int,
    internal val ownAddress: Int,
    internal val random: RandomGenerator,
    internal val allReveivers: List<Int>,
internal val text_length_fixed:Int,
internal val text_length_dynamic:Int,
) : IApplicationStack_Actuator, ITimer {
    private lateinit var parent: IApplicationStack_Middleware
    private lateinit var startUpTimeStamp: Instant
    private var sendingVarianceInSec = 10
    private var eventCounter = 0
    override fun setRouter(router: IApplicationStack_Middleware) {
        parent = router
    }

    override fun startUp() {
        startUpTimeStamp = Clock.System.now()
        parent.registerTimer(startClockInSec.toLong() * 1000000000L + random.getLong(0L, sendingVarianceInSec.toLong() * 1000000000L), this)
    }
private inline fun getRandomString(length: Int) : String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}
    override fun shutDown() {}
    override fun receive(pck: IPayload): IPayload? = pck
    override fun onTimerExpired(clock: Long) {
        if (eventCounter < maxNumber || maxNumber == -1) {
            eventCounter++
            parent.send(
                ownAddress,
                Package_Application_MailGroup(
                    "§${getRandomString(text_length_fixed)}",
                    allReveivers.map { it to "${getRandomString(text_length_dynamic)}" }.toMap()
                )
            )
            parent.flush()
            parent.registerTimer(sendRateInSec.toLong() * 1000000000L + random.getLong(0L, sendingVarianceInSec.toLong() * 1000000000L), this)
        }
    }
}
