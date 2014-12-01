/**
 * Authors: Katrina Ward and San Yeung
 * Description: Main Class
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;


public class QueryOptimizer {
    //main method
    public static void  main(String[] args)throws IOException{
//      parser queryParser =  new parser(args[0]);
//      String output = new String(args[1]);
//      query initialQuery = new query(queryParser.parseQuery());
//      System.out.println("STOP!");    // an easy spot to break and check variables to see if they are correct

       //testing purposes
        ArrayList<ArrayList<String>> schema = new ArrayList<ArrayList<String>>();
        initiateSchema(schema);
        query initialQuery = new query();

        initialQuery.attributes.add("S.sname");
        initialQuery.relations.add(new Tuple<String, String>("Sailors", "S"));
        initialQuery.relations.add(new Tuple<String, String>("Reserves", "R"));
        initialQuery.relations.add(new Tuple<String, String>("Boats", "B"));
//        initialQuery.relations.add(new Tuple<String, String>("HOME", "H"));
//        initialQuery.orderBy = new ArrayList<String>();
//        initialQuery.orderBy.add("S.age");
        initialQuery.where = initialQuery.new whereStatement();
        initialQuery.where.conditions.add("S.sid=R.sid");
        initialQuery.where.operators.add("AND");
        initialQuery.where.conditions.add("R.bid=B.bid");
        initialQuery.where.operators.add("AND");
        initialQuery.where.conditions.add("B.color='green'");
        //initialQuery.where.conditions.add("S.age < 20 OR G.age > 30");

        //test for subquery
        //initialQuery.subquery = new query(initialQuery);

        //Based on the new query object, construct a corresponding tree for that
        QueryTree tree = new QueryTree();
        tree.constructTree(initialQuery);

        //set up the path locations
        String userHome = System.getProperty("user.home");
        String originalPath = userHome+"\\Desktop\\original.gv";
        String ruleOnePath = userHome+"\\Desktop\\ruleOne.gv";
        String ruleTwoPath = userHome+"\\Desktop\\ruleTwo.gv";
        String ruleThreePath = userHome+"\\Desktop\\ruleThree.gv";
        String ruleFourPath = userHome+"\\Desktop\\ruleFour.gv";
        String ruleFivePath = userHome+"\\Desktop\\ruleFive.gv";
        String ruleSixPath = userHome+"\\Desktop\\ruleSix.gv";

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
            ruleTwo(initialQuery, tree.getRoot(), schema);
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


    //Given the alias, will search in the relations for the tuple that it belongs to
    private static Tuple<String, String> findHomeTuple(String s, query initialQuery) {
        Tuple<String, String> homeTuple = null;

        for(Tuple<String, String> tuple : initialQuery.relations){
            //first check to see if the string matches the data stored in the data's left field
            if(s.contentEquals(tuple.getLeft())){
                //found it
                homeTuple = tuple;
            }
            //then check to see if match the data stored in the data's right field
            else if(s.contentEquals(tuple.getRight())){
                //found it
                homeTuple = tuple;
            }
        }

        //output system error when a home tuple cannot be found -> something is wrong
        if(homeTuple == null){
            //can't find tuple
            System.err.println("Can't locate home tuple");
            System.exit(1);
        }

        return homeTuple;
    }

    //Given a tuple, will search for its parent relation node
    //it will not search a node's right side if it contains a "Project" node immediately -> it does not search down for subquery tree
    //assuming a "project" node after a join signifies a "subquery"
    private static Node findHomeRelation(Node selectedNode,  Tuple<String, String> homeTuple) {
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
        while(!joinNode.getName().contentEquals("RELATION")){
            joinNode = joinNode.getLeftChild();
        }
        return joinNode.getData().get(0) == firstHomeTuple;
    }

    //Return an arraylist containing the alias/relation name involved in a select statement
    private static ArrayList<String> getNumRelationsInvolved(String left, query initialQuery) {
        ArrayList<String> result = new ArrayList<String>();

        //get dot index
        int i = left.indexOf(".");
        int j = left.lastIndexOf(".");

        //if the indexes are the same, that mean only one relation, else two relations
        if(i == j){
            result.add(left.substring(1,i));
        }
        else {
            //first get the first alias/relation name before the dot
            String firstPart = left.substring(1,i);
            String remaining = left.substring(i+1);

            //add firstpart to result list
            result.add(firstPart);

            //find out the remaining relation
            for(Tuple<String, String> tuple : initialQuery.relations){
                if(remaining.contains(tuple.getLeft())){
                    result.add(tuple.getLeft());
                }
                else if(remaining.contains(tuple.getRight())){
                    result.add(tuple.getRight());
                }
            }

            //Output System Error
            if(result.size() != 2){
                System.err.println("Can't find valid tuple");
                System.exit(1);
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

    //Optimization rule #1
    //It will break down a conjunctive select statement if it contains any cascading selects.
    //It will break down the select into individual select statement and left the OR statements intact.
    //It will create new nodes for each broken down new select statement and insert into the tree.
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
        tuple.setRight(null);
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
            tuple.setRight(null);
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

    //Optimization rule #2
    //It will move the select statement as far down as possible.
    //If the select statement only contains one relation, it will relocate to position close to its home relation.
    //Else it will be moved to the nodes that occur after immediately joining the two relations.
    private static void ruleTwo(query initialQuery, Node tree, ArrayList<ArrayList<String>> schema) throws IOException {
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
            ArrayList<String> numRelationsInvolved = getNumRelationsInvolved(nodeSelect.getData().get(0).getLeft(), initialQuery);

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
                ruleTwo(initialQuery.subquery, subqueryNode, schema);
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

    //Optimization rule #4: forming theta joins
    //This rule will combine a select statement with a Cartesian Product to form a theta join if the
    //select statement qualifies for the joining condition
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
                //check for all of the select nodes available
                Node selectNode = joinNode.getParent();

                while(selectNode.getName().contentEquals("SELECT")){
                    //check to see if the condition represents a join condition
                    String condition = selectNode.getData().get(0).getLeft();

                    //if it's not a OR statement
                    if(!condition.contains("OR")){
                        //if contains a equal operator
                        if(condition.contains("=")){
                            //if both attributes on the left and on the right are the same
                            String leftAttr = condition.substring(1,condition.indexOf("="));
                            leftAttr = leftAttr.substring(leftAttr.indexOf(".")+1);

                            String rightAttr = condition.substring(condition.indexOf("=")+1);
                            if(rightAttr.contains(leftAttr)){
                                //then it's a join condition

                                //combine them
                                String data = joinNode.getParent().getData().get(0).getLeft();
                                joinNode.getData().get(0).setLeft(data);

                                joinNode.getParent().getParent().setLeftChild(joinNode);
                                joinNode.setParent(joinNode.getParent().getParent());
                            }
                        }
                    }
                    selectNode = selectNode.getParent();
                }
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
    private static void ruleFive(QueryTree tree) throws IOException{
        ArrayList<Node> leaves = new ArrayList<Node>(tree.getLeaves());         // List of leaf nodes in the tree (Pointers to the relation nodes)
        ArrayList<String> attributes = new ArrayList<String>();   // Used to collect a list of needed attributes
        Node tempNode;                       // Current node to check what needs to be projected
        Node itrNode=null;                        // Walks up the tree collecting attributes
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

        if(leaves.size()==1)
            return;
        else{
            for(int i =0; i<leaves.size()-1; i++){
                currentNode = leaves.get(i);
                for( int j=i+1; j<leaves.size(); j++){
                    comparingNode=leaves.get(j);

                }
            }
        }

    }
}
