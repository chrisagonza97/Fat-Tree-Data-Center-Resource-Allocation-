import java.util.ArrayList;


public class OriginalVM {
int vmid;
int vmsize;
ArrayList<Integer> randIncs = new ArrayList<Integer>(0);

public OriginalVM(int vmid){
	this.vmid=vmid;
}
public OriginalVM(int vmid,int vmsize){
	this.vmid=vmid;
	this.vmsize=vmsize;
}
}
