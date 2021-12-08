/*
 * This file is part of the Luposdate3000 distribution (https://github.com/luposdate3000/luposdate3000).
 * Copyright (c) 2020-2021, Institute of Information Systems (Benjamin Warnke and contributors of LUPOSDATE3000), University of Luebeck
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package simora.simulator_iot

internal actual object ReflectionHelper {
    internal actual fun create(name: String): Any {
        return when (name) {
            "simora.simulator_iot.LoggerMeasure" -> simora.simulator_iot.LoggerMeasure()
            "simora.simulator_iot.LoggerStdout" -> simora.simulator_iot.LoggerStdout()
"simora.simulator_iot.applications.scenario.parking.ApplicationFactory_ParkingSensor"->simora.simulator_iot.applications.scenario.parking.ApplicationFactory_ParkingSensor()
"simora.simulator_iot.applications.scenario.parking.ApplicationFactory_QuerySender"->simora.simulator_iot.applications.scenario.parking.ApplicationFactory_QuerySender()
"simora.simulator_iot.applications.scenario.parking.ApplicationFactory_ReceiveParkingSample"->simora.simulator_iot.applications.scenario.parking.ApplicationFactory_ReceiveParkingSample()
"simora.simulator_iot.applications.scenario.parking.ApplicationFactory_ReceiveParkingSampleSOSA"->simora.simulator_iot.applications.scenario.parking.ApplicationFactory_ReceiveParkingSampleSOSA()
"simora.simulator_iot.applications.scenario.mailinglist.ApplicationFactory_MailDistributor"->simora.simulator_iot.applications.scenario.mailinglist.ApplicationFactory_MailDistributor()
"simora.simulator_iot.applications.scenario.mailinglist.ApplicationFactory_MailReceiver"->simora.simulator_iot.applications.scenario.mailinglist.ApplicationFactory_MailReceiver()
"simora.simulator_iot.applications.scenario.mailinglist.ApplicationFactory_MailSender"->simora.simulator_iot.applications.scenario.mailinglist.ApplicationFactory_MailSender()
            else -> TODO("ReflectionHelper.create(\"$name\")")
        }
    }
}
