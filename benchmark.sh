#!/bin/bash
commands=( \
"java -cp $(cat ./build/external_jvm_dependencies | tr "\n" ":"):./build/libs/simora-jvm-0.0.1.jar simora.MainKt jvm.json" \
"./build/bin/linuxX64/releaseExecutable/simora.kexe linux.json"
)


# compare topologies
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
echo $c $r $s $t
x=$(/usr/bin/time -o tmp -v $c $r $s $t | grep simulator_output | sed "s/.*outputdirectory=//g")
mv tmp "$x/time"
done
done
done


# compare routing
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
echo $c $r $s $t
x=$(/usr/bin/time -o tmp -v $c $r $s $t | grep simulator_output | sed "s/.*outputdirectory=//g")
mv tmp "$x/time"
done
done
done


# compare scalability
c="java -cp $(cat ./build/external_jvm_dependencies | tr "\n" ":"):./build/libs/simora-jvm-0.0.1.jar simora.MainKt jvm.json"
r="./resources/routing/ApplicationRPLFastLate.json"
for s in $(find ./resources/scenarios/ -name *.json | sort)
do
for t in $(find ./resources/topologies/ -name *.json | grep Strong | sort)
do
echo
pkill java -9
echo $c $r $s $t
x=$(/usr/bin/time -o tmp -v $c $r $s $t | grep simulator_output | sed "s/.*outputdirectory=//g")
mv tmp "$x/time"
done
done
