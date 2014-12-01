/**
 * Authors: San Yeung and Katrina Ward
 * Description: Node class used in the SQL Optimizer project
 */
import java.io.File;
import java.io.IOException;
import java.util.*;

/**********************************************************
* Class Node for representing each node in the Query-tree.
*
* Methods:
* 
* insert(Node)
* Description: This insert method is used when only inserting a SINGLE node into the tree.
* This implies that the inserted node is NOT a join node; else you would use the other version of insert function.
* The inserted node will ALWAYS be inserted to the LEFT child of the last node currently resides in the tree.
* Pre: The left child of the node being added to is set to null
* Post: The left child of the node being added to is set to the given node
* Param: Node that will be added to the tree
* 
* insert(Node, Node)
* Description: This insert function is used to insert two nodes
* mainly used for inserting children under a "JOIN" node
* Pre: The last node, or farthest left node, of the current tree has NO left or right child
* Post: The last node of the current tree will have BOTh left and right child set to the given nodes
* Case 1: Both left and right child are Relation
* Case 2: Left child is JOIN, while right child is Relation (to preserve right linear property)
* Param1: New left child
* Param2: New right child
* 
* performJoin(query)
* Description: A function to be used to perform join after all other operations, such as SELECTS, have been performed in the QueryTree
* Pre: Tree constructed all operations besides join
* Post: Tree modified with additional representations for join nodes
* 
* print(i)
* Description: Converts the node to a single string needed for .gv format
* Pre: i must represent the id of a node in the tree
* Post: return a string containing the given index, and appended node label
/**********************************************************/

public class Node{
    /*********************************************************************/
    /*              Member Variables                                     */
    /*********************************************************************/
    ArrayList<Tuple<String, String>> data;     // < name, alias> 
    private Node parent;
    private Node leftChild;
    private Node rightChild;
    private String name;            // Project, Join, ... etc

    
    /**********************************************************************/
    /*         Member Methods                                             */
    /**********************************************************************/
    //Node Default Constructor
    public Node(){
        name = "null";
        data = null;
        parent = null;
        leftChild = null;
        rightChild = null;
    }

    //constructor taking in data and name to assign to a node
    public Node(ArrayList< Tuple<String, String> > data, String name){
        this.setName(name);
        this.setData(data);
    }

    //copy constructor
    public Node(Node node){
        this.setData(node.data);
        this.setParent(node.parent);
        this.setLeftChild(node.leftChild);
        this.setRightChild(node.rightChild);
        this.setName(node.name);
    }

    /****************************************/
    //       Accessor & Mutator Functions   //
    /****************************************/

    public ArrayList<Tuple<String, String>> getData(){
        return this.data;
    }

    public Node getParent(){
        return this.parent;
    }

    public Node getLeftChild(){
        return this.leftChild;
    }

    public Node getRightChild(){
        return this.rightChild;
    }

    public String getName(){
        return this.name;
    }

    public void setData(ArrayList<Tuple<String, String>> data){
        this.data = data;
    }

    public void setParent(Node parent){
        this.parent = parent;
    }

    public void setLeftChild(Node left){
        this.leftChild = left;
    }

    public void setRightChild(Node right){
        this.rightChild = right;
    }

    public void setName(String name) {
        this.name = name;
    }

    
    public void insert(Node relation) {
        //if the current node has no children, then assign it to the left child
        if(this.leftChild == null){
            this.setLeftChild(relation);
            relation.setParent(this);
        }
        else{
            //traverse to the node which has a null left child recursively
            this.leftChild.insert(relation);
        }
    }

    
    public void insert(Node relation, Node relation1) {
        //if current node is null, then insert to both left and right children
        if(this.leftChild == null){
            this.leftChild = relation;
            this.rightChild = relation1;
            relation.setParent(this);
            relation1.setParent(this);
        }
        else{
            //traverse to a node whose left child is null
            this.leftChild.insert(relation, relation1);
        }
    }

    
    public void performJoin(query newQuery) {
        int i = newQuery.relations.size();

        //initializing the first join node and insert into the tree
        ArrayList<Tuple<String, String>> init = new ArrayList<Tuple<String, String>>();
        init.add(new Tuple<String, String>("null", "null"));
        this.insert(new Node(new ArrayList<Tuple<String, String>>(init), "JOIN"));

        //perform join operations and insertions into the tree given the number of relations size
        while(i >= 2){
            //if the number of relations exceeds 2, then perform join in the form of "Join, Relation"
            //in which join node insert to the left, and Relation insert to the right to preserve right-linear property
            if(i > 2){
                ArrayList<Tuple<String, String>> list = new ArrayList<Tuple<String, String>>();
                list.add(newQuery.relations.get(i-1));

                init.clear();
                init.add(new Tuple<String, String>("null", "null"));

                //insert into the tree
                this.insert(new Node(new ArrayList<Tuple<String, String>>(init), "JOIN"),
                        new Node(new ArrayList<Tuple<String, String>>(list), "RELATION"));

                i=i-1; //decrease the number of relation
            }
            else{
                //only two relations left
                ArrayList<Tuple<String, String>> listOne = new ArrayList<Tuple<String, String>>();
                listOne.add(newQuery.relations.get(0));

                ArrayList<Tuple<String, String>> listTwo = new ArrayList<Tuple<String, String>>();
                listTwo.add(newQuery.relations.get(1));

                this.insert(new Node(new ArrayList<Tuple<String, String>>(listOne), "RELATION"),
                        new Node(new ArrayList<Tuple<String, String>>(listTwo), "RELATION"));
                i = i-2;
            }
        }
    }

    //This function is similar to the previous join performing function
    //With the exception that this handles queries that contain a sub-query
    //The logics is closely similar with the previous function
    public void performJoinWithSubquery(query newQuery) {
        //First, create a tree for the subquery
        QueryTree sub = new QueryTree();
        sub.constructTree(newQuery.subquery);

        int i = newQuery.relations.size();

        //initializing the first join node
        ArrayList<Tuple<String, String>> init = new ArrayList<Tuple<String, String>>();
        init.add(new Tuple<String, String>("null", "null"));
        this.insert(new Node(new ArrayList<Tuple<String, String>>(init), "JOIN"));

        //if the query contains only one relation, simply join it with the sub-query
        if(i == 1){
            ArrayList<Tuple<String, String>> list = new ArrayList<Tuple<String, String>>();
            list.add(newQuery.relations.get(0));
            this.insert(new Node(new ArrayList<Tuple<String, String>>(list), "RELATION"), sub.getRoot());
        }
        else{
            init.clear();
            init.add(new Tuple<String, String>("null", "null"));
            this.insert(new Node(new ArrayList<Tuple<String, String>>(init), "JOIN"), sub.getRoot());

            while(i >= 2){
                if(i > 2){
                    ArrayList<Tuple<String, String>> list = new ArrayList<Tuple<String, String>>();
                    list.add(newQuery.relations.get(i-1));

                    init.clear();
                    init.add(new Tuple<String, String>("null", "null"));

                    this.insert(new Node(new ArrayList<Tuple<String, String>>(init), "JOIN"),
                            new Node(new ArrayList<Tuple<String, String>>(list), "RELATION"));
                    i=i-1;
                }
                else{
                    ArrayList<Tuple<String, String>> listOne = new ArrayList<Tuple<String, String>>();
                    listOne.add(newQuery.relations.get(0));
                    ArrayList<Tuple<String, String>> listTwo = new ArrayList<Tuple<String, String>>();
                    listTwo.add(newQuery.relations.get(i-1));

                    this.insert(new Node(new ArrayList<Tuple<String, String>>(listOne), "RELATION"),
                            new Node(new ArrayList<Tuple<String, String>>(listTwo), "RELATION"));
                    i = i-2;
                }
            }
        }
    }

    
    public String print(int i) {
        String line = "node" + Integer.toString(i);
        line = line + "[ label = \"";

        //check for the name of the node and append appropriate info
        if(!this.getName().contentEquals("JOIN")){
            //break down into different category
            if(this.getName().contentEquals("PROJECT")){
                line = line + "&#928;" + "( " + tupleToString(this.getData()) + " )\"]";
            }
            else if(this.getName().contentEquals("SELECT")){
                line = line + "&#963;" + "( " + tupleToString(this.getData()) + " )\" ]";
            }
            else if(this.getName().contentEquals("RELATION")){
                line = line + "RELATION" + "( " + relationToString(this.getData()) + " )\" ]";
            }
            else{
                line = line + this.name + "( " + tupleToString(this.getData()) + " )\" ]";
            }
        }
        else{
            //if the node is "JOIN", then check to see whether it's Cartesian Product or Theta-Join
            //thus give appropriate label
            if(this.getName().contentEquals("JOIN") && !this.getData().get(0).getLeft().contentEquals("null")){
                line = line + "JOIN" + "( " + tupleToString(this.getData()) + " )\" ]";
            }
            else{
                line = line + "X" + "\"]";
            }
        }
        return line;
    }

    //Convert relation information to be string to be used in the Print function
    private String relationToString(ArrayList<Tuple<String, String>> data) {
        if(data.get(0).getRight().contentEquals("null")){
            return data.get(0).getLeft()+ " ";
        }
        else{
            return data.get(0).getLeft() + "(" + data.get(0).getRight() + ") ";
        }
    }

    //construct a string from the left field of the tuple
    private String tupleToString(ArrayList<Tuple<String, String>> data) {
        String line = "";
        for(Tuple<String, String> tuple : data){
            if(!tuple.getRight().contentEquals("null")){
                line = line + tuple.getRight()+ "." + tuple.getLeft() + " ";
            }
            else{
                line = line + tuple.getLeft() + " ";
            }
        }
        return line;
    }
}
