// Tuple class because the Java built in version is annoying to find

public class Tuple<T,V>{
  private T left;
  private V right;
  
  // Default constructor
  public Tuple(){
    left=null;
    right=null;
  }
  
  // Constructor
  public Tuple(T leftValue, V rightValue){
    this.left=leftValue;
    this.right=rightValue;
  }
  
  // Copy constructor
  public Tuple(Tuple<T, V> rhs){
    this.left = rhs.left;
    this.right = rhs.right;
  }
  
  // Get the value of the left element
  public T getLeft(){
    return left;
  }
  
  // Get the value of the right element
  public V getRight(){
    return right;
  }
  
  // Is right null?
  public boolean rightNull(){
    return right==null;
  }
  
  // Set the left value
  public void setLeft(T leftValue){
    this.left=leftValue;
  }
  
  // Set right value
  public void setRight(V rightValue){
    this.right=rightValue;
  }
  
  // Assignment
  public void equals(T leftValue, V rightValue){
    this.left=leftValue;
    this.right=rightValue;
  }
  
  // Determine if there is a right value
  public boolean hasRight(){
    return (!(right==null));
  }
}