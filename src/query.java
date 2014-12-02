/* 
*  Katrina Ward and San Yeung
*  CS 5300 SQL Optimizer project
*  Description: Query class that will be used to hold each SQL query
*/

import java.util.concurrent.ExecutionException;
import java.lang.Object;
import java.util.*;


/*
 * Class: Query
 * Description: Holds all the components of the SQL query without the keywords for easy representation as 
 *              relational algebra
 *              
 * Methods:
 * 
 * query
 * Description: Default Constructor
 * 
 * query(query)
 * Description: Copy Constructor
 * 
 * isWhereEmpty
 * Description: Returns whether or not the query has a where statement defined
 * Pre: None
 * Post: Returns true if where is set to null
 * 
 * whereInfoToString
 * Description: Converts the contents of the where class to a single string
 * Pre: Where is not set to null
 * Post: Returns the contents of the where statement as a string
 * 
 */

public class query{
    

    /*********************************************************************************/
  /*               Member Variables                                                */
  /*********************************************************************************/

  public ArrayList<String> attributes;                 // Select attributes
  public ArrayList<Tuple<String, String>> relations;   // From which relations
  public ArrayList<String> orderBy;                    // List of order by attributes if any
  public query subquery;                               // For nested queries
  public whereStatement where;
  public static boolean union;
  public static boolean intersect;
  public static boolean difference;

  /************************************************************************************/
  /*                 Member Methods                                                   */
  /************************************************************************************/
  // Query default constructor
  public query(){
    attributes = new ArrayList<String>();
    relations = new ArrayList<Tuple<String, String>>();
    orderBy = null;
    where = null;
    subquery = null;
    union=false;
    intersect=false;
    difference=false;
  }
  
  
  // Copy constructor
  public query(query newQuery){
    //check to see if newQuery is null
    if(newQuery != null){
      attributes=new ArrayList<String>(newQuery.attributes);
      relations = new ArrayList<Tuple<String, String>>(newQuery.relations);
      if(newQuery.orderBy!=null)
        orderBy = new ArrayList<String>(newQuery.orderBy);
      else
        orderBy=null;
      if(newQuery.where!=null)
        where=new whereStatement(newQuery.where);
      else
        where=null;
      if(newQuery.subquery == null){
        subquery = null;
      }
      else{
        subquery = new query(newQuery.subquery);
      }
    }
    union=false;
    intersect=false;
    difference=false;
  }
  
  // Check if there is a where statement
  public boolean isWhereEmpty()
  {
    if(where == null){
      return true;
    }
    else{
      return(where.conditions.size() == 0);
    }
  }
  
  public void setUnion(){
    union=true;
  }
  
  public void setIntersect(){
    intersect=true;
  }
  
  public void setDifference(){
    difference=true;
  }
  
  

  //return the conditions in wherestatement to a list of string to store in data's node
  public String whereInfoToString() {
    String whereInfo = new String();
    //indexes representing operators and conditions
    int operators = 0;
    int conditions = 0;
    //this loop will iteratively go through conditions and operators and generate a single string out of that
    while(conditions <= this.where.conditions.size()-1){
      whereInfo = whereInfo + "(" + this.where.conditions.get(conditions) + ")";
      conditions++;
      if(this.where.operators != null){
        if(operators <= this.where.operators.size()-1){
          whereInfo = whereInfo + this.where.operators.get(operators);
          operators++;
        }
      }
    }
    return whereInfo;
  }

    /*********************************************************************/
  // Begin Where class
  /*********************************************************************/
  // There where class is only used as part of the query class, so it is defined here as only a member class
  public class whereStatement{                 // Where conditions
    // whereStatement member variables
    ArrayList<String> conditions;
    ArrayList<String> operators;
    
    // whereStatement member methods
    // Constructor
    public whereStatement(){
      conditions = new ArrayList<String>();
      operators = new ArrayList<String>();      
    }
     
    
    // Copy constructor
    public whereStatement(whereStatement rhs){
      conditions = new ArrayList<String>(rhs.conditions);
      if(rhs.operators!=null)
        operators = new ArrayList<String>(rhs.operators);
      else
        operators = null;
    }
    

    public void whereCopy(ArrayList<String> cond, ArrayList<String> ops){
      conditions = new ArrayList<String>(cond);
      operators = new ArrayList<String>(ops);

    }
  }
  
/************************************************************************/
  // End of Where class
/************************************************************************/
}