/* Authors: San Yeung and Katrina Ward
 * Description: QueryTree class file
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.*;

/**********************************************************
 * Class QueryTree for representing a SQL query.
 *
 * Methods:
 *
 * constructTree(query newQuery)
 * Description: This is the main function that will be called to construct a query-tree for a query
 * Pre: There does not exists a tree associated with this query
 * Post: A query-tree will be constructed to represent the query
 * Param: The query that has been parsed and information is stored successfully
 *
 * getLeaves()
 * Description: This function returns a list of pointers to leaf nodes
 *
 * toArrayListTuple(ArrayList<String> stringList)
 * Description: This functions converts a list of strings to be a list of Tuples
 * Post: A tuple of the new list consists of <original-string, "null">
 *
 * toGraph(String filePath, boolean append)
 * Description: This is a output function that can output to .gv file for all the rules
 * It does so by taking care of the initialization and closing actions
 * then call the outputGraph functions to traverse the tree to output the .gv file
 * Post: A .gv file will be outputted to the user specified file-path.
 * Param1: User specified file-path.
 * Param2: Specifies that whether or not it is okay to append to the file if the file already exists
 *
 * outputGraph(String path, boolean append, int currentIndex, int pointToIndex, Node currentNode)
 * Description: A recursive functions that traverse along the tree in order to write out to the .gv file with appropriate index
 * Pre: Called by the toGraph function
 * Post: The entire tree has been walked and each node has been outputted with appropriate index and labels
 * Param1: User specified file-path
 * Param2: Whether or not it is okay to append
 * Param3: The current index of the node that should be assigned to
 * Param4: The point to index of the current node that it should be pointing to (to represent the direction of the arrow)
 * Param5: The current node
 /**********************************************************/

public class QueryTree {
    /*********************************************************************/
    /*              Member Variables                                     */
    /*********************************************************************/
    private Node root; //each tree starts with the root

    /**********************************************************************/
    /*         Member Methods                                             */
    /**********************************************************************/

    //Default Constructor
    public QueryTree(){
        root = null;
    }

    /****************************************/
    //       Accessor & Mutator Functions   //
    /****************************************/

    public Node getRoot(){
        return root;
    }

    public void constructTree(query newQuery) {
        //First check to see if where statement is empty or not: if empty, then only have projection or join
        if(newQuery.isWhereEmpty()){
            //check to see how many relations there are
            //if just one then perform the selection, else, performs a join
            if(newQuery.relations.size() == 1){
                //only one relation, generate the tree
                this.root = new Node();
                this.root.setName("PROJECT");
                this.root.setData(toArrayListTuple(newQuery.attributes));

                //check for orderby
                if(newQuery.orderBy != null){
                    if(newQuery.orderBy.size() != 0){
                        this.root.insert(new Node(toArrayListTuple(newQuery.orderBy), "ORDER-BY"));
                    }
                }

                //if there is a subquery
                if(newQuery.subquery != null){
                    this.root.performJoinWithSubquery(newQuery);
                }
                else{
                    this.root.insert(new Node(newQuery.relations, "RELATION"));
                }
            }
            else{
                //only consider two relations and then perform join (natural join in this case)
                //if equi-join, then whereStatement would not be empty

                //if there is a subquery in the from statement, then need to do something special
                if(newQuery.subquery == null){
                    this.root = new Node();
                    this.root.setName("PROJECT");
                    this.root.setData(toArrayListTuple(newQuery.attributes));

                    //check for orderby
                    if(newQuery.orderBy != null){
                        if(newQuery.orderBy.size() != 0){
                            this.root.insert(new Node(toArrayListTuple(newQuery.orderBy), "ORDER-BY"));
                        }
                    }

                    this.root.performJoin(newQuery);
                }
                else{
                    this.root = new Node();
                    this.root.setName("PROJECT");
                    this.root.setData(toArrayListTuple(newQuery.attributes));

                    //check for orderby
                    if(newQuery.orderBy != null){
                        this.root.insert(new Node(toArrayListTuple(newQuery.orderBy), "ORDER-BY"));
                    }

                    this.root.performJoinWithSubquery(newQuery);
                }

            }
        }
        else{
            //whereStatement is not empty
            //Two cases: only one relation, or multiple relation
            if(newQuery.relations.size() == 1){
                //only one relation, generate the tree
                this.root = new Node();
                this.root.setName("PROJECT");
                this.root.setData(toArrayListTuple(newQuery.attributes));

                //check for orderby
                if(newQuery.orderBy != null){
                    if(newQuery.orderBy.size() != 0){
                        this.root.insert(new Node(toArrayListTuple(newQuery.orderBy), "ORDER-BY"));
                    }
                }

                //get wherestatement info to a string list
                String whereInfo = newQuery.whereInfoToString();
                this.root.insert(new Node(toArrayListTuple(new ArrayList<String>(Arrays.asList(whereInfo))), "SELECT"));

                //if there is a subquery
                if(newQuery.subquery != null){
                    this.root.performJoinWithSubquery(newQuery);
                }
                else{
                    this.root.insert(new Node(newQuery.relations, "RELATION"));
                }
            }
            else{
                //only consider two relations and then perform join (natural join in this case)
                if(newQuery.subquery == null){
                    this.root = new Node();
                    this.root.setName("PROJECT");
                    this.root.setData(toArrayListTuple(newQuery.attributes));

                    //check for orderby
                    if(newQuery.orderBy != null){
                        if(newQuery.orderBy.size() != 0){
                            this.root.insert(new Node(toArrayListTuple(newQuery.orderBy), "ORDER-BY"));
                        }
                    }

                    //get wherestatement info to a string list
                    String whereInfo = newQuery.whereInfoToString();
                    this.root.insert(new Node(toArrayListTuple(new ArrayList<String>(Arrays.asList(whereInfo))), "SELECT"));
                    this.root.performJoin(newQuery);
                }
                else{
                    this.root = new Node();
                    this.root.setName("PROJECT");
                    this.root.setData(toArrayListTuple(newQuery.attributes));

                    //check for orderby
                    if(newQuery.orderBy != null){
                        if(newQuery.orderBy.size() != 0){
                            this.root.insert(new Node(toArrayListTuple(newQuery.orderBy), "ORDER-BY"));
                        }
                    }

                    //get wherestatement info to a string list
                    String whereInfo = newQuery.whereInfoToString();
                    this.root.insert(new Node(toArrayListTuple(new ArrayList<String>(Arrays.asList(whereInfo))), "SELECT"));
                    this.root.performJoinWithSubquery(newQuery);
                }
            }
        }
    }

    public ArrayList<Node> getLeaves(){
        ArrayList<Node> relationList = new ArrayList<Node>();
        treeIterator iterator = new treeIterator(root);

        while(!iterator.isNull()){
            iterator.next();
            if(!iterator.isNull() && iterator.isLeaf()){
                relationList.add(new Node(iterator.getNode()));
            }
        }

        return relationList;
    }

    private ArrayList<Tuple<String, String>> toArrayListTuple(ArrayList<String> stringList) {
        ArrayList<Tuple<String, String>> list = new ArrayList<Tuple<String, String>>();

        for(String value : stringList){
            list.add(new Tuple<String, String>(value, "null"));
        }
        return list;
    }

    public void toGraph(String filePath, boolean append) throws IOException{
        File file = new File(filePath);
        file.createNewFile();

        WriteFile writer = new WriteFile(filePath, append);

        //start writing out to file
        writer.writeToFile("digraph G {");
        writer.writeToFile("edge [dir=back]");

        //traverse the tree

        //current node index and point to index
        int current = 1;
        int pointTo = 1;

        Node node = this.getRoot();
        String line = node.print(current);
        writer.writeToFile(line);

        node = node.getLeftChild();
        current++;

        outputGraph(filePath, append, current, pointTo, node);

        writer.writeToFile("}");
    }

    public int outputGraph(String path, boolean append, int currentIndex, int pointToIndex, Node currentNode)throws IOException{
        File file = new File(path);
        WriteFile writer = new WriteFile(path, append);

        String line = "";

        if(currentNode.isLeaf()){
            //print itself
            line = currentNode.print(currentIndex);
            writer.writeToFile(line);
            writer.writeToFile("node" + Integer.toString(pointToIndex) + "->" + "node" + Integer.toString(currentIndex));
            currentIndex++;
        }
        else if(!currentNode.leftNull() && !currentNode.rightNull()){
            //print itself
            line = currentNode.print(currentIndex);
            writer.writeToFile(line);
            writer.writeToFile("node" + Integer.toString(pointToIndex) + "->" + "node" + Integer.toString(currentIndex));

            //print the left
            pointToIndex = currentIndex;
            currentIndex++;

            currentIndex = outputGraph(path, append, currentIndex, pointToIndex, currentNode.getLeftChild());

            //print the right
            currentIndex = outputGraph(path, append, currentIndex, pointToIndex, currentNode.getRightChild());
        }
        else{
            //always assume that right child is null
            line = currentNode.print(currentIndex);
            writer.writeToFile(line);
            writer.writeToFile("node" + Integer.toString(pointToIndex) + "->" + "node" + Integer.toString(currentIndex));

            //change point to index to be current index
            pointToIndex = currentIndex;
            currentIndex++;

            currentIndex = outputGraph(path, append, currentIndex, pointToIndex, currentNode.getLeftChild());
        }
        return currentIndex;
    }
}
