/* Katrina Ward and San Yeung
*  Parser class for the SQL Optimizer project
*/

import java.io.*;
import java.util.*;


public class parser{
  query sqlQuery = new query();                        // Query to parse
  BufferedReader stream = null;          // input stream for file containing query
  String[] splitQuery;                   // Entire query split into tokens
  
  // Constructor, will open up and verify input stream
  public parser(String input) throws IOException{
    try{
      stream = new BufferedReader(new FileReader(input));
    }catch(IOException ex){
      System.out.println(ex.toString());
      System.out.println("Input File Not Found");
    } 
  }
  
  // Read in a query into the class's query object
  // Returns parsed query
  public query parseQuery() throws IOException{
    String queryString = new String();                   // Complete query in a string
    String buffer = new String();                        // Input from file, one line
    ArrayList<String> temp = new ArrayList<String>();    // Used for separating lists from the query such as attributes, etc
    Tuple<String, String> relTuple = new Tuple<String, String>();
    ArrayList<Tuple<String, String>> relationList = new ArrayList<Tuple<String, String>>(); // For relations
    boolean subqueryFlag = false;                        // Used to indicate when parsing a subquery
    
    // Read entire query into a string for easy parsing
    while((buffer = stream.readLine()) != null){
      queryString = queryString + " " + buffer;
    }
    stream.close();                                      // Close input stream
    splitQuery = queryString.split("\\s");               // Put query into a string array for easy access to tokens
    
    // Parse string into query class   
    for(int i=1; i<splitQuery.length; i++){
      if(splitQuery[i].equals("SELECT")){                                            // SELECT
        i++;
        while(!splitQuery[i].equals("ORDERBY") && !splitQuery[i].equals("FROM")){
          if(splitQuery[i].contains(","))                                // Remove trailing , or ; or )
            temp.add(splitQuery[i].substring(0, splitQuery[i].length()-1));
          else
            temp.add(splitQuery[i]);
            i++;
        }
        sqlQuery.attributes = new ArrayList<String>(temp);        
      }else if(splitQuery[i].equals("ORDERBY") && !subqueryFlag){                   // ORDERBY
        i++;
        while(i!=splitQuery.length && !splitQuery[i].equals("FROM")){    // May appear in two locations in a query
          if(splitQuery[i].contains(",") || splitQuery[i].contains(";"))
            temp.add(splitQuery[i].substring(0, splitQuery[i].length()-1));
          else
            temp.add(splitQuery[i]);
          i++;
        }
        sqlQuery.orderBy=new ArrayList<String>(temp);     
      }else if(splitQuery[i].equals("FROM") && !subqueryFlag){                      // FROM
        i++;
        while(i!=splitQuery.length && !splitQuery[i].equals("WHERE") && !splitQuery[i].equals("(SELECT") ){
          if(splitQuery[i].contains(",") || splitQuery[i].contains(";"))
            relTuple.setLeft(splitQuery[i].substring(0, splitQuery[i].length()-1));
          else
            relTuple.setLeft(splitQuery[i]);
          i++;
          if(splitQuery[i].equals("AS")){
            i++;
            if(splitQuery[i].contains(",") || splitQuery[i].contains(";"))
              relTuple.setRight(splitQuery[i].substring(0, splitQuery[i].length()-1));
            else
              relTuple.setRight(splitQuery[i]);
            i++;
          }
          else
            relTuple.setRight(null);
          relationList.add(new Tuple<String, String>(relTuple));
        }
        sqlQuery.relations = new ArrayList<Tuple<String, String>>(relationList);
        relationList.clear();
      }else if(splitQuery[i].equals("WHERE") && !subqueryFlag){                      // WHERE
        sqlQuery.where = sqlQuery.new whereStatement();
        i++;
        String tempString = new String();
        while(i!=splitQuery.length && !splitQuery[i].equals("ORDERBY")){
          while(i!=splitQuery.length && !splitQuery[i].equals("AND") && !splitQuery[i].equals("ORDERBY")){
            if(splitQuery[i].contains(";"))
              tempString = tempString + " " + splitQuery[i].substring(0, splitQuery[i].length()-1);
            else              
              tempString = tempString + " " + splitQuery[i];
            i++;
          }
          sqlQuery.where.conditions.add(tempString);
          if(i<splitQuery.length && (splitQuery[i].equals("AND"))){
            sqlQuery.where.operators.add(splitQuery[i]);
            i++;
          }
          tempString = "";
          if(sqlQuery.where.operators.isEmpty())     // Should make tree building easier
            sqlQuery.where.operators = null;
        }
      }else if(splitQuery[i].equals("(SELECT") || subqueryFlag){                // Subquery
        // Control the start and end of subquery parsing
        if(splitQuery[i].equals("(SELECT")){
          subqueryFlag=true;
          sqlQuery.subquery = new query();
        }
        else if(splitQuery[i].contains(")"))
          subqueryFlag=false;
        
        
        if(splitQuery[i].equals("(SELECT")){                                      // Subquery SELECT
          i++;
          while(!splitQuery[i].equals("ORDERBY") && !splitQuery[i].equals("FROM")){
            if(splitQuery[i].contains(","))
              temp.add(splitQuery[i].substring(0, splitQuery[i].length()-1));
            else
              temp.add(splitQuery[i]);
              i++;
          }
          sqlQuery.subquery.attributes = new ArrayList<String>(temp);        
        }else if(splitQuery[i].equals("ORDERBY")){                               // Subquery ORDERBY
          i++;
          while(!splitQuery[i].contains(")") && !splitQuery[i].equals("FROM")){
            if(splitQuery[i].contains(","))
              temp.add(splitQuery[i].substring(0, splitQuery[i].length()-1));
            else
              temp.add(splitQuery[i]);
            i++;
          }
          if(splitQuery[i].contains(")")){
            temp.add(splitQuery[i].substring(0, splitQuery[i].length()-2));         
            i++;
            subqueryFlag=false;
          }        
          sqlQuery.subquery.orderBy=new ArrayList<String>(temp);     
        }else if(splitQuery[i].equals("FROM")){                                   // Subquery FROM
          i++;
          while(!splitQuery[i].contains(")") && !splitQuery[i].equals("WHERE")){
            if(splitQuery[i].contains(",") || splitQuery[i].contains(";"))
              relTuple.setLeft(splitQuery[i].substring(0, splitQuery[i].length()-1));
            else
              relTuple.setLeft(splitQuery[i]);
            i++;
            if(splitQuery[i].equals("AS")){
              i++;
              if(splitQuery[i].contains(",")){
                relTuple.setRight(splitQuery[i].substring(0, splitQuery[i].length()-1));
                i++;
              }
              else if(splitQuery[i].contains(")")){
                relTuple.setRight(splitQuery[i].substring(0, splitQuery[i].length()-2));
                subqueryFlag=false;
                i++;
              }
              else{
                relTuple.setRight(splitQuery[i]);
                i++;
              }
            }
            else
              relTuple.setRight(null);
            relationList.add(new Tuple<String, String>(relTuple));
            relTuple.setRight(null);
          }
          if(splitQuery[i].contains(")") && relTuple.rightNull()){
            relTuple.setLeft(splitQuery[i].substring(0, splitQuery[i].length()-2));
            relTuple.setRight(null);
            subqueryFlag=false;
            relationList.add(new Tuple<String, String>(relTuple));
            i++;
          }
          sqlQuery.subquery.relations = new ArrayList<Tuple<String, String>>(relationList);
          relationList.clear();
        }else if(splitQuery[i].equals("WHERE")){                                // subquery WHERE
          sqlQuery.subquery.where = sqlQuery.subquery.new whereStatement();
          i++;
          String tempString = new String();
          while(i!=splitQuery.length && !splitQuery[i].contains(")") && !splitQuery[i].equals("ORDERBY")){
            while(i!=splitQuery.length && !splitQuery[i].contains(")") && !splitQuery[i].equals("AND") 
                  && !splitQuery[i].equals("ORDERBY")){
              tempString = tempString + " " + splitQuery[i];
              i++;
            }
            if(splitQuery[i].contains(")")){
              tempString = tempString + " " + splitQuery[i].substring(0, splitQuery[i].length()-2);
              i++;
              subqueryFlag=false;
            }
            sqlQuery.subquery.where.conditions.add(tempString);
            if(splitQuery[i].equals("AND")){
              sqlQuery.subquery.where.operators.add(splitQuery[i]);
              i++;
            }
            if(sqlQuery.subquery.where.operators.isEmpty())
              sqlQuery.subquery.where.operators = null;
            tempString = "";         
          }
        }  
        // End subquery parsing
      }
      temp.clear();
      i--;                      // Back index up so the key tokens will be caught
    }
    return sqlQuery;
  }  
}
  
 