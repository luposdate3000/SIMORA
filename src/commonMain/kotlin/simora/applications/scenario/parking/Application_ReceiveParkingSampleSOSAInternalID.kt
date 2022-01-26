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
import simora.IPayload
import simora.ITimer
import simora.applications.IApplicationStack_Actuator
import simora.applications.IApplicationStack_Middleware

internal class Application_ReceiveParkingSampleSOSAInternalID(private val ownAddress: Int) : IApplicationStack_Actuator {
    val relatedDatabase = ownAddress

// val relatedDatabase=0
    private lateinit var parent: IApplicationStack_Middleware
    val pending = mutableListOf<Package_Application_ParkingSample>()
    val requestedIDs = mutableMapOf<Int, Int>() // packageID -> sensorID
    val cache = mutableMapOf<Int, IntArray>()
    val crashIDs = mutableMapOf<Int, Int>() // error-queryID -> cause-queryID
/*
  cache = sensorID -> [stage, values...]
  stage=0 -> [stage, sendSensorInitID]
  stage=1 -> [stage, requestSensorID]
  stage=2 -> [stage, ?Sensor, ?ParkingSlotLocation]
*/

    private inline fun sendSensorSample(pck: Package_Application_ParkingSample) {
        val c = cache[pck.sensorID]!!
        val Sensor = c[1]
        val ParkingSlotLocation = c[2]
        val query = StringBuilder()
        query.appendLine("PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>")
        query.appendLine("PREFIX parking: <https://github.com/luposdate3000/parking#>")
        query.appendLine("PREFIX sosa: <http://www.w3.org/ns/sosa/>")
        query.appendLine("PREFIX ssn: <http://www.w3.org/ns/ssn/>")
        query.appendLine("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>")
        query.appendLine("")
        query.appendLine("INSERT DATA {")
        query.appendLine("_:Observation a sosa:Observation .")
        query.appendLine("_:Observation sosa:hasFeatureOfInterest parking:AvailableParkingSpaces .")
        query.appendLine("_:Observation sosa:hasSimpleResult \"${pck.isOccupied}\"^^xsd:boolean .")
        query.appendLine("_:Observation sosa:madeBySensor _:luposdate3000id$Sensor .")
        query.appendLine("_:Observation sosa:observedProperty _:luposdate3000id$ParkingSlotLocation .")
        query.appendLine("_:Observation sosa:phenomenonTime \"${pck.sampleTime}\"^^xsd:dateTime .")
        query.appendLine("_:Observation sosa:resultTime \"${pck.sampleTime}\"^^xsd:dateTime .")
        query.appendLine("_:Observation sosa:usedProcedure parking:SensorOnEachSlot .")
        query.appendLine("_:Observation ssn:wasOriginatedBy parking:CarMovement .")
        query.appendLine("_:luposdate3000id$Sensor sosa:madeObservation _:Observation .")
        query.appendLine("}")
        val pckQuery = Package_Query(ownAddress, query.toString().encodeToByteArray())
        // println("send ${pckQuery.queryID} $query")
        parent.send(relatedDatabase, pckQuery)
    }

    private inline fun requestSensorID(sensorID: Int, crossinline action: (Int) -> Unit) {
        val query = StringBuilder()
        query.appendLine("PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>")
        query.appendLine("PREFIX parking: <https://github.com/luposdate3000/parking#>")
        query.appendLine("PREFIX sosa: <http://www.w3.org/ns/sosa/>")
        query.appendLine("PREFIX ssn: <http://www.w3.org/ns/ssn/>")
        query.appendLine("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>")
        query.appendLine("")
        query.appendLine("SELECT ?Sensor ?ParkingSlotLocation WHERE {")
        query.appendLine("?ParkingSlotLocation sosa:isObservedBy ?Sensor .")
        query.appendLine("?Sensor parking:sensorID \"${sensorID}\"^^xsd:integer .")
        query.appendLine("}")
        val pckQuery = Package_Query(ownAddress, query.toString().encodeToByteArray())
        action(pckQuery.queryID)
        parent.registerTimer(
            360000000000L,
            object : ITimer {
                override fun onTimerExpired(clock: Long) {
// println("send ${pckQuery.queryID} $query")
                    parent.send(relatedDatabase, pckQuery)
                }
            }
        )
    }

    private inline fun sendSensorInit(pck: Package_Application_ParkingSample, crossinline action: (Int) -> Unit) {
        val query = StringBuilder()
        query.appendLine("PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>")
        query.appendLine("PREFIX parking: <https://github.com/luposdate3000/parking#>")
        query.appendLine("PREFIX sosa: <http://www.w3.org/ns/sosa/>")
        query.appendLine("PREFIX ssn: <http://www.w3.org/ns/ssn/>")
        query.appendLine("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>")
        query.appendLine("")
        query.appendLine("INSERT DATA {")
        query.appendLine("parking:AvailableParkingSpaces a sosa:FeatureOfInterest .")
        query.appendLine("parking:CarMovement a ssn:Stimulus .")
        query.appendLine("parking:SensorOnEachSlot a sosa:Procedure .")
        query.appendLine("parking:AvailableParkingSpaces ssn:hasProperty _:ParkingSlotLocation .")
        query.appendLine("parking:CarMovement ssn:isProxyFor _:ParkingSlotLocation .")
        query.appendLine("_:ParkingSlotLocation a sosa:ObservableProperty .")
        query.appendLine("_:ParkingSlotLocation parking:area \"${pck.area}\"^^xsd:integer .")
        query.appendLine("_:ParkingSlotLocation parking:spotInArea \"${pck.spotInArea}\"^^xsd:integer .")
        query.appendLine("_:ParkingSlotLocation sosa:isObservedBy _:Sensor .")
        query.appendLine("_:ParkingSlotLocation ssn:isPropertyOf parking:AvailableParkingSpaces .")
        query.appendLine("_:Sensor a sosa:Sensor .")
        query.appendLine("_:Sensor parking:sensorID \"${pck.sensorID}\"^^xsd:integer .")
        query.appendLine("_:Sensor sosa:observes _:ParkingSlotLocation .")
        query.appendLine("_:Sensor ssn:detects parking:CarMovement .")
        query.appendLine("_:Sensor ssn:implements parking:SensorOnEachSlot .")
        query.appendLine("}")
        val pckQuery = Package_Query(ownAddress, query.toString().encodeToByteArray())
        // println("send ${pckQuery.queryID} $query")
        action(pckQuery.queryID)
        parent.send(relatedDatabase, pckQuery)
    }

    override fun receive(pck: IPayload): IPayload? {
        if (pck is Package_Application_ParkingSample) {
            val c = cache[pck.sensorID]
            if (c == null) {
                sendSensorInit(pck) { i ->
                    requestedIDs[i] = pck.sensorID
                    cache[pck.sensorID] = intArrayOf(0, i, 0)
                    pending.add(pck)
                }
            } else if (c[0] == 2) {
                sendSensorSample(pck)
            } else {
                pending.add(pck)
            }
            return null
        } else if (pck is Package_QueryResponse) {
            // println("receive ${pck.queryID} ${pck.result.decodeToString()}")
            if (crashIDs.contains(pck.queryID)) {
                TODO()
            }
            try {
                val sensorID = requestedIDs.remove(pck.queryID)
                if (sensorID != null) {
                    val c = cache[sensorID]!!
                    if (c[0] == 0) {
                        requestSensorID(sensorID) { i ->
                            parent.registerTimer(
                                360000000000L,
                                object : ITimer {
                                    override fun onTimerExpired(clock: Long) {
                                        requestedIDs[i] = sensorID
                                        c[0] = 1
                                        c[1] = i
                                    }
                                }
                            )
                        }
                        return null
                    } else if (c[0] == 1) {
                        var Sensor = 0
                        var ParkingSlotLocation = 0
                        var r = pck.result.decodeToString()
                        var a = r.indexOf("<result>") + "<result>".length
                        var b = r.indexOf("</result>", a)
                        r = r.substring(a, b)
                        var a1 = r.indexOf("<binding") + "<binding".length
                        var b1 = r.indexOf("</binding>", a1)
                        val r1 = r.substring(a1, b1)
                        var a2 = r.indexOf("<binding", b1) + "<binding".length
                        var b2 = r.indexOf("</binding>", a2)
                        val r2 = r.substring(a2, b2)
                        val c1 = r1.indexOf("<bnode>") + "<bnode>".length
                        val d1 = r1.indexOf("</bnode>", c1)
                        val v1 = r1.substring(c1, d1).toInt()
                        val c2 = r2.indexOf("<bnode>") + "<bnode>".length
                        val d2 = r2.indexOf("</bnode>", c2)
                        val v2 = r2.substring(c2, d2).toInt()
                        if (r1.contains("\"Sensor\"")) {
                            Sensor = v1
                            ParkingSlotLocation = v2
                        } else {
                            Sensor = v2
                            ParkingSlotLocation = v1
                        }
                        c[0] = 2
                        c[1] = Sensor
                        c[2] = ParkingSlotLocation
                        val s = pending.size
                        for (i in 0 until s) {
                            val j = s - i - 1 // reverse iteration
                            if (pending[j].sensorID == sensorID) {
                                sendSensorSample(pending[j])
                                pending.removeAt(j)
                            }
                        }
                        return null
                    } else {
                        return pck
                    }
                } else {
                    return pck
                }
            } catch (e: Throwable) {
                val query = "SELECT * WHERE { ?s ?p ?o . }"
                val pckQuery = Package_Query(ownAddress, query.encodeToByteArray())
                // println("send ${pckQuery.queryID} $query")
                parent.send(relatedDatabase, pckQuery)
                crashIDs[pckQuery.queryID] = pck.queryID
                return null
            }
        } else {
            return pck
        }
    }

    override fun setRouter(router: IApplicationStack_Middleware) {
        parent = router
    }

    override fun startUp() {
    }

    override fun shutDown() {
    }
}
