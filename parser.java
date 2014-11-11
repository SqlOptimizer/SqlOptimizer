/* Katrina Ward and San Yeung
*  Parser class for the SQL Optimizer project
*/

import java.io.*;

public class parser{
  query sqlQuery;                        // Query to parse
  FileInputStream in;                    // File to read query from
  
  // Constructor
  public parser(String input) throws IOException{
    try{
      in = new FileInputStream(input);  
    }catch(IOException ex){
      System.out.println(ex.toString());
      System.out.println("Input File Not Found");
    } 
  }
  
  // Read in a query into the class's query object
  public void readQuery(){
    String[] queryLine = new String[150];               // Array that will hold entire query for easy parsing
    for(int i=0; i<150; i++){                           // Clear the query array of garbage
      queryLine[i]="null";
    }
    // Begin parsing into query object
    
  }
}
  
 