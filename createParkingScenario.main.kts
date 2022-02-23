#!/usr/bin/env kotlin

enum class EQueryDistribution { ROUTING, CENTRALIZED }
enum class ERouting { ASP, RPL, RPL_FAST }
enum class EProgramDistribution { DISTRIBUTED_WITH_QUERY_HOPS, CENTRAL, DISTRIBUTED }
enum class EMulticast { DISABLED, SIMPLE }
enum class EDataDistribution { SIMPLE, ID_1, ID_2, ID_O, ID_S, ID_TWICE, KEY }
enum class EQueries { S0, S1, S2, S3, S4, S5, S6, S7, S8 }

enum class ETopology { UNIFORM }

var topology = ETopology.UNIFORM
var programDistribution = EProgramDistribution.DISTRIBUTED_WITH_QUERY_HOPS
var queries = EQueries.S8
var dataDistribution = EDataDistribution.SIMPLE
var queryDistribution = EQueryDistribution.ROUTING
var multicast = EMulticast.DISABLED
var routing = ERouting.ASP
for (arg in args) {
    println(arg)
}

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
val unused5 = when (queries) {
    EQueries.S0 -> {
    }
    EQueries.S1 -> {
        res.ppendLine("                \"simora.applications.scenario.parking.ApplicationFactory_QuerySender\": [")
        res.ppendLine("                    {")
        res.ppendLine("                        \"maxNumberOfQueries\": 1,")
        res.ppendLine("                        \"query\": \"SELECT ?s ?p ?o WHERE { ?s ?p ?o. }\",")
        res.ppendLine("                        \"sendRateInSeconds\": 1,")
        res.ppendLine("                        \"sendStartClockInSec\": 10000000,")
        res.ppendLine("                    },")
        res.ppendLine("                ],")
    }
    EQueries.S2 -> {
        res.ppendLine("                \"simora.applications.scenario.parking.ApplicationFactory_QuerySender\": [")
        res.ppendLine("                    {")
        res.ppendLine("                        \"maxNumberOfQueries\": 1,")
        res.ppendLine("                        \"query\": \"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\\nPREFIX parking: <https://github.com/luposdate3000/parking#>\\nPREFIX sosa: <http://www.w3.org/ns/sosa/>\\nPREFIX ssn: <http://www.w3.org/ns/ssn/>\\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\\nSELECT DISTINCT ?area WHERE {\\n ?ParkingSlotLocation a sosa:ObservableProperty .\\n ?ParkingSlotLocation parking:area ?area .\\n}\",")
        res.ppendLine("                        \"sendRateInSeconds\": 1,")
        res.ppendLine("                        \"sendStartClockInSec\": 10000000,")
        res.ppendLine("                    },")
        res.ppendLine("                ],")
    }
    EQueries.S3 -> {
        res.ppendLine("                \"simora.applications.scenario.parking.ApplicationFactory_QuerySender\": [")
        res.ppendLine("                    {")
        res.ppendLine("                        \"maxNumberOfQueries\": 1,")
        res.ppendLine("                        \"query\": \"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\\nPREFIX parking: <https://github.com/luposdate3000/parking#>\\nPREFIX sosa: <http://www.w3.org/ns/sosa/>\\nPREFIX ssn: <http://www.w3.org/ns/ssn/>\\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\\nSELECT (COUNT(DISTINCT ?spotInArea) as ?count) WHERE {\\n ?ParkingSlotLocation a sosa:ObservableProperty .\\n ?ParkingSlotLocation parking:area 9 .\\n ?ParkingSlotLocation parking:spotInArea ?spotInArea .\\n}\",")
        res.ppendLine("                        \"sendRateInSeconds\": 1,")
        res.ppendLine("                        \"sendStartClockInSec\": 10000000,")
        res.ppendLine("                    },")
        res.ppendLine("                ],")
    }
    EQueries.S4 -> {
        res.ppendLine("                \"simora.applications.scenario.parking.ApplicationFactory_QuerySender\": [")
        res.ppendLine("                    {")
        res.ppendLine("                        \"maxNumberOfQueries\": 1,")
        res.ppendLine("                        \"query\": \"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\\nPREFIX parking: <https://github.com/luposdate3000/parking#>\\nPREFIX sosa: <http://www.w3.org/ns/sosa/>\\nPREFIX ssn: <http://www.w3.org/ns/ssn/>\\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\\nSELECT (COUNT(?Observation) as ?count) WHERE {\\n ?ParkingSlotLocation a sosa:ObservableProperty .\\n ?ParkingSlotLocation parking:area 6 .\\n ?ParkingSlotLocation parking:spotInArea 1 .\\n ?Observation a sosa:Observation .\\n ?Observation sosa:observedProperty ?ParkingSlotLocation .\\n}\",")
        res.ppendLine("                        \"sendRateInSeconds\": 1,")
        res.ppendLine("                        \"sendStartClockInSec\": 10000000,")
        res.ppendLine("                    },")
        res.ppendLine("                ],")
    }
    EQueries.S5 -> {
        res.ppendLine("                \"simora.applications.scenario.parking.ApplicationFactory_QuerySender\": [")
        res.ppendLine("                    {")
        res.ppendLine("                        \"maxNumberOfQueries\": 1,")
        res.ppendLine("                        \"query\": \"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\\nPREFIX parking: <https://github.com/luposdate3000/parking#>\\nPREFIX sosa: <http://www.w3.org/ns/sosa/>\\nPREFIX ssn: <http://www.w3.org/ns/ssn/>\\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\\nSELECT (MAX(?resultTime) AS ?latestDate) WHERE {\\n ?ParkingSlotLocation a sosa:ObservableProperty .\\n ?ParkingSlotLocation parking:area 7 .\\n ?ParkingSlotLocation parking:spotInArea 1 .\\n ?Observation a sosa:Observation .\\n ?Observation sosa:observedProperty ?ParkingSlotLocation .\\n ?Observation sosa:resultTime ?resultTime .\\n}\",")
        res.ppendLine("                        \"sendRateInSeconds\": 1,")
        res.ppendLine("                        \"sendStartClockInSec\": 10000000,")
        res.ppendLine("                    },")
        res.ppendLine("                ],")
    }
    EQueries.S6 -> {
        res.ppendLine("                \"simora.applications.scenario.parking.ApplicationFactory_QuerySender\": [")
        res.ppendLine("                    {")
        res.ppendLine("                        \"maxNumberOfQueries\": 1,")
        res.ppendLine("                        \"query\": \"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\\nPREFIX parking: <https://github.com/luposdate3000/parking#>\\nPREFIX sosa: <http://www.w3.org/ns/sosa/>\\nPREFIX ssn: <http://www.w3.org/ns/ssn/>\\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\\nSELECT ?spotInArea ?isOccupied ?lastObservedAt WHERE {\\n ?ParkingSlotLocation parking:spotInArea ?spotInArea .\\n ?Observation sosa:observedProperty ?ParkingSlotLocation .\\n ?Observation sosa:resultTime ?lastObservedAt .\\n ?Observation sosa:hasSimpleResult ?isOccupied .\\n {\\n  SELECT(MAX(?resultTime) AS ?lastObservedAt) ?ParkingSlotLocation WHERE {\\n    ?ParkingSlotLocation a sosa:ObservableProperty .\\n    ?ParkingSlotLocation parking:area 9 .\\n    ?Observation a sosa:Observation .\\n    ?Observation sosa:observedProperty ?ParkingSlotLocation .\\n    ?Observation sosa:resultTime ?resultTime .\\n  }\\n  GROUP BY ?ParkingSlotLocation\\n }\\n}\",")
        res.ppendLine("                        \"sendRateInSeconds\": 1,")
        res.ppendLine("                        \"sendStartClockInSec\": 10000000,")
        res.ppendLine("                    },")
        res.ppendLine("                ],")
    }
    EQueries.S7 -> {
        res.ppendLine("                \"simora.applications.scenario.parking.ApplicationFactory_QuerySender\": [")
        res.ppendLine("                    {")
        res.ppendLine("                        \"maxNumberOfQueries\": 1,")
        res.ppendLine("                        \"query\": \"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\\nPREFIX parking: <https://github.com/luposdate3000/parking#>\\nPREFIX sosa: <http://www.w3.org/ns/sosa/>\\nPREFIX ssn: <http://www.w3.org/ns/ssn/>\\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\\nSELECT ?area ?spotInArea ?isOccupied ?lastObservedAt WHERE {\\n ?ParkingSlotLocation parking:area ?area .\\n ?ParkingSlotLocation parking:spotInArea ?spotInArea .\\n ?Observation sosa:observedProperty ?ParkingSlotLocation .\\n ?Observation sosa:resultTime ?lastObservedAt .\\n ?Observation sosa:hasSimpleResult ?isOccupied .\\n { \\n  SELECT(MAX(?resultTime) AS ?lastObservedAt) ?ParkingSlotLocation WHERE { \\n    ?ParkingSlotLocation a sosa:ObservableProperty .\\n    ?ParkingSlotLocation parking:area ?area .\\n    ?Observation a sosa:Observation .\\n    ?Observation sosa:observedProperty ?ParkingSlotLocation .\\n    ?Observation sosa:resultTime ?resultTime .\\n    FILTER (?area IN (9, 8, 2))\\n  }\\n  GROUP BY ?ParkingSlotLocation\\n }\\n}\",")
        res.ppendLine("                        \"sendRateInSeconds\": 1,")
        res.ppendLine("                        \"sendStartClockInSec\": 10000000,")
        res.ppendLine("                    },")
        res.ppendLine("                ],")
    }
    EQueries.S8 -> {
        res.ppendLine("                \"simora.applications.scenario.parking.ApplicationFactory_QuerySender\": [")
        res.ppendLine("                    {")
        res.ppendLine("                        \"maxNumberOfQueries\": 1,")
        res.ppendLine("                        \"query\": \"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\\nPREFIX parking: <https://github.com/luposdate3000/parking#>\\nPREFIX sosa: <http://www.w3.org/ns/sosa/>\\nPREFIX ssn: <http://www.w3.org/ns/ssn/>\\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\\nSELECT (COUNT(?ParkingSlotLocation) AS ?count ) WHERE {\\n ?ParkingSlotLocation parking:spotInArea ?spotInArea .\\n ?Observation sosa:observedProperty ?ParkingSlotLocation .\\n ?Observation sosa:resultTime ?lastObservedAt .\\n ?Observation sosa:hasSimpleResult \\\"false\\\"^^xsd:boolean .\\n {\\n  SELECT(MAX(?resultTime) AS ?lastObservedAt) ?ParkingSlotLocation WHERE {\\n    ?ParkingSlotLocation a sosa:ObservableProperty .\\n    ?ParkingSlotLocation parking:area 9 .\\n    ?Observation a sosa:Observation .\\n    ?Observation sosa:observedProperty ?ParkingSlotLocation .\\n    ?Observation sosa:resultTime ?resultTime .\\n  }\\n  GROUP BY ?ParkingSlotLocation\\n }\\n}\",")
        res.ppendLine("                        \"sendRateInSeconds\": 1,")
        res.ppendLine("                        \"sendStartClockInSec\": 10000000,")
        res.ppendLine("                    },")
        res.ppendLine("                ],")
    }
}
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
    EProgramDistribution.CENTRAL -> {
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
res.appendLine("            \"rangeInMeters\": 150,")
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
val unused4 = when (topology) {
    ETopology.RING -> {
        res.ppendLine("                {")
        res.ppendLine("                    \"count\": 127,")
        res.ppendLine("                    \"deviceType\": \"Database Device\",")
        res.ppendLine("                    \"mode\": \"count\",")
        res.ppendLine("                    \"patterns\": [")
        res.ppendLine("                        {")
        res.ppendLine("                            \"count\": 10,")
        res.ppendLine("                            \"deviceType\": \"Sensor Device\",")
        res.ppendLine("                            \"mode\": \"count\",")
        res.ppendLine("                            \"provideCounterAs\": \"spotInArea\",")
        res.ppendLine("                            \"radius\": 50,")
        res.ppendLine("                            \"type\": \"random_fill\",")
        res.ppendLine("                        },")
        res.ppendLine("                    ],")
        res.ppendLine("                    \"provideCounterAs\": \"area\",")
        res.ppendLine("                    \"radius\": 10000,")
        res.ppendLine("                    \"type\": \"ring\",")
        res.ppendLine("                },")
    }
    ETopology.FULL -> {
        res.ppendLine("                {")
        res.ppendLine("                    \"count\": 127,")
        res.ppendLine("                    \"deviceType\": \"Database Device\",")
        res.ppendLine("                    \"mode\": \"count\",")
        res.ppendLine("                    \"patterns\": [")
        res.ppendLine("                        {")
        res.ppendLine("                            \"count\": 10,")
        res.ppendLine("                            \"deviceType\": \"Sensor Device\",")
        res.ppendLine("                            \"mode\": \"count\",")
        res.ppendLine("                            \"provideCounterAs\": \"spotInArea\",")
        res.ppendLine("                            \"radius\": 50,")
        res.ppendLine("                            \"type\": \"random_fill\",")
        res.ppendLine("                        },")
        res.ppendLine("                    ],")
        res.ppendLine("                    \"provideCounterAs\": \"area\",")
        res.ppendLine("                    \"radius\": 500,")
        res.ppendLine("                    \"type\": \"full\",")
        res.ppendLine("                },")
    }
    ETopology.RANDOM -> {
        res.ppendLine("                {")
        res.ppendLine("                    \"count\": 255,")
        res.ppendLine("                    \"deviceType\": \"Mesh Hop\",")
        res.ppendLine("                    \"mode\": \"count\",")
        res.ppendLine("                    \"radius\": 800,")
        res.ppendLine("                    \"type\": \"random_fill\",")
        res.ppendLine("                },")
        res.ppendLine("                {")
        res.ppendLine("                    \"count\": 127,")
        res.ppendLine("                    \"deviceType\": \"Database Device\",")
        res.ppendLine("                    \"mode\": \"count\",")
        res.ppendLine("                    \"patterns\": [")
        res.ppendLine("                        {")
        res.ppendLine("                            \"count\": 10,")
        res.ppendLine("                            \"deviceType\": \"Sensor Device\",")
        res.ppendLine("                            \"mode\": \"count\",")
        res.ppendLine("                            \"provideCounterAs\": \"spotInArea\",")
        res.ppendLine("                            \"radius\": 50,")
        res.ppendLine("                            \"type\": \"random_fill\",")
        res.ppendLine("                        },")
        res.ppendLine("                    ],")
        res.ppendLine("                    \"provideCounterAs\": \"area\",")
        res.ppendLine("                    \"radius\": 800,")
        res.ppendLine("                    \"type\": \"random_fill\",")
        res.ppendLine("                },")
    }
    ETopology.UNIFORM -> {
        res.appendLine("                {")
        res.appendLine("                    \"count\": 255,")
        res.appendLine("                    \"deviceType\": \"Mesh Hop\",")
        res.appendLine("                    \"mode\": \"count\",")
        res.appendLine("                    \"radius\": 800,")
        res.appendLine("                    \"type\": \"UNIFORM\",")
        res.appendLine("                },")
        res.appendLine("                {")
        res.appendLine("                    \"count\": 127,")
        res.appendLine("                    \"deviceType\": \"Database Device\",")
        res.appendLine("                    \"mode\": \"count\",")
        res.appendLine("                    \"patterns\": [")
        res.appendLine("                        {")
        res.appendLine("                            \"count\": 10,")
        res.appendLine("                            \"deviceType\": \"Sensor Device\",")
        res.appendLine("                            \"mode\": \"count\",")
        res.appendLine("                            \"provideCounterAs\": \"spotInArea\",")
        res.appendLine("                            \"radius\": 50,")
        res.appendLine("                            \"type\": \"random_fill\",")
        res.appendLine("                        },")
        res.appendLine("                    ],")
        res.appendLine("                    \"provideCounterAs\": \"area\",")
        res.appendLine("                    \"radius\": 800,")
        res.appendLine("                    \"type\": \"UNIFORM\",")
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
