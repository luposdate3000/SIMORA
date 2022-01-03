#!/usr/bin/env gnuplot
set terminal epslatex   
set output 'plot_scalability.tex'
set datafile separator ','
set xlabel "number of devices"
set ylabel "initalization time (Seconds)"
set grid xtics ytics
set key left top
set logscale x 2
set logscale y 10
plot 'plot_scalability1.csv' using 2:3:4:5:6 with candlesticks title 'RPL init on demand using global knowledge', \
     'plot_scalability2.csv' using 2:3:4:5:6 with candlesticks title 'RPL using global knowledge', \
     'plot_scalability3.csv' using 2:3:4:5:6 with candlesticks title 'RPL default setup'
