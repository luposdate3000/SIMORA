#!/bin/bash
./gradlew build
c="java -Xmx100g -Xms100g -cp $(cat ./build/external_jvm_dependencies | tr "\n" ":"):./build/libs/simora-jvm-0.0.1.jar simora.MainKt jvm.json"

if false
then
# compare scalability
c="java -Xmx100g -Xms100g -cp $(cat ./build/external_jvm_dependencies | tr "\n" ":"):./build/libs/simora-jvm-0.0.1.jar simora.MainKt jvm.json"
s="./resources/scenarios/personalMail.json"
for t in $(find ./resources/topologies/ -name *.json | grep Strong | sort)
do
for r in "./resources/routing/ApplicationRPLFastLate.json" "./resources/routing/ApplicationRPLFast.json" "./resources/routing/ApplicationRPL.json"
do
echo
pkill java -9
echo $c scalability.json $r $s $t
x=$(/usr/bin/time -o tmp -v $c scalability.json $r $s $t | grep simulator_output | sed "s/.*outputdirectory=//g")
mv tmp "$x/time"
done
done
fi

if false
then
# compare topologies
r="./resources/routing/ApplicationRPLFastLate.json"
for s in $(find ./resources/scenarios/ -name *.json | sort)
do
for t in $(find ./resources/topologies/ -name *.json | grep 128 | sort)
do
echo
pkill java -9
echo $c topologies.json $r $s $t
x=$(/usr/bin/time -o tmp -v $c topologies.json $r $s $t | grep simulator_output | sed "s/.*outputdirectory=//g")
mv tmp "$x/time"
done
done
fi

# compare routing
t="./resources/topologies/Uniform128.json"
#for s in $(find ./resources/scenarios/ -name *.json | sort)
for s in ./resources/scenarios/personalMail.json
do
#for r in $(find ./resources/routing/ -name *.json | sort)
for r in $(find ./resources/routing/ -name *.json | sort)
do
echo
pkill java -9
echo $c routing.json $r $s $t
x=$(/usr/bin/time -o tmp -v $c routing.json $r $s $t | grep simulator_output | sed "s/.*outputdirectory=//g")
mv tmp "$x/time"
done
done


