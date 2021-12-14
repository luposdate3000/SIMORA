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

import simora.simulator_iot.config.LinkType
import simora.simulator_iot.models.Device

internal class DeviceLinker {
    private var sortedLinkTypes: Array<LinkType> = emptyArray()

    private fun getLinkByName(name: String): LinkType = sortedLinkTypes.first { it.name == name }
    internal fun getSortedLinkTypeIndices(linkTypeNames: List<String>): IntArray = linkTypeNames.map { getLinkByName(it) }.map { sortedLinkTypes.indexOf(it) }.sorted().toIntArray()

    internal fun setLinkTypes(types: Array<LinkType>) {
        sortedLinkTypes = types.sortedByDescending { it.dataRateInKbps }.toTypedArray()
    }

    internal fun createAvailableLinks(devices: MutableList<Device>) {
        for (one in devices) {
            for (two in devices) {
                if (!one.isStarNetworkChild && !two.isStarNetworkChild) {
                    linkIfPossible(one, two)
                }
            }
        }
    }

    private fun linkIfPossible(one: Device, two: Device) {
        if (one != two && !one.linkManager.hasLink(two)) {
            val distance = getDistanceInMeters(one, two)
            val oneIndices = one.linkManager.supportedLinkTypes
            val twoIndices = two.linkManager.supportedLinkTypes
            loop@ for (i in oneIndices) {
                for (i2 in twoIndices) {
                    if (i == i2) {
                        if (distance <= sortedLinkTypes[i].rangeInMeters) {
                            one.linkManager.addLink(two.address, sortedLinkTypes[i].dataRateInKbps)
                            two.linkManager.addLink(one.address, sortedLinkTypes[i].dataRateInKbps)
                            return
                        }
                    }
                }
            }
        }
    }

    private fun getDistanceInMeters(one: Device, two: Device): Double = one.location.getDistanceInMeters(two.location)

    internal fun link(one: Device, two: Device, dataRate: Int) {
        val distance = getDistanceInMeters(one, two)
        one.linkManager.addLink(two.address, dataRate)
        two.linkManager.addLink(one.address, dataRate)
    }
}
