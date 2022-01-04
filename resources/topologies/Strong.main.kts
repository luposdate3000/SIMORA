#!/usr/bin/env kotlin
import java.io.File
import kotlin.math.log
import kotlin.math.sqrt
val limit = 300000
val limitLength=limit.toString().length+1
var i = 2
while (i < limit) {
var repeatSimulationCount=128/i
if(repeatSimulationCount<1){
repeatSimulationCount=1
}
val radius=sqrt(i.toDouble()*log(i.toDouble(),2.0))*16.0 // approximated such that there are about 11 links per device in average
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
            out.println("                    \"radius\": $radius,")
            out.println("                    \"type\": \"uniform\",")
            out.println("                },")
            out.println("            ],")
            out.println("        },")
            out.println("    },")
            out.println("    \"repeatSimulationCount\":$repeatSimulationCount,")
            out.println("}")
        }
        i = i * 2
}
