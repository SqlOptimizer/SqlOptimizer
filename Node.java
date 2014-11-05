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
}
