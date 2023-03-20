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
package simora.applications.scenario.parking
import simora.ILogger
import simora.IPayload
import simora.applications.IApplicationStack_Actuator
import simora.applications.IApplicationStack_Middleware
import simora.shared.inline.File

public class Application_QuerySender(
    private val logger: ILogger,
    private val queryPck: IPackage_Database,
    private val receiver: Int,
    private val outputdirectory: String,
    private val label: String,
) : IApplicationStack_Actuator {
    public constructor(
        logger: ILogger,
        query: String,
        receiver: Int,
        outputdirectory: String,
        label: String,
    ) : this(logger, Package_Query(receiver, query.encodeToByteArray()), receiver, outputdirectory, label)
    private var first = true
    private lateinit var parent: IApplicationStack_Middleware
    private var awaitingQueries = mutableSetOf<Int>()

    override fun setRouter(router: IApplicationStack_Middleware) {
        parent = router
    }

    override fun startUp() {
    }

    override fun shutDown() {
    }

    override fun receive(pck: IPayload): IPayload? {
        return if (pck is Package_QueryResponse) {
            if (awaitingQueries.contains(pck.queryID)) {
                awaitingQueries.remove(pck.queryID)
                File(outputdirectory + "result_${pck.queryID}").withOutputStream { out ->
                    out.write(pck.result)
                }
                null
            } else {
                pck
            }
        } else {
            pck
        }
    }

    override fun emptyEventQueue(): String? = if (first) {
        first = false
        val p = queryPck
        if (p is Package_Query) {
            logger.costumData(p)
            awaitingQueries.add(p.queryID)
        }
        parent.send(receiver, p)
        parent.flush()
        label
    } else {
        null
    }
}
