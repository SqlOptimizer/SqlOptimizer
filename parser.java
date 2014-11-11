/* Katrina Ward and San Yeung
*  Parser class for the SQL Optimizer project
*/

import java.io.*;

public class parser{
  query sqlQuery = new query();
  FileInputStream in = null;
  
  // Constructor
  public parser(String input) throws IOException{
    try{
      in = new FileInputStream(input);  
    }catch(IOException ex){
      System.out.println(ex.toString());
      System.out.println("Input File Not Found");
    }    
  }
}