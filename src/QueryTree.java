import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Shen on 11/2/2014.
 */
import java.util.*;

public class QueryTree<T> {
    private Node<T> root;

    //Default Constructor
    public QueryTree(){
        root = null;
    }

    //Tree Constructor
    public QueryTree(List<String> rootData, String name){
        root = new Node<T>(rootData, name);

    }

    public Node<T> getRoot(){
        return root;
    }

    public void constructTree(query newQuery) {
        //First check to see if where statement is empty or not: if empty, then only have projection or join
        if(newQuery.isWhereEmpty()){
            //check to see how many relations there are
            //if just one then perform the selection, else, performs a join
            if(newQuery.relations.length == 1){
                //only one relation, generate the tree
                this.root = new Node<T>();
                this.root.setName("PROJECT");
                this.root.setData(Arrays.asList(newQuery.attributes));
                this.root.insert(new Node<T>(Arrays.asList(newQuery.relations[0]), "RELATION"));
            }
            else{
                //only consider two relations and then perform join (natural join in this case)
                //if equi-join, then whereStatement would not be empty

                //if there is a subquery in the from statement, then need to do something special
                if(newQuery.subquery != null){
                    this.root = new Node<T>();
                    this.root.setName("Project");
                    this.root.setData(Arrays.asList(newQuery.attributes));
                    this.root.performJoin(newQuery);
                }
                else{
                    this.root = new Node<T>();
                    this.root.setName("Project");
                    this.root.setData(Arrays.asList(newQuery.attributes));
                    this.root.performJoinWithSubquery(newQuery);
                }

            }
        }
        else{
            //whereStatement is not empty
            //Two cases: only one relation, or multiple relation
            if(newQuery.relations.length == 1){
                //only one relation, generate the tree
                this.root = new Node<T>();
                this.root.setName("PROJECT");
                this.root.setData(Arrays.asList(newQuery.attributes));
                //get wherestatement info to a string list
                String whereInfo = newQuery.whereInfoToString();
                this.root.insert(new Node<T>(Arrays.asList(whereInfo), "SELECT"));
                this.root.insert(new Node<T>(Arrays.asList(newQuery.relations), "RELATION"));
            }
            else{
                //only consider two relations and then perform join (natural join in this case)
                if(newQuery.subquery != null){
                    this.root = new Node<T>();
                    this.root.setName("Project");
                    this.root.setData(Arrays.asList(newQuery.attributes));
                    //get wherestatement info to a string list
                    String whereInfo = newQuery.whereInfoToString();
                    this.root.insert(new Node<T>(Arrays.asList(whereInfo), "SELECT"));
                    this.root.performJoin(newQuery);
                }
                else{
                    this.root = new Node<T>();
                    this.root.setName("Project");
                    this.root.setData(Arrays.asList(newQuery.attributes));
                    //get wherestatement info to a string list
                    String whereInfo = newQuery.whereInfoToString();
                    this.root.insert(new Node<T>(Arrays.asList(whereInfo), "SELECT"));
                    this.root.performJoinWithSubquery(newQuery);
                }
            }
        }
    }

    public void print() {
        System.out.print(this.root.getName() + ":" + this.root.printData());
        System.out.print("||");
        System.out.print(this.root.getLeftChild().getName() + ":" + this.root.getLeftChild().printData());
    }


}