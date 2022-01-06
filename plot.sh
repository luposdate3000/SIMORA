#!/bin/bash
./benchmark_sumary.main.kts
./plot_scalability.gnuplot
./plot_routing_asp.gnuplot
./plot_routing_rpl.gnuplot
./plot_routing.gnuplot
for x in 0.00 1.00 2.00 3.00 4.00 5.00 6.00 7.00 8.00 9.00 10.00 11.00
do
y=$(grep "^$x" plot_routing_abs.map | sed "s/^$x://g")
sed "s/strut{}$x}/strut{}$y}/g" -i plot_routing.tex
done
#rm plot_routing_abs.map
