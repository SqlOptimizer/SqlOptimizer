/* Katrina Ward and San Yeung
*  Parser class for the SQL Optimizer project
*/

import java.io.*;
import java.util.*;

public class parser{
  query sqlQuery;                        // Query to parse
  query subQuery;
  BufferedReader stream = null;          // input stream for file containing query
  String[] splitQuery;                   // Entire query split into tokens
  
  // Constructor, will open up and verify input stream
  public parser(String input) throws IOException{
    try{
      BufferedReader stream = new BufferedReader(new FileReader(input));
    }catch(IOException ex){
      System.out.println(ex.toString());
      System.out.println("Input File Not Found");
    } 
  }
  
  // Read in a query into the class's query object
  public void readInQuery() throws IOException{
    String queryString = new String();                   // Complete query in a string
    String buffer;
    ArrayList<String> temp = new ArrayList<String>();
    // Read entire query into a string for easy parsing
    while((buffer = stream.readLine()) != null){
      queryString = queryString + " " + buffer;
    }
    stream.close();
    
    // Parse string into query class
    splitQuery = queryString.split("\\s");
    for(int i=0; i<splitQuery.length; i++){
      
    }
  }
  
}
  
 