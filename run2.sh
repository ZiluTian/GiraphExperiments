#!/bin/bash

mvn package -DskipTests -rf :giraph-examples
inputs=( 2dtorus1m_clock )

for input in "${inputs[@]}"
do
  tmp="gol_1w_50fo_$input"
  echo "Input file is $input"
  # echo "Input file is $input" >> $tmp
  $HADOOP_HOME/bin/hadoop dfs -rmr /user/ztian/output/*

  # # $HADOOP_HOME/bin/hadoop jar giraph-examples/target/giraph-examples-1.4.0-SNAPSHOT-for-hadoop-1.2.1-jar-with-dependencies.jar org.apache.giraph.GiraphRunner org.apache.giraph.examples.GameOfLifeExample -vif org.apache.giraph.examples.io.formats.JsonLongDoubleFloatDoubleArrayVertexInputFormat -vip /user/ztian/input/${input}.txt -vof org.apache.giraph.io.formats.IdWithValueTextOutputFormat -op /user/ztian/output/gameOfLife -w 1 > $tmp 2>&1
  $HADOOP_HOME/bin/hadoop jar giraph-examples/target/giraph-examples-1.4.0-SNAPSHOT-for-hadoop-1.2.1-jar-with-dependencies.jar org.apache.giraph.GiraphRunner org.apache.giraph.examples.GameOfLifeExampleClock -vif org.apache.giraph.io.formats.JsonLongDoubleFloatDoubleVertexInputFormat -vip /user/ztian/input/${input}.txt -vof org.apache.giraph.io.formats.IdWithValueTextOutputFormat -op /user/ztian/output/gameOfLife -w 1 > $tmp 2>&1
  
  sudo sh -c "/usr/bin/echo 3 > /proc/sys/vm/drop_caches"
done
