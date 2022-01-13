#!/bin/bash
./gradlew linkReleaseExecutableLinuxX64
m="./resources/multicast/Application.json"
c='./build/bin/linuxX64/releaseExecutable/simora.kexe linux.json'

cmpRoutingAndMulticastAndTopologies=false
cmpScalability=true

if $cmpRoutingAndMulticastAndTopologies
then
# compare routing and multicast
for t in $(find ./resources/topologies/ -name *.json | grep 128 | sort | grep -v Strong)
do
for s in $(find ./resources/scenarios/ -name iotconfiguration*.json | sort)
do
for r in ./resources/routing/ASP.json ./resources/routing/RPLFast.json
do
echo
pkill java -9
echo $c routing.json $m $r $s $t
/usr/bin/time -o tmp -v $c routing.json $m $r $s $t > tmp2
x=$(cat tmp2 | grep simulator_output | sed "s/.*outputdirectory=//g")
mv tmp "$x/time"
done
done
done
fi


if $cmpScalability
then
# compare scalability optimized
s="./resources/scenarios/none.json"
#for t in $(find ./resources/topologies/ -name *.json | grep Strong | sort)
for tt in 2 4 8 16 32 64 128 256 512 1024 2048
do
t=$(find ./resources/topologies/ -name *.json | grep Strong | sort | grep Strong0*$tt.json)
for r in ./resources/routing/ASP.json ./resources/routing/RPLFastLate.json
do
echo
pkill java -9
echo $c scalability.json $m $r $s $t
/usr/bin/time -o tmp -v $c scalability.json $m $r $s $t > tmp2
x=$(cat tmp2 | grep simulator_output | sed "s/.*outputdirectory=//g")
mv tmp "$x/time"
done
done
for tt in 4096 8192 16384 32768
do
t=$(find ./resources/topologies/ -name *.json | grep Strong | sort | grep Strong0*$tt.json)
for r in ./resources/routing/RPLFastLate.json
do
echo
pkill java -9
echo $c scalability.json $m $r $s $t
/usr/bin/time -o tmp -v $c scalability.json $m $r $s $t > tmp2
x=$(cat tmp2 | grep simulator_output | sed "s/.*outputdirectory=//g")
mv tmp "$x/time"
done
done
fi
rm tmp2
