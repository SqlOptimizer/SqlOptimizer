import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Shen on 11/2/2014.
 */
import java.util.*;

/**************************************************/
//  The QueryTree class represents a tree         //
//  for a query translated from SQL               //
/**************************************************/

public class QueryTree {
    //each tree always starts with a root
    private Node root;

    //Default Constructor
    public QueryTree(){
        root = null;
    }

    //Tree Constructor
    public QueryTree(ArrayList<Tuple<String, String>> rootData, String name){
        root = new Node(rootData, name);

    }

    //Gets Functions//

    public Node getRoot(){
        return root;
    }

    //Given a query, this function will construct a tree from that query
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

    // Return a list of pointers to leaf nodes
    public ArrayList<Node> getLeaves(){
        ArrayList<Node> relationList = new ArrayList<Node>();
        treeIterator iterator = new treeIterator(root);

        while(iterator!=null){
            iterator.next();
            if(iterator.isLeaf()){
                relationList.add(new Node(iterator.getNode()));
            }
        }

        return relationList;
    }

    //convert an array list of string to array list of tuples for the attributes field
    private ArrayList<Tuple<String, String>> toArrayListTuple(ArrayList<String> stringList) {
        ArrayList<Tuple<String, String>> list = new ArrayList<Tuple<String, String>>();

        for(String value : stringList){
            list.add(new Tuple<String, String>(value, "null"));
        }
        return list;
    }

    //a output function that can output to .gv file for all the rules
    //it does so by taking care of the initialization and closing actions
    //then call the outputGraph functions to traverse the tree to output
    public void toGraph(String filePath, boolean append) throws IOException{
        File file = new File(filePath);
        file.createNewFile();

        WriteFile writer = new WriteFile(filePath, append);

        //start writing out to file
        writer.writeToFile("digraph G {");
        writer.writeToFile("edge [dir=back]");

        //traverse the tree
        String line = new String();

        //current node index and point to index
        int current = 1;
        int pointTo = 1;

        Node node = this.getRoot();
        line = node.print(current);
        writer.writeToFile(line);

        node = node.getLeftChild();
        current++;

        outputGraph(filePath, append, current, pointTo, node);

        writer.writeToFile("}");
    }

    //output to graph recursively
    public int outputGraph(String path, boolean append, int currentIndex, int pointToIndex, Node currentNode)throws IOException{
        File file = new File(path);
        WriteFile writer = new WriteFile(path, append);

        String line = "";

        if(currentNode.getLeftChild() == null && currentNode.getRightChild() == null){
            //print itself
            line = currentNode.print(currentIndex);
            writer.writeToFile(line);
            writer.writeToFile("node" + Integer.toString(pointToIndex) + "->" + "node" + Integer.toString(currentIndex));
            currentIndex++;
        }
        else if(currentNode.getLeftChild() != null && currentNode.getRightChild()!= null){
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
