// Iterator to traverse the tree in a pre-order way

public class treeIterator{
  Node currentNode;
  
  public treeIterator(){
    currentNode=null;
  }
  
  public treeIterator(Node root){
    currentNode = root;
  }
  
  // Decides if the current node is a leaf node
  public boolean isLeaf(){
    return (currentNode.getLeftChild()==null && currentNode.getRightChild()==null);
  }
  
  // Returns the node the iterator is pointing at
  public Node getNode(){
    return currentNode;
  }
  
  // Moves the iterator one step through the tree in a pre-order fashion
  public void next(){    
    if(currentNode.getLeftChild()!=null)
      currentNode=currentNode.getLeftChild();
    else if(currentNode.getRightChild()!=null)
      currentNode=currentNode.getRightChild();
    else{
      while(currentNode.getParent()!=null && (currentNode.getParent().getRightChild()==currentNode || currentNode.getParent().getRightChild()==null)){
        currentNode=currentNode.getParent();
      }
      if(currentNode.getParent()!=null && currentNode.getParent().getRightChild()!=null)
        currentNode=currentNode.getParent().getRightChild();
      else
        currentNode=null;
    }
  }
  
}