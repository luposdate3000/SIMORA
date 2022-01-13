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

package simora.models.net
import simora.SimulationRun
import kotlin.math.roundToLong

public class LinkManagerMatrix(
    private var config: SimulationRun,
) : ILinkManagerWrite {
    private val largeDouble = 999999999999.0
    private var size = 0
    private var supportedLinkTypes = mutableListOf<IntArray>()
    private var matrix = DoubleArray(0)
    private var matrixNext = IntArray(0)
    private var matrixRate = DoubleArray(0)
    private var noLinksAddedRightNow = true
    private var linkCounter = 0
    internal fun getMatrix(): DoubleArray {
        prepareMatrix()
        return matrix
    }
    internal fun getMatrixNext(): IntArray {
        prepareMatrix()
        return matrixNext
    }
    override fun getNeighbours(addrSrc: Int): List<Int> {
        val res = mutableListOf<Int>()
        for (addrDest in 0 until size) {
            if (addrDest != addrSrc) {
                val idx = addrDest * size + addrSrc
                if (matrix[idx] <largeDouble) {
                    res.add(addrDest)
                }
            }
        }
        return res
    }
    override fun getLinkCount(): Int = linkCounter
    override fun setSupportedLinkTypes(addr: Int, data: IntArray) {
        if (supportedLinkTypes.size <= addr) {
            supportedLinkTypes.add(IntArray(0))
        }
        supportedLinkTypes[addr] = data
        if (!noLinksAddedRightNow) {
            val oldSize = size
            val oldMatrix = matrix
            val oldMatrixNext = matrixNext
            val oldMatrixRate = matrixRate
            size = supportedLinkTypes.size
            matrix = DoubleArray(size * size) { 999999999999.0 }
            matrixNext = IntArray(size * size) { -1 }
            matrixRate = DoubleArray(size * size) { 0.0 }
            for (i in 0 until size) {
                val idx = i * size + i
                matrix[idx] = 0.0
                matrixNext[idx] = i
                matrixRate[idx] = -1.0
            }
            for (x in 0 until oldSize) {
                for (y in 0 until oldSize) {
                    val idxOld = y * oldSize + x
                    val idxNew = y * size + x
                    matrix[idxNew] = oldMatrix[idxOld]
                    matrixNext[idxNew] = oldMatrixNext[idxOld]
                    matrixRate[idxNew] = oldMatrixRate[idxOld]
                }
            }
        }
    }
    override fun getSupportedLinkTypes(addr: Int): IntArray = supportedLinkTypes[addr]
    override fun getTransmissionDelay(addrSrc: Int, addrDest: Int, numberOfBytesToSend: Int): Long {
        val kiloBits = numberOfBytesToSend.toDouble() / 125
        val seconds = kiloBits / matrixRate[addrDest * size + addrSrc]
        return (seconds * 1000 * 1000 * 1000).roundToLong()
    }
    private fun prepareMatrix() {
        if (noLinksAddedRightNow) {
            noLinksAddedRightNow = false
            size = supportedLinkTypes.size
            matrix = DoubleArray(size * size) { largeDouble }
            matrixNext = IntArray(size * size) { -1 }
            matrixRate = DoubleArray(size * size) { 0.0 }
            for (i in 0 until size) {
                val idx = i * size + i
                matrix[idx] = 0.0
                matrixNext[idx] = i
                matrixRate[idx] = -1.0
            }
        }
    }
    override fun addLink(addrSrc: Int, addrDest: Int, dataRateInKbps: Int) {
        prepareMatrix()
        val cost = config.getDistanceInMeters(config.devices[addrSrc], config.devices[addrDest]) + 1.0
        val idx = addrDest * size + addrSrc
        if (dataRateInKbps> matrixRate[idx]) {
            if (cost < matrix[idx]) {
                linkCounter++
                matrix[idx] = cost
                matrixNext[idx] = addrDest
                matrixRate[idx] = dataRateInKbps.toDouble()
            }
        }
        val idx2 = addrSrc * size + addrDest // inverse link
        if (dataRateInKbps> matrixRate[idx2]) {
            if (cost < matrix[idx2]) {
                linkCounter++
                matrix[idx2] = cost
                matrixNext[idx2] = addrSrc
                matrixRate[idx2] = dataRateInKbps.toDouble()
            }
        }
    }
}
