// package org.apache.giraph.examples;

// import org.apache.giraph.graph.BasicComputation;
// import org.apache.giraph.conf.LongConfOption;
// import org.apache.giraph.edge.Edge;
// import org.apache.giraph.graph.Vertex;
// import org.apache.hadoop.io.Writable;
// import org.apache.hadoop.io.DoubleWritable;
// import org.apache.hadoop.io.IntWritable;
// import org.apache.hadoop.io.FloatWritable;
// import org.apache.hadoop.io.LongWritable;
// import org.apache.log4j.Logger;

// import java.io.IOException;
// import java.util.Random;

// public class WealthManagement {
//     double initWealth = 1000;
//     double interestRate = 0.01;
//     double bankDeposit = 500;
//     double cash = 500;
//     double shares = 0;
//     ConditionActionRule currentAction = null;
//     double currentWealth = initWealth;

//     public WealthManagement(double iWealth, double interest){
//         initWealth = iWealth;
//         interestRate = interest;
//         bankDeposit = initWealth * 0.5;
//         cash = initWealth - bankDeposit;
//         currentWealth = initWealth;
//     }

    
//     interface ConditionActionRule {
//         double getStrength();
//         int eval(double stockPrice, List<int> marketState);
//         void feedback(); 
//     }

//     abstract class Rule implements ConditionActionRule {
//         double strength = 0;
        
//         @override 
//         double getStrength() {
//             return strength;
//         }

//         @override 
//         void feedback() {
//             if (success){
//                 strength += 1;
//             } else {
//                 strength -= 1;
//             }
//         }
//     }

//     // 0: None, 1: true, 2: false
//     // Buy if the dividend increases and sell if the dividend decreases
//     class Rule1 extends Rule {
//         @override
//         int eval(double stockPrice, List<Int> marketState) {
//             if (marketState.get(0)==1 && stockPrice < cash){
//                 buyStock(stockPrice);
//                 return 1;
//             } else if (marketState(0)==2 && shares >= 1) {
//                 sellStock(stockPrice);
//                 return 2;
//             } else {
//                 return 0;
//             }
//         }
//     }

//     // Buy if 100-day average decreases and sell if 10-day average increases
//     // If both conditions are met, prioritize sell
//     class rule2(wealth: WealthManagement) extends conditionActionRule {
//         @override 
//         int eval(double stockPrice, List<int> marketState) {
//             if (marketState(1) == 1 && shares >= 1){
//                 sellStock(stockPrice);
//                 return 2;
//             } else if (stockPrice < cash && marketState(2) == 2){
//                 buyStock(stockPrice);
//                 return 1;
//             } else {
//                 return 0;
//             }
//         }
//     }

//     // // Buy if 10-day average decreases and sell if 10-day average increases
//     // class rule3(wealth: WealthManagement) extends conditionActionRule {
//     //     label = 3
//     //     override def eval(double stockPrice, List<int> marketState): Int ={
//     //         if (marketState(1) == 2 && stockPrice < cash){
//     //             buyStock(stockPrice)
//     //             1
//     //         } else if (marketState(1) == 1 && shares >= 1){
//     //             sellStock(stockPrice)
//     //             2
//     //         } else {
//     //             0
//     //         }
//     //     }
//     // }

//     // // Random buy and sell
//     // class rule4(wealth: WealthManagement) extends conditionActionRule {
//     //     label = 4
//     //     override def eval(double stockPrice, List<int> marketState): Int ={
//     //         if (Random.nextBoolean){
//     //             if (stockPrice < cash) {
//     //                 buyStock(stockPrice)
//     //                 1
//     //             } else {
//     //                 0
//     //             }
//     //         } else {
//     //             if (shares >= 1) {
//     //                 sellStock(stockPrice)
//     //                 2
//     //             } else {
//     //                 0
//     //             }
//     //         }
//     //     }
//     // }

//     // // Buy if 50-day average decreases and sell if 50-day average increases
//     // // If both conditions are met, prioritize sell
//     // class rule5(wealth: WealthManagement) extends conditionActionRule {
//     //     label = 5
//     //     override def eval(double stockPrice, List<int> marketState): Int ={
//     //         if (marketState(2) == 1 && shares >= 1){
//     //             sellStock(stockPrice)
//     //             2
//     //         } else if (marketState(2) == 2 && stockPrice < cash){
//     //             buyStock(stockPrice)
//     //             1
//     //         } else {
//     //             0
//     //         }
//     //     }
//     // }

//     List<ConditionActionRule> rules = new ArrayList<ConditionActionRule>();
//     rules.add(new Rule1());
//     rules.add(new Rule2());

//     // (new rule1(this), new rule2(this), new rule3(this), new rule4(this), new rule5(this))

//     public void buyStock(double stockPrice) {
//         shares += 1;
//         cash -= stockPrice;
//     }

//     public void sellStock(double stockPrice) {
//         if (shares > 1){
//             shares -= 1;
//             cash += stockPrice;
//         }
//     }

//     public double estimateWealth(double stockPrice) {
//         return stockPrice * shares + bankDeposit + cash;
//     }

//     public void addInterest() {
//         bankDeposit = bankDeposit * (1+interestRate);
//     }

//     public void addDividends(double dividendPerShare) {
//         if (dividendPerShare > 0) {
//             cash += shares * dividendPerShare; 
//         }
//     }

//     public int takeAction(double stockPrice, List<int> marketState) {
//         // Evaluate the previous rule
//         double updatedWealth = estimateWealth(stockPrice);
//         if (currentAction != null){
//             currentAction.feedback(updatedWealth > currentWealth);
//         }
//         // Find the next rule
//         ConditionActionRule nextAction = rules.sort((x, y) -> x.getStrength().compareTo(y.getStrength())).get(0);
//         // println("Next action is " + nextAction)
//         currentWealth = updatedWealth;
//         // println("Current wealth is " + currentWealth)
//         currentAction = nextAction;
//         return nextAction.eval(stockPrice, marketState);
//     }
// }