/**
 * Authors: Shen Yeung and Katrina Ward
 * CS 5300 Fall 2014 Project
 */
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Class:    Node
 * Purpose:  Used to represent a Node in a QueryTree
 *
 * Methods:
 * 
 * Node(ArrayList<Tuple>, String)
 * Description: Constructor
 * Pre: Name needs to include one of the relational algebra keywords. SELECT, JOIN, etc
 * Post: Will create a new node that contains the given name and data
 * Param 1: Data for the new node
 * Param 2: Name for the node
 * 
 * Node(Node)
 * Description: Copy Constructor
 * Pre: None
 * Post: Will create a new node that is an exact copy of another node
 * Param: Node that will be copied
 * 
 * Get/Set ....
 * Description: Accessors and Mutators
 * Pre: None
 * Post: Will return or set the values inside the node
 * 
 * Insert(Node)
 * Description: Adds a node to this node.
 * Pre: This node must have the left or right child set to null
 * Post: Will attach a node, checking left first, then right. New node is a copy of the node given.
 * 
 * Insert(Node, Node)
 * Description: Adds two nodes to the current node
 * Pre: This node must have both children set to null
 * Post: Will attach two nodes to the current node. These nodes will be copies of the passed in nodes
 * Param 1: Node to be attached to the left
 * Param 2: Node to be attached to the right
 * 
 * 
 * 
 * NEEDS COMPLETED
 */

public class Node{
    /**************************************************************************************************/
    /*                 Member Variables                                                                               */
    /**************************************************************************************************/
    ArrayList<Tuple<String, String>> data;     // < name, alias> 
    private Node parent;
    private Node leftChild;
    private Node rightChild;
    private String name;            // Project, Join, ... etc

    /**************************************************************************************************/
    /*                 Member Methods                                                                 */
    /**************************************************************************************************/
    //Node Default Constructor
    public Node(){
        name = "null";
        data = null;
        parent = null;
        leftChild = null;
        rightChild = null;
    }

    //constructor taking in data and name to assign to a node
    public Node(ArrayList< Tuple<String, String> > newData, String newName){
        this.name = new String(newName);
        this.data = new ArrayList<Tuple<String, String>>(newData);
    }

    //copy constructor
    public Node(Node node){
        data = new ArrayList<Tuple<String, String>>(node.data);
        parent = new Node(node.parent);
        leftChild=new Node(node.leftChild);
        rightChild= new Node(node.rightChild);
        name = new String(node.name);
    }

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

    //inserting a new node to the current node
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

    //this insert function is used to insert two nodes
    //mainly used for inserting children under a "JOIN" node
    public void insert(Node relation, Node relation1) {
        //if current node is null, then insert to both left and right children
        if(this.leftChild == null){
            this.leftChild = relation;
            this.rightChild = relation1;
            relation.setParent(this);
            relation1.setParent(this);

//            //stored the table origin information
//            if(relation.getData().size() != 0){
//                this.getData().add(relation.getData().get(0));
//
//                //add the value to the join node parent
//                Node parent = this.getParent();
//                while(parent.getName() == "JOIN"){
//                    if(!parent.getData().contains(this.getData().get(0))){
//                        parent.getData().add(this.getData().get(0));
//                    }
//                    parent = parent.getParent();
//                }
//            }
        }
        else{
            //traverse to a node whose left child is null
            this.leftChild.insert(relation, relation1);
        }
    }

    public void performJoin(query newQuery) {
        int i = newQuery.relations.size();
        //ArrayList<Tuple<String, String>> init = new ArrayList<Tuple<String, String>>(newQuery.relations);
        //init.remove(i-1);
        //this.insert(new Node(init, "JOIN"));

        ArrayList<Tuple<String, String>> init = new ArrayList<Tuple<String, String>>();
        init.add(new Tuple<String, String>("null", "null"));
        this.insert(new Node(new ArrayList<Tuple<String, String>>(init), "JOIN"));

        while(i >= 2){
            if(i > 2){
                ArrayList<Tuple<String, String>> list = new ArrayList<Tuple<String, String>>();
                list.add(newQuery.relations.get(i-1));

//                ArrayList<Tuple<String, String>> listTwo = new ArrayList<Tuple<String, String>>(newQuery.relations);
//                listTwo.remove(i-1);

//                this.insert(new Node(new ArrayList<Tuple<String, String>>(listTwo), "JOIN"),
//                        new Node(new ArrayList<Tuple<String, String>>(list), "RELATION"));
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
                listTwo.add(newQuery.relations.get(1));

                this.insert(new Node(new ArrayList<Tuple<String, String>>(listOne), "RELATION"),
                        new Node(new ArrayList<Tuple<String, String>>(listTwo), "RELATION"));
                i = i-2;
            }
        }
    }

    public void performJoinWithSubquery(query newQuery) {
        //First, create a tree for the subquery
        QueryTree sub = new QueryTree();
        sub.constructTree(newQuery.subquery);

        int i = newQuery.relations.size();

        ArrayList<Tuple<String, String>> init = new ArrayList<Tuple<String, String>>();
        init.add(new Tuple<String, String>("null", "null"));
        this.insert(new Node(new ArrayList<Tuple<String, String>>(init), "JOIN"));

        if(i == 1){
//            this.insert(new Node(new ArrayList<Tuple<String, String>>(), "JOIN"));
            init.clear();
            init.add(new Tuple<String, String>("null", "null"));
            this.insert(new Node(init, "JOIN"));
            ArrayList<Tuple<String, String>> list = new ArrayList<Tuple<String, String>>();
            list.add(newQuery.relations.get(0));
            this.insert(new Node(new ArrayList<Tuple<String, String>>(list), "RELATION"), sub.getRoot());
        }
        else{
//            this.insert(new Node(newQuery.relations, "JOIN"));
//            this.insert(new Node(new ArrayList<Tuple<String, String>>(), "JOIN"), sub.getRoot());
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

    //used to print information for outputing the .gv extension file
    public String print(int i) {
        String line = "node" + Integer.toString(i);
        line = line + "[ label = \"";
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
            if(this.getName().contentEquals("JOIN") && !this.getData().get(0).getLeft().contentEquals("null")){
                line = line + "JOIN" + "( " + tupleToString(this.getData()) + " )\" ]";
            }
            else{
                line = line + "X" + "\"]";
            }

        }
        return line;
    }

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

//    public int outputSubquery(int i, String filePath, boolean append)throws IOException{
//        WriteFile writer = new WriteFile(filePath, append);
//
//        //traverse the tree
//        String line = new String();
//
//        //index to denote the number of node (to output to the file)
//        int j = i;
//
//        Node node = this;
//        line = node.print(j);
//        writer.writeToFile(line);
//        node = node.getLeftChild();
//        while(node != null){
//            if(node.getName() != "JOIN"){
//                if(node.getParent().getName() != "JOIN"){
//                    line = node.print(++j);
//                    writer.writeToFile(line);
//                    writer.writeToFile("node" + Integer.toString(j-1) + "->" + "node" + Integer.toString(j));
//                    node = node.getLeftChild();
//                }
//                else{
//                    node = node.getLeftChild();
//                    j++;
//                }
//            }
//            else{
//                if(node.getParent().getName() != "JOIN"){
//                    line = node.print(++j);
//                    writer.writeToFile(line);
//                    writer.writeToFile("node" + Integer.toString(j-1) + "->" + "node" + Integer.toString(j));
//                    line = node.getRightChild().print(j+1);
//                    writer.writeToFile(line);
//                    writer.writeToFile("node" + Integer.toString(j) + "->" + "node" + Integer.toString(j+1));
//                    line = node.getLeftChild().print(j+2);
//                    writer.writeToFile(line);
//                    writer.writeToFile("node" + Integer.toString(j) + "->" + "node" + Integer.toString(j+2));
//                    node = node.getLeftChild();
//                    j = j+2;
//                }
//                else{
//                    line = node.getRightChild().print(j+1);
//                    writer.writeToFile(line);
//                    writer.writeToFile("node" + Integer.toString(j) + "->" + "node" + Integer.toString(j+1));
//                    line = node.getLeftChild().print(j+2);
//                    writer.writeToFile(line);
//                    writer.writeToFile("node" + Integer.toString(j) + "->" + "node" + Integer.toString(j+2));
//                    node = node.getLeftChild();
//                    j = j+2;
//                }
//            }
//        }
//        return j;
//    }
}
