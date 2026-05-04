import java.io.*;
import java.util.Scanner;

/**
 * An ABC neural network, a type of artificial neural network that consists of three activation layers: an input layer
 * (A), a hidden layer (B), and an output layer (C). We will be training this network for the AND, OR and XOR
 * gates, using gradient descent, improved with back propagation. This class will train and run the network based on a
 * sigmoid curve. This object is now entirely controlled by control files.
 *
 * public static void main(String[] args)
 * public void configurate(String fileName)
 * public void echo()
 * public void arrayAlloc()
 * public void arrayPop()
 * public double randomize(double low, double high)
 * public void pureRun()
 * public void runAllCases()
 * public void report()
 * public void investigateWeightArrays()
 * public void runForTrain()
 * public void runForRunAllCases()
 * public void train()
 * public double activationFunction(double x)
 * public double derivativeActivationFunction(double x)
 * public void saveWeights()
 * public void loadWeights()
 *
 * @author Varun Bhupathi
 * @date February 23, 2024
 *
 * Note that "Little Miss Echo Java Code" was used in main and requires the following crediting and documentation:
 *    Echo.java
 *    Little Miss Echo
 *    Print what is passed on the command line with each argument in a set of wakas.
 *    Example usage:
 *    java Echo hello world
 *    You passed <hello> <world>
 *    @author Dr. Eric R. Nelson, modified by Varun Bhupathi
 *    @date April 16, 2022
 */
public class ABCNeuralNetworkwBackProp
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
   public double[][] testCases;           // where the truth table for OR, AND, or XOR will be stored, including inputs
                                          // and outputs
   public double[][] testCaseOutputs;     // where the outputs for each testCase will be stored when runAllCases()
                                          // is called
   public double avgError;                // where the avgError will be stored when calculated
   public double[] w;                     // A variable used in the math for training
   public double[] smallPsi;              // A variable used in the math for training
   public double timeBeforeTraining;      // A double to store the time stamp before training
   public double timeAfterTraining;       // A double to store the time stamp after training
   public String controlFileFileName;     // Stores the file name of the control file being used
   public String truthTableFileName;      // Stores the file name of the truth table being used
   public String weightsFileName;         // The name of the weights file we will save the weights to
   public int iterations;                 // Stores the number of iterations that occurred during training
   public static final String DEFAULTCONTROLFILENAME = "AB1ControlFile";
                                          // The default control file name if none is provided
   
   /**
    * Actually constructs, trains and/or runs the Neural Network
    *
    * @param args argument necessary for the command line, where the control file name will be passed in
    */
   public static void main(String[] args)
   {
      ABCNeuralNetworkwBackProp net = new ABCNeuralNetworkwBackProp();
      
      if (args.length > 0)
      {
         net.configurate(args[0]);
         System.out.printf("You passed ");
         for (int i = 0; i < args.length; ++i) System.out.printf("<%s> ", args[i]);
         System.out.printf("\n");
      }
      else
      {
         net.configurate(DEFAULTCONTROLFILENAME);
         System.out.printf("Sorry, but you did not pass any arguments so using the default control file: " +
                           "\"" + DEFAULTCONTROLFILENAME + "\".\n");
      }
      
      net.echo();
      net.arrayAlloc();
      net.arrayPop();
      if (net.training)
         net.train();
      else
         net.pureRun();
      net.report();
      System.out.println("What Weights: " + net.whatWeights);
      net.saveWeights();
   } // public static void main(String[] args)
   
   /**
    * Sets the various instance variables with the given values, used to build the appropriate network
    * Reads said values from a file
    *
    * @param fileName the name of the control file
    */
   public void configurate(String fileName)
   {
      try
      {
         controlFileFileName = fileName;
         
         String valuePreCursor = " = ";
         int valuePreCursorLength = valuePreCursor.length();
         
         Scanner reader = new Scanner(new File(fileName));
         
         String line = reader.nextLine();
         lambda = Double.parseDouble(line.substring(line.indexOf(valuePreCursor) + valuePreCursorLength));
         line = reader.nextLine();
         maxIter = Integer.valueOf(line.substring(line.indexOf(valuePreCursor) + valuePreCursorLength));
         line = reader.nextLine();
         avgErrorCut = Double.parseDouble(line.substring(line.indexOf(valuePreCursor) + valuePreCursorLength));
         
         reader.nextLine();
         line = reader.nextLine();
         numInAct = Integer.valueOf(line.substring(line.indexOf(valuePreCursor) + valuePreCursorLength));
         line = reader.nextLine();
         numHidAct = Integer.valueOf(line.substring(line.indexOf(valuePreCursor) + valuePreCursorLength));
         line = reader.nextLine();
         numOutAct = Integer.valueOf(line.substring(line.indexOf(valuePreCursor) + valuePreCursorLength));
         line = reader.nextLine();
         numTestCases = Integer.valueOf(line.substring(line.indexOf(valuePreCursor) + valuePreCursorLength));
         
         reader.nextLine();
         line = reader.nextLine();
         lowRand = Double.parseDouble(line.substring(line.indexOf(valuePreCursor) + valuePreCursorLength));
         line = reader.nextLine();
         highRand = Double.parseDouble(line.substring(line.indexOf(valuePreCursor) + valuePreCursorLength));
         
         reader.nextLine();
         line = reader.nextLine();
         training = Boolean.valueOf(line.substring(line.indexOf(valuePreCursor) + valuePreCursorLength));
         line = reader.nextLine();
         whatWeights = Integer.valueOf(line.substring(line.indexOf(valuePreCursor) + valuePreCursorLength,
                                       line.indexOf(valuePreCursor) + 4));
         line = reader.nextLine();
         truthTableFileName = line.substring(line.indexOf(valuePreCursor) + valuePreCursorLength);
         line = reader.nextLine();
         weightsFileName = line.substring(line.indexOf(valuePreCursor) + valuePreCursorLength);
         
         reader.close();
      } // try
      catch (FileNotFoundException e)
      {
         throw new RuntimeException(e);
      }
   } // public void configurate(String fileName)
   
   /**
    * Echo (prints out) what was received by configurate, in a digestible and easy to read way
    */
   public void echo()
   {
      System.out.println("\nECHO:");
      System.out.println("Network Configuration = " + numInAct + "-" + numHidAct + "-" + numOutAct);
      System.out.println("Number of Test Cases = " + numTestCases);
      System.out.println("Saving Weights to " + "\""+ weightsFileName + "\" file");
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
         System.out.println("Populating for loaded weights from \"" + weightsFileName + "\" " + "file");
      else
         System.out.println("Populating for Pre-set weights");
      
      System.out.println("Control File: " + controlFileFileName);
      System.out.println("Truth Table File: " + truthTableFileName);
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
         hiddenTheta = new double[numHidAct];
         w = new double[numOutAct];
         smallPsi = new double[numOutAct];
      }
   } // public void arrayAlloc()
   
   /**
    * Populates weight arrays randomly, manually (using predetermined values), or loaded (from an external file)
    * depending on user input. Additionally, assigns values from the truth table file to the testCases array.
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
      try
      {
         Scanner reader = new Scanner(new File(truthTableFileName));
         
         for (int test = 0; test < numTestCases; test++)
         {
            for (int indexIn = 0; indexIn < numInAct; indexIn++)
               testCases[test][indexIn] = Double.valueOf(reader.nextLine());
            
            for (int indexOut = 0; indexOut < numOutAct; indexOut++)
               testCases[test][indexOut + numInAct] = Double.valueOf(reader.nextLine());
            reader.nextLine();
         }
      } // try
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
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
    * Just runs the network on all testCases
    */
   public void pureRun()
   {
      for (int test = 0; test < numTestCases; test++)
      {
         for (int in = 0; in < numInAct; in++)
            input[in] = testCases[test][in];
         
         runForRunAllCases();
         
         for (int i = 0; i < numOutAct; i++)
            testCaseOutputs[test][i] = output[i];
      }
   } // public void pureRun()
   
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
         
         for (int i = 0; i < numOutAct; i++)
         {
            error = testCases[test][i + numInAct] - output[i];
            totalError += error * error / 2.0;
            testCaseOutputs[test][i] = output[i];
         }
      } // for (int test = 0; test < numTestCases; test++)
      avgError = totalError / ((double) numTestCases);
   } // public void runAllCases()
   
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
         
         System.out.print("=");
         
         if (training)
         {
            for (int out = 0; out < numOutAct; out++)
               System.out.print(" " + testCases[test][out + numInAct]);
            System.out.print(":");
         }
         
         for (int out = 0; out < numOutAct; out++)
            System.out.print("   " + String.format("%.4f", testCaseOutputs[test][out]));
         
         System.out.println();
      } // for (int test = 0; test < numTestCases; test++)
      
      if(training)
      {
         System.out.println("Time to train: " + (timeAfterTraining - timeBeforeTraining) + " milliseconds");
         
         if (maxIter < iterations)
            System.out.println("\nCOMPLETED TRAINING BECAUSE EXCEED MAX ITERATIONS");
         if (avgError < avgErrorCut)
            System.out.println("\nCOMPLETED TRAINING BECAUSE AVERAGE ERROR IS LOW ENOUGH");
         
         System.out.println("Iterations: " + iterations);
         System.out.println("Average Error: " + avgError);
      } // if(training)
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
   public void runForTrain(int test)
   {
      double outputTheta;
      
      for (int j = 0; j < numHidAct; j++)
      {
         hiddenTheta[j] = 0.0;
         for (int k = 0; k < numInAct; k++)
            hiddenTheta[j] += input[k] * weightsInHid[k][j];
         hidden[j] = activationFunction(hiddenTheta[j]);
      }
      
      for (int i = 0; i < numOutAct; i++)
      {
         outputTheta = 0.0;
         for (int j = 0; j < numHidAct; j++)
            outputTheta += hidden[j] * weightsHidOut[j][i];
         output[i] = activationFunction(outputTheta);
         w[i] = testCases[test][i + numInAct] - output[i];
         smallPsi[i] = w[i] * derivativeActivationFunction(outputTheta);
      }
   } // public void runForTrain(int test)
   
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
         hidden[j] = activationFunction(theta);
      }
      
      for (int i = 0; i < numOutAct; i++)
      {
         theta = 0.0;
         for (int j = 0; j < numHidAct; j++)
            theta += hidden[j] * weightsHidOut[j][i];
         output[i] = activationFunction(theta);
      }
   } // public void runForRunAllCases()
   
   /**
    * Trains the network using gradient descent, improved via back propagation
    * Stops iteratively training when the number of max iterations is exceeded or when average error is sufficiently low,
    * which ever comes first
    * Additionally calls runAllCases(), in order to calculate average Error
    * Prints out why the network stopped training, the number of iterations and average Error.
    */
   public void train()
   {
      boolean done = false;                           // Initialize needed variables
      double psi;
      double omega;
      double changeInWeights;
      
      timeBeforeTraining = System.currentTimeMillis();
      while (!done)
      {
         for (int test = 0; test < numTestCases; test++)
         {
            for (int in = 0; in < numInAct; in++)     // Run Network
               input[in] = testCases[test][in];
            runForTrain(test);
            
            for (int j = 0; j < numHidAct; j++)
            {
               omega = 0.0;
               
               for (int i = 0; i < numOutAct; i++)
               {
                  omega += smallPsi[i] * weightsHidOut[j][i];
                  changeInWeights = lambda * hidden[j] * smallPsi[i];
                  weightsHidOut[j][i] += changeInWeights;
               }
               
               psi = omega * derivativeActivationFunction(hiddenTheta[j]);
               
               for (int k = 0; k < numInAct; k++)
               {
                  changeInWeights = lambda * input[k] * psi;
                  weightsInHid[k][j] += changeInWeights;
               }
            } // for (int j = 0; j < numHidAct; j++)
         } // for (int test = 0; test < 4; test++)
         timeAfterTraining = System.currentTimeMillis();
         
         runAllCases();
         
         iterations++;
         
         if (maxIter < iterations)                    // Determine if we should stop training
            done = true;
         else if (avgError < avgErrorCut)
            done = true;
      } // while (!areDone)
   } // public void train()
   
   /**
    * The activation function we will use for the network running and training
    * A basic sigmoid function
    * f(x) = 1/(1+e^-x)
    *
    * @param x the input for the sigmoid function
    * @return the result of f(x)
    */
   public double activationFunction(double x)
   {
      return (1.0/(1.0 + Math.exp(-x)));
   }
   
   /**
    * The derivative of the activation function we will use for the network training
    * The derivative of the sigmoid function
    *
    * @param x the input for the derivative of the sigmoid function
    * @return the result of f'(x)
    */
   public double derivativeActivationFunction(double x)
   {
      double sig = activationFunction(x);
      return (sig * (1.0 - sig));
   }
   
   /**
    * Saves the weights currently in weightsInHid and weightsHidOut in a file called "Weights"
    */
   public void saveWeights()
   {
      try
      {
         PrintWriter out = new PrintWriter(new FileWriter(weightsFileName), true);
         
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
      catch (IOException e)
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
         Scanner reader = new Scanner(new File(weightsFileName));
         reader.nextLine();
         
         if (Double.parseDouble(reader.nextLine()) != numInAct)
            throw new RuntimeException("The number of Input Activations, configured to \"Weights\" does not match the" +
                  " user input configuration");
         
         if (Double.parseDouble(reader.nextLine()) != numHidAct)
            throw new RuntimeException("The number of Hidden Activations, configured to \"Weights\" does not match the" +
                  " user input configuration");
         
         if (Double.parseDouble(reader.nextLine()) != numOutAct)
            throw new RuntimeException("The number of Output Activations, configured to \"Weights\" does not match the" +
                  " user input configuration");
         
         reader.nextLine();
         reader.nextLine();
         
         for (int k = 0; k < numInAct; k++)
         {
            for (int j = 0; j < numHidAct; j++)
               weightsInHid[k][j] = Double.parseDouble(reader.nextLine());
            reader.nextLine();
         }
         reader.nextLine();
         reader.nextLine();
         
         for (int j = 0; j < numHidAct; j++)
         {
            for (int i = 0; i < numOutAct; i++)
               weightsHidOut[j][i] = Double.parseDouble(reader.nextLine());
            reader.nextLine();
         }
         reader.close();
      } // try
      catch (FileNotFoundException e)
      {
         throw new RuntimeException(e);
      }
   } // public void loadWeights()
} // public class ABCNeuralNetworkwBackProp