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

package simora.simulator_iot.models.net

import simora.simulator_iot.models.Device
import kotlin.math.roundToLong

public class LinkManager(
    internal val supportedLinkTypes: IntArray
) {

    private var link_Addresses = mutableListOf<Int>()
    private var link_dataRateInKbps = mutableListOf<Int>()

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun getTransmissionDelay(destinationAddress: Int, numberOfBytesToSend: Int): Long {
        val idx = link_Addresses.indexOf(destinationAddress)
        if (idx <0) {
            println("getTransmissionDelay .. $destinationAddress")
        }
        val kiloBits = numberOfBytesToSend.toDouble() / 125
        val seconds = kiloBits / link_dataRateInKbps[idx].toDouble()
        return (seconds * 1000 * 1000 * 1000).roundToLong()
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun hasLink(otherDevice: Device): Boolean = link_Addresses.indexOf(otherDevice.address) >= 0

    public fun getNeighbours(): List<Int> = link_Addresses

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun addLink(
        addr: Int,
        dataRateInKbps: Int,
    ) {
        val idx = link_Addresses.indexOf(addr)
        if (idx <0) {
            link_Addresses.add(addr)
            link_dataRateInKbps.add(dataRateInKbps)
        } else {
            link_dataRateInKbps[idx] = dataRateInKbps
        }
    }
}
