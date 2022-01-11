#!/bin/bash
./benchmark_sumary.main.kts
for f in plot_routing_abs.csv plot_routing.csv
do
sed "s/MulticastStateOfTheArt/Multicast(SOA)/g" -i $f
sed "s/Multicast,/Multicast(DC),/g" -i $f
done
./plot_scalability.gnuplot
./plot_routing.gnuplot
for x in $(cat plot_routing_abs.map | sed "s/:.*/ /g" | tr "\n" " ")
do
y=$(grep "^$x" plot_routing_abs.map | sed "s/^$x://g")
sed "s/strut{}$x}/strut{}$y}/g" -i plot_routing.tex
done
#rm plot_routing_abs.map

