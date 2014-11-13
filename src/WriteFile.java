/**
 * Created by San on 11/13/2014.
 */

//a class for writing to a file

import java.io.*;

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

    public void writeToFile(String textLine) throws IOException{
        FileWriter writer = new FileWriter(path, appendToFile);
        PrintWriter printLine = new PrintWriter(writer);

        printLine.printf("%s" + "%n", textLine);
        printLine.close();
    }

//    public static void main(String[] args) throws IOException{
//        File file = new File("C:/Users/San/Desktop/test.txt");
//        file.createNewFile();
//        String fileName = "C:/Users/San/Desktop/test.txt";
//        WriteFile data = new WriteFile(fileName, true);
//        data.writeToFile("Hello World!");
//    }
}
