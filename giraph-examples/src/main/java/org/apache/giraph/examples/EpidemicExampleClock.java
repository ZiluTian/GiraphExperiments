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
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Random;
import org.apache.giraph.examples.utils.DoubleArrayWritable;

import org.apache.commons.math3.distribution.GammaDistribution;

@Algorithm(
    name = "Epidemic example",
    description = "Simulate the epidemic"
)
public class EpidemicExampleClock extends BasicComputation<
    LongWritable, DoubleArrayWritable, FloatWritable, DoubleWritable> {
  private static final Logger LOG =
      Logger.getLogger(EpidemicExampleClock.class);

  public static final int cfreq = 1;
  
    // Extended SIR model
    public static final int Susceptible = 0;
    public static final int Exposed = 1;
    public static final int Infectious = 2;
    public static final int Hospitalized = 3;
    public static final int Recover = 4;
    public static final int Deceased = 5;
    // Encodings (vulnerability levels)
    public static final int Low = 0;
    public static final int High = 1;
    // Infectious parameter (gamma distribution)
    public static final double infectiousAlpha = 0.25;
    public static final double infectiousBeta = 1d;
    public static final double symptomaticSkew = 2d;
    public static final GammaDistribution gamma = new GammaDistribution(infectiousAlpha, infectiousBeta);

    public int stateDuration(int health) {
      Random r = new Random();
      int randDuration = (int)Math.round(3*r.nextGaussian());
      switch (health) {
          case Infectious:
              if (randDuration + 6 < 2) {
                return 2;
              } else {
                return randDuration + 6;
              }
          case Hospitalized: 
              if (randDuration + 7 < 2) {
                return 2;
              } else {
                return randDuration + 7;
              }
          case Exposed:
            if (randDuration + 5 < 3) {
                return 3;
              } else {
                return randDuration + 5;
              }
          default:
            return 5;
      }
    }

    public double infectiousness(int health, int symptomatic) {
      Random r = new Random();
        if (health == Infectious) {
            // double gd = gamma.sample();
            // double gd = r.nextDouble();
            double gd =new GammaDistribution(infectiousAlpha, infectiousBeta).sample();
            if (symptomatic==1){
                gd = gd * 2;
            }
            return gd;
        } else {
            return 0;
        }
    }

    public double eval(int vulnerability, int currentHealth, int projectedHealth) {
        switch (vulnerability) {
            case Low:
                if ((currentHealth == Exposed) && (projectedHealth == Infectious)){
                  return 0.6;
                } else if ((currentHealth == Infectious) && (projectedHealth == Hospitalized)){
                  return 0.1; 
                } else if ((currentHealth == Hospitalized) && (projectedHealth == Deceased)) {
                  return 0.1;
                } else {
                  return 0.01;
                }
            case High:
                if ((currentHealth == Exposed) && (projectedHealth == Infectious)){
                  return 0.9;
                } else if ((currentHealth == Infectious) && (projectedHealth == Hospitalized)){
                  return 0.4; 
                } else if ((currentHealth == Hospitalized) && (projectedHealth == Deceased)) {
                  return 0.5;
                } else {
                  return 0.05;
                }
            default:
              return 0.01;
        }
    }

    public int change(int health, int vulnerability) {
        Random r = new Random();
        double worse_prob = 0;
        switch (health) {
            case Susceptible: 
              return Exposed;
            case Exposed: 
                worse_prob = eval(vulnerability, Exposed, Infectious);
                if (r.nextDouble() < worse_prob) {
                    return Infectious;
                } else {
                    return Recover;
                }
            case Infectious: 
                worse_prob = eval(vulnerability, Infectious, Hospitalized);
                if (r.nextDouble() < worse_prob) {
                    return Hospitalized;
                } else {
                    return Recover;
                }
            case Hospitalized:
                worse_prob = eval(vulnerability, Hospitalized, Deceased);
                if (r.nextDouble() < worse_prob) {
                    return Deceased;
                } else {
                    return Recover;
                }
            default: 
              return health;
        }
    }

    // agent states, persist across runs
    // double age = 0;
    // double symptomatic = 0;
    // double health = 0;
    // double vulnerability = 0; 
    // double daysInfected = 0;

  @Override
  public void compute(
      Vertex<LongWritable, DoubleArrayWritable, FloatWritable> vertex,
      Iterable<DoubleWritable> messages) throws IOException {
      Random r = new Random();

      if (getSuperstep() == 0) {
        // Initialize agents
        // (age, symptomatic, health, vulnerability, daysInfected)
            DoubleWritable agentStates[] = new DoubleWritable[5];  
            double age = r.nextInt(90) + 10; 

            agentStates[0] = new DoubleWritable(age);
            
            if (r.nextBoolean()) {
              agentStates[1] = new DoubleWritable(0); 
            } else {
              agentStates[1] = new DoubleWritable(1);
            }
            // health
            if (r.nextInt(100) ==0) {
              // infectious, seed infectious patients 
              agentStates[2] = new DoubleWritable(2);
            } else { 
              agentStates[2] = new DoubleWritable(0);
            }
            // vulnerability
            if (age > 60) {
              agentStates[3] = new DoubleWritable(1); 
            } else {
              agentStates[3] = new DoubleWritable(0);
            }
            agentStates[4] = new DoubleWritable(0);

            if (LOG.isDebugEnabled()) {
            LOG.debug("Vertex " + vertex.getId() + " symptomatic " + agentStates[1] + " age " + age + " health " + agentStates[2] + "\n");
            }

            vertex.setValue(new DoubleArrayWritable(agentStates));
        }
                
        // clock
      if (vertex.getId().get() == 0) {
        if (getSuperstep() < 50){
            sendMessage(vertex.getId(), new DoubleWritable(-1));
            for (DoubleWritable m: messages){
                m.get();
            }

            for (Edge<LongWritable, FloatWritable> edge : vertex.getEdges()) {
                sendMessage(edge.getTargetVertexId(), new DoubleWritable(-1));
            }
        }
      } else { 
        DoubleWritable agentStates[] = (DoubleWritable [])vertex.getValue().get();
        int age = (int)((DoubleWritable)agentStates[0]).get();
        int symptomatic = (int)((DoubleWritable)agentStates[1]).get();
        int health = (int)((DoubleWritable)agentStates[2]).get();
        int vulnerability = (int)((DoubleWritable)agentStates[3]).get();
        int daysInfected = (int)((DoubleWritable)agentStates[4]).get();
           
        // tune comm. frequency  
        if (getSuperstep() < 50){
          // if (getSuperstep()%5 ==1) {
            int totalMsg = 0;
            for (DoubleWritable m : messages) {
                // Discard the message from clock
                if (m.get() != -1) {
                    if (health == 0) {
                        double personalRisk = m.get();
                        if (age > 60) {
                            personalRisk = personalRisk * 2;
                        }
                        if (personalRisk > 1) {
                            health = change(health, vulnerability);
                        }
                    }
                } 
                totalMsg += 1;
            }

            if (LOG.isDebugEnabled()) {
              LOG.debug("Vertex " + vertex.getId() + " is " + health + " in " + getSuperstep() + " receives " + totalMsg + " messages \n");
            }

            // if (LOG.isDebugEnabled()) {
            //   LOG.debug("Vertex " + vertex.getId() + " is " + health + " in " + getSuperstep() + " receives " + totalMsg + " messages \n");
            // }

            if (health != Deceased) {
              if (health == Infectious) {
                for (int i=0; i<cfreq; i++) {
                  for (Edge<LongWritable, FloatWritable> edge : vertex.getEdges()) {
                    double selfRisk = infectiousness(health, symptomatic);                    
                      sendMessage(edge.getTargetVertexId(), new DoubleWritable(selfRisk));
                  }
                }
              }

              if ((health != Susceptible) && (health != Recover)) {
                  if (daysInfected >= stateDuration(health)) {
                      health = change(health, vulnerability);
                      daysInfected = 0;
                  } else {
                      daysInfected = daysInfected + 1;
                  }
              }
            } 

            agentStates[0] = new DoubleWritable(age);
            agentStates[1] = new DoubleWritable(symptomatic);
            // health
            agentStates[2] = new DoubleWritable(health);
            // vulnerability
            agentStates[3] = new DoubleWritable(vulnerability);
            agentStates[4] = new DoubleWritable(daysInfected);
            vertex.setValue(new DoubleArrayWritable(agentStates));
        }  
      }
    vertex.voteToHalt();
  }
}