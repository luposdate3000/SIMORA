#!/bin/bash
./benchmark_sumary.main.kts > summary.csv
cat summary.csv | grep scalability | grep ApplicationRPLFastLate | sed "s/.*Strong0*//g" | sed "s/,/-/" | sed "s/,.*//g" | sed "s/-/,/g" | sort -n > plot_scalability1.csv
cat summary.csv | grep scalability | grep ApplicationRPLFast | sed "s/.*Strong0*//g" | sed "s/,/-/" | sed "s/,.*//g" | sed "s/-/,/g" | sort -n > plot_scalability2.csv
cat summary.csv | grep scalability | grep ApplicationRPL | sed "s/.*Strong0*//g" | sed "s/,/-/" | sed "s/,.*//g" | sed "s/-/,/g" | sort -n > plot_scalability3.csv
./plot_scalability.gnuplot
