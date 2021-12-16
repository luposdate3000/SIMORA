#!/bin/bash
commands=( \
"java -Xmx100g -Xms100g -cp $(cat ./build/external_jvm_dependencies | tr "\n" ":"):./build/libs/simora-jvm-0.0.1.jar simora.MainKt jvm.json" \
"./build/bin/linuxX64/releaseExecutable/simora.kexe linux.json"
)
./gradlew build


# compare topologies
if false
then
r="./resources/routing/ApplicationRPLFastLate.json"
for s in $(find ./resources/scenarios/ -name *.json | sort)
do
for t in $(find ./resources/topologies/ -name *.json | grep 64 | sort)
do
for ((i = 0; i < ${#commands[@]}; i++))
do
c="${commands[$i]}"
echo
pkill java -9
echo $c topologies.json $r $s $t
x=$(/usr/bin/time -o tmp -v $c topologies.json $r $s $t | grep simulator_output | sed "s/.*outputdirectory=//g")
mv tmp "$x/time"
done
done
done
fi


# compare routing
if false
then
t="./resources/topologies/Uniform64.json"
for s in $(find ./resources/scenarios/ -name *.json | sort)
do
for r in $(find ./resources/routing/ -name *.json | sort)
do
for ((i = 0; i < ${#commands[@]}; i++))
do
c="${commands[$i]}"
echo
pkill java -9
echo $c routing.json $r $s $t
x=$(/usr/bin/time -o tmp -v $c routing.json $r $s $t | grep simulator_output | sed "s/.*outputdirectory=//g")
mv tmp "$x/time"
done
done
done
fi


# compare scalability
c="java -Xmx100g -Xms100g -cp $(cat ./build/external_jvm_dependencies | tr "\n" ":"):./build/libs/simora-jvm-0.0.1.jar simora.MainKt jvm.json"
r="./resources/routing/ApplicationRPLFastLate.json"
s="./resources/scenarios/personalMail.json"
for t in $(find ./resources/topologies/ -name *.json | grep Strong | sort)
do
echo
pkill java -9
echo $c scalability.json $r $s $t
x=$(/usr/bin/time -o tmp -v $c scalability.json $r $s $t | grep simulator_output | sed "s/.*outputdirectory=//g")
mv tmp "$x/time"
done

# compare scalability2
c="./build/bin/linuxX64/releaseExecutable/simora.kexe linux.json"
r="./resources/routing/ApplicationRPLFastLate.json"
s="./resources/scenarios/personalMail.json"
for t in $(find ./resources/topologies/ -name *.json | grep Strong | sort)
do
echo
pkill java -9
echo $c scalability.json $r $s $t
x=$(/usr/bin/time -o tmp -v $c scalability.json $r $s $t | grep simulator_output | sed "s/.*outputdirectory=//g")
mv tmp "$x/time"
done
