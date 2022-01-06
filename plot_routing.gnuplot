#!/usr/bin/env gnuplot
set terminal epslatex   
set output 'plot_routing.tex'
set datafile separator ','
unset key
set view map scale 1
#set palette maxcolors 100
set cbrange [1000:100000]
set palette model RGB
set logscale cb
set palette defined 
set palette defined (0 "white", 100000 "red")
plot 'plot_routing.csv' matrix rowheaders columnheaders using 1:2:3 with image, \
     'plot_routing_abs.csv' matrix rowheaders columnheaders using 1:2:(sprintf('%.2f', $3)) with labels
