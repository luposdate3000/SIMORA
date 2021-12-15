#!/bin/bash
routing=( \
./resources/routing/ApplicationRPLFast.json \
./resources/routing/NoMulticastRPLFast.json \
./resources/routing/RoutingAndApplicationRPLFast.json \
./resources/routing/RoutingRPLFast.json \
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
echo
pkill java -9
echo java -cp $(cat ./build/external_jvm_dependencies | tr "\n" ":"):./build/libs/simora-jvm-0.0.1.jar simora.MainKt $r $s $t jvm.json
x=$(/usr/bin/time -o tmp -v java -cp $(cat ./build/external_jvm_dependencies | tr "\n" ":"):./build/libs/simora-jvm-0.0.1.jar simora.MainKt $r $s $t jvm.json | grep simulator_output | sed "s/.*outputdirectory=//g")
mv tmp "$x/time"
exit
#echo
#echo ./build/bin/linuxX64/releaseExecutable/simora.kexe $r $s $t linux.json
#/usr/bin/time -v ./build/bin/linuxX64/releaseExecutable/simora.kexe $r $s $t linux.json
#echo
#echo ./build/bin/linuxX64/debugExecutable/simora.kexe $r $s $t linux.json
#/usr/bin/time -v ./build/bin/linuxX64/debugExecutable/simora.kexe $r $s $t linux.json
done
done
done
