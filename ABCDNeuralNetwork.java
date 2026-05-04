import java.io.*;
import java.util.*;

/**
 * An ABCD neural network, a type of artificial neural network that consists of four activation layers: an input layer
 * (A), 2 hidden layers (B and C), and an output layer (D). Using gradient descent, improved with back propagation, this
 * class will train and run the network based on a sigmoid curve. This object is now entirely controlled by control files.
 *
 * public static void main(String[] args)
 * public void configurate(String fileName)
 * public void echo()
 * public void arrayAlloc()
 * public void arrayPop()
 * public double randomize(double low, double high)
 * public void report()
 * public void runForTrain(int test)
 * public void pureRun()
 * public void runAllCases()
 * public void train()
 * public double activationFunction(double x)
 * public double sigmoid(double x)
 * public double derivativeActivationFunction(double x)
 * public double derivativeSigmoid(double x)
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
public class ABCDNeuralNetwork
{
   public double lambda;                  // a learning factor that controls the speed of descent
   public int maxIter;                    // during training, once the number of iterations exceeds this value, we will
                                          // stop training
   public double avgErrorCut;             // during training, once average error is below this value, we will stop
                                          // training
   public int[] numLayerLength;           // the number of activations in each layer
   public int numInAct;                   // the number of input activations
   public int numHidAct1 = 5;                 // the number of hidden activations in the first hidden layer
   public int numHidAct2 = 10;                 // the number of hidden activations in the second hidden layer
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
   public double[][][] weights;           // where the weights for all the layers will be stored
   public double[][] activations;         // where the hidden activations are stored with the sigmoid curve applied
   public double[][] hiddenTheta;         // where the hidden activations are stored without the sigmoid curve applied
                                          // only used in training
   public double[][] testCases;           // where the truth table for OR, AND, or XOR will be stored, including inputs
                                          // and outputs
   public double[][] testCaseOutputs;     // where the outputs for each testCase will be stored when runAllCases()
                                          // is called
   public double avgError;                // where the avgError will be stored when calculated
   public double[][] psi;                 // A variable used in the math for training
   public double littleOmega;             // A variable used in the math for training
   public double timeBeforeTraining;      // A double to store the time stamp before training
   public double timeAfterTraining;       // A double to store the time stamp after training
   public String controlFileFileName;     // Stores the file name of the control file being used
   public String truthTableFileName;      // Stores the file name of the truth table being used if training
   public String inputsFileName;          // Stores the file name of the inputs being used if just running
   public String weightsFileName;         // The name of the weights file we will save the weights to
   public int iterations;                 // Stores the number of iterations that occurred during training
   public int numLayers; // the number of layers in this network is predetermined to be 4
   
   public String networkType;
   public static final int INPUTLAYERINDEX = 0;
                                          // What position the input layer is in, in the order of layers
   public static final int FIRSTHIDDENLAYERINDEX = 1;
                                          // What position the first hidden layer is in, in the order of layers
   public static final int SECONDHIDDENLAYERINDEX = 2;
                                          // What position the second hidden layer is in, in the order of layers
   public static final int OUTPUTLAYERINDEX = 3;
                                          // What position the output layer is in, in the order of layers
   public static final String DEFAULTCONTROLFILENAME = "NLayerControlFile";
                                          // The default control file name if none is provided
   
   /**
    * Actually constructs, trains and/or runs the Neural Network
    *
    * @param args argument necessary for the command line, where the control file name will be passed in
    */
   public static void main(String[] args)
   {
      ABCDNeuralNetwork net = new ABCDNeuralNetwork();
      
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
      net.runAllCases();
      net.report();
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
         numTestCases = Integer.parseInt(line.substring(line.indexOf(valuePreCursor) + valuePreCursorLength));
         line = reader.nextLine();
         inputsFileName = line.substring(line.indexOf(valuePreCursor) + valuePreCursorLength);
         line = reader.nextLine();
         weightsFileName = line.substring(line.indexOf(valuePreCursor) + valuePreCursorLength);
         
         reader.nextLine();
         line = reader.nextLine();
         numLayers = Integer.parseInt(line.substring(line.indexOf(valuePreCursor) + valuePreCursorLength));
         line = reader.nextLine();
         networkType = line.substring(line.indexOf(valuePreCursor) + valuePreCursorLength);
         numInAct = Integer.parseInt(networkType.substring(0,networkType.indexOf("-")));
         numOutAct = Integer.parseInt(networkType.substring(networkType.lastIndexOf("-")+1));
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
      System.out.println("Network Configuration = " + networkType);
      System.out.println("Saving/Loading weights to " + "\""+ weightsFileName + "\" file");
      System.out.println("What Weights = " + whatWeights);
      System.out.println("Is Training = " + training);
      if (training)
      {
         System.out.println("   Number of Test Cases = " + numTestCases);
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
      System.out.println("Inputs File: " + inputsFileName);
   } // public void echo()
   
   /**
    * Allocates space for all major arrays (arrays included as instance variables)
    * Arrays used only for training will be allocated only if training
    */
   public void arrayAlloc()
   {
      int largestLayerLength = findLargestLayerLength();
      weights = new double[numLayers -1][largestLayerLength][largestLayerLength];
      activations = new double[numLayers][largestLayerLength];
      testCaseOutputs = new double[numTestCases][numOutAct];
      numLayerLength = new int[numLayers];
      
      if (training)
      {
         hiddenTheta = new double[numLayers][largestLayerLength];
         psi = new double[numLayers][largestLayerLength];
         testCases = new double[numTestCases][numInAct+numOutAct];
      }
      else
         testCases = new double[numTestCases][numInAct];
   } // public void arrayAlloc()
   
   // Helper for arrayAlloc
   public int findLargestLayerLength()
   {
      String[] net = networkType.split("-");                                  // is this ok?????????
      int largest = 0;
      
      for (int i = 0; i < net.length; i++)
         largest = Math.max(largest, Integer.parseInt(net[i]));
      
      return largest;
   }
   
   /**
    * Populates weight arrays randomly, manually (using predetermined values), or loaded (from an external file)
    * depending on user input. Additionally, assigns values from the truth table file to the testCases array.
    */
   public void arrayPop()
   {
      int[] numLayerLength = new int[4];
      numLayerLength[0] = 2;
      numLayerLength[1] = 5;
      numLayerLength[2] = 10;
      numLayerLength[3] = 3;
      
      if (whatWeights == 0)
      {
         for (int n = 0; n < numLayers -1; n++)
            for (int j = 0; j < numLayerLength[n]; j++)
               for (int i = 0; i < numLayerLength[n+1]; i++)
               {
                  System.out.println("" + n + j + i);
                  weights[n][j][i] = randomize(lowRand, highRand);
               }
      } // if (whatWeights == 0)
      else if (whatWeights == 1)
         loadWeights();
      else
      {
         weights[0][0][0] = 0.1;
         weights[0][1][0] = 0.2;
         weights[0][0][1] = 0.3;
         weights[0][1][1] = 0.4;
         
         weights[1][0][0] = 0.5;
         weights[1][1][0] = 0.6;
         
         weights[2][0][0] = 0.7;
      }
      try
      {
         if (training)
         {
            String[] net = networkType.split("-");                            // This OK????????
            
            for (int layerIndex = 0; layerIndex < numLayers; layerIndex++)
            {
               //numLayerLength[layerIndex] = Integer.parseInt(net[layerIndex]);
               //System.out.println("layerIndex" + numLayerLength[layerIndex]);
            }
            
            Scanner reader = new Scanner(new File(truthTableFileName));
         
            for (int test = 0; test < numTestCases; test++)
            {
               for (int indexIn = 0; indexIn < numInAct; indexIn++)
                  testCases[test][indexIn] = Double.valueOf(reader.nextLine());
               
               for (int indexOut = 0; indexOut < numOutAct; indexOut++)
                  testCases[test][indexOut + numInAct] = Double.valueOf(reader.nextLine());
               reader.nextLine();
            }
         } // if (training)
         else
         {
            Scanner reader = new Scanner(new File(inputsFileName));
            
            for (int test = 0; test < numTestCases; test++)
            {
               for (int m = 0; m < numInAct; m++)
                  testCases[test][m] = Double.valueOf(reader.nextLine()); // OK?
               reader.nextLine();
            }
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
    * if we are training reports/prints everything in testCaseOutputs, the average Error for the current model, and why
    * the model stopped training
    * if we are just running reports the inputs and their outputs
    */
   public void report()
   {
      System.out.println("\nReporting Data:");
      
      if (training)
      {
         for (int test = 0; test < numTestCases; test++)
         {
            for (int in = 0; in < numInAct; in++)
               System.out.print(testCases[test][in] + ",");
            System.out.print("=");
            
            for (int out = 0; out < numOutAct; out++)
               System.out.print(" " + testCases[test][out + numInAct]);
            System.out.print(":");
            
            for (int out = 0; out < numOutAct; out++)
               System.out.print("   " + String.format("%.4f", testCaseOutputs[test][out]));
            System.out.println();
         } // for (int test = 0; test < numTestCases; test++)
         
         System.out.println("Time to train: " + (timeAfterTraining - timeBeforeTraining) + " milliseconds");
         
         if (maxIter < iterations)
            System.out.println("\nCOMPLETED TRAINING BECAUSE EXCEED MAX ITERATIONS");
         
         if (avgError < avgErrorCut)
            System.out.println("\nCOMPLETED TRAINING BECAUSE AVERAGE ERROR IS LOW ENOUGH");
         
         System.out.println("Iterations: " + iterations);
         System.out.println("Average Error: " + avgError);
      } // if (training)
      else
      {
         for (int test = 0; test < numTestCases; test++)
         {
            for (int in = 0; in < numInAct; in++)
               System.out.print(testCases[test][in] + ",");
            
            System.out.print("=");
            
            System.out.print(":");
            
            for (int out = 0; out < numOutAct; out++)
               System.out.print("   " + String.format("%.4f", testCaseOutputs[test][out]));
            System.out.println();
         } // for (int test = 0; test < numTestCases; test++)
      } // else
      System.out.println();
   } // public void report()
   
   /**
    * Executes a run with the weights and input activations given within their corresponding instance variables
    * In other words, only uses the values already stored in the input layer
    * Stores the final values (with sigmoid) of each hidden activation inside their respective index and layer
    * in the hidden[][] array
    * Stores the final value (with sigmoid) of the output activation in output
    * Stores the summation (without sigmoid) of each hidden activation inside their respective index and layer
    * in the hiddenTheta[][] array
    * Acts as a helper method for train()
    */
   public void runForTrain(int test)
   {
      double outputTheta;
      int numLayers = 4;
      int[] numLayerLength = new int[4];
      numLayerLength[0] = 2;
      numLayerLength[1] = 5;
      numLayerLength[2] = 10;
      numLayerLength[3] = 3;
      
      for (int n = 1; n < numLayers-1; n++)  // -1!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
         for (int j = 0; j < numLayerLength[n]; j++)
         {
            hiddenTheta[n][j] = 0.0;
            for (int k = 0; k < numLayerLength[n-1]; k++)
               hiddenTheta[n][j] += activations[n-1][k] * weights[n-1][k][j];
            activations[n][j] = activationFunction(hiddenTheta[n][j]);
         }
      
      int n = OUTPUTLAYERINDEX;
      for (int i = 0; i < numOutAct; i++)
      {
         outputTheta = 0.0;
         for (int j = 0; j < numLayerLength[n-1]; j++)
            outputTheta += activations[n-1][j] * weights[n-1][j][i];
         activations[n][i] = activationFunction(outputTheta);
         psi[n][i] = (testCases[test][i + numInAct] - activations[n][i]) * derivativeActivationFunction(outputTheta);
      }
   } // public void runForTrain(int test)
   
   /**
    * Executes a run with the weights and input activations given within their corresponding instance variables
    * In other words, only uses the values already stored in the input layer
    * Stores the final values (with sigmoid) of each hidden activation inside their respective index and layer in the
    * hidden[][] array
    * Stores the final value (with sigmoid) of the output activation in output
    */
   public void pureRun()
   {
      double theta;
      int[] numLayerLength = new int[4];
      numLayerLength[0] = 2;
      numLayerLength[1] = 5;
      numLayerLength[2] = 10;
      numLayerLength[3] = 3;
      
      for (int n = 1; n < 4; n++)
      {
         for (int i = 0; i < numLayerLength[n]; i++)
         {
            theta = 0.0;
            for (int j = 0; j < numLayerLength[n-1]; j++)
            {
               theta += activations[n - 1][j] * weights[n - 1][j][i];
               //System.out.println("" + n + i + j + ": " + weights[n - 1][j][i]);
            }
            activations[n][i] = activationFunction(theta);
         }
      } // for (int n = 1; n < numLayers; n++)
   } // public void pureRun()
   
   /**
    * Runs all cases and stores their output values in testCaseOutputs
    */
   public void runAllCases()
   {
      for (int test = 0; test < numTestCases; test++)
      {
         for (int m = 0; m < numInAct; m++)
            activations[INPUTLAYERINDEX][m] = testCases[test][m];
         
         pureRun();
         
         for (int i = 0; i < numOutAct; i++)
            testCaseOutputs[test][i] = activations[OUTPUTLAYERINDEX][i];
      }
   } // public void runAllCases()
   
   /**
    * Trains the network using gradient descent, improved via back propagation
    * Stops iteratively training when the number of max iterations is exceeded or when average error is sufficiently low,
    * which ever comes first
    */
   public void train()
   {
      boolean done = false;
      int n;
      double lastPsi;
      double omega;
      double totalError;
      double error;
      
      int[] numLayerLength = new int[4];
      numLayerLength[0] = 2;
      numLayerLength[1] = 5;
      numLayerLength[2] = 10;
      numLayerLength[3] = 3;
      
      timeBeforeTraining = System.currentTimeMillis();
      while (!done)
      {
         totalError = 0.0;
         for (int test = 0; test < numTestCases; test++)
         {
            for (int in = 0; in < numInAct; in++)
               activations[INPUTLAYERINDEX][in] = testCases[test][in];
            runForTrain(test);
            
            n = SECONDHIDDENLAYERINDEX;
            for (int j = 0; j < numLayerLength[n]; j++)
            {
               omega = 0.0;
               
               for (int i = 0; i < numLayerLength[n+1]; i++)
               {
                  omega += psi[n+1][i] * weights[n][j][i];
                  weights[n][j][i] += lambda * activations[n][j] * psi[n+1][i];
               }
               psi[n][j] = omega * derivativeActivationFunction(hiddenTheta[n][j]);
            } // for (int j = 0; j < numHidAct; j++)
            
            n = FIRSTHIDDENLAYERINDEX;
            for (int k = 0; k < numLayerLength[n]; k++)
            {
               omega = 0.0;
               for (int j = 0; j < numLayerLength[n+1]; j++)
               {
                  omega += psi[n+1][j] * weights[n][k][j];
                  weights[n][k][j] += lambda * activations[n][k] * psi[n+1][j];
               }
               
               lastPsi = omega * derivativeActivationFunction(hiddenTheta[n][k]);
               
               for (int m = 0; m < numLayerLength[n-1]; m++)
                  weights[n-1][m][k] += lambda * activations[n-1][m] * lastPsi;
            } // for (int k = 0; k < numHidAct1; k++)
            
            runForTrain(test);
            for (int i = 0; i < numOutAct; i++)
            {
               error = testCases[test][i + numInAct] - activations[OUTPUTLAYERINDEX][i];
               totalError += error * error / 2.0;
            }
         } // for (int test = 0; test < 4; test++)
         timeAfterTraining = System.currentTimeMillis();
         
         avgError = totalError / ((double) numTestCases);
         
         iterations++;
         
         if (maxIter < iterations)
            done = true;
         else if (avgError < avgErrorCut)
            done = true;
      } // while (!areDone)
   } // public void train()
   
   /**
    * A wrapper for the activation function we will use for the network running and training
    *
    * @param x the input for f(x)
    * @return the result of f(x)
    */
   public double activationFunction(double x)
   {
      return sigmoid(x);
   }
   
   /**
    * A basic sigmoid function
    * f(x) = 1/(1+e^-x)
    *
    * @param x the input for the sigmoid function
    * @return the result of sigmoid(x)
    */
   public double sigmoid(double x)
   {
      return (1.0/(1.0 + Math.exp(-x)));
   }
   
   /**
    * The derivative of the activation function we will use for the network training
    *
    * @param x the input for the derivative
    * @return the result of f'(x)
    */
   public double derivativeActivationFunction(double x)
   {
      return derivativeSigmoid(x);
   }
   
   /**
    * The derivative of the sigmoid function
    *
    * @param x the input for the derivative
    * @return the result of sigmoid'(x)
    */
   public double derivativeSigmoid(double x)
   {
      double sig = sigmoid(x);
      return (sig * (1.0 - sig));
   }
   
   /**
    * Saves the weights currently in weights array in a file called "Weights"
    */
   public void saveWeights()
   {
      int[] numLayerLength = new int[4];
      numLayerLength[0] = 2;
      numLayerLength[1] = 5;
      numLayerLength[2] = 10;
      numLayerLength[3] = 3;
      try
      {
         PrintWriter out = new PrintWriter(new FileWriter(weightsFileName), true);
         
         out.println("Network: ");
         out.println(networkType);
         
         for (int n = 0; n < numLayers-1; n++) {
            System.out.println("numLayer Length: " + numLayerLength[n]);
            out.print("\nWeights for the " + n + " Layer to the " + (n + 1) + " Layer:\n");
            for (int j = 0; j < numLayerLength[n]; j++)
            {
               for (int i = 0; i < numLayerLength[n + 1]; i++)
                  out.println(weights[n][j][i]);
               out.println();
            }
         }
         
         out.close();
      } // try
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   } // public void saveWeights()
   
   /**
    * Loads the weights from the file "Weights" into weights array
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
         
         if (Double.parseDouble(reader.nextLine()) != numHidAct1)
            throw new RuntimeException("The number of first Hidden Activations, configured to \"Weights\" " +
                  "does not match the user input configuration");
         
         if (Double.parseDouble(reader.nextLine()) != numHidAct2)
            throw new RuntimeException("The number of second Hidden Activations, configured to \"Weights\" " +
                  "does not match the user input configuration");
         
         if (Double.parseDouble(reader.nextLine()) != numOutAct)
            throw new RuntimeException("The number of Output Activations, configured to \"Weights\" does not match the" +
                  " user input configuration");
         
         reader.nextLine();
         reader.nextLine();
         int n = INPUTLAYERINDEX;
         
         for (int m = 0; m < numInAct; m++)
         {
            for (int k = 0; k < numHidAct1; k++)
               weights[n][m][k] = Double.parseDouble(reader.nextLine());
            reader.nextLine();
         }
         
         reader.nextLine();
         reader.nextLine();
         n = FIRSTHIDDENLAYERINDEX;
         
         for (int k = 0; k < numHidAct1; k++)
         {
            for (int j = 0; j < numHidAct2; j++)
               weights[n][k][j] = Double.parseDouble(reader.nextLine());
            reader.nextLine();
         }
         
         reader.nextLine();
         reader.nextLine();
         n = SECONDHIDDENLAYERINDEX;
         
         for (int j = 0; j < numHidAct2; j++)
         {
            for (int i = 0; i < numOutAct; i++)
               weights[n][j][i] = Double.parseDouble(reader.nextLine());
            reader.nextLine();
         }
         
         reader.close();
      } // try
      catch (FileNotFoundException e)
      {
         throw new RuntimeException(e);
      }
   } // public void loadWeights()
} // public class ABCDNeuralNetwork