#!/usr/bin/env gnuplot
set terminal epslatex   
set output 'plot_scalability.tex'
set datafile separator ','
set xlabel "number of devices"
set ylabel "initalization time (Seconds)"
set grid xtics ytics
set key below
set logscale x 2
set logscale y 10
plot 'plot_scalability1.csv' using 1:2 with lines title 'RPL(JVM)' lt 1 dt 1 lw 2, \
     'plot_scalability2.csv' using 1:2 with lines title 'ASP(JVM)' lt 2 dt 2 lw 2, \
     'plot_scalability3.csv' using 1:2 with lines title 'RPL(Native)' lt 3 dt 3 lw 2, \
     'plot_scalability4.csv' using 1:2 with lines title 'ASP(Native)' lt 4 dt 4 lw 2
