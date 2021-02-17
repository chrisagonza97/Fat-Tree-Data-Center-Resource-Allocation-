
public class MiVM {
	int prevpm;//id of pm where the VM replica was placed from MCF
	int newpm;// id of pm where the VM replica will move to
	int cost;//cost of replica
	int vmid;//
	int conflict;
	Node tree[];
	
	public MiVM(Node tree [],int prevpm,int newpm,int cost,int vmid){
		this.prevpm=prevpm;
		this.newpm=newpm;
		this.cost=cost;
		this.vmid=vmid;
		this.tree=tree;
		decEsc();
		
	}
	public MiVM(Node tree [],int prevpm,int newpm,int cost,int vmid,int conflict){
		this.prevpm=prevpm;
		this.newpm=newpm;
		this.cost=cost;
		this.vmid=vmid;
		this.tree=tree;
		this.conflict=conflict;
		decEsc();
		
	}
	public void reverse(){
		tree[newpm].vmcount--;
	}
	public void decEsc(){
		tree[newpm].vmcount++;
	}
}
