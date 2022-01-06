#!/usr/bin/env gnuplot
set terminal epslatex   
set output 'plot_routing_asp.tex'
set datafile separator ','
unset key
set view map scale 1
set cbrange [0:1]
set palette maxcolors 100
set palette defined (0 "white", 1 "red")
plot 'plot_routing_asp.csv' matrix rowheaders columnheaders using 1:2:3 with image, \
     'plot_routing_asp.csv' matrix rowheaders columnheaders using 1:2:(sprintf('%.2f', $3)) with labels
