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

import kotlin.math.roundToLong

public class LinkManagerList : ILinkManagerWrite {
    private var supportedLinkTypes = mutableListOf<IntArray>()
    private var link_Addresses = mutableListOf<MutableList<Int>>()
    private var link_dataRateInKbps = mutableListOf<MutableList<Double>>()
    override fun getLinkCount(): Int = link_Addresses.sumOf { it.size }
    override fun setSupportedLinkTypes(addr: Int, data: IntArray) {
        if (supportedLinkTypes.size <= addr) {
            supportedLinkTypes.add(IntArray(0))
            link_Addresses.add(mutableListOf())
            link_dataRateInKbps.add(mutableListOf())
        }
        supportedLinkTypes[addr] = data
    }
    override fun getSupportedLinkTypes(addr: Int): IntArray = supportedLinkTypes[addr]
    override fun getTransmissionDelay(addrSrc: Int, addrDest: Int, numberOfBytesToSend: Int): Long {
        val idx = link_Addresses[addrSrc].indexOf(addrDest)
        if (idx <0) {
            println("getTransmissionDelay .. $addrDest")
        }
        val kiloBits = numberOfBytesToSend.toDouble() / 125
        val seconds = kiloBits / link_dataRateInKbps[addrSrc][idx]
        return (seconds * 1000 * 1000 * 1000).roundToLong()
    }

    internal fun getNeighbours(addrSrc: Int): List<Int> = link_Addresses[addrSrc]

    override fun addLink(addrSrc: Int, addrDest: Int, dataRateInKbps: Int,) {
        val idx = link_Addresses[addrSrc].indexOf(addrDest)
        if (idx <0) {
            link_Addresses[addrSrc].add(addrDest)
            link_dataRateInKbps[addrSrc].add(dataRateInKbps.toDouble())
        } else {
            link_dataRateInKbps[addrSrc][idx] = dataRateInKbps.toDouble()
        }
        val idx2 = link_Addresses[addrDest].indexOf(addrSrc)
        if (idx2 <0) {
            link_Addresses[addrDest].add(addrSrc)
            link_dataRateInKbps[addrDest].add(dataRateInKbps.toDouble())
        } else {
            link_dataRateInKbps[addrDest][idx2] = dataRateInKbps.toDouble()
        }
    }
}
