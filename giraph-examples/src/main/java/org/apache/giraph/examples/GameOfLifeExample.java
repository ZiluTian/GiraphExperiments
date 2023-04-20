/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.giraph.examples;

import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.conf.LongConfOption;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Random;

@Algorithm(
    name = "Game of life example",
    description = "Simulate the game of life example"
)
public class GameOfLifeExample extends BasicComputation<
    LongWritable, DoubleWritable, FloatWritable, DoubleWritable> {
  private static final Logger LOG =
      Logger.getLogger(GameOfLifeExample.class);

  public static final int cfreq = 1;

  @Override
  public void compute(
      Vertex<LongWritable, DoubleWritable, FloatWritable> vertex,
      Iterable<DoubleWritable> messages) throws IOException {
      if (getSuperstep() == 0) {
        Random r = new Random();
        if (r.nextBoolean()){
          vertex.setValue(new DoubleWritable(1.0));
        } else {
          vertex.setValue(new DoubleWritable(0.0));
        }
      }
  
      // tune comm. frequency  
      if (getSuperstep() < 200){
        double alivedNeighbors = 0d;
        int receivedMessages = 0;
        for (DoubleWritable m : messages) {
          alivedNeighbors += m.get();
          receivedMessages += 1;
        }
        
        if (LOG.isDebugEnabled()) {
          LOG.debug("Vertex " + vertex.getId() + " received " + receivedMessages + " messages in " + getSuperstep());
        }

        if (vertex.getValue().get() == 1.0 && (alivedNeighbors > 3.0*cfreq || alivedNeighbors < 2.0*cfreq)){
          vertex.setValue(new DoubleWritable(0d));
        } else if (vertex.getValue().get() == 0d && (alivedNeighbors == 3.0*cfreq)){
          vertex.setValue(new DoubleWritable(1.0));
        } else {
          vertex.setValue(new DoubleWritable(vertex.getValue().get()));
        }

        for (Edge<LongWritable, FloatWritable> edge : vertex.getEdges()) {
          for (int i = 0; i<cfreq; i++) {
            double alive = vertex.getValue().get();
            sendMessage(edge.getTargetVertexId(), new DoubleWritable(alive));
          }
        }
      }     
    vertex.voteToHalt();
  }
}