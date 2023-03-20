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

package simora

import simora.shared.inline.File

@OptIn(kotlin.time.ExperimentalTime::class)
internal class LoggerMeasureFlush : LoggerMeasureBase() {
    private fun appendLineToFile(name: String, header: () -> String, line: String) {
        val f = File(simRun.outputDirectory + name)
        val flag = f.exists()
        val stream = f.openOutputStream(flag)
        stream.println(header())
        stream.println(line)
        stream.close()
    }

    override fun reset(label: String, finish: Boolean) {
        dataLabels.add(label)
        if (!finish) {
            onShutDown()
            onStopSimulation()
            data.add(DoubleArray(StatCounter))
            data.last()[StatNumberOfDevices] = data.first()[StatNumberOfDevices]
            packageCounter.add(MutableList(packageByTopic.size) { 0.0 })
            packageSize.add(MutableList(packageByTopic.size) { 0.0 })
            packageSizeAggregated.add(MutableList(packageByTopic.size) { 0.0 })
            packageSizeSelfMessage.add(MutableList(packageByTopic.size) { 0.0 })
            onStartSimulation()
            onStartUpRouting()
            onStartUp()
        }
        if (dataLabels.size > 1 && data.size > 1) {
            val (datas, labels) = getDataAggregated2()
            val headerLine = "phase," + getHeadersAggregated().toList().joinToString(",")
            appendLineToFile("measurement.csv", { headerLine }, labels[0] + "," + datas[0].toList().joinToString(","))

            dataLabels.removeFirst()
            data.removeFirst()
            packageCounter.removeFirst()
            packageSize.removeFirst()
            packageSizeAggregated.removeFirst()
            packageSizeSelfMessage.removeFirst()
        }
    }
}
