import java.util.ArrayList;


public class Graph {
	int V;
	ArrayList<Vertex> ver=new ArrayList<Vertex>(0);
	ArrayList<Edge> edge= new ArrayList<Edge>(0);
	
	public Graph (int V){
		this.V=V;
		for(int i=0;i<V;i++){
			ver.add(new Vertex(0,0));
		}
		
	}
	void addEdge(int u, int v, int capacity){
		edge.add(new Edge(0,capacity,u,v));
	}
	void preFlow(int s){
		ver.get(s).h=ver.size();
		for(int i=0;i<edge.size();i++){
			if (edge.get(i).u==s){
				edge.get(i).flow=edge.get(i).capacity;
				ver.get(edge.get(i).v).e_flow+= edge.get(i).flow;
				edge.add(new Edge(-edge.get(i).flow,0,edge.get(i).v,s));
				
			}
		}
	}
	int overFlowVertex(ArrayList<Vertex> ver,int s, int t){
		for(int i=1; i<ver.size()-1;i++){
			
			if(i!= s && i !=t &&ver.get(i).e_flow>0)
			//if(ver.get(i).e_flow>0)
				return i;
		}
		return -1;
	}
	void updateReverseEdgeFlow(int i, int flow){
		int u = edge.get(i).v, v=edge.get(i).u;
		
		for(int j=0; j<edge.size();j++){
			if(edge.get(j).v==v&& edge.get(j).u==u ){
				edge.get(j).flow-=flow;
				return;
			}
		}
		Edge e = new Edge(0,flow,u,v);
		edge.add(e);
	}
	Boolean push(int u){
		for (int i=0;i<edge.size();i++){
			if (edge.get(i).u==u){
				if(edge.get(i).flow==edge.get(i).capacity)
					continue;
				if(ver.get(u).h>ver.get(edge.get(i).v).h){
					int flow = Math.min(edge.get(i).capacity - edge.get(i).flow, ver.get(u).e_flow);
					ver.get(u).e_flow-= flow;
					ver.get(edge.get(i).v).e_flow+=flow;
					edge.get(i).flow+=flow;
					
					updateReverseEdgeFlow(i,flow);
					
					return true;
				}
			}
		}
		return false;
	}
	void relabel(int u){
		int mh= Integer.MAX_VALUE;
		
		for(int i=0; i<edge.size();i++){
			if (edge.get(i).u==u){
				if(edge.get(i).flow==edge.get(i).capacity)
					continue;
				if(ver.get(edge.get(i).v).h<mh){
					mh = ver.get(edge.get(i).v).h;
					ver.get(u).h= mh+1;
				}
			}
		}
	}
	int getMaxFlow(int s, int t){
		preFlow(s);
		
		while (overFlowVertex(ver,s,t)!=-1){
			//System.out.println("thanks");
			int u = overFlowVertex(ver,s,t);
			if(!push(u))
				relabel(u);
		}
		//return ver.get(ver.size()-1).e_flow;
		return ver.get(t).e_flow;
	}
}
