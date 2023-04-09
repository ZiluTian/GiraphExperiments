#!/bin/bash

mvn package -DskipTests 
# -rf :giraph-examples

# inputs=( stockMarket100k )
inputs=( dumbExample )
# inputs=( ERM5k ERM10k ERM50k ERM100k )
# inputs=( 2dtorus5k 2dtorus50k )
example=example
workers=( 1 )

cfreq=1
# inputs=( 2dtorus1k 2dtorus10k 2dtorus100k 2dtorus1m )

# After the class name
# -ca giraph.logLevel=debug 

for input in "${inputs[@]}"
do
    for worker in "${workers[@]}"
    do
      tmp="${input}_${worker}"
      # echo "Input file is $input" >> $tmp
      $HADOOP_HOME/bin/hadoop dfs -rmr /user/root/output/$example
      $HADOOP_HOME/bin/hadoop jar giraph-examples/target/giraph-examples-1.4.0-SNAPSHOT-for-hadoop-1.2.1-jar-with-dependencies.jar org.apache.giraph.GiraphRunner org.apache.giraph.examples.DumbClock -vif org.apache.giraph.io.formats.JsonLongDoubleFloatDoubleVertexInputFormat -vip /user/root/input/${input}.txt -vof org.apache.giraph.io.formats.IdWithValueTextOutputFormat -op /user/root/output/${example} -w ${worker} > $tmp 2>&1

      # stock market example
      # $HADOOP_HOME/bin/hadoop jar giraph-examples/target/giraph-examples-1.4.0-SNAPSHOT-for-hadoop-1.2.1-jar-with-dependencies.jar org.apache.giraph.GiraphRunner org.apache.giraph.examples.StockMarketExample -vif org.apache.giraph.examples.io.formats.JsonLongArrayDoubleArrayFloatDoubleVertexInputFormat -vip /user/root/input/${input}.txt -vof org.apache.giraph.io.formats.IdWithValueTextOutputFormat -op /user/root/output/${example} -w ${worker} > $tmp 2>&1
      # epidemic example
      # $HADOOP_HOME/bin/hadoop jar giraph-examples/target/giraph-examples-1.4.0-SNAPSHOT-for-hadoop-1.2.1-jar-with-dependencies.jar org.apache.giraph.GiraphRunner  org.apache.giraph.examples.EpidemicExampleClock -vif org.apache.giraph.examples.io.formats.JsonLongDoubleArrayFloatDoubleVertexInputFormat -vip /user/root/input/${input}.txt -vof org.apache.giraph.io.formats.IdWithValueTextOutputFormat -op /user/root/output/${example} -w ${worker} > $tmp 2>&1
      # game of life example (cfreq)
      # $HADOOP_HOME/bin/hadoop jar giraph-examples/target/giraph-examples-1.4.0-SNAPSHOT-for-hadoop-1.2.1-jar-with-dependencies.jar org.apache.giraph.GiraphRunner org.apache.giraph.examples.GameOfLifeExample -vif org.apache.giraph.examples.io.formats.JsonLongDoubleFloatDoubleArrayVertexInputFormat -vip /user/root/input/${input}.txt -vof org.apache.giraph.io.formats.IdWithValueTextOutputFormat -op /user/root/output/${example} -w ${worker} > $tmp 2>&1
      # game of life (regular)
      # $HADOOP_HOME/bin/hadoop jar giraph-examples/target/giraph-examples-1.4.0-SNAPSHOT-for-hadoop-1.2.1-jar-with-dependencies.jar org.apache.giraph.GiraphRunner org.apache.giraph.examples.GameOfLifeExample -vif org.apache.giraph.io.formats.JsonLongDoubleFloatDoubleVertexInputFormat -vip /user/root/input/${input}.txt -vof org.apache.giraph.io.formats.IdWithValueTextOutputFormat -op /user/root/output/${example} -w ${worker} > $tmp 2>&1
      
      sudo sh -c "/usr/bin/echo 3 > /proc/sys/vm/drop_caches"
    done
done
