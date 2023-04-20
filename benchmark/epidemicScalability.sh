#!/bin/bash

# mvn package -DskipTests 
# -rf :giraph-examples

example=ERM
workers=(2 3 4)
# inputs=( SBM_0.01_2_20000_giraph SBM_0.01_3_30000_giraph SBM_0.01_4_40000_giraph )
inputs=( SBM_0.01_5_1000_giraph SBM_0.01_5_5000_giraph SBM_0.01_5_10000_giraph SBM_0.01_5_50000_giraph SBM_0.01_5_100000_giraph )

# After the class name
# -ca giraph.logLevel=debug 

for repeat in {1..3}
  do
  # for worker in "${workers[@]}"
  for input in "${inputs[@]}"
    do
      tmp="log/${example}_scaleout_$repeat"
      $HADOOP_HOME/bin/hadoop dfs -rmr /user/root/output/$example
      $HADOOP_HOME/bin/hadoop jar giraph-examples/target/giraph-examples-1.4.0-SNAPSHOT-for-hadoop-1.2.1-jar-with-dependencies.jar org.apache.giraph.GiraphRunner  org.apache.giraph.examples.EpidemicExampleClock -vif org.apache.giraph.examples.io.formats.JsonLongDoubleArrayFloatDoubleVertexInputFormat -vip /user/root/input/${input}.txt -vof org.apache.giraph.io.formats.IdWithValueTextOutputFormat -op /user/root/output/${example} -w 1 >> $tmp 2>&1
      # $HADOOP_HOME/bin/hadoop jar giraph-examples/target/giraph-examples-1.4.0-SNAPSHOT-for-hadoop-1.2.1-jar-with-dependencies.jar org.apache.giraph.GiraphRunner  org.apache.giraph.examples.EpidemicExampleClock -vif org.apache.giraph.examples.io.formats.JsonLongDoubleArrayFloatDoubleVertexInputFormat -vip /user/root/input/ERM_0.01_${worker}0000_giraph.txt -vof org.apache.giraph.io.formats.IdWithValueTextOutputFormat -op /user/root/output/${example} -w ${worker} >> $tmp 2>&1
      sudo sh -c "/usr/bin/echo 3 > /proc/sys/vm/drop_caches"
  done
done
