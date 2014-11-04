/* 
*  Katrina Ward and San Yeung
*  CS 5300 SQL Optimizer project
*  Description: Query class that will be used to hold each SQL query
*/

public class query{
  String[] attributes;                 // Select attributes
  String[] relations;                  // From which relations
  query subquery;                      // For nested queries
  whereStatement where;
  
  
  // Query default constructor
  public query(){
    attributes = new String[10];
    relations = new String[10];
    where = new whereStatement();
    subquery = null;
  }
  
  // Query Constructor
  public query(String[] att, String[] rel, String[] whereAtt, char[] whereOps,
               String[] whereSets){
    System.arraycopy(att, 0, attributes, 0, 10);
    System.arraycopy(rel, 0, relations, 0, 10);
    where.equals(whereAtt, whereOps, whereSets);
  }
  /*********************************************************************/
  // Begin Where class
  /*********************************************************************/
  private class whereStatement{                 // Where conditions
    String[] attributes;
    char[] operators;
    String[] sets;
    int attributeSize;
    int operatorSize;
    int setSize;
    
    // Constructor
    public whereStatement(){
      attributes = new String[10];
      operators = new char[5];
      sets = new String[5];
      attributeSize=0;
      operatorSize=0;
      setSize=0;
      for(int i=0; i<10; i++){
        attributes[i]="null";
        if(i<5){
          operators[i]='x';
          sets[i]="null";
        }
      }
    } 
    
    // Assignment
    public void equals(String[] att, char[] ops, String[] set){
      System.arraycopy(att, 0, attributes, 0, 10);
      System.arraycopy(ops, 0, operators, 0, 5);
      System.arraycopy(set, 0, sets, 0, 5);
      attributeSize=0;
      operatorSize=0;
      setSize=0;
      for(int i=0; i<10; i++){
        if(attributes[i]!="null")
          attributeSize++;
        if(i<5){
          if(operators[i]!='x')
            operatorSize++;
          if(sets[i]!="null")
            setSize++;
        }
      }  
    }
  }  
/************************************************************************/
  // End of Where class
/************************************************************************/
}