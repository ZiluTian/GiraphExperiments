#!/bin/bash

# mvn package -DskipTests 
# -rf :giraph-examples

example=stockMarket

inputs=( stockMarket_1000_giraph stockMarket_5000_giraph stockMarket_10000_giraph stockMarket_50000_giraph stockMarket_100000_giraph stockMarket_20000_giraph stockMarket_30000_giraph stockMarket_40000_giraph )
workers=( 2 3 4 )
# After the class name
# -ca giraph.logLevel=debug 

for repeat in {1..3}
  do
  for worker in "${workers[@]}"
  # for input in "${inputs[@]}"
    do
      tmp="log/${example}_scaleout_$repeat"
      # echo "Input file is $input" >> $tmp
      $HADOOP_HOME/bin/hadoop dfs -rmr /user/root/output/$example
      # stock market example
      # $HADOOP_HOME/bin/hadoop jar giraph-examples/target/giraph-examples-1.4.0-SNAPSHOT-for-hadoop-1.2.1-jar-with-dependencies.jar org.apache.giraph.GiraphRunner org.apache.giraph.examples.StockMarketExample -vif org.apache.giraph.examples.io.formats.JsonLongArrayDoubleArrayFloatDoubleVertexInputFormat -vip /user/root/input/${input}.txt -vof org.apache.giraph.io.formats.IdWithValueTextOutputFormat -op /user/root/output/${example} -w 1 >> $tmp 2>&1
      $HADOOP_HOME/bin/hadoop jar giraph-examples/target/giraph-examples-1.4.0-SNAPSHOT-for-hadoop-1.2.1-jar-with-dependencies.jar org.apache.giraph.GiraphRunner org.apache.giraph.examples.StockMarketExample -vif org.apache.giraph.examples.io.formats.JsonLongArrayDoubleArrayFloatDoubleVertexInputFormat -vip /user/root/input/stockMarket_${worker}0000_giraph.txt -vof org.apache.giraph.io.formats.IdWithValueTextOutputFormat -op /user/root/output/${example} -w ${worker} >> $tmp 2>&1
      sudo sh -c "/usr/bin/echo 3 > /proc/sys/vm/drop_caches"
  done
done
