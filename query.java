/* 
*  Katrina Ward and San Yeung
*  CS 5300 SQL Optimizer project
*  Description: Query class that will be used to hold each SQL query
*/

public class query{
  public String[] attributes;                 // Select attributes
  public String[] relations;                  // From which relations
  public String[] orderBy;                    // List of order by attributes if any
  public query subquery;                      // For nested queries
  public whereStatement where;
  
  
  // Query default constructor
  public query(){
    attributes = new String[10];
    relations = new String[10];
    orderBy = new String[2];
    where = new whereStatement();
    subquery = null;
  }
  
  // Query Constructor
  public query(String[] att, String[] rel, String[] order, String[] whereCond, String[] whereOps){
    System.arraycopy(att, 0, attributes, 0, 10);
    System.arraycopy(rel, 0, relations, 0, 10);
    System.arraycopy(order,0,orderBy,0,2);
    where.equals(whereCond, whereOps);
  }
  
  // Constructor for a query with a subquery
  public query(String[] att, String[] rel, String[] order, String[] whereCond, String[] whereOps, query sub){
    System.arraycopy(att, 0, attributes, 0, 10);
    System.arraycopy(rel, 0, relations, 0, 10);
    System.arraycopy(order,0,orderBy,0,2);
    where.equals(whereCond, whereOps);
    subquery = new query(sub);
  }
  
  // Copy constructor
  public query(query newQuery){
    System.arraycopy(newQuery.attributes, 0, attributes, 0, 10);
    System.arraycopy(newQuery.relations, 0, relations, 0, 10);
    System.arraycopy(newQuery.orderBy, 0, orderBy, 0, 2);
    where=new whereStatement(newQuery.where);    
  }
  /*********************************************************************/
  // Begin Where class
  /*********************************************************************/
  private class whereStatement{                 // Where conditions
    String[] conditions;
    String[] operators;
    int operatorSize;
    int conditionSize;
    
    // Constructor
    public whereStatement(){
      conditions = new String[10];
      operators = new String[5];
      conditionSize=0;
      operatorSize=0;
      for(int i=0; i<10; i++){
        conditions[i]="null";
        if(i<5)
          operators[i]="null";        
      }
    } 
    
    // Copy constructor
    public whereStatement(whereStatement rhs){
      System.arraycopy(rhs.conditions, 0, conditions, 0, 10);
      System.arraycopy(rhs.operators, 0, operators, 0, 5);
      conditionSize=rhs.conditionSize;
      operatorSize=rhs.operatorSize;
    }
    
    // Assignment
    public void equals(String[] cond, String[] ops){
      System.arraycopy(cond, 0, conditions, 0, 10);
      System.arraycopy(ops, 0, operators, 0, 5);
      conditionSize=0;
      operatorSize=0;
      for(int i=0; i<10; i++){
        if(conditions[i]!="null")
          conditionSize++;
        if(i<5){
          if(operators[i]!="null")
            operatorSize++;
        }
      }  
    }
  }  
/************************************************************************/
  // End of Where class
/************************************************************************/
}