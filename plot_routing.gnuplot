#!/usr/bin/env gnuplot
set terminal epslatex size 13cm,5cm
set output 'plot_routing.tex'
set datafile separator ','
unset key
set view map scale 1
#set palette maxcolors 100
set cbrange [1000:100000]
set palette model RGB
set logscale cb
set palette defined 
set palette defined (0 "white", 100000 "gray")
plot 'plot_routing.csv' matrix rowheaders columnheaders using 1:2:3 with image, \
     'plot_routing_abs.csv' matrix rowheaders columnheaders using 1:2:(sprintf('%.2f', $3)) with labels
