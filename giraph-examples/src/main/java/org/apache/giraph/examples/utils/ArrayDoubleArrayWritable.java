package org.apache.giraph.examples.utils;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.DoubleWritable;

public class ArrayDoubleArrayWritable extends ArrayWritable{
  
  public ArrayDoubleArrayWritable() {
    super(DoubleArrayWritable.class);
  }

  public ArrayDoubleArrayWritable(DoubleArrayWritable[] values) {
    super(DoubleArrayWritable.class, values);
  }
}