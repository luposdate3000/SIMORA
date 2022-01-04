#!/bin/bash
./gradlew ktlintformat jvmjar
c="java -Xmx100g -Xms100g -cp $(cat ./build/external_jvm_dependencies | tr "\n" ":"):./build/libs/simora-jvm-0.0.1.jar simora.MainKt jvm.json"

if true
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


if true
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


if true
then
# compare scalability
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
done
fi

