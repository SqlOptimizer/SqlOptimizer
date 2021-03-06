/**
 * Authors: Katrina Ward and San Yeung
 * Description: Main Class
 */

import javax.management.Query;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;

/*********************************************************************************/
/*               Method Documentation                                            */
/*********************************************************************************/

/*
 * initiateSchema(ArrayList<ArrayList<String>>)
 * Description: Loads the schema of the database into a list
 * Pre: None
 * Post: Schema will contain the database schema. Each list will the name of a relation followed by its attributes 
 * Param: Structure to hold schema
 * 
 * findHomeTuple(String, query)
 * Description: Given an alias, will find the relation it came from
 * Pre: None
 * Post: Will return the relation the alias belongs to
 * Param 1: Alias
 * Param 2: Query where alias was defined
 * 
 * findHomeRelation(Node, Tuple)
 * Description: Given a Tuple, will start at the given Node and find the relation that the Tuple belongs to
 * Pre: Node must be in the same branch as the relation being searched for
 * Post: Will return a pointer to the node representing the relation in the Tuple
 * Param 1: Node to start search from
 * Param 2: Tuple containing relation or alias to search for a relation for
 * 
 * getAttributes(ArrayList<Tuple>>, String)
 * Description: Finds all the attributes in the list that belong to the given relation
 * Pre: None
 * Post: Returns a list of attributes that are in the given list and belong to the given relation
 * Param 1: List to fins attributes in
 * Param 2: Relation the attributes must belong to
 * 
 * ruleOne(Node, query)
 * Description: Applies the requirements of rule one of query optimization where select statements are broken up
 * Pre: Node must be the root of the tree, query must be the same query the tree was built from
 * Post: Tree will now represent the status of the query after rule one is applied
 * Param 1: Root node of the tree
 * Param 2: Query that the tree was formed from
 * 
 * ruleTwo(query, Node, ArrayList<ArrayList<String>>)
 * Description: Applies optimization rule 2 where Select statements are moved down the tree as far as possible
 * Pre: Best if rule one is applied first
 * Post: Tree will now be in a state that represents rule two
 * Param 1: Query tree was built from
 * Param 2: Root node of tree
 * Param 3: Schema of the database
 * 
 * ruleThree(QueryTree)
 * Description: Applies rule 3 where leaf branches are rearranged so the most restrictive selects are applied first
 * Pre: Best if Rules 1 and 2 are applied first
 * Post: Changes the tree to represent the state where rule 3 has been applied. Will move branches and nodes
 * Param: Tree to apply rule to
 * 
 * ruleFour(query, Node)
 * Description: Applies rule four where cartesian products and selects are combined to form Joins
 * Pre: Use previous rules first
 * Post: Will change the status of the tree by removing and adding nodes to represent the state of the tree after rule 4
 * Param 1: Query tree was formed from
 * Param 2: Root node of tree
 * 
 * ruleFive(QueryTree)
 * Description: Applies rule five to the tree where projection nodes are added
 * Pre: All previous rules run first
 * Post: Will change the tree to add projection nodes
 * Param: tree to apply rule to
 * 
 * ruleSix(QueryTree)
 * Description: Branches of the tree that are exactly the same are merged together
 * Pre: All other rules applied first
 * Post: Will delete nodes and change data of join nodes
 * Param: Tree to apply rule to
 */
public class QueryOptimizer {

    //main method
    public static void  main(String[] args)throws IOException, Exception{
        //queryString will return a string representation of the SQL query
        String queryString = doublenested(args[0]);
        //If the queryString contains IN and one of the set operators, then we take the special approach
        if(queryString.contains(" IN ")){
            if(queryString.contains("INTERSECT") || queryString.contains("UNION") || queryString.contains("EXCEPT") ||
                    queryString.contains("DIFFERENCE")){
                handleINandNestedWithinNested(queryString, args[1]);
            }
            else{
                handleGeneralQueryOptimziation(args[0], args[1]);
            }
        }
        else{
            handleGeneralQueryOptimziation(args[0], args[1]);
        }
    }

/*****************************************************************************************/
/*                    Methods                                                            */
    /*****************************************************************************************/
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

    private static boolean RightContainsRelation(Node joinNode, Tuple<String, String> firstHomeTuple) {
        joinNode = joinNode.getRightChild();
        while(!joinNode.getName().contentEquals("RELATION")){
            joinNode = joinNode.getLeftChild();
        }
        return joinNode.getData().get(0) == firstHomeTuple;
    }

    private static ArrayList<String> getNumRelationsInvolved(String left, query initialQuery) {
        ArrayList<String> result = new ArrayList<String>();

        //Process the strings to remove any parenthesies and spaces
        left = left.replaceAll(" ", "");
        left= left.replaceAll("\\(", "");
        left = left.replaceAll("\\)", "");

        //get dot index
        int i = left.indexOf(".");
        int j = left.lastIndexOf(".");

        //if the indexes are the same, that mean only one relation, else two relations
        if(i == j){
            result.add(left.substring(0,i));
        }
        else {
            //first get the first alias/relation name before the dot
            String firstPart = left.substring(0,i);
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
            if(result.size() < 1 || result.size() > 2){
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
                    temp=tokens[j].substring(0, tokens[j].indexOf("."));
                    if(temp.equals(relation)){
                        if(tokens[j].contains("=")){
                            if(!attributes.contains(tokens[j]))
                                attributes.add(tokens[j].substring(0, tokens[j].indexOf("=")));
                        }
                        else{
                            if(!attributes.contains(tokens[j]))
                                attributes.add(tokens[j]);
                        }
                    }
                }
            }
        }
        return attributes;
    }

    //Handle General Optimization
    private static void handleGeneralQueryOptimziation(String file, String outputLocation)throws IOException{
        parser queryParser =  new parser(file);
        String output = new String(outputLocation);
        ArrayList<query> initialQueries = new ArrayList<query>();
        initialQueries.addAll(queryParser.parseQuery());
        ArrayList<ArrayList<String>> schema = new ArrayList<ArrayList<String>>();
        initiateSchema(schema);

        //A list of query-trees, so that if initialQueries contain more than one queries, it can still handle it
        ArrayList<QueryTree> trees = new ArrayList<QueryTree>();

        //Construct a query
        QueryTree tree = new QueryTree();
        tree.constructTree(initialQueries.get(0));
        tree.toGraph(output+"original1.gv", true);

        //apply all the rules
        ruleOne(tree.getRoot(), initialQueries.get(0));
        tree.toGraph(output+"ruleOne1.gv", true);
        ruleTwo(initialQueries.get(0), tree.getRoot(), schema);
        tree.toGraph(output+"ruleTwo1.gv", true);
        ruleThree(tree, initialQueries.get(0), schema);
        tree.toGraph(output + "ruleThree1.gv", true);
        ruleFour(initialQueries.get(0), tree.getRoot());
        tree.toGraph(output+"ruleFour1.gv", true);
        ruleFive(tree);
        tree.toGraph(output+"ruleFive1.gv", true);
        ruleSix(tree);
        tree.toGraph(output+"ruleSix1.gv", true);

        trees.add(new QueryTree(tree));
        // check for a second query from set operators
        if(initialQueries.size()>1){
            tree=new QueryTree();
            tree.constructTree(initialQueries.get(1));
            tree.toGraph(output+"original2.gv", true);

            //apply all the rules
            ruleOne(tree.getRoot(), initialQueries.get(1));
            tree.toGraph(output+"ruleOne2.gv", true);
            ruleTwo(initialQueries.get(1), tree.getRoot(), schema);
            tree.toGraph(output+"ruleTwo2.gv", true);
            ruleThree(tree, initialQueries.get(1), schema);
            tree.toGraph(output+"ruleThree2.gv", true);
            ruleFour(initialQueries.get(1), tree.getRoot());
            tree.toGraph(output+"ruleFour2.gv", true);
            ruleFive(tree);
            tree.toGraph(output+"ruleFive2.gv", true);
            ruleSix(tree);
            tree.toGraph(output+"ruleSix2.gv", true);
            //check for union, etc.
            if(query.union){
                //Merge the two trees
                QueryTree unionTree = new QueryTree();
                unionTree.constructSetOperationTree(initialQueries, trees, tree, "UNION");
                unionTree.toGraph(output+"final.gv", true);
            }else if (query.intersect){
                //Merge the two trees
                QueryTree intersectTree = new QueryTree();
                intersectTree.constructSetOperationTree(initialQueries, trees, tree, "INTERSECT");
                intersectTree.toGraph(output+"final.gv", true);
            }else if(query.difference){
                //Merge the two trees
                QueryTree differenceTree = new QueryTree();
                differenceTree.constructSetOperationTree(initialQueries, trees, tree, "DIFFERENCE");
                differenceTree.toGraph(output+"final.gv", true);
            }
        }
    }

    //Handle specific query similar to query F where there's an IN and a nested query within a nested query with presence
    //of a set operator
    private static void handleINandNestedWithinNested(String queryString, String outputLocation) throws IOException{
        //I am going to split the main query together with the IN
        String[] result = queryString.split("INTERSECT");
        String output = outputLocation;

        //then I am going to feed into the queryParser
        ArrayList<query> initialQueries = new ArrayList<query>();
        parser queryParserTwo = new parser();
        initialQueries.addAll(queryParserTwo.parseQuery(result[0]));

        //then I am going to process the other string
        ArrayList<query> secondQuery = new ArrayList<query>();
        queryParserTwo = new parser();
        secondQuery.addAll(queryParserTwo.parseQuery(result[1]));

        ArrayList<ArrayList<String>> schema = new ArrayList<ArrayList<String>>();
        initiateSchema(schema);

        //find out the specific set operators
        String operator = findSetOperator(queryString);

        //Combine the tree and output for original
        //Generate the two trees
        QueryTree tree1Copy = new QueryTree();
        tree1Copy.constructTree(initialQueries.get(0));
        QueryTree tree2Copy = new QueryTree();
        tree2Copy.constructTree(secondQuery.get(0));
        combineTwoTrees(tree1Copy, tree2Copy, secondQuery, operator);
        tree1Copy.toGraph(output+"original.gv", true);

        //for rule one
        tree1Copy = getTreeCopyRuleOne(initialQueries.get(0));
        tree2Copy = getTreeCopyRuleOne(secondQuery.get(0));
        combineTwoTrees(tree1Copy, tree2Copy, secondQuery, operator);
        tree1Copy.toGraph(output+"ruleOne.gv", true);

        //rule Two
        tree1Copy = getTreeCopyRuleTwo(initialQueries.get(0), schema);
        tree2Copy = getTreeCopyRuleTwo(secondQuery.get(0), schema);
        combineTwoTrees(tree1Copy, tree2Copy, secondQuery, operator);
        tree1Copy.toGraph(output+"ruleTwo.gv", true);

        //rule three
        tree1Copy = getTreeCopyRuleThree(initialQueries.get(0), schema);
        tree2Copy = getTreeCopyRuleThree(secondQuery.get(0), schema);
        combineTwoTrees(tree1Copy, tree2Copy, secondQuery, operator);
        tree1Copy.toGraph(output+"ruleThree.gv", true);

        //rule four
        tree1Copy = getTreeCopyRuleFour(initialQueries.get(0), schema);
        tree2Copy = getTreeCopyRuleFour(secondQuery.get(0), schema);
        combineTwoTrees(tree1Copy, tree2Copy, secondQuery, operator);
        tree1Copy.toGraph(output+"ruleFour.gv", true);

        //rule Five
        tree1Copy = getTreeCopyRuleFive(initialQueries.get(0), schema);
        tree2Copy = getTreeCopyRuleFive(secondQuery.get(0), schema);
        combineTwoTrees(tree1Copy, tree2Copy, secondQuery, operator);
        tree1Copy.toGraph(output+"ruleFive.gv", true);

        //rule Six
        tree1Copy = getTreeCopyRuleSix(initialQueries.get(0), schema);
        tree2Copy = getTreeCopyRuleSix(secondQuery.get(0), schema);
        combineTwoTrees(tree1Copy, tree2Copy, secondQuery, operator);
        tree1Copy.toGraph(output+"ruleSix.gv", true);
    }

    //Used in handleINandNestedWithinNested to combine 2 trees
    private static void combineTwoTrees(QueryTree tree1Copy, QueryTree tree2Copy, ArrayList<query> secondQuery, String operator) {
        //first of all I have to locate the node of the subtree
        Node subqueryNode = tree1Copy.getRoot();

        while(!subqueryNode.getName().contentEquals("JOIN")){
            subqueryNode = subqueryNode.getLeftChild();
        }
        subqueryNode = subqueryNode.getRightChild();
        //located the subquery join Node

        //begin constructing the copytree
        Node root = new Node();
        root.setParent(subqueryNode.getParent());
        subqueryNode.getParent().setRightChild(root);
        root.setName("PROJECT");
        root.setData(QueryTree.toArrayListTuple(secondQuery.get(0).attributes));
        ArrayList<Tuple<String, String>> init = new ArrayList<Tuple<String, String>>();
        init.add(new Tuple<String, String>("null", "null"));
        root.insert(new Node(init, operator));
        root.insert(subqueryNode, tree2Copy.getRoot());
    }

    private static String findSetOperator(String queryString) {
        String setOperator = "";
        if(queryString.contains("INTERSECT")){
            setOperator = "INTERSECT";
        }
        else if (queryString.contains("UNION")){
            setOperator = "UNION";
        }
        else if(queryString.contains("DIFFERENCE") || queryString.contains("EXCEPT")){
            setOperator = "DIFFERENCE";
        }
        return setOperator;
    }

    //return a single string from the given SQL query
    private static String doublenested(String s)throws IOException{
        String queryString = new String();                   // Complete query in a string
        String buffer = new String();                        // Input from file, one line
        ArrayList<String> temp = new ArrayList<String>();    // Used for separating lists from the query such as attributes, etc
        Tuple<String, String> relTuple = new Tuple<String, String>();  // Temp tuple used during parsing
        ArrayList<Tuple<String, String>> relationList = new ArrayList<Tuple<String, String>>(); // For relations
        boolean subqueryFlag = false;                       // Used to indicate when parsing a subquery
        ArrayList<query> queryList = new ArrayList<query>();
        BufferedReader stream = new BufferedReader(new FileReader(s));

        // Read entire query into a string for easy parsing
        while((buffer = stream.readLine()) != null){
            queryString = queryString + " " + buffer;
        }
        stream.close();
        return queryString;
    }

    public static void ruleOne(Node root, query initialQuery)throws IOException{
        //First check if there is one or more than one conjunction
        if(initialQuery.where.operators != null){
            if(initialQuery.where.operators.size() != 0){
                //traverse the tree until you see select
                Node selectNode = root;

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
            }
            // else no optimization can be done
        }
        //check to see if the query has a subquery
        if(initialQuery.subquery != null) {
            //check to see if the subquery has one or more conjunction
            if(initialQuery.subquery.where != null){
                if ( initialQuery.subquery.where.operators != null) {
                    if(initialQuery.subquery.where.operators.size() != 0){
                        //then apply rule one also

                        //find the root of the subquery
                        Node subqueryNode = root;
                        while (!subqueryNode.getName().contentEquals("JOIN")) {
                            subqueryNode = subqueryNode.getLeftChild();
                        }
                        subqueryNode = subqueryNode.getRightChild();

                        //call RuleOne and apply to subquery
                        ruleOne(subqueryNode, initialQuery.subquery);
                    }
                }
            }
        }
    }

    private static void ruleTwo(query initialQuery, Node root, ArrayList<ArrayList<String>> schema) throws IOException {
        //Check to see if the number of relations is greater than one or if it contains a subquery
        if(initialQuery.relations.size() > 1){
            Node selectedNode = root;

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
                    if(initialQuery.relations.size()!=1){
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
            }
        }
        //else no optimization can be done
        //check to see if the query has a subquery
        if(initialQuery.subquery != null) {
            //check to see if the subquery has one or more relations, if true, then apply rule two also
            if (initialQuery.subquery.relations.size() > 1) {
                //find the root of the subquery
                Node subqueryNode = root;
                while (!subqueryNode.getName().contentEquals("JOIN")) {
                    subqueryNode = subqueryNode.getLeftChild();
                }
                subqueryNode = subqueryNode.getRightChild();

                //call RuleTwo and apply it to the subquery
                ruleTwo(initialQuery.subquery, subqueryNode, schema);
            }
        }
    }

    /*private static void ruleThree(QueryTree tree)throws IOException{
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
    }*/

    //A modified version for rule three
    private static void ruleThree(QueryTree tree, query initialQuery, ArrayList<ArrayList<String>> schema)throws IOException{
        Node joinNode = tree.getRoot();
        int selectCount = 0;
        //first locate the joinNode
        while(!joinNode.getName().contentEquals("JOIN")){
            if(joinNode.getName().contentEquals("SELECT")){
                selectCount++;
            }
            joinNode = joinNode.getLeftChild();
        }

        //JoinNode located, begin optimization if select count is greater than 1
        if(selectCount > 1){
            //begin optimization
            //right now it's kind of naive, I choose to switch place of the join node's right child relation
            //with the next join node's right child
            //and the call rule 2 again to perform the relocation

            //first locate the safe node of the right child to be moved
            Node firstJoinRightSafeNode = joinNode.getRightChild();

            //move the second join node
            joinNode = joinNode.getLeftChild();
            while(!joinNode.getName().contentEquals("JOIN")){
                joinNode = joinNode.getLeftChild();
            }
            //second join node located
            Node secondJoinRightSafeNode = joinNode.getRightChild();

            //switch places
            Node firstNodeParent = firstJoinRightSafeNode.getParent();
            firstJoinRightSafeNode.setParent(secondJoinRightSafeNode.getParent());
            secondJoinRightSafeNode.getParent().setRightChild(firstJoinRightSafeNode);

            firstNodeParent.setRightChild(secondJoinRightSafeNode);
            secondJoinRightSafeNode.setParent(firstNodeParent);

            //and then call rule two
            ruleTwo(initialQuery, tree.getRoot(), schema);
        }
    }

    private static void ruleFour(query initialQuery, Node root) {
        //check for IN case
        if(initialQuery.relations.size() == 1){
            Node joinNode = root;

            //locate join node
            while(!joinNode.getName().contentEquals("JOIN")){
                if(joinNode.getLeftChild() != null){
                    joinNode = joinNode.getLeftChild();
                }
                else{
                    //no join node found
                    break;
                }
            }

            //only proceed if join node found
            if(joinNode.getName().contentEquals("JOIN")){
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
                                leftAttr = leftAttr.replaceAll(" ", "");

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
            }
        }
        //Check if the number of relations is greater than one
        if(initialQuery.relations.size() > 1){
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
                                leftAttr = leftAttr.replaceAll(" ", "");

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
        }
        //else no optimization need to be done
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
            if(tempNode.getData().get(0).getRight().equals("null"))
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
                        if(itrNode.getName().equals("PROJECT") && itrNode!=tree.getRoot())
                            itrNode=itrNode.getParent();
                        if(itrNode==null)
                            break;
                        attributes.addAll((getAttributes(itrNode.getData(), currentRelation)));
                        itrNode=itrNode.getParent();
                    }

                    // Add project node to tree
                    // remove duplicates from attribute list
                    HashSet<String> hs = new HashSet<String>();
                    hs.addAll(attributes);
                    attributes.clear();
                    attributes.addAll(hs);
                    // Create data in right format
                    newData=new ArrayList<Tuple<String, String>>();
                    for(int k=0; k<attributes.size(); k++){
                        newData.add(new Tuple<String, String>(attributes.get(k), "null"));
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
                            newNode.setLeftChild(tempNode);
                            tempNode.getParent().setRightChild(newNode);
                        }
                        tempNode.setParent(newNode);
                    }
                    tempNode=tempNode.getParent();
                }
                attributes.clear();
            }
        }
    }

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

    /* working version for rule six if necessary
    private static void newRuleSix(QueryTree tree){
        Node binaryNode = tree.getRoot();
        String binaryNames = "JOIN UNION INTERSECT DIFFERENCE";

        while(!binaryNames.contains(binaryNode.getName())){
            binaryNode.getLeftChild();
        }

        //now it is one of the binary nodes

        //then I will first select the left node to be that benchmark point
        Node benchmark = binaryNode.getLeftChild();
        //also set up the tracker for the right branch
        Node rightTracker = binaryNode.getRightChild();

        //if they don't match then need to move down and reassign benchmark
        if(!nodeMatchCheck(benchmark, rightTracker)){

        }
    }

    //identify two same subtrees
    private static boolean identifySameSubtrees(Node n1, Node n2){
        boolean match = true;
        //if binary, go to the left, then go to the right
        //else, just go to the left
        return match;
    }

    //check to see if two nodes match
    private static boolean nodeMatchCheck(Node n1, Node n2){
        boolean match = true;

        if(n1.getName().contentEquals(n2.getName())){
            //name match, also need to check for tuple records
            if(n1.getData().size() == n2.getData().size()){
                for(int i=0; i<n1.getData().size(); i++){
                    Tuple<String, String> n1data = n1.getData().get(i);
                    Tuple<String, String> n2data = n2.getData().get(i);
                    if(!n1data.getLeft().contentEquals(n2data.getLeft())){
                        if(!n1data.getRight().contentEquals(n2data.getRight())){
                            match = false;
                        }
                    }
                }
            }
            else{
                match = false;
            }
        }
        else{
            match = false;
        }
        return match;
    }*/

    private static QueryTree getTreeCopyRuleOne(query initialQuery) throws IOException {
        QueryTree newTree = new QueryTree();
        newTree.constructTree(initialQuery);
        QueryOptimizer.ruleOne(newTree.getRoot(), initialQuery);
        return newTree;
    }

    private static QueryTree getTreeCopyRuleTwo(query initialQuery, ArrayList<ArrayList<String>> schema) throws IOException {
        QueryTree newTree = new QueryTree();
        newTree.constructTree(initialQuery);
        QueryOptimizer.ruleOne(newTree.getRoot(), initialQuery);
        ruleTwo(initialQuery, newTree.getRoot(), schema);
        return newTree;
    }

    private static QueryTree getTreeCopyRuleThree(query initialQuery, ArrayList<ArrayList<String>> schema) throws IOException {
        QueryTree newTree = new QueryTree();
        newTree.constructTree(initialQuery);
        QueryOptimizer.ruleOne(newTree.getRoot(), initialQuery);
        ruleTwo(initialQuery, newTree.getRoot(), schema);
        ruleThree(newTree, initialQuery, schema);
        return newTree;
    }

    private static QueryTree getTreeCopyRuleFour(query initialQuery, ArrayList<ArrayList<String>> schema) throws IOException {
        QueryTree newTree = new QueryTree();
        newTree.constructTree(initialQuery);
        QueryOptimizer.ruleOne(newTree.getRoot(), initialQuery);
        ruleTwo(initialQuery, newTree.getRoot(), schema);
        ruleThree(newTree, initialQuery, schema);
        ruleFour(initialQuery, newTree.getRoot());
        return newTree;
    }

    private static QueryTree getTreeCopyRuleFive(query initialQuery, ArrayList<ArrayList<String>> schema) throws IOException {
        QueryTree newTree = new QueryTree();
        newTree.constructTree(initialQuery);
        QueryOptimizer.ruleOne(newTree.getRoot(), initialQuery);
        ruleTwo(initialQuery, newTree.getRoot(), schema);
        ruleThree(newTree, initialQuery, schema);
        ruleFour(initialQuery, newTree.getRoot());
        ruleFive(newTree);
        return newTree;
    }

    private static QueryTree getTreeCopyRuleSix(query initialQuery, ArrayList<ArrayList<String>> schema) throws IOException {
        QueryTree newTree = new QueryTree();
        newTree.constructTree(initialQuery);
        QueryOptimizer.ruleOne(newTree.getRoot(), initialQuery);
        ruleTwo(initialQuery, newTree.getRoot(), schema);
        ruleThree(newTree, initialQuery, schema);
        ruleFour(initialQuery, newTree.getRoot());
        ruleFive(newTree);
        ruleSix(newTree);
        return newTree;
    }
}
