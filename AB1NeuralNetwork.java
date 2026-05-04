/**
 * An AB1 neural network, a type of artificial neural network that consists of three activation layers: an input layer
 * (A), a hidden layer (B), and a single neuron output layer. We will be training this network for the AND, OR and XOR
 * gates, using gradient descent. This class will train and run the network based on a sigmoid curve.
 *
 * configurate(): void
 * main(String[]): void
 * echo(): void
 * arrayAlloc(): void
 * arrayPop(): void
 * randomize(double, double): double
 * runAllCases(): void
 * report(): void
 * run(): void
 * train(): void
 * sigmoid(double): double
 * derivativeSigmoid(double): double
 *
 * @author Varun Bhupathi
 * @date January 28, 2024
 */
public class AB1NeuralNetwork
{
   public double lambda;                  // a learning factor that controls the speed of descent
   public int maxIter;                    // during training, once the number of iterations exceeds this value, we will
   // stop training
   public double avgErrorCut;             // during training, once average error is below this value, we will stop
   // training
   public int numInAct;                   // the number of input activations
   public int numHidAct;                  // the number of hidden activations
   public int NUMOUTACT = 1;              // the number of output activations, is a constant at 1 for this model
   public double lowRand;                 // the lowest value a random weight can be
   public double highRand;                // the exclusive upper bound a random weight can be
   public boolean training;               // if true we will train the model and then run it, if false we will
   // simply run it with random or predetermined weights
   public boolean randomizeWeights;       // if true we will start with randomized weights, if false we will start with
   // predetermined weights
   public double[][] weightsInHid;        // where the weights for the input to hidden layers will be stored
   public double[][] weightsHidOut;       // where the weights for the hidden to output layers will be stored
   public double[] input;                 // where the input activations are stored
   public double[] hidden;                // where the hidden activations are stored with the sigmoid curve applied
   public double[] hiddenTheta;           // where the hidden activations are stored without the sigmoid curve applied
   public double output;                  // where the output activation will be stored with the sigmoid curve applied
   public double outputTheta;             // where the output activation will be stored without the sigmoid curve applied
   public double[][] testCases;           // where the truth table for OR, AND, or XOR will be stored, including inputs
   // and outputs
   public double[] testCaseOutputs;       // where the outputs for each testCase will be stored when runAllCases()
   // is called
   public double avgError;                // where the avgError will be stored when calculated
   public int gate;                       // specifies which gate this network will be executing for, 0=OR, 1=AND, 2=XOR
   public double[][] changeWeightsInHid;  // where the change in weights during training for the input to hidden layer
   // will be stored
   public double[][] changeWeightsHidOut; // where the change in weights during training for the hidden to output layer
   // will be stored
   
   /**
    * Actually constructs, trains and/or runs the Neural Network
    *
    * @param args argument necessary for the command line
    */
   public static void main(String[] args)
   {
      AB1NeuralNetwork net = new AB1NeuralNetwork();
      net.configurate();
      net.echo();
      net.arrayAlloc();
      net.arrayPop();
      if (net.training)
         net.train();
      else
         net.runAllCases();
      net.report();
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
      numHidAct = 4;
      
      lowRand = -1.5;
      highRand = 1.5;
      
      gate = 1; //0=OR, 1=AND, 2=XOR
      training = true;
      randomizeWeights = true;
   } // public void configurate()
   
   /**
    * Echo (prints out) what was received by configurate, in a digestible and easy to read way
    */
   public void echo()
   {
      System.out.println("\nECHO:");
      System.out.println("Network Configuration = " + numInAct + "-" + numHidAct + "-" + NUMOUTACT);
      System.out.println("Random number range = " + lowRand + " through " + highRand);
      System.out.println("Is Training = " + training);
      if (training)
      {
         System.out.println("   lambda = " + lambda);
         System.out.println("   maxIter = " + maxIter);
         System.out.println("   avgErrorCut = " + avgErrorCut);
      }
      
      if (randomizeWeights)
         System.out.println("Populating for random weights");
      else
         System.out.println("Populating for defualt weights");
      
      if (gate == 0) // OR
         System.out.println("Executing for an OR gate");
      else if (gate == 1) // AND
         System.out.println("Executing for an AND gate");
      else if (gate == 2) //XOR
         System.out.println("Executing for an XOR gate");
   } // public void echo()
   
   /**
    * Allocates space for all major arrays (arrays included as instance variables)
    */
   public void arrayAlloc()
   {
      weightsInHid = new double[numInAct][numHidAct];
      weightsHidOut = new double[numHidAct][NUMOUTACT];
      input = new double[numInAct];
      hidden = new double[numHidAct];
      hiddenTheta = new double[numHidAct];
      testCases = new double[4][3];
      testCaseOutputs = new double[4];
      
      if (training)
      {
         changeWeightsInHid = new double[numInAct][numHidAct];
         changeWeightsHidOut = new double[numHidAct][NUMOUTACT];
      }
   } // public void arrayAlloc()
   
   /**
    * Populates weight arrays randomly, or manually (using predetermined values) depending on user input
    * Determines which test case the network is executing for and assigns values to testCases appropriately
    */
   public void arrayPop()
   {
      if (randomizeWeights)
      {
         for (int indexIn = 0; indexIn < numInAct; indexIn++)
            for (int indexHid = 0; indexHid < numHidAct; indexHid++)
               weightsInHid[indexIn][indexHid] = randomize(lowRand, highRand);
         
         for (int indexHid = 0; indexHid < numHidAct; indexHid++)
            for (int indexOut = 0; indexOut < NUMOUTACT; indexOut++)
               weightsHidOut[indexHid][indexOut] = randomize(lowRand, highRand);
      }
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
      
      if (gate == 0) // OR
      {
         testCases[0][2] = 0.0;
         testCases[1][2] = 1.0;
         testCases[2][2] = 1.0;
         testCases[3][2] = 1.0;
      }
      else if (gate == 1) // AND
      {
         testCases[0][2] = 0.0;
         testCases[1][2] = 0.0;
         testCases[2][2] = 0.0;
         testCases[3][2] = 1.0;
      }
      else if (gate == 2) //XOR
      {
         testCases[0][2] = 0.0;
         testCases[1][2] = 1.0;
         testCases[2][2] = 1.0;
         testCases[3][2] = 0.0;
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
    * Run not used for training. Tests all testCases and stores their outputs in testCaseOutputs
    * Calculates average error as well and stores it in avgError
    */
   public void runAllCases()
   {
      double totalError = 0;
      for (int test = 0; test < testCases.length; test++)
      {
         for (int in = 0; in < numInAct; in++)
            input[in] = testCases[test][in];
         
         run();
         double error = testCases[test][testCases[0].length-1] - output;
         totalError += error*error/2.0;
         testCaseOutputs[test] = output;
      }
      avgError = totalError/testCases.length;
   } // public void run()
   
   /**
    * Reports/Prints everything in testCaseOutputs and the average Error for the current model
    */
   public void report()
   {
      System.out.println("\nReporting Data:");
      for (int test = 0; test < testCases.length; test++)
         System.out.println(testCases[test][0] + "," + testCases[test][1] + " = "
               + testCases[test][2] + ": " + testCaseOutputs[test]);
      System.out.println("Average Error: " + avgError);
   }
   
   /**
    * Executes a run with the weights and input activations given within their corresponding instance variables
    * In other words, only uses the values already stored in the input layer
    * Stores the final values (with sigmoid) of each hidden activation inside their respective index in the hidden[] array
    * Stores the final value (with sigmoid) of the output activation in output
    * Stores the summation (without sigmoid) of each hidden activation inside their respective index in the hiddenTheta[]
    * array
    * Stores the summation (without sigmoid) of the output activation in outputTheta
    * Acts as a helper method for runAllCases() and train()
    */
   public void run()
   {
      for (int j = 0; j < hiddenTheta.length; j++)
         hiddenTheta[j] = 0.0;
      outputTheta = 0.0;
      
      for (int k = 0; k < numInAct; k++)
         for (int j = 0; j < numHidAct; j++)
            hiddenTheta[j] += input[k] * weightsInHid[k][j];
      
      for (int j = 0; j < numHidAct; j++)
         hidden[j] = sigmoid(hiddenTheta[j]);
      
      for (int j = 0; j < numHidAct; j++)
         outputTheta += hidden[j] * weightsHidOut[j][0];
      
      output = sigmoid(outputTheta);
   } // public void runForTrain()
   
   /**
    * Trains the network using gradient descent
    * Stops iteratively training when the number of max iterations is exceeded or when average error is sufficiently low,
    * which ever comes first
    * Additionally calls runAllCases(), in order to calculate average Error
    * Prints out why the network stopped training, the number of iterations and average Error.
    */
   public void train()
   {
      boolean areDone = false;                        // Initialize needed variables
      int iterations = 0;
      double w;
      double omega;
      double psi;
      double partialDerivative;
      
      while (!areDone)
      {
         for (int test = 0; test < testCases.length; test++)
         {
            for (int in = 0; in < numInAct; in++)     // Run Network
               input[in] = testCases[test][in];
            run();
            w = testCases[test][numInAct] - output;
            
            for (int k = 0; k < numInAct; k++)        // Calculate Change in Weights
               for (int j = 0; j < numHidAct; j++)
               {
                  omega = w * derivativeSigmoid(outputTheta) * weightsHidOut[j][0];
                  psi = omega * derivativeSigmoid(hiddenTheta[j]);
                  partialDerivative = -input[k] * psi;
                  changeWeightsInHid[k][j] = -lambda * partialDerivative;
               }
            
            for (int j = 0; j < numHidAct; j++)
            {
               psi = w * derivativeSigmoid(outputTheta);
               partialDerivative = -hidden[j] * psi;
               changeWeightsHidOut[j][0] = -lambda * partialDerivative;
            }
            
            for (int k = 0; k < numInAct; k++)        // Apply Change in Weights
               for (int j = 0; j < numHidAct; j++)
                  weightsInHid[k][j] += changeWeightsInHid[k][j];
            
            for (int j = 0; j < numHidAct; j++)
               weightsHidOut[j][0] += changeWeightsHidOut[j][0];
         } // for (int test = 0; test < 4; test++)
         iterations++;
         
         runAllCases();
         
         if (maxIter < iterations)                    // Determine if we should stop training
         {
            areDone = true;
            System.out.println("\nCOMPLETED TRAINING BECAUSE EXCEED MAX ITERATIONS");
            System.out.println("Iterations: " + iterations);
            System.out.println("Average Error: " + avgError);
         }
         else if (avgError < avgErrorCut)
         {
            areDone = true;
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
} // public class AB1NeuralNetwork