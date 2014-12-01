/* 
 * Authors: Katrina Ward and San Yeung
 * Description: Iterator to traverse the tree in a pre-order way
 * 
 */


/*
 * Class: treeIterator
 * Description: Iterator that will traverse the tree in a pre-order way
 * 
 * Methods:
 * 
 * treeIterator(Node)
 * Description: Sets the iterator to the root of the tree
 * Pre: Node passed must be the root of the tree
 * Post: Creates a pointer to the node given, assumed to be the root of the tree
 * Param: Node that is to be the root of the tree
 * 
 * isLeaf
 * Description: Tells if the current node is a leaf node
 * Pre: Iterator cannot be pointed to null
 * Post: Returns true if the node pointed at is a leaf node
 * 
 * getNode
 * Description: Accessor. Allows access to the node being pointed at
 * Pre: None
 * Post: Will return the node being pointed at or null
 * 
 * next
 * Description: Moves the iterator one step
 * Pre: Pointer cannot be set to null
 * Post: Will move the pointer to another node in the tree. Moves in a pre-order fashion.
 */
public class treeIterator{
  /*************************************************************/
  /*            Member Variables                               */
  /*************************************************************/
  Node currentNode;
  
  
  /*************************************************************/
  /*           Member Methods                                  */
  /*************************************************************/
  public treeIterator(){
    currentNode=null;
  }
  
  public treeIterator(Node root){
    currentNode = root;
  }
  
  // Decides if the current node is a leaf node
  public boolean isLeaf(){
    return (currentNode.leftNull() && currentNode.rightNull());
  }
  
  // Returns the node the iterator is pointing at
  public Node getNode(){
    return currentNode;
  }
  
  public boolean isNull(){
    return currentNode==null;
  }
  
  // Moves the iterator one step through the tree in a pre-order fashion
  public void next(){    
    if(!currentNode.leftNull())
      currentNode=currentNode.getLeftChild();
    else if(!currentNode.rightNull())
      currentNode=currentNode.getRightChild();
    else{
      while(!currentNode.parentNull() && (currentNode.getParent().getRightChild()==currentNode || currentNode.getParent().rightNull())){
        currentNode=currentNode.getParent();
      }
      if(!currentNode.parentNull() && !currentNode.getParent().rightNull())
        currentNode=currentNode.getParent().getRightChild();
      else
        currentNode=null;
    }
  }
  
}