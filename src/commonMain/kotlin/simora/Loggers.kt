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

internal class Loggers internal constructor(internal val loggers: MutableList<ILogger>) : ILogger {
    override fun initialize(simRun: SimulationRun) {
        for (log in loggers) {
            log.initialize(simRun)
        }
    }

    override fun onSendNetworkPackage(src: Int, dest: Int, hop: Int, pck: IPayload, delay: Long) {
        for (logger in loggers) {
            logger.onSendNetworkPackage(src, dest, hop, pck, delay)
        }
    }

    override fun onReceiveNetworkPackage(address: Int, pck: IPayload) {
        for (logger in loggers) {
            logger.onReceiveNetworkPackage(address, pck)
        }
    }

    override fun onSendPackage(src: Int, dest: Int, pck: IPayload) {
        for (logger in loggers) {
            logger.onSendPackage(src, dest, pck)
        }
    }

    override fun onReceivePackage(address: Int, pck: IPayload) {
        for (logger in loggers) {
            logger.onReceivePackage(address, pck)
        }
    }

    override fun addConnectionTable(src: Int, dest: Int, hop: Int) {
        for (logger in loggers) {
            logger.addConnectionTable(src, dest, hop)
        }
    }

    override fun onStartUp() {
        for (logger in loggers) {
            logger.onStartUp()
        }
    }

    override fun onStartUpRouting() {
        for (logger in loggers) {
            logger.onStartUpRouting()
        }
    }

    override fun onShutDown() {
        for (logger in loggers) {
            logger.onShutDown()
        }
    }

    override fun onStartSimulation() {
        for (logger in loggers) {
            logger.onStartSimulation()
        }
    }

    override fun onStopSimulation() {
        for (logger in loggers) {
            logger.onStopSimulation()
        }
    }

    override fun addDevice(address: Int, x: Double, y: Double) {
        for (logger in loggers) {
            logger.addDevice(address, x, y)
        }
    }
    override fun reset(label: String, finish: Boolean) {
        for (logger in loggers) {
            logger.reset(label, finish)
        }
    }
    override fun costumData(data: Any) {
        for (logger in loggers) {
            logger.costumData(data)
        }
    }
}
