#!/bin/bash
routing=( \
./resources/routing/Application_RPLFast.json \
./resources/routing/NoMulticast_RPLFast.json \
./resources/routing/RoutingAndApplication_RPLFast.json \
./resources/routing/Routing_RPLFast.json \
)
scenarios=( \
./resources/scenarios/mail.json \
)
topologies=( \
./resources/topologies/Full16.json \
./resources/topologies/Full32.json \
./resources/topologies/Full4.json \
./resources/topologies/Full64.json \
./resources/topologies/Full8.json \
./resources/topologies/Random16.json \
./resources/topologies/Random32.json \
./resources/topologies/Random4.json \
./resources/topologies/Random64.json \
./resources/topologies/Random8.json \
./resources/topologies/Ring16.json \
./resources/topologies/Ring32.json \
./resources/topologies/Ring4.json \
./resources/topologies/Ring64.json \
./resources/topologies/Ring8.json \
./resources/topologies/Uniform16.json \
./resources/topologies/Uniform32.json \
./resources/topologies/Uniform4.json \
./resources/topologies/Uniform64.json \
./resources/topologies/Uniform8.json \
)

for r in ${routing[@]}
do
for s in ${scenarios[@]}
do
for t in ${topologies[@]}
do
echo $r $s $t
done
done
done
