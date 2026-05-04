import java.io.*;
import java.util.*;

/**
 * An N Layer neural network, a type of artificial neural network that consists of N activation layers: an input layer
 * N-2 hidden layers, and an output layer. Using gradient descent, improved with back propagation, this
 * class will train and run the network based on a sigmoid curve. This object is now entirely controlled by control files.
 *
 *
 * public static void main(String[] args)
 * public void configurate(String fileName)
 * public void echo()
 * public int preArrayAllocation()
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
public class NLayerNeuralNetwork
{
   public double lambda;                  // a learning factor that controls the speed of descent
   public int maxIter;                    // during training, once the number of iterations exceeds this value, we will
                                          // stop training
   public int keepAlive;                  // the number of iterations between progress messages during training
                                          // (or no output if it is set to zero)
   public double avgErrorCut;             // during training, once average error is below this value, we will stop
                                          // training
   
   public int numInAct;                   // the number of input activations
   public int numOutAct;                  // the number of output activations
   public int[] numLayerLength;           // the number of activations in each layer
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
   public double[][] activations;         // where the activations are stored with the sigmoid curve applied
   public double[][] hiddenTheta;         // where the hidden activations are stored without the sigmoid curve applied
                                          // only used in training
   public double[][] testCases;           // if training, where the truth table will be stored, including inputs and
                                          // outputs, if just running where the inputs will be stored
   public double[][] testCaseOutputs;     // where the outputs for each testCase will be stored when runAllCases()
                                          // is called
   public double avgError;                // where the avgError will be stored when calculated
   public double[][] psi;                 // A variable used in the math for training
   public double timeBeforeTraining;      // A double to store the time stamp before training
   public double timeAfterTraining;       // A double to store the time stamp after training
   public String controlFileFileName;     // Stores the file name of the control file being used
   public String truthTableFileName;      // Stores the file name of the truth table being used if training
   public String inputsFileName;          // Stores the file name of the inputs being used if just running
   public String weightsFileName;         // The name of the weights file we will save/load the weights to and from
   public int iterations;                 // Stores the number of iterations that occurred during training
   public int numLayers;                  // the number of layers in this network is
   public String networkType;             // the type of network in String form, Example: 2-20-3
   public int outputLayerIndex;           // The position of the output layer, in the order of layers
   
   public static final int INPUTLAYERINDEX = 0;                               // What position the input layer is in,
                                                                              // in the order of layers
   public static final int FIRSTHIDDENLAYERINDEX = 1;                         // What position the first hidden layer
                                                                              // is in, in the order of layers
   public static final String DEFAULTCONTROLFILENAME = "NLayerControlFile";   // The default control file name if none
                                                                              // is provided
   
   /**
    * Actually constructs, trains and/or runs the Neural Network, using a control file that is passed in, or a defualt
    *
    * @param args argument necessary for the command line, where the control file name will be passed in
    */
   public static void main(String[] args)
   {
      NLayerNeuralNetwork net = new NLayerNeuralNetwork();
      
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
         line = reader.nextLine();
         keepAlive = Integer.valueOf(line.substring(line.indexOf(valuePreCursor) + valuePreCursorLength));
         
         reader.nextLine();
         line = reader.nextLine();
         networkType = line.substring(line.indexOf(valuePreCursor) + valuePreCursorLength);
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
      System.out.println("Inputs File: " + inputsFileName + "\n");
   } // public void echo()
   
   /**
    * Allocates space for all major arrays (arrays included as instance variables)
    * Arrays used only for training will be allocated only if training
    * Includes a call to preArrayAllocation()
    */
   public void arrayAlloc()
   {
      int largestLayerLength = preArrayAllocation();
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
   
   /**
    * Acts as a helper for arrayAlloc, by doing everything that must be done before arrayAlloc can execute
    * This means that the network type is read and interpreted to fill instance variable not filled in configurate. In
    * other words this methods fill all non-array instance variables that are not read directly from the control file.
    * Additionally, finds the layer will the highest number of activations and returns that number of activations.
    * @return the number of activations in the largest layer
    */
   public int preArrayAllocation()
   {
      String[] net = networkType.split("-");
      numLayers = net.length;
      outputLayerIndex = numLayers-1;
      numInAct = Integer.parseInt(net[0]);
      numOutAct = Integer.parseInt(net[numLayers-1]);
      
      int largest = 0;
      for (int i = 0; i < net.length; i++)
         largest = Math.max(largest, Integer.parseInt(net[i]));
      
      return largest;
   } // public int preArrayAllocation()
   
   /**
    * First, assigns the number of activations in each layer from networkType to numLayerLength[]. Then, populates
    * weight arrays randomly, manually (using predetermined values), or loaded (from an external file)
    * depending on user input. Additionally, assigns values from the truth table file (if training) or the inputs file
    * (if just running) to the testCases array.
    */
   public void arrayPop()
   {
      String[] net = networkType.split("-");
      
      for (int layerIndex = 0; layerIndex < numLayers; layerIndex++)
         numLayerLength[layerIndex] = Integer.parseInt(net[layerIndex]);
      
      if (whatWeights == 0)
      {
         for (int n = 0; n < numLayers-1; n++)
            for (int j = 0; j < numLayerLength[n]; j++)
               for (int i = 0; i < numLayerLength[n+1]; i++)
                  weights[n][j][i] = randomize(lowRand, highRand);
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
      } // else
      try
      {
         if (training)
         {
            Scanner readerTruthTable = new Scanner(new File(truthTableFileName));
            
            for (int test = 0; test < numTestCases; test++)
            {
               for (int indexIn = 0; indexIn < numInAct; indexIn++)
                  testCases[test][indexIn] = Double.valueOf(readerTruthTable.nextLine());
               
               for (int indexOut = 0; indexOut < numOutAct; indexOut++)
                  testCases[test][indexOut + numInAct] = Double.valueOf(readerTruthTable.nextLine());
               readerTruthTable.nextLine();
            }
         } // if (training)
         else
         {
            Scanner reader = new Scanner(new File(inputsFileName));
            
            for (int test = 0; test < numTestCases; test++)
            {
               for (int m = 0; m < numInAct; m++)
                  testCases[test][m] = Double.valueOf(reader.nextLine());
               //reader.nextLine();                   // PLEASE MAKE THIS AN ACTUAL LINE OF CODE!!!!!
            }
         } // else
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
    * if we are training reports/prints everything in testCaseOutputs, the average Error for the current model, why
    * the model stopped training, and how long the model took to train
    * if we are just running reports the inputs and their outputs
    */
   public void report()
   {
      System.out.println("\nReporting Data:");
      
      if (training)
      {
         for (int test = 0; test < numTestCases; test++)
         {
            //for (int in = 0; in < numInAct; in++)
               //System.out.print(testCases[test][in] + ",");
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
            //for (int in = 0; in < numInAct; in++)
               //System.out.print(testCases[test][in] + ",");
            
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
      
      for (int n = 1; n < numLayers-1; n++)
      {
         for (int j = 0; j < numLayerLength[n]; j++)
         {
            hiddenTheta[n][j] = 0.0;
            for (int k = 0; k < numLayerLength[n - 1]; k++)
               hiddenTheta[n][j] += activations[n - 1][k] * weights[n - 1][k][j];
            activations[n][j] = activationFunction(hiddenTheta[n][j]);
         }
      }
      
      int n = outputLayerIndex;
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
    * In other words, only uses the values already stored in the first layer of activations[][][]
    * Stores the final values (with sigmoid) of each hidden and output activation inside their respective
    * layer, start index and destination index in the activations[][][] array
    */
   public void pureRun()
   {
      double theta;
      
      for (int n = 1; n < numLayers; n++)
      {
         for (int i = 0; i < numLayerLength[n]; i++)
         {
            theta = 0.0;
            for (int j = 0; j < numLayerLength[n-1]; j++)
               theta += activations[n - 1][j] * weights[n - 1][j][i];
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
            testCaseOutputs[test][i] = activations[outputLayerIndex][i];
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
      
      timeBeforeTraining = System.currentTimeMillis();
      while (!done)
      {
         totalError = 0.0;
         for (int test = 0; test < numTestCases; test++)
         {
            for (int in = 0; in < numInAct; in++)
               activations[INPUTLAYERINDEX][in] = testCases[test][in];
            runForTrain(test);
            
            for (n = numLayers-2; n > FIRSTHIDDENLAYERINDEX; n--)
            {
               for (int j = 0; j < numLayerLength[n]; j++)
               {
                  omega = 0.0;
                  for (int i = 0; i < numLayerLength[n + 1]; i++)
                  {
                     omega += psi[n + 1][i] * weights[n][j][i];
                     weights[n][j][i] += lambda * activations[n][j] * psi[n + 1][i];
                  }
                  psi[n][j] = omega * derivativeActivationFunction(hiddenTheta[n][j]);
               } // for (int j = 0; j < numHidAct; j++)
            } // for (n = numLayers-2; n > FIRSTHIDDENLAYERINDEX; n--)
            
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
            
            pureRun();
            for (int i = 0; i < numOutAct; i++)
            {
               error = testCases[test][i + numInAct] - activations[outputLayerIndex][i];
               totalError += error * error / 2.0;
            }
         } // for (int test = 0; test < 4; test++)
         timeAfterTraining = System.currentTimeMillis();
         
         avgError = totalError / ((double) numTestCases);
         
         iterations++;
         
         //
         //NLayerCopy nety = new NLayerCopy();
         //String[] str = {"{\"NLayerControlFileTesting\"}"};
         //nety.main(str);
         //
         
         if (maxIter < iterations)
            done = true;
         else if (avgError < avgErrorCut)
            done = true;
         
         if ((keepAlive > 0) && (iterations % keepAlive == 0))
         {
            System.out.printf("Iteration %d, Error = %f\n", iterations, avgError);
            if(iterations % 500 == 0)
            {
               //weightsFileName = weightsFileName + iterations;
               saveWeights();
            }
         }
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
      try
      {
         PrintWriter out = new PrintWriter(new FileWriter(weightsFileName), true);
         
         out.println("Network: ");
         out.println(networkType);
         
         for (int n = 0; n < numLayers-1; n++)
         {
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
         
         if (!reader.nextLine().equals(networkType))
            throw new RuntimeException("The network type, configured to \"Weights\" does not match the" +
                  " user input configuration");
         
         for (int n = 0; n < numLayers-1; n++)
         {
            reader.nextLine();
            for (int j = 0; j < numLayerLength[n]; j++)
            {
               reader.nextLine();
               for (int i = 0; i < numLayerLength[n + 1]; i++)
                  weights[n][j][i] = Double.parseDouble(reader.nextLine());
            }
            reader.nextLine();
         }
         reader.close();
      } // try
      catch (FileNotFoundException e)
      {
         throw new RuntimeException(e);
      }
   } // public void loadWeights()
} // public class NLayerNeuralNetwork