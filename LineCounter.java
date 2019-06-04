import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
/**
 * Sehr dumm. -LG
 */
public class LineCounter{
    public LineCounter(){
        count();
    }
    public static void count(){
        try{
            System.out.println("Lines: "+getLines("."));
        }catch(Exception e){}
    }
    
    /**
     * rekursiv
     */
    public static int getLines(String pathname){
        if (!new File(pathname).isDirectory() && pathname.indexOf(".java")!=-1){
            int num=0;
            try{
                FileInputStream fi=new FileInputStream(pathname);
                int re=fi.read();
                while(re!=-1){
                    if ((char) re=='\n'){
                        num=num+1;
                    }
                    re=fi.read();
                }
                num=num+1; //erste bzw. letzte Zeile
                fi.close();
            }
            catch(IOException e){}
            return num;
        }
        else if (!new File(pathname).isDirectory()){
            return 0;
        }
        int lines=0;
        for(File file: new File(pathname).listFiles()){
            lines=lines+getLines(file.getPath());
        }
        return lines;
    }
}