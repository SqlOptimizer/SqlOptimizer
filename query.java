/*
*  Katrina Ward and San Yeung
*  CS 5300 SQL Optimizer project
*  Description: Query class that will be used to hold each SQL query
*/

import java.util.List;

public class query {
    String[] attributes;                 // Select attributes
    String[] relations;                  // From which relations
    query subquery;                      // For nested queries
    whereStatement where;


    // Query default constructor
    public query() {
        attributes = new String[10];
        relations = new String[10];
        where = new whereStatement();
        subquery = null;
    }

    // Query Constructor
    public query(String[] att, String[] rel, String[] whereCond, String[] whereOps) {
        System.arraycopy(att, 0, attributes, 0, 10);
        System.arraycopy(rel, 0, relations, 0, 10);
        where.equals(whereCond, whereOps);
    }

    //check whether whereStatement is empty
    public boolean isWhereStatementEmpty() {
        if(this.where.conditionSize == 0){
            return true;
        }
        else {
            return false;
        }

    }

    //return the conditions in wherestatement to a list of string to store in data's node
    public String whereInfoToString() {
        String whereInfo = new String();
        int o = 0;
        int c = 0;

        while(c <= this.where.conditionSize-1){
            whereInfo = whereInfo + "(" + this.where.conditions[c] + ")";
            c++;
            if(o <= this.where.operatorSize-1){
                whereInfo = whereInfo + this.where.operators[o];
                o++;
            }
        }
        return whereInfo;
    }
    /*********************************************************************/
    // Begin Where class

    /**
     * *****************************************************************
     */
    private class whereStatement {                 // Where conditions
        String[] conditions;
        String[] operators;
        int operatorSize;
        int conditionSize;

        // Constructor
        public whereStatement() {
            conditions = new String[10];
            operators = new String[5];
            conditionSize = 0;
            operatorSize = 0;
            for (int i = 0; i < 10; i++) {
                conditions[i] = "null";
                if (i < 5)
                    operators[i] = "null";
            }
        }

        // Assignment
        public void equals(String[] cond, String[] ops) {
            System.arraycopy(cond, 0, conditions, 0, 10);
            System.arraycopy(ops, 0, operators, 0, 5);
            conditionSize = 0;
            operatorSize = 0;
            for (int i = 0; i < 10; i++) {
                if (conditions[i] != "null")
                    conditionSize++;
                if (i < 5) {
                    if (operators[i] != "null")
                        operatorSize++;
                }
            }
        }
    }
/************************************************************************/
// End of Where class
/************************************************************************/
}