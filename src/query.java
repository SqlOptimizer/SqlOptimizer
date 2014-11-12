/* 
*  Katrina Ward and San Yeung
*  CS 5300 SQL Optimizer project
*  Description: Query class that will be used to hold each SQL query
*/

import java.util.*;

public class query{
  /*********************************************************************************/
  /*               Member Variables                                                */
  /*********************************************************************************/
  public ArrayList<String> attributes;                 // Select attributes
  public ArrayList<String> relations;                  // From which relations
  public ArrayList<String> orderBy;                    // List of order by attributes if any
  public query subquery;                               // For nested queries
  public whereStatement where;
  
  
  /************************************************************************************/
  /*                 Member Methods                                                   */
  /************************************************************************************/
  // Query default constructor
  public query(){
    attributes = new ArrayList<String>();
    relations = new ArrayList<String>();
    orderBy = new ArrayList<String>();
    where = null;
    subquery = null;
  }
  
  // Query Constructor
  public query(ArrayList<String> att, ArrayList<String> rel, ArrayList<String> order, 
              ArrayList<String> whereCond, ArrayList<String> whereOps){
    attributes = new ArrayList<String>(att);
    relations = new ArrayList<String>(rel);
    orderBy = new ArrayList<String>(order);
    where.whereCopy(whereCond, whereOps);
  }
  
  // Constructor for a query with a subquery
  public query(ArrayList<String> att, ArrayList<String> rel, ArrayList<String> order, 
               ArrayList<String> whereCond, ArrayList<String> whereOps, query sub){
    attributes = new ArrayList<String>(att);
    relations = new ArrayList<String>(rel);
    orderBy = new ArrayList<String>(order);
    where.whereCopy(whereCond, whereOps);
    subquery = new query(sub);
  }
  
  // Copy constructor
  private query(query newQuery){
    attributes=new ArrayList<String>(newQuery.attributes);
    relations = new ArrayList<String>(newQuery.relations);
    orderBy = new ArrayList<String>(newQuery.orderBy);
    where=new whereStatement(newQuery.where);    
    subquery = new query(newQuery.subquery);
  }
  
  // Check if there is a where statement
  public boolean isWhereEmpty()
  {
    return(where==null);
  }
  
  /*********************************************************************/
  // Begin Where class
  /*********************************************************************/
  private class whereStatement{                 // Where conditions
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
      operators = new ArrayList<String>(rhs.operators);
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