import com.sun.xml.internal.fastinfoset.util.StringArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by San on 11/4/2014.
 */
public class QueryOptimizer {

    //main method
    public static void  main(String[] args)throws IOException{
      //After reading query, query class have been generated.
      parser queryParser =  new parser(args[0]);
	  query initialQUery = new query(queryParser.parseQuery());
      System.out.println("STOP!");    // an easy spot to break and check variables to see if they are correct
        //testing purposes
//        newQuery.attributes.add("name");
//        newQuery.relations.add("STUDENT");
        //newQuery.relations.add("GRADE");
        //newQuery.relations.add("CONTACTS");
        //newQuery.relations.add("HOME");
//        newQuery.orderBy.add("age");
//        newQuery.where = newQuery.new whereStatement();
//        newQuery.where.conditions.add("age>10");
//        newQuery.where.operators.add("AND");
//        newQuery.where.operators.add("OR");
//        newQuery.where.conditions.add("name = Bob");
//        newQuery.where.conditions.add("age < 20");

        //test for subquery
        //newQuery.subquery = new query(newQuery);

        //Based on the new query object, construct a corresponding tree for that
        //QueryTree<List<String>> tree = new QueryTree<List<String>>();
        //tree.constructTree(newQuery);
        //output the tree to a graphviz file .gv
        //tree.output("C:/Users/San/Desktop/test.gv", true);
    }
}
