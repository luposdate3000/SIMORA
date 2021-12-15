#!/bin/bash
routing=( $(find ./resources/routing/ -name *.json | sort))
topologies=($(find ./resources/topologies/ -name *.json | sort))
scenarios=( \
./resources/scenarios/mail.json \
)
for t in ${topologies[@]}
do
for r in ${routing[@]}
do
for s in ${scenarios[@]}
do
echo
#pkill java -9
#echo java -cp $(cat ./build/external_jvm_dependencies | tr "\n" ":"):./build/libs/simora-jvm-0.0.1.jar simora.MainKt $r $s $t jvm.json
#x=$(/usr/bin/time -o tmp -v java -cp $(cat ./build/external_jvm_dependencies | tr "\n" ":"):./build/libs/simora-jvm-0.0.1.jar simora.MainKt $r $s $t jvm.json | grep simulator_output | sed "s/.*outputdirectory=//g")
#mv tmp "$x/time"
done
done
done
for t in ${topologies[@]}
do
for r in ${routing[@]}
do
for s in ${scenarios[@]}
do
echo
echo ./build/bin/linuxX64/releaseExecutable/simora.kexe $r $s $t linux.json
x=$(/usr/bin/time -o tmp -v ./build/bin/linuxX64/releaseExecutable/simora.kexe $r $s $t linux.json | grep simulator_output | sed "s/.*outputdirectory=//g")
mv tmp "$x/time"
done
done
done
