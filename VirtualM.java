import java.util.*;

public class VirtualM
{
    // instance variables - replace the example below with your own
    private int vid; //virtual machine id
    private int copycount=1;
    private int pmid; //id of PM this vm belongs to
    ArrayList <Integer> copypm = new ArrayList<Integer>(0);
    ArrayList <CopyVM> copylist=new ArrayList<CopyVM>(0);
    /**
     * Constructor for objects of class VirtualM
     */
    public VirtualM(int vid, int pmid)
    {
        this.vid = vid;
        this.pmid = pmid;
    }
    public void addcopy(int id){
        copypm.add(new Integer(id));
    }
    public int getpmid(){
        return pmid;
    }
    public int getvid(){
        return vid;
    }
    public void copyAdd(int cost){
    	copylist.add(new CopyVM(vid,pmid,cost,copycount));
    	copycount++;
    }
    public int getCCost(int i){
    	return copylist.get(i).getcost();
    }
    public String toString(){
        String string="";
        for (int i=0; i<copypm.size();i++){
            string+=copypm.get(i)+", ";
        }
        return string;
    }
}
