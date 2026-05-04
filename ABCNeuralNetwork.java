import java.io.*;
import java.util.Scanner;

/**
 * An ABC neural network, a type of artificial neural network that consists of three activation layers: an input layer
 * (A), a hidden layer (B), and an output layer (C). We will be training this network for the AND, OR and XOR
 * gates, using gradient descent. This class will train and run the network based on a sigmoid curve.
 *
 * public static void main(String[] args)
 * public void configurate()
 * public void echo()
 * public void arrayAlloc()
 * public void arrayPop()
 * public double randomize(double low, double high)
 * public void runAllCases()
 * public void report()
 * public void runForTrain()
 * public void runForRunAllCases()
 * public void train()
 * public double sigmoid(double x)
 * public double derivativeSigmoid(double x)
 * public void saveWeights()
 * public void loadWeights()
 *
 * @author Varun Bhupathi
 * @date February 23, 2024
 */
public class ABCNeuralNetwork
{
   public double lambda;                  // a learning factor that controls the speed of descent
   public int maxIter;                    // during training, once the number of iterations exceeds this value, we will
                                          // stop training
   public double avgErrorCut;             // during training, once average error is below this value, we will stop
                                          // training
   public int numInAct;                   // the number of input activations
   public int numHidAct;                  // the number of hidden activations
   public int numOutAct;                  // the number of output activations
   
   public int numTestCases;               // the number of Test Cases, the user wants. The network will train with all
                                          // of these cases
   public double lowRand;                 // the lowest value a random weight can be
   public double highRand;                // the exclusive upper bound a random weight can be
   public boolean training;               // if true we will train the model and then run it, if false we will
                                          // simply run it with random, predetermined or loaded weights
   public int whatWeights;                // if 0 we will start with randomized weights, if 1 we will start with
                                          // loaded weights from the file "Weights", if 2 we will start with
                                          // pre-set weights
   public double[][] weightsInHid;        // where the weights for the input to hidden layers will be stored
   public double[][] weightsHidOut;       // where the weights for the hidden to output layers will be stored
   public double[] input;                 // where the input activations are stored
   public double[] hidden;                // where the hidden activations are stored with the sigmoid curve applied
   public double[] hiddenTheta;           // where the hidden activations are stored without the sigmoid curve applied
                                          // only used in training
   public double [] output;               // where the output activation will be stored with the sigmoid curve applied
   public double [] outputTheta;          // where the output activation will be stored without the sigmoid curve applied
                                          // only used in training
   public double[][] testCases;           // where the truth table for OR, AND, or XOR will be stored, including inputs
                                          // and outputs
   public double[][] testCaseOutputs;     // where the outputs for each testCase will be stored when runAllCases()
                                          // is called
   public double avgError;                // where the avgError will be stored when calculated
   public double[][] changeWeightsInHid;  // where the change in weights during training for the input to hidden layer
                                          // will be stored. Only used in training
   public double[][] changeWeightsHidOut; // where the change in weights during training for the hidden to output layer
                                          // will be stored. Only used in training
   public double[] w;                     // A variable used in the math for training
   public double[] smallPsi;              // A variable used in the math for training
   
   /**
    * Actually constructs, trains and/or runs the Neural Network
    *
    * @param args argument necessary for the command line
    */
   public static void main(String[] args)
   {
      ABCNeuralNetwork net = new ABCNeuralNetwork();
      net.configurate();
      net.echo();
      net.arrayAlloc();
      net.arrayPop();
      if (net.training)
         net.train();
      else
         net.runAllCases();
      net.investigateWeightArrays();
      net.report();
      net.saveWeights();
   } // public static void main(String[] args)
   
   /**
    * Sets the various instance variables with the given values, used to build the appropriate network
    * The user interface for network parameters
    */
   public void configurate()
   {
      lambda = 0.3;
      maxIter = 100000;
      avgErrorCut = .0002;
      
      numInAct = 2;
      numHidAct = 5;
      numOutAct = 3;
      numTestCases = 4;
      
      lowRand = 0.1;
      highRand = 1.5;
      
      training = true;
      whatWeights = 0; // 0 = Random Weights; 1 = Load Weights; 2 = Pre-set Weights
   } // public void configurate()
   
   /**
    * Echo (prints out) what was received by configurate, in a digestible and easy to read way
    */
   public void echo()
   {
      System.out.println("\nECHO:");
      System.out.println("Network Configuration = " + numInAct + "-" + numHidAct + "-" + numOutAct);
      System.out.println("Is Training = " + training);
      if (training)
      {
         System.out.println("   lambda = " + lambda);
         System.out.println("   maxIter = " + maxIter);
         System.out.println("   avgErrorCut = " + avgErrorCut);
         System.out.println("   random number range = " + lowRand + " through " + highRand);
      }
      
      if (whatWeights == 0)
         System.out.println("Populating for random weights");
      else if (whatWeights == 1)
         System.out.println("Populating for loaded weights");
      else
         System.out.println("Populating for Pre-set weights");
   } // public void echo()
   
   /**
    * Allocates space for all major arrays (arrays included as instance variables)
    * Arrays used only for training will be allocated only if training
    */
   public void arrayAlloc()
   {
      weightsInHid = new double[numInAct][numHidAct];
      weightsHidOut = new double[numHidAct][numOutAct];
      input = new double[numInAct];
      hidden = new double[numHidAct];
      output = new double[numOutAct];
      testCases = new double[numTestCases][numInAct+numOutAct];
      testCaseOutputs = new double[numTestCases][numOutAct];
      
      if (training)
      {
         changeWeightsInHid = new double[numInAct][numHidAct];
         changeWeightsHidOut = new double[numHidAct][numOutAct];
         hiddenTheta = new double[numHidAct];
         outputTheta = new double[numOutAct];
         w = new double[numOutAct];
         smallPsi = new double[numOutAct];
      }
   } // public void arrayAlloc()
   
   /**
    * Populates weight arrays randomly, manually (using predetermined values), or loaded (from an external file)
    * depending on user input. Additionally, assigns values to the testCases array.
    */
   public void arrayPop()
   {
      if (whatWeights == 0)
      {
         for (int indexIn = 0; indexIn < numInAct; indexIn++)
            for (int indexHid = 0; indexHid < numHidAct; indexHid++)
               weightsInHid[indexIn][indexHid] = randomize(lowRand, highRand);
         
         for (int indexHid = 0; indexHid < numHidAct; indexHid++)
            for (int indexOut = 0; indexOut < numOutAct; indexOut++)
               weightsHidOut[indexHid][indexOut] = randomize(lowRand, highRand);
      }
      else if (whatWeights == 1)
         loadWeights();
      else
      {
         weightsInHid[0][0] = 0.1;
         weightsInHid[1][0] = 0.2;
         weightsInHid[0][1] = 0.3;
         weightsInHid[1][1] = 0.4;
         
         weightsHidOut[0][0] = 0.5;
         weightsHidOut[1][0] = 0.6;
      }
      
      testCases[0][0] = 0.0;
      testCases[0][1] = 0.0;
      testCases[1][0] = 0.0;
      testCases[1][1] = 1.0;
      testCases[2][0] = 1.0;
      testCases[2][1] = 0.0;
      testCases[3][0] = 1.0;
      testCases[3][1] = 1.0;
      
      // testCases[0][2] = 0.0; // XOR for A-B-1
      // testCases[1][2] = 1.0;
      // testCases[2][2] = 1.0;
      // testCases[3][2] = 0.0;
      
      testCases[0][2] = 0.0;    // A-B-C truth table
      testCases[0][3] = 0.0;
      testCases[0][4] = 0.0;
      testCases[1][2] = 0.0;
      testCases[1][3] = 1.0;
      testCases[1][4] = 1.0;
      testCases[2][2] = 0.0;
      testCases[2][3] = 1.0;
      testCases[2][4] = 1.0;
      testCases[3][2] = 1.0;
      testCases[3][3] = 1.0;
      testCases[3][4] = 0.0;
   } // public void arrayPop()
   
   /**
    * A helper method that outputs a random double in between an inclusive low and an exclusive high
    *
    * @param low the inclusive lower bound for the range
    * @param high the exclusive higher bound for the range
    * @return a random number between low (inclusive) and high (exclusive)
    */
   public double randomize(double low, double high)
   {
      return low + Math.random() * (high - low);
   }
   
   /**
    * Tests all testCases and stores their outputs in testCaseOutputs
    * Calculates average error as well and stores it in avgError
    */
   public void runAllCases()
   {
      double totalError = 0.0;
      double error;
      for (int test = 0; test < numTestCases; test++)
      {
         for (int in = 0; in < numInAct; in++)
            input[in] = testCases[test][in];
         
         runForRunAllCases();
         
         for(int i = 0; i < numOutAct; i++)
         {
            error = testCases[test][i + numInAct] - output[i];
            totalError += error * error / 2.0;
            testCaseOutputs[test][i] = output[i];
         }
      }
      avgError = totalError/numTestCases;
   } // public void run()
   
   /**
    * Reports/Prints everything in testCaseOutputs and the average Error for the current model
    */
   public void report()
   {
      System.out.println("\nReporting Data:");
      for (int test = 0; test < numTestCases; test++)
      {
         for (int in = 0; in < numInAct; in++)
            System.out.print(testCases[test][in] + ",");
         
         System.out.println("=");
         
         for (int out = 0; out < numOutAct; out++)
            System.out.println("   " + testCases[test][out + numInAct] + ": " + testCaseOutputs[test][out]);
      }
      System.out.println("Average Error: " + avgError);
   } // public void report()
   
   /**
    * Executes a run with the weights and input activations given within their corresponding instance variables
    * In other words, only uses the values already stored in the input layer
    * Stores the final values (with sigmoid) of each hidden activation inside their respective index in the hidden[] array
    * Stores the final value (with sigmoid) of the output activation in output
    * Stores the summation (without sigmoid) of each hidden activation inside their respective index in the hiddenTheta[]
    * array
    * Stores the summation (without sigmoid) of the output activation in outputTheta
    * Acts as a helper method for train()
    */
   public void runForTrain()
   {
      for (int j = 0; j < hiddenTheta.length; j++)
         hiddenTheta[j] = 0.0;
      
      for (int i = 0; i < numOutAct; i++)
         outputTheta[i] = 0.0;
      
      for (int k = 0; k < numInAct; k++)
         for (int j = 0; j < numHidAct; j++)
            hiddenTheta[j] += input[k] * weightsInHid[k][j];
      
      for (int j = 0; j < numHidAct; j++)
         hidden[j] = sigmoid(hiddenTheta[j]);
      
      for (int j = 0; j < numHidAct; j++)
         for (int i = 0; i < numOutAct; i++)
            outputTheta[i] += hidden[j] * weightsHidOut[j][i];
      
      for (int i = 0; i < numOutAct; i++)
         output[i] = sigmoid(outputTheta[i]);
   } // public void runForTrain()
   
   /**
    * Executes a run with the weights and input activations given within their corresponding instance variables
    * In other words, only uses the values already stored in the input layer
    * Stores the final values (with sigmoid) of each hidden activation inside their respective index in the hidden[] array
    * Stores the final value (with sigmoid) of the output activation in output
    * Acts as a helper method for runAllCases()
    */
   public void runForRunAllCases()
   {
      double theta;
      
      for (int j = 0; j < numHidAct; j++)
      {
         theta = 0.0;
         for (int k = 0; k < numInAct; k++)
            theta += input[k] * weightsInHid[k][j];
         hidden[j] = sigmoid(theta);
      }
      
      for (int i = 0; i < numOutAct; i++)
      {
         theta = 0.0;
         for (int j = 0; j < numHidAct; j++)
            theta += hidden[j] * weightsHidOut[j][i];
         output[i] = sigmoid(theta);
      }
   } // public void runForRunAllCases()
   
   /**
    * Prints the arrays weightsInHid, weightsHidOut, and testCases
    * Used for debugging and not used in the final model
    */
   public void investigateWeightArrays()
   {
      System.out.println("\nWeights from Input Layer to Hidden Layer:");
      for (int k = 0; k< numInAct; k++)
      {
         for (int j = 0; j < numHidAct; j++)
            System.out.print("" + k + j + "  " + weightsInHid[k][j] + ", ");
         System.out.println();
      }
      
      System.out.println("\nWeights from Hidden Layer to Output Layer:");
      for (int j = 0; j< numHidAct; j++)
      {
         for (int i = 0; i < numOutAct; i++)
            System.out.print("" + i + j + "  " + weightsHidOut[j][i] + ",\t");
         System.out.println();
      }
      
      for (int i = 0; i < 4; i++)
      {
         for (int j = 0; j < testCases[0].length; j++)
            System.out.print(testCases[i][j] + ",");
         System.out.println();
      }
   } // public void investigateWeightArrays()
   
   /**
    * Trains the network using gradient descent
    * Stops iteratively training when the number of max iterations is exceeded or when average error is sufficiently low,
    * which ever comes first
    * Additionally calls runAllCases(), in order to calculate average Error
    * Prints out why the network stopped training, the number of iterations and average Error.
    */
   public void train()
   {
      boolean done = false;                        // Initialize needed variables
      int iterations = 0;
      double psi;
      double partialDerivative;
      
      while (!done)
      {
         for (int test = 0; test < numTestCases; test++)
         {
            for (int in = 0; in < numInAct; in++)     // Run Network
               input[in] = testCases[test][in];
            runForTrain();
            
            for(int i = 0; i < numOutAct; i++)
            {
               w[i] = testCases[test][i + numInAct] - output[i];
               smallPsi[i] = w[i] * derivativeSigmoid(outputTheta[i]);
            }
            
            for (int j = 0; j < numHidAct; j++)        // Calculate Change in Weights
            {
               double omega = 0.0;
               for (int I = 0; I < numOutAct; I++)
                  omega += smallPsi[I] * weightsHidOut[j][I];
               
               psi = omega * derivativeSigmoid(hiddenTheta[j]);
               
               for (int k = 0; k < numInAct; k++)
               {
                  partialDerivative = -input[k] * psi;
                  changeWeightsInHid[k][j] = -lambda * partialDerivative;
               }
            }
            
            for (int j = 0; j < numHidAct; j++)
               for (int i = 0; i < numOutAct; i++)
               {
                  partialDerivative = -hidden[j] * smallPsi[i];
                  changeWeightsHidOut[j][i] = -lambda * partialDerivative;
               }
            
            for (int k = 0; k < numInAct; k++)        // Apply Change in Weights
               for (int j = 0; j < numHidAct; j++)
                  weightsInHid[k][j] += changeWeightsInHid[k][j];
            
            for (int j = 0; j < numHidAct; j++)
               for (int i = 0; i < numOutAct; i++)
                  weightsHidOut[j][i] += changeWeightsHidOut[j][i];
         } // for (int test = 0; test < 4; test++)
         iterations++;
         
         runAllCases();
         
         if (maxIter < iterations)                    // Determine if we should stop training
         {
            done = true;
            System.out.println("\nCOMPLETED TRAINING BECAUSE EXCEED MAX ITERATIONS");
            System.out.println("Iterations: " + iterations);
            System.out.println("Average Error: " + avgError);
         }
         else if (avgError < avgErrorCut)
         {
            done = true;
            System.out.println("\nCOMPLETED TRAINING BECAUSE AVERAGE ERROR IS LOW ENOUGH");
            System.out.println("Iterations: " + iterations);
            System.out.println("Average Error: " + avgError);
         }
      } // while (!areDone)
   } // public void train()
   
   /**
    * A basic sigmoid function
    * f(x) = 1/(1+e^-x)
    *
    * @param x the input for the sigmoid function
    * @return the result of f(x)
    */
   public double sigmoid(double x)
   {
      return 1.0/(1.0 + Math.exp(-x));
   }
   
   /**
    * The derivative of the sigmoid function
    *
    * @param x the input for the derivative of the sigmoid function
    * @return the result of f'(x)
    */
   public double derivativeSigmoid(double x)
   {
      double sig = sigmoid(x);
      return sig * (1.0 - sig);
   }
   
   /**
    * Saves the weights currently in weightsInHid and weightsHidOut in a file called "Weights"
    */
   public void saveWeights()
   {
      try
      {
         PrintWriter out = new PrintWriter(new FileWriter("Weights"), true);
         
         out.println("Network: ");
         out.println(numInAct);
         out.println(numHidAct);
         out.println(numOutAct);
         
         out.print("\nWeights For the Input Layer to Hidden Layer:\n");
         for (int k = 0; k < numInAct; k++)
         {
            for (int j = 0; j < numHidAct; j++)
               out.println(weightsInHid[k][j]);
            out.println();
         }
         
         out.print("\nWeights For the Hidden Layer to Output Layer:\n");
         for (int j = 0; j < numHidAct; j++)
         {
            for (int i = 0; i < numOutAct; i++)
               out.println(weightsHidOut[j][i]);
            out.println();
         }
         
         out.close();
      } // try
      catch(IOException e)
      {
         throw new RuntimeException(e);
      }
   } // public void saveWeights()
   
   /**
    * Loads the weights from the file "Weights" into weightsInHid and weightsHidOut
    * First, checks if the weights used in loading are for the same network as the user desires
    */
   public void loadWeights()
   {
      try
      {
         Scanner in = new Scanner(new File("Weights"));
         in.nextLine();
         
         if (Double.parseDouble(in.nextLine()) != numInAct)
            throw new RuntimeException("The number of Input Activations, configured to \"Weights\" does not match the" +
                  " user input configuration");
         
         if (Double.parseDouble(in.nextLine()) != numHidAct)
            throw new RuntimeException("The number of Hidden Activations, configured to \"Weights\" does not match the" +
                  " user input configuration");
         
         if (Double.parseDouble(in.nextLine()) != numOutAct)
            throw new RuntimeException("The number of Output Activations, configured to \"Weights\" does not match the" +
                  " user input configuration");
         
         in.nextLine();
         in.nextLine();
         
         for (int k = 0; k < numInAct; k++)
         {
            for (int j = 0; j < numHidAct; j++)
               weightsInHid[k][j] = Double.parseDouble(in.nextLine());
            in.nextLine();
         }
         in.nextLine();
         in.nextLine();
         
         for (int j = 0; j < numHidAct; j++)
         {
            for (int i = 0; i < numOutAct; i++)
               weightsHidOut[j][i] = Double.parseDouble(in.nextLine());
            in.nextLine();
         }
         in.close();
      } // try
      catch(FileNotFoundException e)
      {
         throw new RuntimeException(e);
      }
   } // public void loadWeights()
} // public class ABCNeuralNetwork