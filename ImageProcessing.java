import java.io.DataInputStream;
import java.io.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ImageProcessing
{
   public static int[] bmpDemension =
         {5016, 3530,
               3859, 3302,
               4123, 3293,
               3853, 3176,
               3755, 3278,
               4102, 3247};
   public int letterIndex;
   public String inFileName;
   public static int scaleNumRows = 100;
   public static int scaleNumColums = 100;
   public static int distanceFromCenter = 100;
   
   public static void main(String[] args)
   {
      String[] letters = {"A", "B", "C", "D", "E", "F"};
      for (int i = 0; i < 6; i++)
      {
         ImageProcessing processer = new ImageProcessing("Set" + letters[i] + "_F5.bin", i);
         PelArray pel = new PelArray(processer.fileToArray());
         pel = pel.scale(scaleNumRows, scaleNumColums);
         pel = pel.onesComplimentImage();
         System.out.println(pel.calcCOM());
         System.out.println(pel.getXcom() + pel.getYcom());
         //pel = pel.crop(pel.getXcom() - distanceFromCenter, pel.getYcom() - distanceFromCenter, pel.getXcom() + distanceFromCenter, pel.getYcom() + distanceFromCenter);
         processer.arrayToFile(pel.getPelArray());
      }
   }
   
   public ImageProcessing(String inFileName, int letterIndex)
   {
      this.inFileName = inFileName;
      this.letterIndex = letterIndex;
   }
   
   public int[][] fileToArray()
   {
      int imageRowSize = bmpDemension[letterIndex*2+1];
      int imageColumnSize = bmpDemension[letterIndex*2]; //4350
      
      int[][] array = new int[imageRowSize][imageColumnSize];
      try
      {
         FileInputStream fstream = new FileInputStream(inFileName);
         
         DataInputStream in = new DataInputStream(fstream);
         
         for (int intRows = 0; intRows < imageRowSize; intRows++)
            for (int intColums = 0; intColums < imageColumnSize; intColums++)
               array[intRows][intColums] = in.readByte();
      }
      catch (FileNotFoundException e) { throw new RuntimeException(e);}
      catch (IOException e) {throw new RuntimeException(e);}
      return array;
   }
   
   public void arrayToFile(int[][] array)
   {
      byte[] bytes = new byte[distanceFromCenter*distanceFromCenter];
      int x = 0;
      
      for (int i = 0; i < distanceFromCenter; i++)
         for (int j = 0; j < distanceFromCenter; j++)
         {
            bytes[x] = (Integer.valueOf(array[i][j])).byteValue();
            x++;
         }
      
      try (FileOutputStream outputStream = new FileOutputStream("processed:" + inFileName))
      {
         outputStream.write(bytes);
      }
      catch (FileNotFoundException e) { throw new RuntimeException(e);}
      catch (IOException e) {throw new RuntimeException(e);}
      /*
      // Open the file that is the first command line parameter
      FileInputStream fstream = new FileInputStream(inFileName);
      
      // Convert our input stream to a DataInputStream
      DataInputStream in = new DataInputStream(fstream);
      
      try
      {
         PrintWriter out = new PrintWriter(new FileWriter("processed:" + inFileName), true);
         
         for (int j = 0; j < numRows; j++)
         {
            for (int i = 0; i < numColums; i++)
               out.println(array[j][i]);
            out.println();
         }
         out.close();
      } // try
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }*/
   }
}
