#!/bin/bash

# mvn package -DskipTests 
# -rf :giraph-examples

# inputs=( stockMarket100k )
# inputs=( ERM5k ERM10k ERM50k ERM100k )
# inputs=( 2dtorus5k 2dtorus50k )
example=gameOfLife
workers=( 2 3 4 )

cfreq=1
# inputs=( 2DTorus_1000_giraph 2DTorus_5000_giraph 2DTorus_10000_giraph 2DTorus_50000_giraph 2DTorus_100000_giraph 2DTorus_20000_giraph 2DTorus_30000_giraph 2DTorus_40000_giraph )

# After the class name
# -ca giraph.logLevel=debug 

for repeat in {1..3}
  do
  # for input in "${inputs[@]}"
  for worker in "${workers[@]}"
    do
      tmp="log/gol_scaleout_$repeat"
      # echo "Input file is $input" >> $tmp
      $HADOOP_HOME/bin/hadoop dfs -rmr /user/root/output/$example
      # game of life example (cfreq)
      # $HADOOP_HOME/bin/hadoop jar giraph-examples/target/giraph-examples-1.4.0-SNAPSHOT-for-hadoop-1.2.1-jar-with-dependencies.jar org.apache.giraph.GiraphRunner org.apache.giraph.examples.GameOfLifeExampleClock -vif org.apache.giraph.examples.io.formats.JsonLongDoubleFloatDoubleArrayVertexInputFormat -vip /user/root/input/${input}.txt -vof org.apache.giraph.io.formats.IdWithValueTextOutputFormat -op /user/root/output/${example} -w ${worker} > $tmp 2>&1
      # game of life (regular)
      # $HADOOP_HOME/bin/hadoop jar giraph-examples/target/giraph-examples-1.4.0-SNAPSHOT-for-hadoop-1.2.1-jar-with-dependencies.jar org.apache.giraph.GiraphRunner org.apache.giraph.examples.GameOfLifeExample -vif org.apache.giraph.io.formats.JsonLongDoubleFloatDoubleVertexInputFormat -vip /user/root/input/${input}.txt -vof org.apache.giraph.io.formats.IdWithValueTextOutputFormat -op /user/root/output/${example} -w 1 >> $tmp 2>&1
      $HADOOP_HOME/bin/hadoop jar giraph-examples/target/giraph-examples-1.4.0-SNAPSHOT-for-hadoop-1.2.1-jar-with-dependencies.jar org.apache.giraph.GiraphRunner org.apache.giraph.examples.GameOfLifeExample -vif org.apache.giraph.io.formats.JsonLongDoubleFloatDoubleVertexInputFormat -vip /user/root/input/2DTorus_${worker}0000_giraph.txt -vof org.apache.giraph.io.formats.IdWithValueTextOutputFormat -op /user/root/output/${example} -w ${worker} >> $tmp 2>&1
      
      sudo sh -c "/usr/bin/echo 3 > /proc/sys/vm/drop_caches"
  done
done
