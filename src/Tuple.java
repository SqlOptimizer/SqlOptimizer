/* 
 * Authors: Katrina Ward and San Yeung
 * Description: Custom tuple class that holds any two objects
 * 
 */

/*
 * Class: Tuple
 * Description: Structure that holds a pair of objects of any type
 * 
 * 
 * 
 * Methods:
 * 
 * Tuple(T, V)
 * Description: Constructor
 * Pre: None
 * Post: Creates a Tuple with the two given values
 * 
 * Tuple(Tuple)
 * Description: Copy Constructor
 * 
 * getLeft, getRight
 * Description: Accessors to get the value of the left and right side of the Tuple
 * 
 * rightNull
 * Description: Returns whether or not the right side of the Tuple is set to null
 * Pre: None
 * Post: Returns true if the right side is null
 * 
 * setLeft, setRight
 * Description: Mutators to change the values of the left and right side independently
 * Pre: None
 * Post: Values of the left or right side will be changed
 * 
 * setEqual(Tuple)
 * Description: Assignment operator equivalent. Makes the Tuple a copy of the given Tuple
 * Pre: Value types for left and right have to match
 * Post: Will change the values of the left and right to match the values of the given Tuple
 * 
 * equals(Tuple)
 * Description: Returns whether or not the two Tuples are identical
 * Pre: Types T and V must have the equals() function defined
 * Post: Returns true if both Tuples are identical
 */

public class Tuple<T,V>{
  /*************************************************/
  /*     Member Variables                          */
  /*************************************************/
  private T left;
  private V right;
  
  /*************************************************/
  /*     Member Methods                            */
  /*************************************************/
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
  public void setEqual(T leftValue, V rightValue){
    this.left=leftValue;
    this.right=rightValue;
  }
  
  // Checks if rhs is equal to this
  public boolean equals(Tuple<T, V> rhs){
    return left.equals(rhs.left) && right.equals(rhs.right);
  }
  
  
}