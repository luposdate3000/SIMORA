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
import simora.simulator_iot.SimulationRun
import kotlin.math.roundToLong

public class LinkManagerMatrix(
    private var config: SimulationRun,
) : ILinkManagerWrite {
    private var size = 0
    private var supportedLinkTypes = mutableListOf<IntArray>()
    internal var matrix = DoubleArray(0)
    internal var matrixNext = IntArray(0)
    private var matrixRate = DoubleArray(0)
    private var canAddDevices = true
    private var linkCounter = 0

    override fun getLinkCount(): Int = linkCounter
    override fun setSupportedLinkTypes(addr: Int, data: IntArray) {
        if (!canAddDevices) {
            TODO()
        }
        if (supportedLinkTypes.size <= addr) {
            supportedLinkTypes.add(IntArray(0))
        }
        supportedLinkTypes[addr] = data
    }
    override fun getSupportedLinkTypes(addr: Int): IntArray = supportedLinkTypes[addr]
    override fun getTransmissionDelay(addrSrc: Int, addrDest: Int, numberOfBytesToSend: Int): Long {
        val kiloBits = numberOfBytesToSend.toDouble() / 125
        val seconds = kiloBits / matrixRate[addrDest * size + addrSrc]
        return (seconds * 1000 * 1000 * 1000).roundToLong()
    }

    override fun hasLink(addrSrc: Int, addrDest: Int): Boolean = matrixNext[addrDest * size + addrSrc] >= 0

    override fun addLink(addrSrc: Int, addrDest: Int, dataRateInKbps: Int,) {
        if (canAddDevices) {
            canAddDevices = false
            size = supportedLinkTypes.size
            matrix = DoubleArray(size * size) { -1.0 }
            matrixNext = IntArray(size * size) { -1 }
            matrixRate = DoubleArray(size * size) { 0.0 }
            for (i in 0 until size) {
                val idx = i * size + i
                matrix[idx] = 0.0
                matrixNext[idx] = i
                matrixRate[idx] = -1.0
            }
        }
        val idx = addrDest * size + addrSrc
        val cost = config.getDistanceInMeters(config.devices[addrSrc], config.devices[addrDest]) + 0.0001
        if (cost < matrix[idx] || matrix[idx] <0.0) {
            linkCounter++
            matrix[idx] = cost
            matrixNext[idx] = addrDest
            matrixRate[idx] = dataRateInKbps.toDouble()
        }
    }
}
