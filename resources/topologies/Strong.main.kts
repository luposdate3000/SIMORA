#!/usr/bin/env kotlin
import java.io.File

val limit = 9999999
val limitLength=limit.toString().length+1
var i = 2
var radius = 0.0025
while (i < limit) {
    for (j in 0 until 2) {
        File("Strong${i.toString().padStart(limitLength,'0')}.json").printWriter().use { out ->
            out.println("{")
            out.println("    \"fixedDevice\": {")
            out.println("        \"CentralTower\": {")
            out.println("            \"applications\": {},")
            out.println("            \"deviceType\": \"CentralTower\",")
            out.println("            \"latitude\": 53.83759450606049,")
            out.println("            \"longitude\": 10.702377248379614,")
            out.println("            \"patterns\": [")
            out.println("                {")
            out.println("                    \"count\": ${i - 1},")
            out.println("                    \"deviceType\": \"EdgeDevice\",")
            out.println("                    \"mode\": \"count\",")
            out.println("                    \"radius\": ${radius},")
            out.println("                    \"type\": \"full\",")
            out.println("                },")
            out.println("            ],")
            out.println("        },")
            out.println("    },")
            out.println("}")
        }
        i = i * 2
    }
    radius = radius * 2
}
