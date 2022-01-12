/*
 * This file is part of the Luposdate3000 distribution (https://github.com/luposdate3000/SIMORA).
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
package simora

internal actual object ReflectionHelper {
    @Suppress("NOTHING_TO_INLINE")
    actual inline fun create(name: String): Any {
return when (name) {
            "simora.LoggerMeasure" -> LoggerMeasure()
            "simora.LoggerStdout" -> LoggerStdout()
            "simora.applications.scenario.parking.ApplicationFactory_ParkingSensor" -> simora.applications.scenario.parking.ApplicationFactory_ParkingSensor()
            "simora.applications.scenario.parking.ApplicationFactory_QuerySender" -> simora.applications.scenario.parking.ApplicationFactory_QuerySender()
            "simora.applications.scenario.parking.ApplicationFactory_ReceiveParkingSample" -> simora.applications.scenario.parking.ApplicationFactory_ReceiveParkingSample()
            "simora.applications.scenario.parking.ApplicationFactory_ReceiveParkingSampleSOSA" -> simora.applications.scenario.parking.ApplicationFactory_ReceiveParkingSampleSOSA()
            "simora.applications.scenario.mailinglist.ApplicationFactory_MailDistributor" -> simora.applications.scenario.mailinglist.ApplicationFactory_MailDistributor()
            "simora.applications.scenario.mailinglist.ApplicationFactory_MailReceiver" -> simora.applications.scenario.mailinglist.ApplicationFactory_MailReceiver()
            "simora.applications.scenario.mailinglist.ApplicationFactory_MailSender" -> simora.applications.scenario.mailinglist.ApplicationFactory_MailSender()
            "simora.applications.scenario.mailinglist.ApplicationFactory_MailSenderIdentical" -> simora.applications.scenario.mailinglist.ApplicationFactory_MailSenderIdentical()
"simora.applications.scenario.configfile.ApplicationFactory_ConfigReceiver"->simora.applications.scenario.configfile.ApplicationFactory_ConfigReceiver()
"simora.applications.scenario.configfile.ApplicationFactory_ConfigSender"->simora.applications.scenario.configfile.ApplicationFactory_ConfigSender()
"simora.applications.scenario.configfile.ApplicationFactory_ConfigDistributor"->simora.applications.scenario.configfile.ApplicationFactory_ConfigDistributor()
            else -> TODO("ReflectionHelper.create(\"$name\")")
        }
//        return eval("new $name()") as Any
    }
}
