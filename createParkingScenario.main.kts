#!/usr/bin/env kotlin
import kotlin.math.sqrt

enum class EQueryDistribution { ROUTING, CENTRALIZED }
enum class ERouting { ASP, RPL, RPL_FAST }
enum class EProgramDistribution { DISTRIBUTED_WITH_QUERY_HOPS, CENTRAL, DISTRIBUTED }
enum class EMulticast { DISABLED, SIMPLE }
enum class EDataDistribution { SIMPLE, ID_1, ID_2, ID_O, ID_S, ID_TWICE, KEY }
enum class ETopology { UNIFORM, RING, RANDOM, FULL }

var topology = ETopology.UNIFORM
var programDistribution = EProgramDistribution.DISTRIBUTED_WITH_QUERY_HOPS
var dataDistribution = EDataDistribution.SIMPLE
var queryDistribution = EQueryDistribution.ROUTING
var multicast = EMulticast.DISABLED
var routing = ERouting.ASP
var size = 128
var commRange = 150
var avgConnections = 10.0
var sensorsPerDatabase = 10

if (args.size == 0) {
    println("usage ./createParkingScenario.main.kts [options]")
    println("--topology=${ETopology.values().toSet()}")
    println("--programDistribution=${EProgramDistribution.values().toSet()}")
    println("--dataDistribution=${EDataDistribution.values().toSet()}")
    println("--queryDistribution=${EQueryDistribution.values().toSet()}")
    println("--multicast=${EMulticast.values().toSet()}")
    println("--routing=${ERouting.values().toSet()}")
    println("--size=<NUMBER>")
} else {
    for (arg in args) {
        println(arg)
        val a = arg.split("=")
        val k = a[0]
        val v = a[1]
        val unused = when (k) {
            "--topology" -> topology = ETopology.valueOf(v)
            "--programDistribution" -> programDistribution = EProgramDistribution.valueOf(v)
            "--dataDistribution" -> dataDistribution = EDataDistribution.valueOf(v)
            "--queryDistribution" -> queryDistribution = EQueryDistribution.valueOf(v)
            "--multicast" -> multicast = EMulticast.valueOf(v)
            "--routing" -> routing = ERouting.valueOf(v)
            "--size" -> size = v.toInt()
            else -> TODO("unknown argument $k = $v")
        }
    }
    val count = size * 3.0 // for union and random
    val radius = sqrt(count * commRange * commRange / avgConnections)
    val res = StringBuilder()
    res.appendLine("{")
    res.appendLine("    \"applications\": {")
    res.appendLine("        \"simora.applications.scenario.parking.ApplicationFactory_ReceiveParkingSampleSOSAInternalID\": {},")
    res.appendLine("        \"lupos.simulator_db.luposdate3000.ApplicationFactory_Luposdate3000\": {")
    when (queryDistribution) {
        EQueryDistribution.ROUTING -> {
            res.appendLine("            \"queryDistributionMode\": \"Routing\",")
        }
        EQueryDistribution.CENTRALIZED -> {
            res.appendLine("            \"queryDistributionMode\": \"Centralized\",")
        }
    }
    res.appendLine("            \"REPLACE_STORE_WITH_VALUES\": false,")
    res.appendLine("            \"SharedMemoryDictionaryCheat\": false,")
    res.appendLine("            \"databaseQuery\": true,")
    res.appendLine("            \"databaseStore\": true,")
    res.appendLine("            \"enabled\": false,")
    res.appendLine("            \"mergeLocalOperatorgraphs\": true,")
    res.appendLine("            \"useDictionaryInlineEncoding\": true,")
    val unused1 = when (dataDistribution) {
        EDataDistribution.SIMPLE -> {
            res.appendLine("            \"predefinedPartitionScheme\": \"Simple\",")
        }
        EDataDistribution.KEY -> {
            res.appendLine("            \"predefinedPartitionScheme\": \"PartitionByKeyAllCollations\",")
        }
        EDataDistribution.ID_1 -> {
            res.appendLine("            \"predefinedPartitionScheme\": \"PartitionByID_1_AllCollations\",")
        }
        EDataDistribution.ID_2 -> {
            res.appendLine("            \"predefinedPartitionScheme\": \"PartitionByID_2_AllCollations\",")
        }
        EDataDistribution.ID_O -> {
            res.appendLine("            \"predefinedPartitionScheme\": \"PartitionByID_O_AllCollations\",")
        }
        EDataDistribution.ID_S -> {
            res.appendLine("            \"predefinedPartitionScheme\": \"PartitionByID_S_AllCollations\",")
        }
        EDataDistribution.ID_TWICE -> {
            res.appendLine("            \"predefinedPartitionScheme\": \"PartitionByIDTwiceAllCollations\",")
        }
    }
    res.appendLine("        },")
    res.appendLine("    },")
    res.appendLine("    \"deterministic\": true,")
    res.appendLine("    \"deviceType\": {")
    res.appendLine("        \"Central Tower\": {")
    res.appendLine("            \"applications\": {")
    res.appendLine("                \"lupos.simulator_db.luposdate3000.ApplicationFactory_OntologySender\": {")
    res.appendLine("                    \"enabled\": true,")
    res.appendLine("                    \"fileName\": \"src/luposdate3000_simulator_db/src/jvmMain/resources/ontologySOSA.n3\",")
    res.appendLine("                    \"sendStartClockInSec\": 10,")
    res.appendLine("                },")
    res.appendLine("                \"lupos.simulator_db.luposdate3000.ApplicationFactory_Luposdate3000\": {")
    res.appendLine("                    \"databaseQuery\": true,")
    res.appendLine("                    \"databaseStore\": true,")
    res.appendLine("                    \"enabled\": true,")
    res.appendLine("                },")
    res.appendLine("                \"simora.applications.scenario.parking.ApplicationFactory_QuerySender\": [")
    res.appendLine("                    {")
    res.appendLine("                        \"label\": \"S1\",")
    res.appendLine("                        \"query\": \"SELECT ?s ?p ?o WHERE { ?s ?p ?o. }\",")
    res.appendLine("                    },")
    res.appendLine("                    {")
    res.appendLine("                        \"label\": \"S2\",")
    res.appendLine("                        \"query\": \"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\\nPREFIX parking: <https://github.com/luposdate3000/parking#>\\nPREFIX sosa: <http://www.w3.org/ns/sosa/>\\nPREFIX ssn: <http://www.w3.org/ns/ssn/>\\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\\nSELECT DISTINCT ?area WHERE {\\n ?ParkingSlotLocation a sosa:ObservableProperty .\\n ?ParkingSlotLocation parking:area ?area .\\n}\",")
    res.appendLine("                    },")
    res.appendLine("                    {")
    res.appendLine("                        \"label\": \"S3\",")
    res.appendLine("                        \"query\": \"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\\nPREFIX parking: <https://github.com/luposdate3000/parking#>\\nPREFIX sosa: <http://www.w3.org/ns/sosa/>\\nPREFIX ssn: <http://www.w3.org/ns/ssn/>\\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\\nSELECT (COUNT(DISTINCT ?spotInArea) as ?count) WHERE {\\n ?ParkingSlotLocation a sosa:ObservableProperty .\\n ?ParkingSlotLocation parking:area 9 .\\n ?ParkingSlotLocation parking:spotInArea ?spotInArea .\\n}\",")
    res.appendLine("                    },")
    res.appendLine("                    {")
    res.appendLine("                        \"label\": \"S4\",")
    res.appendLine("                        \"query\": \"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\\nPREFIX parking: <https://github.com/luposdate3000/parking#>\\nPREFIX sosa: <http://www.w3.org/ns/sosa/>\\nPREFIX ssn: <http://www.w3.org/ns/ssn/>\\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\\nSELECT (COUNT(?Observation) as ?count) WHERE {\\n ?ParkingSlotLocation a sosa:ObservableProperty .\\n ?ParkingSlotLocation parking:area 6 .\\n ?ParkingSlotLocation parking:spotInArea 1 .\\n ?Observation a sosa:Observation .\\n ?Observation sosa:observedProperty ?ParkingSlotLocation .\\n}\",")
    res.appendLine("                    },")
    res.appendLine("                    {")
    res.appendLine("                        \"label\": \"S5\",")
    res.appendLine("                        \"query\": \"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\\nPREFIX parking: <https://github.com/luposdate3000/parking#>\\nPREFIX sosa: <http://www.w3.org/ns/sosa/>\\nPREFIX ssn: <http://www.w3.org/ns/ssn/>\\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\\nSELECT (MAX(?resultTime) AS ?latestDate) WHERE {\\n ?ParkingSlotLocation a sosa:ObservableProperty .\\n ?ParkingSlotLocation parking:area 7 .\\n ?ParkingSlotLocation parking:spotInArea 1 .\\n ?Observation a sosa:Observation .\\n ?Observation sosa:observedProperty ?ParkingSlotLocation .\\n ?Observation sosa:resultTime ?resultTime .\\n}\",")
    res.appendLine("                    },")
    res.appendLine("                    {")
    res.appendLine("                        \"label\": \"S6\",")
    res.appendLine("                        \"query\": \"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\\nPREFIX parking: <https://github.com/luposdate3000/parking#>\\nPREFIX sosa: <http://www.w3.org/ns/sosa/>\\nPREFIX ssn: <http://www.w3.org/ns/ssn/>\\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\\nSELECT ?spotInArea ?isOccupied ?lastObservedAt WHERE {\\n ?ParkingSlotLocation parking:spotInArea ?spotInArea .\\n ?Observation sosa:observedProperty ?ParkingSlotLocation .\\n ?Observation sosa:resultTime ?lastObservedAt .\\n ?Observation sosa:hasSimpleResult ?isOccupied .\\n {\\n  SELECT(MAX(?resultTime) AS ?lastObservedAt) ?ParkingSlotLocation WHERE {\\n    ?ParkingSlotLocation a sosa:ObservableProperty .\\n    ?ParkingSlotLocation parking:area 9 .\\n    ?Observation a sosa:Observation .\\n    ?Observation sosa:observedProperty ?ParkingSlotLocation .\\n    ?Observation sosa:resultTime ?resultTime .\\n  }\\n  GROUP BY ?ParkingSlotLocation\\n }\\n}\",")
    res.appendLine("                    },")
    res.appendLine("                    {")
    res.appendLine("                        \"label\": \"S7\",")
    res.appendLine("                        \"query\": \"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\\nPREFIX parking: <https://github.com/luposdate3000/parking#>\\nPREFIX sosa: <http://www.w3.org/ns/sosa/>\\nPREFIX ssn: <http://www.w3.org/ns/ssn/>\\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\\nSELECT ?area ?spotInArea ?isOccupied ?lastObservedAt WHERE {\\n ?ParkingSlotLocation parking:area ?area .\\n ?ParkingSlotLocation parking:spotInArea ?spotInArea .\\n ?Observation sosa:observedProperty ?ParkingSlotLocation .\\n ?Observation sosa:resultTime ?lastObservedAt .\\n ?Observation sosa:hasSimpleResult ?isOccupied .\\n { \\n  SELECT(MAX(?resultTime) AS ?lastObservedAt) ?ParkingSlotLocation WHERE { \\n    ?ParkingSlotLocation a sosa:ObservableProperty .\\n    ?ParkingSlotLocation parking:area ?area .\\n    ?Observation a sosa:Observation .\\n    ?Observation sosa:observedProperty ?ParkingSlotLocation .\\n    ?Observation sosa:resultTime ?resultTime .\\n    FILTER (?area IN (9, 8, 2))\\n  }\\n  GROUP BY ?ParkingSlotLocation\\n }\\n}\",")
    res.appendLine("                    },")
    res.appendLine("                    {")
    res.appendLine("                        \"label\": \"S8\",")
    res.appendLine("                        \"query\": \"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\\nPREFIX parking: <https://github.com/luposdate3000/parking#>\\nPREFIX sosa: <http://www.w3.org/ns/sosa/>\\nPREFIX ssn: <http://www.w3.org/ns/ssn/>\\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\\nSELECT (COUNT(?ParkingSlotLocation) AS ?count ) WHERE {\\n ?ParkingSlotLocation parking:spotInArea ?spotInArea .\\n ?Observation sosa:observedProperty ?ParkingSlotLocation .\\n ?Observation sosa:resultTime ?lastObservedAt .\\n ?Observation sosa:hasSimpleResult \\\"false\\\"^^xsd:boolean .\\n {\\n  SELECT(MAX(?resultTime) AS ?lastObservedAt) ?ParkingSlotLocation WHERE {\\n    ?ParkingSlotLocation a sosa:ObservableProperty .\\n    ?ParkingSlotLocation parking:area 9 .\\n    ?Observation a sosa:Observation .\\n    ?Observation sosa:observedProperty ?ParkingSlotLocation .\\n    ?Observation sosa:resultTime ?resultTime .\\n  }\\n  GROUP BY ?ParkingSlotLocation\\n }\\n}\",")
    res.appendLine("                    },")
    res.appendLine("                ],")
    res.appendLine("            },")
    res.appendLine("            \"performance\": 100.0,")
    res.appendLine("            \"supportedLinkTypes\": [")
    res.appendLine("                \"WPAN\",")
    res.appendLine("                \"WLAN\",")
    res.appendLine("            ],")
    res.appendLine("        },")
    res.appendLine("        \"Database Device\": {")
    res.appendLine("            \"applications\": {")
    val unused2 = when (programDistribution) {
        EProgramDistribution.DISTRIBUTED_WITH_QUERY_HOPS, EProgramDistribution.DISTRIBUTED -> {
            res.appendLine("                \"lupos.simulator_db.luposdate3000.ApplicationFactory_Luposdate3000\": {")
            res.appendLine("                    \"databaseQuery\": true,")
            res.appendLine("                    \"databaseStore\": true,")
            res.appendLine("                    \"enabled\": true,")
            res.appendLine("                },")
        }
        EProgramDistribution.CENTRAL -> {
        }
    }
    res.appendLine("            },")
    res.appendLine("            \"performance\": 30.0,")
    res.appendLine("            \"supportedLinkTypes\": [")
    res.appendLine("                \"WPAN\",")
    res.appendLine("                \"WLAN\",")
    res.appendLine("            ],")
    res.appendLine("        },")
    res.appendLine("        \"Mesh Hop\": {")
    res.appendLine("            \"applications\": {")
    val unused3 = when (programDistribution) {
        EProgramDistribution.DISTRIBUTED_WITH_QUERY_HOPS -> {
            res.appendLine("                \"lupos.simulator_db.luposdate3000.ApplicationFactory_Luposdate3000\": {")
            res.appendLine("                    \"databaseQuery\": true,")
            res.appendLine("                    \"databaseStore\": false,")
            res.appendLine("                    \"enabled\": true,")
            res.appendLine("                },")
        }
        EProgramDistribution.CENTRAL, EProgramDistribution.DISTRIBUTED -> {
        }
    }
    res.appendLine("            },")
    res.appendLine("            \"performance\": 20.0,")
    res.appendLine("            \"supportedLinkTypes\": [")
    res.appendLine("                \"WLAN\",")
    res.appendLine("            ],")
    res.appendLine("        },")
    res.appendLine("        \"Sensor Device\": {")
    res.appendLine("            \"applications\": {")
    res.appendLine("                \"simora.applications.scenario.parking.ApplicationFactory_ParkingSensor\": {")
    res.appendLine("                    \"maxSamples\": 5,")
    res.appendLine("                    \"rateInSec\": 60,")
    res.appendLine("                    \"sendStartClockInSec\": 15,")
    res.appendLine("                },")
    res.appendLine("            },")
    res.appendLine("            \"performance\": 1.0,")
    res.appendLine("            \"supportedLinkTypes\": [")
    res.appendLine("                \"WPAN\",")
    res.appendLine("            ],")
    res.appendLine("        },")
    res.appendLine("    },")
    res.appendLine("    \"linkType\": {")
    res.appendLine("        \"WLAN\": {")
    res.appendLine("            \"dataRateInKbps\": 25000,")
    res.appendLine("            \"rangeInMeters\": $commRange,")
    res.appendLine("        },")
    res.appendLine("        \"WPAN\": {")
    res.appendLine("            \"dataRateInKbps\": 25000,")
    res.appendLine("            \"rangeInMeters\": 60,")
    res.appendLine("        },")
    res.appendLine("    },")
    res.appendLine("    \"fixedDevice\": {")
    res.appendLine("        \"Fog\": {")
    res.appendLine("            \"applications\": {},")
    res.appendLine("            \"deviceType\": \"Central Tower\",")
    res.appendLine("            \"latitude\": 53.83759450606049,")
    res.appendLine("            \"longitude\": 10.702377248379614,")
    res.appendLine("            \"patterns\": [")
    fun addSensors() {
        res.appendLine("                        {")
        res.appendLine("                            \"count\": $sensorsPerDatabase,")
        res.appendLine("                            \"deviceType\": \"Sensor Device\",")
        res.appendLine("                            \"mode\": \"count\",")
        res.appendLine("                            \"provideCounterAs\": \"spotInArea\",")
        res.appendLine("                            \"radius\": 50,")
        res.appendLine("                            \"type\": \"random_fill\",")
        res.appendLine("                        },")
    }
    addSensors()
    val unused4 = when (topology) {
        ETopology.RING -> {
            res.appendLine("                {")
            res.appendLine("                    \"count\": ${size - 1},")
            res.appendLine("                    \"deviceType\": \"Database Device\",")
            res.appendLine("                    \"mode\": \"count\",")
            res.appendLine("                    \"patterns\": [")
            addSensors()
            res.appendLine("                    ],")
            res.appendLine("                    \"provideCounterAs\": \"area\",")
            res.appendLine("                    \"radius\": 10000,")
            res.appendLine("                    \"type\": \"ring\",")
            res.appendLine("                },")
        }
        ETopology.FULL -> {
            res.appendLine("                {")
            res.appendLine("                    \"count\": ${size - 1},")
            res.appendLine("                    \"deviceType\": \"Database Device\",")
            res.appendLine("                    \"mode\": \"count\",")
            res.appendLine("                    \"patterns\": [")
            addSensors()
            res.appendLine("                    ],")
            res.appendLine("                    \"provideCounterAs\": \"area\",")
            res.appendLine("                    \"radius\": 50000,")
            res.appendLine("                    \"type\": \"full\",")
            res.appendLine("                },")
        }
        ETopology.RANDOM -> {
            res.appendLine("                {")
            res.appendLine("                    \"count\": ${size * 2},")
            res.appendLine("                    \"deviceType\": \"Mesh Hop\",")
            res.appendLine("                    \"mode\": \"count\",")
            res.appendLine("                    \"radius\": $radius,")
            res.appendLine("                    \"type\": \"random_fill\",")
            res.appendLine("                },")
            res.appendLine("                {")
            res.appendLine("                    \"count\": ${size - 1},")
            res.appendLine("                    \"deviceType\": \"Database Device\",")
            res.appendLine("                    \"mode\": \"count\",")
            res.appendLine("                    \"patterns\": [")
            addSensors()
            res.appendLine("                    ],")
            res.appendLine("                    \"provideCounterAs\": \"area\",")
            res.appendLine("                    \"radius\": $radius,")
            res.appendLine("                    \"type\": \"random_fill\",")
            res.appendLine("                },")
        }
        ETopology.UNIFORM -> {
            res.appendLine("                {")
            res.appendLine("                    \"count\": ${size * 2},")
            res.appendLine("                    \"deviceType\": \"Mesh Hop\",")
            res.appendLine("                    \"mode\": \"count\",")
            res.appendLine("                    \"radius\": $radius,")
            res.appendLine("                    \"type\": \"uniform\",")
            res.appendLine("                },")
            res.appendLine("                {")
            res.appendLine("                    \"count\": ${size - 1},")
            res.appendLine("                    \"deviceType\": \"Database Device\",")
            res.appendLine("                    \"mode\": \"count\",")
            res.appendLine("                    \"patterns\": [")
            addSensors()
            res.appendLine("                    ],")
            res.appendLine("                    \"provideCounterAs\": \"area\",")
            res.appendLine("                    \"radius\": $radius,")
            res.appendLine("                    \"type\": \"uniform\",")
            res.appendLine("                },")
        }
    }
    res.appendLine("            ],")
    res.appendLine("        },")
    res.appendLine("    },")
    res.appendLine("    \"rootRouter\": \"Fog\",")
    res.appendLine("    \"logging\": {")
    res.appendLine("        \"lupos.visualize.distributed.database.VisualisationNetwork\": {")
    res.appendLine("            \"enabled\": false,")
    res.appendLine("        },")
    res.appendLine("        \"simora.LoggerMeasure\": {")
    res.appendLine("            \"enabled\": true,")
    res.appendLine("        },")
    res.appendLine("        \"simora.LoggerStdout\": {")
    res.appendLine("            \"enabled\": false,")
    res.appendLine("        },")
    res.appendLine("    },")
    res.appendLine("    \"routing\": {")
    val unused6 = when (multicast) {
        EMulticast.DISABLED -> {
            res.appendLine("        \"multicast\": \"None\",")
        }
        EMulticast.SIMPLE -> {
            res.appendLine("        \"multicast\": \"Simple\",")
        }
    }
    val unused7 = when (routing) {
        ERouting.ASP -> {
            res.appendLine("        \"protocol\": \"AllShortestPath\",")
        }
        ERouting.RPL -> {
            res.appendLine("        \"protocol\": \"RPL\",")
        }
        ERouting.RPL_FAST -> {
            res.appendLine("        \"compatibilityMode\": false,")
            res.appendLine("        \"protocol\": \"RPL_Fast\",")
        }
    }
    res.appendLine("    },")
    res.appendLine("}")
    println(res.toString())
}
