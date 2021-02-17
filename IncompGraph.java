import java.util.ArrayList;


public class IncompGraph {
	CopyVM hvmcList[];
	ArrayList<CompNode> list = new ArrayList<CompNode> (0);
	public IncompGraph(CopyVM[] hvmcList) {
		//super();
		this.hvmcList = hvmcList;
		for(int i=0; i < hvmcList.length;i++){
			CopyVM temp = hvmcList[i]; 
			list.add(new CompNode(temp.vmid,temp.pmid, temp.cost));
		}
	}
	
	public void addBin(int pmid,){
		
	}
	

}
