
/**
 * Write a description of class Node here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
import java.util.*;
public class Node
{
    // instance variables - replace the example below with your own
    int id;
     int type; //0 is core, 1 is aggregation, 2 is edge, 3 is PM
    private int podid; //id of the pod the node belongs to if any
    private int edgeid; //id of the edge node a physical machine belongs to
    private ArrayList <Node> edgeto = new ArrayList<Node>();//list of edges from core to aggs
    private ArrayList <Node> pmsofedge = new ArrayList<Node>(0);//list of Pms an edge switch has
    private ArrayList <Integer> vmlist = new ArrayList<Integer>(0);//list of vms that a PM has.
    ArrayList<OriginalVM> vmlist2 = new ArrayList<OriginalVM>(0);
    ArrayList <CopyVM> vmclist = new ArrayList<CopyVM>(0);
     int vmcount=0;//the amount of VM a pm has, so we don't give a vm to a PM if it is at capacity
     int esc;//effective storage capacity
    private boolean issource = false;//flag for if this PM is a PM with original copy
    private boolean isactive = false;//is the PM active, have a VM
    private int replicount= 0;
    private int originalcount=0;
    int m;
    int color=-1;
    /**
     * Constructor for objects of class Node
     */
    public Node()
    {
        // initialise instance variables
        id = 0;
    }
    public void setactive(){
        isactive = true;
    }
    public int getvmclength(){
        return vmclist.size();
    }
    public void addOrigi(){
    	originalcount++;
    }
    public void setissource(){
        issource = true;
    }
    public boolean getactive(){
        return isactive;
    }
    public boolean getissource(){
        return issource;
    }
    public int getreplicount(){
        return replicount;
    }
    public int getvmcount(){
    return vmcount;
    }
    public void setConflicts(int firstpm,int lastpm){
    	if (vmclist.isEmpty()){
    		return;
    	}
    	Random rand= new Random();
    	int randnum;
    	boolean flag;
    	for (int i=0;i<vmclist.size();i++){
    		do{
    			randnum = rand.nextInt((lastpm -firstpm)+1)+ firstpm;
    			if(randnum==vmclist.get(i).getpmid()){
    				flag=false;
    				continue;
    			}
    			flag=true;
    			vmclist.get(i).conflict=randnum;
    		}while(!flag);
    		
    	}
    }
    
    public boolean addVMc (int vmid,int pmid,int cost,int c){
    	
        if (vmcount>=c){
            return false;
        }
        vmclist.add(new CopyVM(vmid,pmid,cost));
        vmcount++;
        replicount++;
        setactive();
        return true;
    }
public boolean addVMcsize (int vmid,int pmid,int cost,int c,int size){
    	
        if (vmcount>=c){
            return false;
        }
        if (esc<size){
        	return false;
        }
        vmclist.add(new CopyVM(vmid,pmid,cost));
        vmcount++;
        esc-=size;
        replicount++;
        setactive();
        return true;
    }
public boolean addVMc (int vmid,int pmid,int cost,int c,int conflict){
    	
        if (vmcount>=c){
            return false;
        }
        vmclist.add(new CopyVM(vmid,pmid,cost,conflict));
        vmcount++;
        replicount++;
        setactive();
        return true;
    }
    public void removeVMc(int i){
        int size = vmclist.size();
        for (int j=0;j<size;j++){
            if (vmclist.get(j).getvmid()==i){
            vmclist.remove(j);
            vmcount--;
            replicount--;
            //System.out.println("got here");
            break;
            }
        }
        if(vmclist.isEmpty()&&vmlist.isEmpty()){
            isactive=false;
        }
        vmclist.trimToSize();
    }
    public boolean addVM(int vmid, int c){
        if (vmcount >= c){
            return false;
        }
        for (int i=0; i<vmlist.size();i++){
            if (vmid == vmlist.get(i).intValue())
            return false;
        }
        vmlist.add(new Integer(vmid));
        replicount++;
        vmcount++;
        return true;
    }
    public boolean addVM(int vmid, int c,int randsize){
        if (vmcount >= c){
            return false;
        }
        for (int i=0; i<vmlist.size();i++){
            if (vmid == vmlist.get(i).intValue())
            return false;
        }
        vmlist.add(new Integer(vmid));
        replicount++;
        vmcount++;
        return true;
    }
    public boolean addVM112020(int vmid, int c){
        if (vmcount >= c){
            return false;
        }
        for (int i=0; i<vmlist2.size();i++){
            if (vmid == vmlist2.get(i).vmid)
            return false;
        }
       // vmlist.add(new Integer(vmid));
        vmlist2.add(new OriginalVM(vmid));
        replicount++;
        vmcount++;
        return true;
    }
    public boolean addVM112020size(int vmid, int c,int vmsize){
        if (vmcount >= c){
            return false;
        }
        if(vmsize>esc){
        	
        	//System.out.println("hey "+vmsize+""+esc);
        	return false;
        }
        for (int i=0; i<vmlist2.size();i++){
            if (vmid == vmlist2.get(i).vmid)
            return false;
        }
       // vmlist.add(new Integer(vmid));
        vmlist2.add(new OriginalVM(vmid));
        replicount++;
        //vmcount++;
        esc-=vmsize;
        return true;
    }
    public void decesc(){
       esc--; 
    } 
    public void decesc(int randsize){
        esc-=randsize; 
     } 
    public int getesc(){
        return esc;
    }
    public void setesc(int mnum){
        esc=mnum;
    }
    public void addedge(Node s){
        edgeto.add(s);
    }
    public int getedge(int n){
        return edgeto.get(n).getid();
    }
    public void addpmsofedge(Node node){
        pmsofedge.add(node);
    }
    public int getvmcost(int i){
        return vmclist.get(i).getcost();
    }
    public int getvmconflist(int i){
    	return vmclist.get(i).conflict;
    }
    public int getvmid(int i){
        return vmclist.get(i).getvmid();
    }
    public int getcpm(int i){
      return vmclist.get(i).getpmid();  
    }
    public boolean checkVm(int vmid){
     boolean flag= false;
     for (int i=0; i<vmlist.size();i++){
            if (vmid == vmlist.get(i).intValue())
            flag = true;
        } 
     for (int i=0; i<vmclist.size();i++){
            if (vmid == vmclist.get(i).getvmid())
            flag = true;
        }
     
     return flag;
        
    }
    public Node(int id,int type)
    {
        // initialise instance variables
        this.id = id;
        this.type = type;
    }
    public int getedgeid(){
        return edgeid;
    }
    public void setedgeid(int id){
        edgeid = id;
    }
    public int getpodid(){
        return podid;
    }
    public void setpodid(int podid){
        this.podid=podid;
    }
    public int getid(){
        return id;
    }
    public Node getPM(int id){
        return pmsofedge.get(id);
    }
    public int gettype(){
        return type;
    }
    
    public String toString(){
      String stype="";
      if (type==0){
         stype = "Core Switch"; 
      }
      if (type==1){
         stype = "Aggregate switch"; 
      }
      if (type==2){
         stype = "Edge Switch"; 
      }
      if (type==3){
         stype = "Physical machine"; 
      }
      return stype + " ID of " + id+"\n";
    }
    public String printVms(){
        String ret="";
        boolean flag= false;
        ret+= "PM with ID:"+id+" has VMs: \n";
        for (int i=0;i<vmlist.size();i++){
        ret+="  VM "+ vmlist.get(i)+" Original copy \n";
        flag=true;
        }
       /* for (int i=0;i<vmlist2.size();i++){
            ret+="  VM "+ vmlist2.get(i).vmid+" Original copy \n";
            flag=true;
            }*/
        for(int i=0;i<vmclist.size();i++){
        ret+="  VM " + vmclist.get(i).getvmid()+ " replica "+"(cost== "+vmclist.get(i).getcost()+") \n";
        flag=true;
        }
        if (flag==false){
            return null;
        }
        return ret;
    }
}
