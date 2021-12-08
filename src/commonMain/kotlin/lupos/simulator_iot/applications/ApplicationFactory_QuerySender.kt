/*
 * This file is part of the Luposdate3000 distribution (https://github.com/simoradate3000/simoradate3000).
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

package simora.simulator_iot.applications

import simora.parser.IJsonParserValue
import simora.parser.JsonParserArray
import simora.parser.JsonParserObject
import simora.simulator_iot.ILogger
import simora.simulator_iot.RandomGenerator

public class ApplicationFactory_QuerySender : IApplication_Factory {
    override fun registerFeatures(features: MutableList<IApplicationFeature>) {}
    override fun create(json: IJsonParserValue, ownAddress: Int, logger: ILogger, outputDirectory: String, random: RandomGenerator): List<IApplicationStack_Actuator> {
        json as JsonParserArray
        val res = mutableListOf<IApplicationStack_Actuator>()
        for (it in json) {
            it as JsonParserObject
            if (it.getOrDefault("enabled", true)) {
                res.add(
                    Application_QuerySender(
                        it.getOrDefault("sendStartClockInSec", 0),
                        it.getOrDefault("sendRateInSec", 1),
                        it.getOrDefault("maxNumberOfQueries", 1),
                        it.getOrDefault("query", ""),
                        ownAddress,
                        outputDirectory + "/",
                    )
                )
            }
        }
        return res
    }
}
