import java.util.ArrayList;


/**
 * Write a description of class CopyVM here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class CopyVM
{
    // instance variables - replace the example below with your own
     int vmid;
     int copyid;
     int pmid;
     int cost;
    int conflict;
    ArrayList <Integer> suitpms = new ArrayList<Integer>(0);
    /**
     * Constructor for objects of class CopyVM
     */
    public CopyVM(int vmid,int pmid,int cost,int conflict )
    {
        this.vmid=vmid;
        this.pmid=pmid;
        this.cost=cost;
        this.conflict=conflict;
    }
    public CopyVM(int vmid,int pmid,int cost)
    {
        this.vmid=vmid;
        this.pmid=pmid;
        this.cost=cost;
    }
   /* public CopyVM(int vmid, int pmid, int cost, int copyid){
    	this.vmid=vmid;
        this.pmid=pmid;
        this.cost=cost;
        this.copyid=copyid;
    }*/
    public int getcopyid(){
    	return copyid;
    }
    public boolean checkIfPossible(int onPM){
    	for (int i=0;i<suitpms.size();i++){
    		if(suitpms.get(i).intValue()==onPM){
    			return true;
    		}
    	}
    	return false;
    }
    public int getvmid(){
    return vmid;
    }
    public int getpmid(){
    return pmid;
    }
    public int getcost(){
    return cost;
    }
}
