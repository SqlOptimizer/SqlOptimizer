import com.sun.xml.internal.fastinfoset.util.StringArray;

import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
        ArrayList<ArrayList<String>> schema = new ArrayList<ArrayList<String>>();
        initiateSchema(schema);
        query initialQuery = new query();

        initialQuery.attributes.add("S.name");
        initialQuery.relations.add(new Tuple<String, String>("STUDENT", "S"));
        initialQuery.relations.add(new Tuple<String, String>("GRADE", "G"));
        initialQuery.relations.add(new Tuple<String, String>("CONTACTS", "C"));
//        initialQuery.relations.add(new Tuple<String, String>("HOME", "H"));
//        initialQuery.orderBy = new ArrayList<String>();
//        initialQuery.orderBy.add("S.age");
        initialQuery.where = initialQuery.new whereStatement();
        initialQuery.where.conditions.add("S.age>10");
        initialQuery.where.operators.add("AND");
        initialQuery.where.operators.add("AND");
        initialQuery.where.conditions.add("S.name = Bob");
        initialQuery.where.conditions.add("S.age < 20 OR G.age > 30");

        //test for subquery
        initialQuery.subquery = new query(initialQuery);

        //Based on the new query object, construct a corresponding tree for that
        QueryTree tree = new QueryTree();
        tree.constructTree(initialQuery);

        //set up the path locations
        String userHome = System.getProperty("user.home");
        //String originalPath = userHome+"\\Desktop\\original.gv";
        //String ruleOnePath = userHome+"\\Desktop\\ruleOne.gv";
        //String ruleTwoPath = userHome+"\\Desktop\\ruleTwo.gv";
        String ruleThreePath = userHome+"\\Desktop\\ruleThree.gv";
        String ruleFourPath = userHome+"\\Desktop\\ruleFour.gv";
        String ruleFivePath = userHome+"\\Desktop\\ruleFive.gv";
        String ruleSixPath = userHome+"\\Desktop\\ruleSix.gv";

        //set paths to be user defined so the results are not going into unexpected locations

        String originalPath = args[0];
        String ruleOnePath = args[0];
        String ruleTwoPath = args[0];

        //output the tree to a graphviz file .gv
        tree.toGraph(originalPath, true);

        //apply rule one and output the tree if there is one or more than one conjunction
        if(initialQuery.where.operators != null){
            if(initialQuery.where.operators.size() != 0){
                ruleOne(tree.getRoot(), initialQuery);
                tree.toGraph(ruleOnePath, true);
            }
        }

        //apply rule two if the number of relations is greater than one or if it contains a subquery
        if(initialQuery.relations.size() > 1 || initialQuery.subquery != null){
            ruleTwo(initialQuery, tree.getRoot());
            tree.toGraph(ruleTwoPath,true);
        }

        //apply rule four to form theta joins if there is one or more than one relations
        if(initialQuery.relations.size() > 1){
            ruleFour(initialQuery, tree.getRoot());
            tree.toGraph(ruleFourPath,true);
        }
        System.out.println("done");

    }

    // Fill in schema for data base
    public static void initiateSchema(ArrayList<ArrayList<String>> schema){
        schema.clear();
        ArrayList<String> relationTable = new ArrayList<String>();
        // Sailors relation
        relationTable.add("Sailors");
        relationTable.add("sid");
        relationTable.add("sname");
        relationTable.add("rating");
        relationTable.add("age");
        schema.add(new ArrayList<String>(relationTable)); //need to add as a new object, otherwise the reference will be changed
        relationTable.clear();

        // Boats relation
        relationTable.add("Boats");
        relationTable.add("bid");
        relationTable.add("color");
        schema.add(new ArrayList<String>(relationTable));
        relationTable.clear();

        // Reserves relation
        relationTable.add("Reserves");
        relationTable.add("sid");
        relationTable.add("bid");
        relationTable.add("day");
        schema.add(new ArrayList<String>(relationTable));
    }


    //given the alias, will search in the relations for the tuple that it belongs to
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

    //given a tuple, will search for its parent relation node
    //it will not search a node's right side if it contains a "Project" node immediately -> it does not search down for subquery tree
    //assuming a "project" node after a join signifies a "subquery"
    private static Node findHomeRelation(Node selectedNode,  Tuple<String, String> homeTuple) {
//        Node homeRelation = selectedNode.getLeftChild();
//
//        //locate the Join node
//        while(homeRelation.getName() != "JOIN"){
//            homeRelation = homeRelation.getLeftChild();
//        }
//
//        //examine the tuples data in the join node and decide go to right or left
//        while(homeRelation.getName() != "RELATION"){
//            if(homeRelation.getData().contains(homeTuple) || homeRelation.getName() == "SELECT"){
//                //go to the left
//                homeRelation = homeRelation.getLeftChild();
//            }
//            else{
//                //found it
//                homeRelation = homeRelation.getRightChild();
//            }
//        }
//        return homeRelation;

        //traverse down the tree to locate the homeRelation
        Node homeRelation = selectedNode;
        //if doesn't match
        if(!selectedNode.getData().get(0).equals(homeTuple)){
            //keep searching
            if(selectedNode.getLeftChild() != null){
                homeRelation = findHomeRelation(selectedNode.getLeftChild(), homeTuple);
                if(homeRelation == null){
                    //search the right child, ignore the right child in which is to  begins a subquery
                    if(selectedNode.getRightChild() != null && !selectedNode.getRightChild().getName().contentEquals("PROJECT")){
                        homeRelation = findHomeRelation(selectedNode.getRightChild(), homeTuple);
                    }
                    else{
                        homeRelation = null;
                    }
                }
            }
            else{
                //search the right child
                if(selectedNode.getRightChild() != null && !selectedNode.getRightChild().getName().contentEquals("PROJECT")){
                    homeRelation = findHomeRelation(selectedNode.getRightChild(), homeTuple);
                }
                else{
                    homeRelation = null;
                }
            }
        }
        return homeRelation;
    }

    //Given a tuple, search the subtrees of the node to see if it contains a Relation node that matches the tuple
    private static boolean RightContainsRelation(Node joinNode, Tuple<String, String> firstHomeTuple) {
        joinNode = joinNode.getRightChild();
        while(!joinNode.getName().equals("RELATION")){
            joinNode = joinNode.getLeftChild();
        }
        return joinNode.getData().get(0) == firstHomeTuple;
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
        else {
            //check to see if they are two different relations
            if (left.substring(i - 1, i).contentEquals(left.substring(j - 1, j))) {
                //still one relation
                result.add(left.substring(i - 1, i));
            } else {
                result.add(left.substring(i - 1, i));
                result.add(left.substring(j - 1, j));

            }
        }
        return result;
    }

    private static ArrayList<String> getAttributes(ArrayList<Tuple<String,String>> data, String relation){
        ArrayList<String> attributes = new ArrayList<String>();
        String[] tokens;
        String temp = new String();

        for(int i=0; i<data.size(); i++){
            tokens=data.get(i).getLeft().split("\\s");
            for(int j=0; j<tokens.length; j++){
                if(tokens[j].contains(".")){
                    temp=tokens[j].substring(0, tokens[j].indexOf(".")-1);
                    if(temp.equals(relation))
                        attributes.add(tokens[j]);
                }
            }
        }
        return attributes;
    }

    //optimization rule #1
    public static void ruleOne(Node tree, query initialQuery)throws IOException{
        //traverse the tree until you see select
        Node selectNode = tree;

        while(!selectNode.getName().equals("SELECT")){
            selectNode = selectNode.getLeftChild();
        }

        //selectNode located
        String[] cascadeConditions = selectNode.getData().get(0).getLeft().split("AND");

        //set current select node to assign a part of the condition
        ArrayList<Tuple<String, String>> data = new ArrayList<Tuple<String, String>>();
        Tuple<String, String> tuple = new Tuple<String, String>();
        tuple.setLeft(cascadeConditions[0]);
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
            tuple.setLeft(cascadeConditions[i]);
            tuple.setRight("null");
            data.add(new Tuple<String, String>(tuple));

            newNode.setData(new ArrayList<Tuple<String, String>>(data));
            selectNode = newNode;
        }

        //check to see if the query has a subquery
        if(initialQuery.subquery != null) {
            //check to see if the subquery has one or more conjunction
            if (initialQuery.subquery.where.operators.size() != 0) {
                //then apply rule one also

                //find the root of the subquery
                Node subqueryNode = tree;
                while (!subqueryNode.getName().contentEquals("JOIN")) {
                    subqueryNode = subqueryNode.getLeftChild();
                }
                subqueryNode = subqueryNode.getRightChild();

                //call RuleOne and apply to subquery
                ruleOne(subqueryNode, initialQuery.subquery);
            }
        }
    }

    //optimization rule #2
    private static void ruleTwo(query initialQuery, Node tree) throws IOException {
        Node selectedNode = tree;

        //locate the select node
        while(!selectedNode.getName().equals("SELECT")){
            selectedNode = selectedNode.getLeftChild();
        }

        while (selectedNode.getName().equals("SELECT")){
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
                //condition involves two different relations, move it down as far down as possible

                //check to see if a "join" node's right child is one of the two relations, if it is, then don't move the select node
                //else, move down to the "join" node's left side and repeat the process
                Node joinNode = new Node(nodeSelect);

                //first locate the join node
                while(!joinNode.getName().equals("JOIN")){
                    joinNode = joinNode.getLeftChild();
                }

                //if there is a subquery, then skip to the next join node, otherwise, ruleTwo would not work
                //it will see that the right subtrees contains its parent
                if(initialQuery.subquery != null){
                    joinNode = joinNode.getLeftChild();
                }

                Boolean completed = false;

                //create the two hometuples
                Tuple<String, String> firstHomeTuple = findHomeTuple(numRelationsInvolved.get(0), initialQuery);
                Tuple<String, String> secondHomeTuple = findHomeTuple(numRelationsInvolved.get(1), initialQuery);

                while (!completed){
                    if(RightContainsRelation(joinNode, firstHomeTuple) || RightContainsRelation(joinNode, secondHomeTuple)){
                        //if the right side contains the one of the two tuple, then don't move
                        completed = true;
                    }
                    else{
                        //move to the join node's left side
                        joinNode = joinNode.getLeftChild();

                        //move the selected node to be nearby the join node
                        nodeSelect.getParent().setLeftChild(nodeSelect.getLeftChild());
                        nodeSelect.getLeftChild().setParent(nodeSelect.getParent());
                        joinNode.getParent().setLeftChild(nodeSelect);
                        nodeSelect.setParent(joinNode.getParent());
                        nodeSelect.setLeftChild(joinNode);
                        joinNode.setParent(nodeSelect);
                    }
                }
                //select the next "select" node
                selectedNode = selectedNode.getLeftChild();
            }
        }

        //check to see if the query has a subquery
        if(initialQuery.subquery != null) {
            //check to see if the subquery has one or more relations, if true, then apply rule two also
            if (initialQuery.relations.size() > 1) {
                //find the root of the subquery
                Node subqueryNode = tree;
                while (!subqueryNode.getName().contentEquals("JOIN")) {
                    subqueryNode = subqueryNode.getLeftChild();
                }
                subqueryNode = subqueryNode.getRightChild();

                //call RuleTwo and apply it to the subquery
                ruleTwo(initialQuery.subquery, subqueryNode);
            }
        }
    }

    //optimization rule #3
    private static void ruleThree(QueryTree tree)throws IOException{
        ArrayList<Node> leaves = new ArrayList<Node>(tree.getLeaves());
        int[] numSelects = new int[leaves.size()];
        Node tempNode;
        Node greatestNode;

        if(leaves.size()==1)
            return;
        else{
            for(int i=0; i<leaves.size(); i++){
                numSelects[i]=0;
                tempNode=leaves.get(i).getParent();
                while(!tempNode.getName().equals("JOIN")){
                    if(tempNode.getName().equals("SELECT"))
                        numSelects[i]++;
                    tempNode=tempNode.getParent();
                }
            }
            // check if each has the same number of selects
            boolean flag=true;
            for(int i=1; i<numSelects.length; i++){
                if(numSelects[i]!=numSelects[0])
                    flag = false;
            }
            if(flag) // All branches have same number of selects
                return;
            else{
                flag = true;
                // Check if already in correct order
                for(int i=1; i<numSelects.length; i++){
                    if(numSelects[i]<numSelects[i-1])
                        flag=false;
                }
                if(flag)// Already in correct order
                    return;
                else{
                    int greatestIndex=0;
                    int secondGreatest=1;
                    for(int i=1; i<numSelects.length; i++){
                        if(numSelects[i]>numSelects[greatestIndex])
                            greatestIndex=i;
                        else if(numSelects[i]==numSelects[greatestIndex])
                            secondGreatest=i;
                    }
                    greatestNode=leaves.get(greatestIndex);
                    tempNode=leaves.get(secondGreatest);
                    while(!greatestNode.getName().equals("JOIN") || !tempNode.getName().equals("JOIN")){
                        if(!greatestNode.getName().equals("JOIN"))
                            greatestNode=greatestNode.getParent();
                        if(!tempNode.getParent().equals("JOIN"))
                            tempNode=tempNode.getParent();
                    }
                    if(greatestNode==tempNode)
                        return;
                }
            }
        }
    }

    //optimization rule #4: forming theta joins
    private static void ruleFour(query initialQuery, Node root) {
        Node joinNode = root;

        //get the number of relations
        int relationSize = initialQuery.relations.size();

        for(int i = 1; i < relationSize; i++){
            //locate join node
            while(!joinNode.getName().contentEquals("JOIN")){
                joinNode = joinNode.getLeftChild();
            }

            if(!joinNode.getParent().getName().contentEquals("SELECT")){
                //do nothing
            }
            else{
                //combine them
                String data = joinNode.getParent().getData().get(0).getLeft();
                joinNode.getData().get(0).setLeft(data);

                joinNode.getParent().getParent().setLeftChild(joinNode);
                joinNode.setParent(joinNode.getParent().getParent());
            }

            //go to the next join node
            joinNode = joinNode.getLeftChild();
        }

        //check for subquery
        if(initialQuery.subquery != null){
            //get the subquery root
            Node subqueryNode = root;
            while(!subqueryNode.getName().contentEquals("JOIN")){
                subqueryNode =  subqueryNode.getLeftChild();
            }
            ruleFour(initialQuery.subquery, subqueryNode.getRightChild());
        }
    }

    //optimization rule #5
    private static void ruleFive(QueryTree tree, ArrayList<ArrayList<String>> schema) throws IOException{
        ArrayList<Node> leaves = new ArrayList<Node>(tree.getLeaves());         // List of leaf nodes in the tree (Pointers to the relation nodes)
        ArrayList<String> attributes = new ArrayList<String>();   // Used to collect a list of needed attributes
        Node tempNode;                       // Current node to check what needs to be projected
        Node itrNode=null;                        // Walks up the tree collecting attributes
        int schemaIndex=0;                   // Index of the schema for the relation currently being addressed
        Node newNode;                        // For adding a new node to the tree
        String currentRelation;
        ArrayList<Tuple<String, String>> newData;
        for(int i=0; i<leaves.size(); i++){    // Starting at leaf nodes
            attributes=new ArrayList<String>();  // Reset attributes
            tempNode = leaves.get(i);            // get relation to work with
            if(tempNode.getData().get(0).rightNull())
                currentRelation = new String(tempNode.getData().get(0).getLeft());
            else
                currentRelation = new String(tempNode.getData().get(0).getRight());

            // Start at leaf and work up the tree to find where to insert PROJECT nodes
            while(tempNode!=tree.getRoot() && tempNode!=tree.getRoot().getLeftChild() && tempNode!=tree.getRoot().getRightChild()){
                if(tempNode.getName().equals("PROJECT")){     // Skip PROJECT nodes
                    tempNode=tempNode.getParent();
                }
                else{
                    itrNode=tempNode.getParent();
                    // Make sure iterator isn't counting attributes at already made PROJECT nodes
                    if(itrNode.getName().equals("PROJECT") && itrNode!=tree.getRoot())
                        itrNode = itrNode.getParent();

                    // Find attributes needed in tree
                    while(itrNode != null){
                        if(itrNode.getName().equals("PROJECT"))
                            itrNode=itrNode.getParent();
                        if(itrNode==null)
                            break;
                        attributes.addAll((getAttributes(itrNode.getData(), currentRelation)));
                        itrNode=itrNode.getParent();
                    }

                    // Add project node to tree
                    // remove duplicates from attribute list
                    HashSet<String> hs = new HashSet();
                    hs.addAll(attributes);
                    attributes.clear();
                    attributes.addAll(hs);
                    // Create data in right format
                    newData=new ArrayList<Tuple<String, String>>();
                    for(int k=0; k<attributes.size(); k++){
                        newData.add(new Tuple<String, String>(attributes.get(k), null));
                    }
                    if(tempNode.getParent().getName().equals("PROJECT")){   // Add to existing Project Node
                        newData.addAll(tempNode.getParent().getData());
                        tempNode.getParent().setData(newData);
                    }
                    else{   // Create project node
                        newNode = new Node(new ArrayList<Tuple<String, String>>(newData), "PROJECT");
                        newNode.setParent(tempNode.getParent());
                        if(tempNode.getParent().getLeftChild()==tempNode){
                            newNode.setLeftChild(tempNode);
                            tempNode.getParent().setLeftChild(newNode);
                        }
                        else{
                            newNode.setRightChild(tempNode);
                            tempNode.getParent().setRightChild(newNode);
                        }
                        tempNode.setParent(newNode);
                    }
                    tempNode=tempNode.getParent();
                }
            }
            // Print tree after this optimization
            tree.toGraph("rule5.gv", false);
        }
    }

    // optimization rule #6
    private static void ruleSix(QueryTree tree) throws IOException{

      ArrayList<Node> leaves = new ArrayList<Node>(tree.getLeaves());
      Node currentNode;
      Node comparingNode;
      boolean equal=true;

      if(leaves.size()==1)     // Only one branch, so no need to check
        return;
      else{
        for(int i =0; i<leaves.size()-1; i++){
          equal=true;                             // set flag
          currentNode = leaves.get(i);           // set working node         
          for( int j=i+1; j<leaves.size(); j++){
            if(leaves.get(i).getName().equals(leaves.get(j).getName()))     // Only continue if the leaf nodes match
            {
              comparingNode=leaves.get(j);                                 // Set iterating node
              // Walk up until both nodes are just before a JOIN node
              while(!currentNode.getParent().getName().equals("JOIN") || !comparingNode.getParent().getName().equals("JOIN")){
                if(!currentNode.equals(comparingNode))
                  equal=false;
                if(!currentNode.getParent().getName().equals("JOIN"))
                  currentNode=currentNode.getParent();                  
                if(!comparingNode.getParent().getName().equals("JOIN"))
                  comparingNode=comparingNode.getParent();
              }
              if(!equal)   // If the branch isn't equal, keep them as they are
                break;
              else{        // exact same branches need to be merged (delete excess branch)
                if(comparingNode.getParent().getLeftChild() == comparingNode){
                  comparingNode=comparingNode.getParent();
                  comparingNode.setLeftChild(null);
                }
                else{
                  comparingNode=comparingNode.getParent();
                  comparingNode.setRightChild(null);
                }
                // Need to make sure the JOIN conditions are still met (merge joins)
                currentNode=currentNode.getParent();
                ArrayList<Tuple<String, String>> badJoin = new ArrayList<Tuple<String, String>>(comparingNode.getData());
                ArrayList<Tuple<String, String>> mergedJoin = new ArrayList<Tuple<String, String>>(currentNode.getData());
                for(int k=0; k<badJoin.size(); k++){
                  if(!mergedJoin.contains(badJoin.get(k)))
                    mergedJoin.add(badJoin.get(k));
                }
                currentNode.setData(mergedJoin);
                // Remove unneeded JOIN node
                if(currentNode.getLeftChild()!=null){
                  currentNode.getLeftChild().setParent(currentNode.getParent());
                  if(currentNode.getParent().getLeftChild()==currentNode)
                    currentNode.getParent().setLeftChild(currentNode.getLeftChild());
                  else
                    currentNode.getParent().setRightChild(currentNode.getLeftChild());
                  currentNode.setParent(null);
                  currentNode.setLeftChild(null);
                }
                else{
                  currentNode.getRightChild().setParent(currentNode.getParent());
                  if(currentNode.getParent().getLeftChild()==currentNode)
                    currentNode.getParent().setLeftChild(currentNode.getRightChild());
                  else
                    currentNode.getParent().setRightChild(currentNode.getRightChild());
                  currentNode.setParent(null);
                  currentNode.setRightChild(null);
                }
              }
            }
          }
        }
      }
    }
}
