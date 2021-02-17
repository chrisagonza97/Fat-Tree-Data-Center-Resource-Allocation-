import java.util.ArrayList;


public class CompNode {
	int vmid=-1;
	int pmid=-1;
	int color=-1;
	int cost=-1;
	int index=-1;
	ArrayList<Integer> nbors = new ArrayList<Integer>(0); //neighbors of a node(incompatable by index.
	
	public CompNode(int vmid, int pmid,int cost){
		this.vmid=vmid;
		this.pmid=pmid;
		this.cost=cost;
	}
	public CompNode(int pmid){
		this.pmid=pmid;
	}
	//method returns if node or neighbors are the passed color.
	public boolean checkColor(int color){
		if (this.color==color){
			return true;
		}
		for(int i=0;i<nbors.size();i++){
			//if (nbors.get(i).color==color){
				//return.true
			//}
		}
		return false;
	}

}
