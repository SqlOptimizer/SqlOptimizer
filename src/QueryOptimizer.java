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
      parser queryParser =  new parser(args[0]);
	  query initialQUery = new query(queryParser.parseQuery());
      System.out.println("STOP!");    // an easy spot to break and check variables to see if they are correct
        //testing purposes
        /*initialQUery.attributes.add("name");
        initialQUery.relations.add("STUDENT");
        initialQUery.relations.add("GRADE");
        initialQUery.relations.add("CONTACTS");
        initialQUery.relations.add("HOME");
        initialQUery.orderBy.add("age");
        initialQUery.where = initialQUery.new whereStatement();
        initialQUery.where.conditions.add("age>10");
        initialQUery.where.operators.add("AND");
        initialQUery.where.operators.add("AND");
        initialQUery.where.conditions.add("name = Bob");
        initialQUery.where.conditions.add("age < 20");

        //test for subquery
        initialQUery.subquery = new query(initialQUery);

        //Based on the new query object, construct a corresponding tree for that
        QueryTree<List<String>> tree = new QueryTree<List<String>>();
        //tree.constructTree(initialQUery);
        //output the tree to a graphviz file .gv

        tree.output("C:/Users/San/Desktop/test.gv", true);

        //apply rule one and output the tree if there is one or more than one conjunction
        if(initialQUery.where.operators.size() != 0){
            ruleOne(tree);*/
       // }
       // System.out.println();

    }

   /* public static void ruleOne(QueryTree tree){
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
            newNode.setData(Arrays.asList(cascadeConditions[i]));
            selectNode = newNode;
        }
        //tree.output("C:/Users/San/Desktop/test.gv", true);

    }*/
}
