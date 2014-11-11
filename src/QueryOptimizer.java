import com.sun.xml.internal.fastinfoset.util.StringArray;

import java.util.List;

/**
 * Created by San on 11/4/2014.
 */
public class QueryOptimizer {

    //main method
    public static void  main(String[] args){
        //After reading query, query class have been generated.
        query newQuery = new query();
        //for testing
        //query newQuery = new query(new String[]{"name"}, new String[]{"EMPLOYEE"}, new String[]{}, new char[]{}, new String[]{});

        //Based on the new query object, construct a corresponding tree for that
        QueryTree<List<String>> tree = new QueryTree<List<String>>();
        tree.constructTree(newQuery);
        tree.print();
    }
}
