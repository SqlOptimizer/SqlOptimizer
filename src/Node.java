/**
 * Created by Shen on 11/2/2014.
 */
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Node<T> {
    List<String> data;
    private Node<T> parent;
    private Node<T> leftChild;
    private Node<T> rightChild;
    private String name;

    //Node Default Constructor
    public Node(){
        name = "null";
        data = null;
        parent = null;
        leftChild = null;
        rightChild = null;
    }

    public Node(List<String> data, String name){
        this.setName(name);
        this.setData(data);
    }

    public List getData(){
        return this.data;
    }

    public Node<T> getParent(){
        return this.parent;
    }

    public Node<T> getLeftChild(){
        return this.leftChild;
    }

    public Node<T> getRightChild(){
        return this.rightChild;
    }

    public String getName(){
        return this.name;
    }

    public void setData(List<String> data){
        this.data = data;
    }

    public void setParent(Node<T> parent){
        this.parent = parent;
    }

    public void setLeftChild(Node<T> left){
        this.leftChild = left;
        left.setParent(this);
    }

    public void setRightChild(Node<T> right){

        this.rightChild = right;
        right.setParent(this);
    }

    public void setName(String name) {
        this.name = name;
    }

    //inserting a new node to the current node
    public void insert(Node<T> relation) {
        //if the current node has no children, then assign it to the left child
        if(this.leftChild == null){
            this.setLeftChild(relation);
        }
        else{
            //traverse to the node which has a null left child recursively
            this.leftChild.insert(relation);
        }
    }

    public void insert(Node<T> relation, Node<T> relation1) {
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
        this.insert(new Node<T>(null, "JOIN"));
        while(i >= 2){
            if(i > 2){
                this.insert(new Node<T>(null, "JOIN"),
                        new Node<T>(Arrays.asList(newQuery.relations.get(i-1)), "RELATION"));
                i=i-1;
            }
            else{
                this.insert(new Node<T>(Arrays.asList(newQuery.relations.get(0)), "RELATION"),
                        new Node<T>(Arrays.asList(newQuery.relations.get(1)), "RELATION"));
                i = i-2;
            }
        }
    }

    public void performJoinWithSubquery(query newQuery) {
        //First, create a tree for the subquery
        QueryTree<T> sub = new QueryTree<T>();
        sub.constructTree(newQuery.subquery);

        int i = newQuery.relations.size();
        this.insert(new Node<T>(null, "JOIN"));
        this.insert(new Node<T>(null, "JOIN"), sub.getRoot());

        while(i >= 2){
            if(i > 2){
                this.insert(new Node<T>(null, "JOIN"),
                        new Node<T>(Arrays.asList(newQuery.relations.get(i-1)), "RELATION"));
                i=i-1;
            }
            else{
                this.insert(new Node<T>(Arrays.asList(newQuery.relations.get(0)), "RELATION"),
                        new Node<T>(Arrays.asList(newQuery.relations.get(1)), "RELATION"));
                i = i-2;
            }
        }
    }

    public String print(int i) {
        String line = new String("node" + Integer.toString(i));
        line = line + "[ label = \"";
        if(this.getName() != "JOIN"){
            line = line + this.name + "( " + this.data.toString() + " )\" ]";
        }
        else{
            line = line + this.name + "\"]";
        }
        return line;
    }

    public int outputSubquery(int i, String filePath, boolean append)throws IOException{
        WriteFile writer = new WriteFile(filePath, append);

        //traverse the tree
        String line = new String();

        //index to denote the number of node (to output to the file)
        int j = i;

        Node<T> node = this;
        line = node.print(j);
        writer.writeToFile(line);
        node = node.getLeftChild();
        while(node != null){
            if(node.getName() != "JOIN"){
                if(node.getParent().getName() != "JOIN"){
                    line = node.print(++j);
                    writer.writeToFile(line);
                    writer.writeToFile("node" + Integer.toString(j-1) + "->" + "node" + Integer.toString(j));
                    node = node.getLeftChild();
                }
                else{
                    node = node.getLeftChild();
                    j++;
                }
            }
            else{
                if(node.getParent().getName() != "JOIN"){
                    line = node.print(++j);
                    writer.writeToFile(line);
                    writer.writeToFile("node" + Integer.toString(j-1) + "->" + "node" + Integer.toString(j));
                    line = node.getRightChild().print(j+1);
                    writer.writeToFile(line);
                    writer.writeToFile("node" + Integer.toString(j) + "->" + "node" + Integer.toString(j+1));
                    line = node.getLeftChild().print(j+2);
                    writer.writeToFile(line);
                    writer.writeToFile("node" + Integer.toString(j) + "->" + "node" + Integer.toString(j+2));
                    node = node.getLeftChild();
                    j = j+2;
                }
                else{
                    line = node.getRightChild().print(j+1);
                    writer.writeToFile(line);
                    writer.writeToFile("node" + Integer.toString(j) + "->" + "node" + Integer.toString(j+1));
                    line = node.getLeftChild().print(j+2);
                    writer.writeToFile(line);
                    writer.writeToFile("node" + Integer.toString(j) + "->" + "node" + Integer.toString(j+2));
                    node = node.getLeftChild();
                    j = j+2;
                }
            }
        }
        return j;
    }
}
