import java.util.ArrayList;


public class VNF {
int locationid;
int vnfid;
ArrayList <Integer> VMpairs = new ArrayList<Integer>(0);
ArrayList <Integer> mcfVMpairs = new ArrayList<Integer>(0);
ArrayList <Integer> vmbVMpairs = new ArrayList<Integer>(0);
ArrayList <Integer> mbbVMpairs = new ArrayList<Integer>(0);
int vmbpairs=0;
int mbbpairs=0;
int mcfpairs=0;

public VNF(int locationid, int vnfid){
	this.locationid=locationid;
	this.vnfid= vnfid;
	}
}
