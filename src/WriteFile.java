/**
 * Created by San on 11/13/2014.
 */

//a class for writing to a file

import java.io.*;

//A File Writer class

public class WriteFile {
    private String path;
    private boolean appendToFile = false;

    //constructor
    public WriteFile(String filePath){
        path = filePath;
    }

    //constructor
    public WriteFile(String filePath, boolean append){
        path = filePath;
        appendToFile = append;
    }

    //write the line to the designated file with new line 
    public void writeToFile(String textLine) throws IOException{
        FileWriter writer = new FileWriter(path, appendToFile);
        PrintWriter printLine = new PrintWriter(writer);

        printLine.printf("%s" + "%n", textLine);
        printLine.close();
    }
}
