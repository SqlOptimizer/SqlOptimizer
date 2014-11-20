import com.sun.xml.internal.fastinfoset.util.StringArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by San on 11/4/2014.
 */
public class QueryOptimizer {

    //main method
    public static void  main(String[] args)throws IOException{
      //After reading query, query class have been generated.
//      parser queryParser =  new parser(args[0]);
//      query initialQuery = new query(queryParser.parseQuery());
//      System.out.println("STOP!");    // an easy spot to break and check variables to see if they are correct
        //testing purposes
        
        query initialQuery = new query(); 
        initialQuery.attributes.add("name");
        initialQuery.relations.add(new Tuple<String, String>("STUDENT", "S"));
        initialQuery.relations.add(new Tuple<String, String>("GRADE", "G"));
//        initialQuery.relations.add("CONTACTS");
//        initialQuery.relations.add("HOME");
//        initialQuery.orderBy = new ArrayList<String>();
//        initialQuery.orderBy.add("age");
//        initialQuery.where = initialQuery.new whereStatement();
//        initialQuery.where.conditions.add("age>10");
//        initialQuery.where.operators.add("AND");
//        initialQuery.where.operators.add("AND");
//        initialQuery.where.conditions.add("name = Bob");
//        initialQuery.where.conditions.add("age < 20");

        //test for subquery
        //initialQuery.subquery = new query(initialQuery);

        //Based on the new query object, construct a corresponding tree for that
        QueryTree<List<String>> tree = new QueryTree<List<String>>();
        tree.constructTree(initialQuery);

        //output the tree to a graphviz file .gv
        tree.output("C:/Users/San/Desktop/original.gv", true);

        //apply rule one and output the tree if there is one or more than one conjunction
        if(initialQuery.where.operators != null){
            if(initialQuery.where.operators.size() != 0){
                ruleOne(tree);
            }
        }

        //apply rule two if the number of relations is greater than one or if it contains a subquery
        if(initialQuery.relations.size() > 1 || initialQuery.subquery != null){
            ruleTwo(tree);
        }

        System.out.println("done");

    }

    //optimization rule 1
    public static void ruleOne(QueryTree tree)throws IOException{
        //traverse the tree until you see select
        Node selectNode = tree.getRoot();

        while(selectNode.getName() != "SELECT"){
            selectNode = selectNode.getLeftChild();
        }

        //selectNode located
        String[] cascadeConditions = selectNode.getData().get(0).toString().split("AND");

        //working on only two conditions right now
        //set current select node to assign a part of the condition
        selectNode.setData(Arrays.asList(cascadeConditions[0]));

        for(int i = 1; i < cascadeConditions.length; i++){
            Node<List<String>> newNode = new Node<List<String>>();
            newNode.setParent(selectNode);
            newNode.setLeftChild(selectNode.getLeftChild());
            selectNode.setLeftChild(newNode);
            selectNode.getLeftChild().setParent(newNode);
            newNode.setName("SELECT");
            //newNode.setData(Arrays.asList(cascadeConditions[i]));
            selectNode = newNode;
        }
        tree.output("C:/Users/San/Desktop/ruleOne.gv", true);
    }

    //optimization rule #2
    private static void ruleTwo(QueryTree<List<String>> tree) {
        //if only one relations then do not need to apply the rule

    }
}
