/**
 * Created by Shen on 11/2/2014.
 */
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
        name = name;
        data = data;
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

    public String printData() {
        String list = new String();
        list = this.data.get(0);
        for(int i = 1; i <= this.data.size(); i++){
            list = list+" "+ i;
        }
        return list;
    }

    //inserting a new node to the current node
    public void insert(Node<T> relation) {
        //if the current node has no children, then assign it to the left child
        if(this.leftChild == null){
            this.leftChild = relation;
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
        }
        else{
            //traverse to a node whose left child is null
            this.leftChild.insert(relation, relation1);
        }
    }

    public void performJoin(query newQuery) {
        int i = newQuery.relations.length;
        this.insert(new Node<T>(null, "JOIN"));
        while(i >= 2){
            if(i > 2){
                this.insert(new Node<T>(null, "JOIN"),
                        new Node<T>(Arrays.asList(newQuery.relations[i-1]), "RELATION"));
                i=i-1;
            }
            else{
                this.insert(new Node<T>(Arrays.asList(newQuery.relations[0]), "RELATION"),
                        new Node<T>(Arrays.asList(newQuery.relations[1]), "RELATION"));
                i = i-2;
            }
        }
    }
}
