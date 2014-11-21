import com.sun.xml.internal.fastinfoset.util.StringArray;

import java.io.IOException;
import java.lang.reflect.Array;
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
        initialQuery.attributes.add("S.name");
        initialQuery.relations.add(new Tuple<String, String>("STUDENT", "S"));
        initialQuery.relations.add(new Tuple<String, String>("GRADE", "G"));
//        initialQuery.relations.add(new Tuple<String, String>("CONTACTS", "C"));
//        initialQuery.relations.add(new Tuple<String, String>("HOME", "H"));
//        initialQuery.orderBy = new ArrayList<String>();
//        initialQuery.orderBy.add("S.age");
        initialQuery.where = initialQuery.new whereStatement();
        initialQuery.where.conditions.add("S.age>10");
        initialQuery.where.operators.add("AND");
//        initialQuery.where.operators.add("AND");
        initialQuery.where.conditions.add("S.name = Bob");
//        initialQuery.where.conditions.add("S.age < 20");

        //test for subquery
        //initialQuery.subquery = new query(initialQuery);

        //Based on the new query object, construct a corresponding tree for that
        QueryTree tree = new QueryTree();
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
            ruleTwo(initialQuery, tree);
        }

        System.out.println("done");

    }

    //optimization one
    public static void ruleOne(QueryTree tree)throws IOException{
        //traverse the tree until you see select
        Node selectNode = tree.getRoot();

        while(selectNode.getName() != "SELECT"){
            selectNode = selectNode.getLeftChild();
        }

        //selectNode located
        String[] cascadeConditions = selectNode.getData().get(0).getLeft().split("AND");


        //set current select node to assign a part of the condition
        ArrayList<Tuple<String, String>> data = new ArrayList<Tuple<String, String>>();
        Tuple<String, String> tuple = new Tuple<String, String>();
        int j = cascadeConditions[0].indexOf(".");
        tuple.setLeft(new String(cascadeConditions[0]));
        tuple.setRight("null");
        data.add(new Tuple<String, String>(tuple));
        selectNode.setData(new ArrayList<Tuple<String, String>>(data));

        for(int i = 1; i < cascadeConditions.length; i++){
            Node newNode = new Node();
            newNode.setParent(selectNode);
            newNode.setLeftChild(selectNode.getLeftChild());
            selectNode.getLeftChild().setParent(newNode);
            selectNode.setLeftChild(newNode);
            newNode.setName("SELECT");

            data.clear();
            j = cascadeConditions[i].indexOf(".");
            tuple.setLeft(new String(cascadeConditions[i]));
            tuple.setRight("null");
            data.add(new Tuple<String, String>(tuple));

            newNode.setData(new ArrayList<Tuple<String, String>>(data));
            selectNode = newNode;
        }
        tree.output("C:/Users/San/Desktop/ruleOne.gv", true);
    }

    //optimization rule #2
    private static void ruleTwo(query initialQuery, QueryTree tree) throws IOException {
        Node selectedNode = tree.getRoot();

        while(selectedNode.getName() != "SELECT"){
            selectedNode = selectedNode.getLeftChild();
        }

        while (selectedNode.getName() == "SELECT"){
            //if the attributes of a select statement only applies to one relation, then move down
            //else no change
            //Check to see how many relations the condition involves
            Node nodeSelect = new Node(selectedNode);
            ArrayList<String> numRelationsInvolved = getNumRelationsInvolved(nodeSelect.getData().get(0).getLeft());

            //if the size of the list is one, then only one relation
            if(numRelationsInvolved.size() == 1){
                //move down to the home relation
                //create the home tuple
                Tuple<String, String> homeTuple = findHomeTuple(numRelationsInvolved.get(0), initialQuery);
                Node homeRelation = findHomeRelation(nodeSelect, homeTuple);

                //move the selected node to be nearby its homeRelation
                nodeSelect.getParent().setLeftChild(nodeSelect.getLeftChild());
                nodeSelect.getLeftChild().setParent(nodeSelect.getParent());
                //Determine if the homeRelation is a left child or right child
                if(homeRelation.getParent().getLeftChild() == homeRelation){
                    homeRelation.getParent().setLeftChild(nodeSelect);
                    nodeSelect.setParent(homeRelation.getParent());
                    nodeSelect.setLeftChild(homeRelation);
                    homeRelation.setParent(nodeSelect);
                }
                else{
                    homeRelation.getParent().setRightChild(nodeSelect);
                    nodeSelect.setParent(homeRelation.getParent());
                    nodeSelect.setLeftChild(homeRelation);
                    homeRelation.setParent(nodeSelect);
                }

                selectedNode = selectedNode.getLeftChild();
            }
            else{
                //condition involves two different relations, no change
                selectedNode = selectedNode.getLeftChild();
            }
        }
        tree.output("C:/Users/San/Desktop/ruleTwo.gv", true);
    }

    private static Tuple<String, String> findHomeTuple(String s, query initialQuery) {
        Tuple<String, String> homeTuple = null;
        for(Tuple<String, String> tuple : initialQuery.relations){
            if(s.contentEquals(tuple.getRight())){
                //found the home tuple
                homeTuple = tuple;
            }
        }
        return homeTuple;
    }

    private static Node findHomeRelation(Node selectedNode,  Tuple<String, String> homeTuple) {
        Node homeRelation = selectedNode.getLeftChild();

        //locate the Join node
        while(homeRelation.getName() != "JOIN"){
            homeRelation = homeRelation.getLeftChild();
        }

        //examine the tuples data in the join node and decide go to right or left
        while(homeRelation.getName() != "RELATION"){
            if(homeRelation.getData().contains(homeTuple) || homeRelation.getName() == "SELECT"){
                //go to the left
                homeRelation = homeRelation.getLeftChild();
            }
            else{
                //found it
                homeRelation = homeRelation.getRightChild();
            }
        }
        return homeRelation;
    }

    //return the an arraylist containing the number of relations involved in a select statement
    private static ArrayList<String> getNumRelationsInvolved(String left) {
        ArrayList<String> result = new ArrayList<String>();

        //get dot index
        int i = left.indexOf(".");
        int j = left.lastIndexOf(".");

        //if the indexes are the same, that mean only one relation, else two relations
        if(i == j){
            result.add(left.substring(i-1,i));
        }
        else{
            //get the first relation
            result.add(left.substring(i-1,i));
            result.add(left.substring(j-1,j));
        }
        return result;
    }
}
