
/**
 * Write a description of class POD here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
import java.util.*;
public class POD
{
    // instance variables - replace the example below with your own
    private int id;
    private int k;
    private ArrayList<Node> slist = new ArrayList<Node>(0);

    /**
     * Constructor for objects of class POD
     */
    public POD(int id,int k )
    {
        // initialise instance variables
        this.id = id;
        this.k=k;
    }
    public Node getelem(int n){
        return slist.get(n);
    }
    public int getid(){
        return id;
    }
    public void addNode(Node s){
        slist.add(s);
        s.setpodid(id);
        
    }
    public String toString(){
        String str="ID of POD is: " + id;
        str+= "\nthe switches and PMs under this pod are:\n";
        for (int i=0; i<slist.size();i++){
            str+=slist.get(i).toString() + "\n";
            if(slist.get(i).gettype()==2){
                for(int j=0; j<(k/2);j++){
                    str+= slist.get(i).getPM(j).toString()+"\n";
                }
            }
                
        }
        return str;
    }
    
}
