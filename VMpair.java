
public class VMpair {
	int pairid;//the id of the vnf that the vm pair is originally(randomly) assigned to
	int vm1pm;
	int vm2pm;
	int VNFid; 
	int comrate;
	int mcfVNFid;//the id of the vnf that the vm pair is assigned to after mcf migration
	int vmbVNFid;//the id of the vnf that the vm pair is assigned to after vmbased (algo1) migration
	int mbbVNFid;//the id of the vnf that the vm pair is assigned to after mbbased (algo2) migration
	boolean mbbmigrated=false;
	public VMpair(int pairid,int vm1pm,int vm2pm,int VNFid,int comrate){
		this.pairid=pairid;
		this.vm1pm = vm1pm;
		this.vm2pm = vm2pm;
		this.VNFid = VNFid;
		this.comrate = comrate;
	}
}
