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
import org.apache.giraph.examples.utils.ArrayDoubleArrayWritable;

@Algorithm(
    name = "Stock market example",
    description = "Simulate the evolutionary stock market"
)
public class StockMarketExample extends BasicComputation<
    LongWritable, ArrayDoubleArrayWritable, FloatWritable, DoubleArrayWritable> {
  private static final Logger LOG =
      Logger.getLogger(StockMarketExample.class);

    // Stock states
    public final static double priceAdjustmentFactor = 0.01;
    public final static double interestRate = 0.0001;
    public final static int cfreq = 1; 
    private static final int stockLength = 210; 
    Random r = new Random();

    int update(int window, int timer, double lastAvg, double[] stock_timeseries) {
        // moving window    
        double calculated_avg = -1; 
        double sumPrice = 0; 

        if (timer > window) {
            int i = timer-window;
            while(i<timer){
                sumPrice = stock_timeseries[i] + sumPrice;  
                i += 1;
            }
            calculated_avg = sumPrice/window;
        } else {
            int i = 0;
            while (i<timer){
                sumPrice += stock_timeseries[i];
                i += 1;  
            }
            calculated_avg = sumPrice/timer;
        }

        if (lastAvg < calculated_avg){
          return 1;
        } else {
          return 0;
        }
    }
    
    double[] evalRule(int rule, double stockPrice, double[] marketState, double cash, double shares) {
        double dividendIncrease = marketState[0];
        double recent10AvgInc = marketState[1];
        double recent50AvgInc = marketState[2];  
        double recent100AvgInc = marketState[3];    

        double action = 0;
        double buy = 1;
        double sell = 2;

        switch (rule) {
            case 1:  
                if ((dividendIncrease == 1) && (stockPrice < cash)) {
                    action = buy;
                } else if ((dividendIncrease == 2) && (shares > 1)) {
                    action = sell;
                } 
                break;
            case 2:
                if ((recent10AvgInc == 1) && (shares >= 1)){
                    action = sell;
                } else if (stockPrice < cash && recent10AvgInc == 2){
                    action = buy;
                } 
                break;
            case 3:
                if ((recent50AvgInc == 1) && (shares >= 1)){
                    action = sell;
                } else if ((stockPrice < cash) && (recent50AvgInc == 2)){
                    action = buy;
                } 
            case 4:
                if ((recent100AvgInc == 1) && (shares >= 1)){
                    action = sell;
                } else if ((stockPrice < cash) && (recent100AvgInc == 2)){
                    action = buy;
                } 
            default: 
                Random r = new Random();
                if (r.nextBoolean()){
                    if (stockPrice < cash) {
                        action = buy;
                    } 
                } else {
                    if (shares > 1) {
                        action = sell;
                    }
                }
        }
        double ans[] = new double[3]; 
        if (action == buy) {
            ans[0] = buy; 
            ans[1] = cash - stockPrice;
            ans[2] = shares + 1; 
        } else if (action == sell) {
            ans[0] = sell; 
            ans[1] = cash + stockPrice;
            ans[2] = shares - 1; 
        } else {
            ans[0] = 0; 
            ans[1] = cash;
            ans[2] = shares; 
        }
        return ans; 
    }

  @Override
  public void compute(
      Vertex<LongWritable, ArrayDoubleArrayWritable, FloatWritable> vertex,
      Iterable<DoubleArrayWritable> messages) throws IOException {        
        if (getSuperstep() == 0) {
          DoubleArrayWritable agentStates[] = new DoubleArrayWritable[5];  

          // Initialize market
          // time series of stock price
          DoubleWritable stockTimeseries[] = new DoubleWritable[stockLength];
          for (int i = 0; i < stockLength; i++){
            stockTimeseries[i] = new DoubleWritable(0);
          }
          // Initially the price is 100
          stockTimeseries[0] = new DoubleWritable(100);
          // stockTimeseries[0] = new DoubleWritable(0);
          agentStates[0] = new DoubleArrayWritable(stockTimeseries);
          // last dividend
          DoubleWritable lastDividendTS[] = new DoubleWritable[3];
          lastDividendTS[0] = new DoubleWritable(0);
          // last average
          lastDividendTS[1] = new DoubleWritable(-1);
          // current price 
          lastDividendTS[2] = new DoubleWritable(100);
          agentStates[1] = new DoubleArrayWritable(lastDividendTS); 
          // timer
          DoubleWritable periodTS[] = new DoubleWritable[1];
          periodTS[0] = new DoubleWritable(0);
          agentStates[2] = new DoubleArrayWritable(periodTS); 

          // Initialize trader
          // wealth
          DoubleWritable wealthTS[] = new DoubleWritable[3];
          // cash
          wealthTS[0] = new DoubleWritable(1000);
          // shares 
          wealthTS[1] = new DoubleWritable(1);
          // estimated wealth 
          wealthTS[2] = new DoubleWritable(0);
          agentStates[3] = new DoubleArrayWritable(wealthTS); 
          // rules (5 rules and their respective strength, initially 0)
          DoubleWritable rules[] = new DoubleWritable[7];
          rules[0] = new DoubleWritable(0);
          rules[1] = new DoubleWritable(0); 
          rules[2] = new DoubleWritable(0);
          rules[3] = new DoubleWritable(0); 
          rules[4] = new DoubleWritable(0);
          // most recent rule 
          rules[5] = new DoubleWritable(0); 
          // next action 
          rules[6] = new DoubleWritable(0);
          agentStates[4] = new DoubleArrayWritable(rules);
          vertex.setValue(new ArrayDoubleArrayWritable(agentStates));
        } 
      
        DoubleArrayWritable agentStates[] = (DoubleArrayWritable[])vertex.getValue().get();
        int timer = 0;
        double currentPrice = 100;
        double lastDividend=0;
        double lastAvg=0;

        double buyOrders = 0;
        double sellOrders = 0;
        double dividendPerShare = 0;

        double dividendIncrease=0;
        double recent10AvgInc=currentPrice;
        double recent50AvgInc=currentPrice;
        double recent100AvgInc=currentPrice;

        double prices[] = new double[stockLength]; 
        double cash; 
        double shares; 
        double lastEstimatedWealth; 
        double estimatedWealth = 1000;
        int lastRule = 0;
        int receivedMsg = 0;

        for (int i = 0; i < stockLength; i++){
          prices[i] = ((DoubleWritable) agentStates[0].get()[i]).get();
        }

        lastDividend = ((DoubleWritable)agentStates[1].get()[0]).get();
        lastAvg = ((DoubleWritable)agentStates[1].get()[1]).get();
        currentPrice = ((DoubleWritable)agentStates[1].get()[2]).get();
        timer = (int)((DoubleWritable)agentStates[2].get()[0]).get();
        cash = ((DoubleWritable)agentStates[3].get()[0]).get();
        shares = ((DoubleWritable)agentStates[3].get()[1]).get();
        lastEstimatedWealth = ((DoubleWritable)agentStates[3].get()[2]).get();
        lastRule = (int)((DoubleWritable)agentStates[4].get()[5]).get();
        int totalMsg = 0;

        if (getSuperstep() < 200){
          timer += 1;

          if (vertex.getId().get() == 0) {  // market agent
            buyOrders = 0;
            sellOrders = 0;

            totalMsg = 0;
            // receive buy and sell info from traders 
            for (DoubleArrayWritable m : messages) {
              // if (m.get() != null) {
                double decoded_action = ((DoubleWritable) m.get()[0]).get();
                if (decoded_action == 1) {
                  buyOrders = buyOrders + 1;
                } else if (decoded_action == 2) {
                  sellOrders = sellOrders + 1;
                } 
                totalMsg += 1; 
            }

            if (LOG.isDebugEnabled()) {
              LOG.debug("Vertex " + vertex.getId() + " in " + getSuperstep() + " price " + currentPrice + " receives " + totalMsg + " messages \n");
            }
            // Update price based on buy-sell orders
            currentPrice = currentPrice*(1 + priceAdjustmentFactor*(buyOrders - sellOrders));
             // Update the stock time series with the new price
            prices[timer] = currentPrice;
            // update last average
            double tmpSum = 0;
            for (int i=0; i<timer; i++) {
              tmpSum += prices[i];
            }
            lastAvg = tmpSum / timer;

            // new dividend per share
            double newDividend = 0.1* r.nextGaussian();
            if (newDividend < 0) {
              newDividend = 0;
            } 

            if(newDividend == 0){
              dividendIncrease = 0;
            } else if (lastDividend > newDividend){
              dividendIncrease = 2;
            }

            // println("Market state is " + List(dividendIncrease, recent10AvgInc, recent50AvgInc))
            // Market state encodes both short-term and long-term information
            DoubleWritable marketState[] = new DoubleWritable[7]; 
            // update market info
            marketState[0] = new DoubleWritable(lastDividend);
            marketState[1] = new DoubleWritable(lastAvg);
            marketState[2] = new DoubleWritable(currentPrice);
            marketState[3] = new DoubleWritable(dividendIncrease);
            
            // Mid-term info: whether 10-day avg has increased
            recent10AvgInc = update(10, timer, lastAvg, prices);
            // Long-term info: whether 50-day avg has increased
            recent50AvgInc = update(50, timer, lastAvg, prices);
            recent100AvgInc = update(100, timer, lastAvg, prices);

            marketState[4] = new DoubleWritable(recent10AvgInc);
            marketState[5] = new DoubleWritable(recent50AvgInc);
            marketState[6] = new DoubleWritable(recent100AvgInc);

            for (Edge<LongWritable, FloatWritable> edge : vertex.getEdges()) {
              for (int i=0; i<cfreq; i++) {
                sendMessage(edge.getTargetVertexId(), new DoubleArrayWritable(marketState));
              }
            }
          } 
          else {  // trader agent
            int action = 1;
            receivedMsg = 0;

            cash = cash * (1 + interestRate);

            for (DoubleArrayWritable m : messages) {
              receivedMsg += 1;
              if (((DoubleWritable)m.get()[0]).get()!=-1) {
              double localPrice = ((DoubleWritable)m.get()[0]).get();
              double localDividend = ((DoubleWritable)m.get()[1]).get();
              dividendIncrease = ((DoubleWritable) m.get()[2]).get();
              recent10AvgInc = ((DoubleWritable)m.get()[3]).get();
              recent50AvgInc = ((DoubleWritable)m.get()[4]).get();
              recent100AvgInc = ((DoubleWritable)m.get()[5]).get();

              shares = shares * (1 + localDividend);
              
              estimatedWealth = cash + shares * localPrice; 

              double marketState[] = new double[4];
              marketState[0] = dividendIncrease;
              marketState[1] = recent10AvgInc;
              marketState[2] = recent50AvgInc;
              marketState[3] = recent100AvgInc;

              // if the total estimated wealth has increased, then the strength of the rule increases by 1
              double lastStrength = ((DoubleWritable)agentStates[4].get()[lastRule]).get();
              if (estimatedWealth > lastEstimatedWealth) {
                // update the strength of the last rule 
                agentStates[4].get()[lastRule] = new DoubleWritable(lastStrength + 1);
              } else {
                agentStates[4].get()[lastRule] = new DoubleWritable(lastStrength - 1);
              }

              double maxStrength = 0;
              int nextRule = 0; 
              // select the rule with the strongest strength
              for (int i=0; i< 5; i++) {
                double strength = ((DoubleWritable)agentStates[4].get()[i]).get();
                if (strength > maxStrength) {
                  maxStrength = strength; 
                  nextRule = i;
                } 
              }

              double evaluatedRules[] = evalRule(nextRule, currentPrice, marketState, cash, shares);
              agentStates[4].get()[6] = new DoubleWritable(evaluatedRules[0]);
              estimatedWealth = evaluatedRules[1];
              shares = evaluatedRules[2];
            } 
            if (receivedMsg > 0) {
              for (Edge<LongWritable, FloatWritable> edge : vertex.getEdges()) {
                // for (int i=0; i < cfreq; i++) {
                  DoubleWritable[] msg = new DoubleWritable[1]; 
                  msg[0] = new DoubleWritable(action);
                  sendMessage(edge.getTargetVertexId(), new DoubleArrayWritable(msg));
                // }
              }
            }
            }

            if (LOG.isDebugEnabled()) {
              LOG.debug("Vertex " + vertex.getId() + " in " + getSuperstep() + " received " + receivedMsg + " sent " + totalMsg + " messages \n");
            }
          }

          // time series of stock price
          DoubleWritable stockTimeseries[] = new DoubleWritable[stockLength];
          for (int i=0; i < stockLength; i++) {
              stockTimeseries[i] = new DoubleWritable(prices[i]);
          }
          agentStates[0] = new DoubleArrayWritable(stockTimeseries);
          // last dividend
          DoubleWritable lastDividendTS[] = new DoubleWritable[3];
          lastDividendTS[0] = new DoubleWritable(dividendPerShare);
          lastDividendTS[1] = new DoubleWritable(lastAvg);
          lastDividendTS[2] = new DoubleWritable(currentPrice);
          agentStates[1] = new DoubleArrayWritable(lastDividendTS); 

          // timer
          DoubleWritable periodTS[] = new DoubleWritable[1];
          periodTS[0] = new DoubleWritable(timer);
          agentStates[2] = new DoubleArrayWritable(periodTS); 

          // wealth
          DoubleWritable wealthTS[] = new DoubleWritable[3];
          wealthTS[0] = new DoubleWritable(cash);
          wealthTS[1] = new DoubleWritable(shares);
          wealthTS[2] = new DoubleWritable(estimatedWealth);
          agentStates[3] = new DoubleArrayWritable(wealthTS); 

          vertex.setValue(new ArrayDoubleArrayWritable(agentStates));
        }
      vertex.voteToHalt();
  }
}