/* Katrina Ward and San Yeung
*  Parser class for the SQL Optimizer project
*/

import java.io.*;
import java.util.*;


public class parser{
  query sqlQuery = new query();                        // Query to parse
  BufferedReader stream = null;          // input stream for file containing query
  String[] splitQuery;                   // Entire query split into tokens
  
  // Constructor, will open up and verify input stream, read query into a string, and close input stream
  public parser(String input) throws IOException{
    try{
      BufferedReader stream = new BufferedReader(new FileReader(input));
    }catch(IOException ex){
      System.out.println(ex.toString());
      System.out.println("Input File Not Found");
    } 
  }
  
  // Read in a query into the class's query object
  // Returns parsed query
  public query parseQuery() throws IOException{
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
      if(splitQuery[i]=="SELECT"){
        i++;
        while(splitQuery[i]!="ORDERBY" && splitQuery[i]!="FROM"){
            temp.add(splitQuery[i]);
            i++;
        }
        sqlQuery.attributes = new ArrayList<String>(temp);        
      }else if(splitQuery[i]=="ORDERBY"){
        i++;
        while(splitQuery[i]!="FROM" && i!=splitQuery.length){
          temp.add(splitQuery[i]);
          i++;
        }
        sqlQuery.orderBy=new ArrayList<String>(temp);     
      }else if(splitQuery[i]=="FROM"){
        i++;
        while(splitQuery[i]!="WHERE" && splitQuery[i]!="(SELECT" && i!=splitQuery.length){
          temp.add(splitQuery[i]);
          i++;
        }
        sqlQuery.relations = new ArrayList<String>(temp);
      }else if(splitQuery[i]=="WHERE"){
        sqlQuery.where = sqlQuery.new whereStatement();
        i++;
        String tempString = new String();
        while(i!=splitQuery.length && splitQuery[i]!="ORDERBY"){
          while(i!=splitQuery.length && splitQuery[i]!="AND" && splitQuery[i]!="OR" && splitQuery[i]!="ORDERBY"){
            tempString = tempString + " " + splitQuery[i];
            i++;
          }
          sqlQuery.where.conditions.add(tempString);
          if(i<splitQuery.length && (splitQuery[i]=="AND" || splitQuery[i]=="OR")){
            sqlQuery.where.operators.add(splitQuery[i]);
            i++;
          }
          tempString = "";         
        }
      }else if(splitQuery[i]=="(SELECT"){
        // Fill in sub query
        sqlQuery.subquery = new query();
        if(splitQuery[i]=="(SELECT"){
          i++;
          while(splitQuery[i]!="ORDERBY" && splitQuery[i]!="FROM"){
            temp.add(splitQuery[i]);
            i++;
          }
          sqlQuery.subquery.attributes = new ArrayList<String>(temp);        
        }else if(splitQuery[i]=="ORDERBY"){
          i++;
          while(splitQuery[i]!="FROM" && i!=splitQuery.length){
            temp.add(splitQuery[i]);
            i++;
          }
          sqlQuery.subquery.orderBy=new ArrayList<String>(temp);     
        }else if(splitQuery[i]=="FROM"){
          i++;
          while(splitQuery[i]!="WHERE" && i!=splitQuery.length){
            temp.add(splitQuery[i]);
            i++;
          }
          sqlQuery.subquery.relations = new ArrayList<String>(temp);
        }else if(splitQuery[i]=="WHERE"){
          sqlQuery.subquery.where = sqlQuery.subquery.new whereStatement();
          i++;
          String tempString = new String();
          while(i!=splitQuery.length && splitQuery[i]!="ORDERBY"){
            while(i!=splitQuery.length && splitQuery[i]!="AND" && splitQuery[i]!="OR" && splitQuery[i]!="ORDERBY"){
              tempString = tempString + " " + splitQuery[i];
              i++;
            }
            sqlQuery.subquery.where.conditions.add(tempString);
            if(i<splitQuery.length && (splitQuery[i]=="AND" || splitQuery[i]=="OR")){
              sqlQuery.subquery.where.operators.add(splitQuery[i]);
              i++;
            }
            tempString = "";         
          }
        }
        // End sub query
      }
      temp.clear();
      i--;
    }
    return sqlQuery;
  }  
}
  
 