#!/bin/bash
./benchmark_sumary.main.kts > summary.csv

ctr=1
CRouting=1
CX=1
CBoxMin=1
CWhiskerMin=1
CWhiskerHigh=1
CBoxHigh=1
CAvg=1
for i in $(head summary.csv -n 1|sed "s/ /-/g" | sed "s/,/ /g") ; do
    if [ $i == "routing" ] ; then
	CRouting=$(cat summary.csv | grep -e scalability -e benchmark_case | cut -d ',' -f$ctr | tr "\n" ",")
    fi
    if [ $i == "topology" ] ; then
        CX=$(cat summary.csv | grep -e scalability -e benchmark_case | cut -d ',' -f$ctr | tr "\n" "," | sed "s/Strong0*//g")
    fi
    if [ $i == "simulation-total-duration-real-(Seconds)" ] ; then
        CAvg=$(cat summary.csv | grep -e scalability -e benchmark_case | cut -d ',' -f$ctr | tr "\n" ",")
    fi
    if [ $i == "simulation-total-duration-real-(Seconds)Q1" ] ; then
        CBoxMin=$(cat summary.csv | grep -e scalability -e benchmark_case | cut -d ',' -f$ctr | tr "\n" ",")
    fi
    if [ $i == "simulation-total-duration-real-(Seconds)Min" ] ; then
        CWhiskerMin=$(cat summary.csv | grep -e scalability -e benchmark_case | cut -d ',' -f$ctr | tr "\n" ",")
    fi
    if [ $i == "simulation-total-duration-real-(Seconds)Max" ] ; then
        CWhiskerHigh=$(cat summary.csv | grep -e scalability -e benchmark_case | cut -d ',' -f$ctr | tr "\n" ",")
    fi
    if [ $i == "simulation-total-duration-real-(Seconds)Q3" ] ; then
        CBoxHigh=$(cat summary.csv | grep -e scalability -e benchmark_case | cut -d ',' -f$ctr | tr "\n" ",")
    fi
    ctr=$((ctr + 1))
done
#candlesticks
#echo $CRouting > tmp.csv
#echo $CX >> tmp.csv
#echo $CBoxMin >> tmp.csv
#echo $CWhiskerMin >> tmp.csv
#echo $CWhiskerHigh >> tmp.csv
#echo $CBoxHigh >> tmp.csv

echo $CRouting > tmp.csv
echo $CX >> tmp.csv
echo $CAvg >> tmp.csv
csvtool transpose tmp.csv | grep -w ApplicationRPLFastLate | sed "s/ApplicationRPLFastLate,//g" | sort -n > plot_scalability1.csv
csvtool transpose tmp.csv | grep -w ApplicationRPLFast | sed "s/ApplicationRPLFast,//g" | sort -n > plot_scalability2.csv
csvtool transpose tmp.csv | grep -w ApplicationRPL | sed "s/ApplicationRPL,//g" | sort -n > plot_scalability3.csv
#	x  box_min  whisker_min  whisker_high  box_high
./plot_scalability.gnuplot
rm tmp.csv
