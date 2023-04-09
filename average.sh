#!/bin/bash

# inputs=( ERM10k_1_freq1 ERM10k_1_freq10 ERM10k_1_freq20 ERM10k_1_freq30 SBM10k_1_freq1 SBM10k_1_freq10 SBM10k_1_freq20 SBM10k_1_freq30 )
inputs=(dumbExample_1)

for input in "${inputs[@]}"
do
    # echo $input > $log
    grep Superstep ${input} | grep ms | cut -d '=' -f 2 > foo
    awk '{ total += $1 } END { print total/NR }' foo 
    # >> $log
done
