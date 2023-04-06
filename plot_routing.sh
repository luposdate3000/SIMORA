#!/bin/bash
./benchmark_sumary.main.kts
./plot_routing.gnuplot
while read p; do
  IFS=':'; q=($p); unset IFS;
  sed "s#${q[0]}#${q[1]}#g" -i plot_routing.tex
done <plot_routing_abs.map
sed "s#Multicast}#\\\\ac{DC} Multicast}#g" -i plot_routing.tex
sed "s#MulticastSOA#\\\\ac{SOA} Multicast#g" -i plot_routing.tex

