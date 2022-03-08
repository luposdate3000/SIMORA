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

internal class LoggerStdout : ILogger {
    private lateinit var simRun: SimulationRun
    override fun initialize(simRun: SimulationRun) {
        this.simRun = simRun
    }

    override fun onSendNetworkPackage(src: Int, dest: Int, hop: Int, pck: IPayload, delay: Long) {
        println("${simRun.clock} onSendNetworkPackage $src $dest $hop $delay $pck")
    }

    override fun onReceiveNetworkPackage(address: Int, pck: IPayload) {
        println("${simRun.clock} onReceiveNetworkPackage $address $pck")
    }

    override fun onSendPackage(src: Int, dest: Int, pck: IPayload) {
        println("${simRun.clock} onSendPackage $src $dest $pck")
    }

    override fun onReceivePackage(address: Int, pck: IPayload) {
        println("${simRun.clock} onReceivePackage $address $pck")
    }

    override fun addConnectionTable(src: Int, dest: Int, hop: Int) {}
    override fun onStartUp() {
        println("${simRun.clock} onStartUp")
    }
    override fun onStartUpRouting() {
        println("${simRun.clock} onStartUpRouting")
    }

    override fun onShutDown() {
        println("${simRun.clock} onShutDown")
    }

    override fun onStartSimulation() {
        println("${simRun.clock} onStartSimulation")
    }

    override fun onStopSimulation() {
        println("${simRun.clock} onStopSimulation")
    }

    override fun addDevice(address: Int, x: Double, y: Double) {
        println("addDevice $address $x $y")
    }
    override fun reset(label: String, finish: Boolean) {
        println("${simRun.clock} reset($label, $finish)")
    }
}
