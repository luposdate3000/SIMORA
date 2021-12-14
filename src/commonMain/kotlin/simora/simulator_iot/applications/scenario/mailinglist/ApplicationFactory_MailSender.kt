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

package simora.simulator_iot.applications.scenario.mailinglist

import simora.parser.IJsonParserValue
import simora.parser.JsonParserObject
import simora.simulator_iot.ILogger
import simora.simulator_iot.RandomGenerator
import simora.simulator_iot.applications.IApplicationFeature
import simora.simulator_iot.applications.IApplicationStack_Actuator
import simora.simulator_iot.applications.IApplication_Factory

internal class ApplicationFactory_MailSender : IApplication_Factory {
    override fun registerFeatures(features: MutableList<IApplicationFeature>) {
        features.add(ApplicationFactory_MailSenderFeature())
    }

    override fun create(json: IJsonParserValue, ownAddress: Int, logger: ILogger, outputDirectory: String, random: RandomGenerator, factories: MutableMap<String, IApplication_Factory>): List<IApplicationStack_Actuator> {
        json as JsonParserObject
        var mailReceiverFactory: ApplicationFactory_MailReceiver? = null
        for ((_, f) in factories) {
            if (f is ApplicationFactory_MailReceiver) {
                mailReceiverFactory = f
                break
            }
        }
        if (mailReceiverFactory == null) {
            mailReceiverFactory = ApplicationFactory_MailReceiver()
            factories["simora.simulator_iot.applications.scenario.mailinglist.ApplicationFactory_MailReceiver"] = mailReceiverFactory
        }
        if (json.getOrDefault("enabled", true)) {
            return listOf(
                Application_MailSender(
                    json.getOrDefault("sendStartClockInSec", 0),
                    json.getOrDefault("rateInSec", 0),
                    json.getOrDefault("maxSamples", -1),
                    ownAddress,
                    random,
                    mailReceiverFactory.allReceivers,
                    json.getOrDefault("text_length_fixed", 100),
                    json.getOrDefault("text_length_dynamic", 10),
                    json.getOrDefault("receiverCount", 10),
                    json.getOrDefault("useApplicationSideMulticast", true),
                )
            )
        }
        return listOf()
    }
}
