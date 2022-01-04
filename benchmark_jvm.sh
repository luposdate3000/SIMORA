#!/bin/bash
./gradlew jvmjar
c="java -Xmx100g -Xms100g -cp $(cat ./build/external_jvm_dependencies | tr "\n" ":"):./build/libs/simora-jvm-0.0.1.jar simora.MainKt jvm.json"

cmpTopologies=false
cmpRoutingAndMulticast=false
cmpScalability=true

if $cmpTopologies
then
# compare topologies
m="./resources/multicast/Application.json"
r="./resources/routing/RPLFastLate.json"
for s in $(find ./resources/scenarios/ -name *.json | sort)
do
for t in $(find ./resources/topologies/ -name *.json | grep 128 | sort)
do
echo
pkill java -9
echo $c topologies.json $m $r $s $t
x=$(/usr/bin/time -o tmp -v $c topologies.json $m $r $s $t | grep simulator_output | sed "s/.*outputdirectory=//g")
mv tmp "$x/time"
done
done
fi


if $cmpRoutingAndMulticast
then
# compare routing and multicast
t="./resources/topologies/Uniform128.json"
for s in $(find ./resources/scenarios/ -name *.json | sort)
#for s in ./resources/scenarios/personalMail.json
do
for m in $(find ./resources/multicast/ -name *.json | sort)
do
for r in $(find ./resources/routing/ -name *.json | sort)
do
echo
pkill java -9
echo $c routing.json $m $r $s $t
x=$(/usr/bin/time -o tmp -v $c routing.json $m $r $s $t | grep simulator_output | sed "s/.*outputdirectory=//g")
mv tmp "$x/time"
done
done
done
fi


if false
then
# compare scalability ALL
m="./resources/multicast/Application.json"
c="java -Xmx100g -Xms100g -cp $(cat ./build/external_jvm_dependencies | tr "\n" ":"):./build/libs/simora-jvm-0.0.1.jar simora.MainKt jvm.json"
s="./resources/scenarios/personalMail.json"
for t in $(find ./resources/topologies/ -name *.json | grep Strong | sort)
do
for r in $(find ./resources/routing/ -name *.json | sort)
do
echo
pkill java -9
echo $c scalability.json $m $r $s $t
x=$(/usr/bin/time -o tmp -v $c scalability.json $m $r $s $t | grep simulator_output | sed "s/.*outputdirectory=//g")
mv tmp "$x/time"
done
done
fi

if $cmpScalability
then
# compare scalability optimized
m="./resources/multicast/Application.json"
c="java -Xmx100g -Xms100g -cp $(cat ./build/external_jvm_dependencies | tr "\n" ":"):./build/libs/simora-jvm-0.0.1.jar simora.MainKt jvm.json"
s="./resources/scenarios/personalMail.json"
#for tt in 2 4 8 16 32 64 128 256 512 1024 2048
for tt in x
do
t=$(find ./resources/topologies/ -name *.json | grep Strong | sort | grep Strong0*$tt.json)
for r in ./resources/routing/ASP.json ./resources/routing/RPLFast.json ./resources/routing/RPLFastLate.json ./resources/routing/RPL.json
do
echo
pkill java -9
echo $c scalability.json $m $r $s $t
x=$(/usr/bin/time -o tmp -v $c scalability.json $m $r $s $t | grep simulator_output | sed "s/.*outputdirectory=//g")
mv tmp "$x/time"
done
done
#for tt in 4096
for tt in x
do
t=$(find ./resources/topologies/ -name *.json | grep Strong | sort | grep Strong0*$tt.json)
for r in ./resources/routing/RPLFast.json ./resources/routing/RPLFastLate.json ./resources/routing/RPL.json
do
echo
pkill java -9
echo $c scalability.json $m $r $s $t
x=$(/usr/bin/time -o tmp -v $c scalability.json $m $r $s $t | grep simulator_output | sed "s/.*outputdirectory=//g")
mv tmp "$x/time"
done
done
for tt in 8192 16384 32768 65536 131072 262144
do
t=$(find ./resources/topologies/ -name *.json | grep Strong | sort | grep Strong0*$tt.json)
for r in ./resources/routing/RPLFastLate.json ./resources/routing/RPL.json
do
echo
pkill java -9
echo $c scalability.json $m $r $s $t
x=$(/usr/bin/time -o tmp -v $c scalability.json $m $r $s $t | grep simulator_output | sed "s/.*outputdirectory=//g")
mv tmp "$x/time"
done
done
fi
