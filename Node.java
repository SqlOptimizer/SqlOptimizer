/**
 * Created by Shen on 11/2/2014.
 */

public class Node<T> {
    private T data;
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

    public Node(T data, String name){
        name = name;
        data = data;
    }

    public T getData(){
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

    public void setData(T data){
        this.data = data;
    }

    public void setParent(Node<T> parent){
        this.parent = parent;
    }

    public void setLeftChild(Node<T> left){
        this.leftChild = left;
    }

    public void setRightChild(Node<T> right){
        this.rightChild = right;
    }

    public void setName(String name) {
        this.name = name;
    }
}
