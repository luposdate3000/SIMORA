#!/usr/bin/env gnuplot
set terminal tikz size 13cm,8cm
set output 'plot_scalability.tex'
set datafile separator ','
set xlabel "number of devices"
set ylabel "initalization time (seconds)"
set grid xtics ytics
set key below
set logscale x 2
set logscale y 10
set yrange [*:1000]
log2(x) = log(x)/log(2)
f1(x) = 0.01528148348810443 + 0.00023902338707248452*x + 2.0606706385706276e-08*x*x + 0.00013026390464609405*log2(x) + 4.21024326513475e-13*x*x*x + 4.016783205386121e-05*log2(x)*x
f2(x) = 0.1 + 0.1*x + 3.40495099360072e-05*x*x + 0.1*log2(x) + 0.05086279290153707*x*x*x + 0.1*log2(x)*x
f3(x) = 0.0014108805490180122 + 5.533150006766796e-05*x + 4.5250288704945524e-08*x*x + 1.6737376742903542e-07*log2(x) + 1.1004253064328395e-12*x*x*x + 1.4594468470441324e-05*log2(x)*x
f4(x) = 0.1 + 0.1*x + 0.0030453775123807426*x*x + 0.1*log2(x) + 0.06332193540888445*x*x*x + 0.1*log2(x)*x
f5(x) = 0.010251052962317443 + 0.0010630989646437216*x + 9.08232045071511e-08*x*x + 0.0005319131552025957*log2(x) + 1.1634360141954404e-12*x*x*x + 8.94865246317711e-05*log2(x)*x
f6(x) = 0.011429821741367264 + 2.946354886569358e-05*x + 1.7291978174349687e-07*x*x + 0.0028084582032618212*log2(x) + 7.722014037647718e-08*x*x*x + 0.0005567488246862984*log2(x)*x
plot 'plot_scalability1.csv' using 1:2 with lines title '\ac{RPL}(\ac{JVM})' lt 1 dt 1 lw 2, \
     'plot_scalability3.csv' using 1:2 with lines title '\ac{RPL}(Native)' lt 3 dt 3 lw 2, \
     'plot_scalability5.csv' using 1:2 with lines title '\ac{RPL}(\ac{JS})' lt 5 dt 5 lw 2, \
     'plot_scalability2.csv' using 1:2 with lines title '\ac{ASP}(\ac{JVM})' lt 2 dt 2 lw 2, \
     'plot_scalability4.csv' using 1:2 with lines title '\ac{ASP}(Native)' lt 4 dt 4 lw 2, \
     'plot_scalability6.csv' using 1:2 with lines title '\ac{ASP}(\ac{JS})' lt 6 dt 6 lw 2
#, \
#     f1(x) with lines title 'RPL2(JVM)' lt 1 dt 1 lw 2, \
#     f3(x) with lines title 'RPL2(Native)' lt 3 dt 3 lw 2, \
#     f5(x) with lines title 'RPL2(Js)' lt 5 dt 5 lw 2, \
#     f2(x) with lines title 'ASP2(JVM)' lt 2 dt 2 lw 2, \
#     f4(x) with lines title 'ASP2(Native)' lt 4 dt 4 lw 2, \
#     f6(x) with lines title 'ASP2(Js)' lt 6 dt 6 lw 2

