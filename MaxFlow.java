import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


public class MaxFlow {
	final int inf= Integer.MAX_VALUE;
	
	int n; //n is the total amount of nodes in the graph
	int k;
	int p;
	int r;//might not need
	ArrayList <ArrayList<Integer>> capacity ;
	ArrayList <ArrayList<Integer>> flow ;
	
	ArrayList<Integer> height,excess,seen;
	Queue<Integer> excess_vertices ;
	
	public MaxFlow(ArrayList <ArrayList<Integer>> capacity,int k,int p, int r){
		this.capacity=capacity;
		this.k=k;
		this.p=p;
		this.r=r;
		this.n=1+p+(k*k*k/4)+1;
		
		height = new ArrayList<Integer>(n);
		excess = new ArrayList<Integer>(n);
		seen = new ArrayList<Integer>(n);
		excess_vertices = new LinkedList<Integer>();
		flow = new ArrayList <ArrayList<Integer>>(n);
		
		for (int i=0;i<n;i++){
			flow.add(i, new ArrayList<Integer>(n));
		}
		
	}
	
	//push method
	void push (int u, int v){
		int d = Math.min(excess.get(u), capacity.get(u).get(v)-flow.get(u).get(v));
		flow.get(u).set(v, flow.get(u).get(v)+d);
		flow.get(v).set(u, flow.get(v).get(u)-d);
		excess.set(u, excess.get(u)-d);
		excess.set(v, excess.get(v)+d);
		
		if(d>0&&excess.get(v)==d)
			excess_vertices.add(v);
		
		
	}
	
	void relabel (int u){
		int d = inf;
		for(int i=0; i<n; i++){
			
			if(capacity.get(u).get(i)-flow.get(u).get(i)>0){
				d=Math.min(d, height.get(i));
			}
			
		}
		if(d<inf){
			height.set(u,d+1);
		}
	}
	void discharge(int u){
		while (excess.get(u)>0){
			//System.out.println("dbug");
			if(seen.get(u)<n){
				int v= seen.get(u);
				if(capacity.get(u).get(v)-flow.get(u).get(v)>0 && height.get(u)>height.get(v)){
					push(u,v);
				}
				else{
					seen.set(u,u+1);
				}
			}
			else{
				relabel(u);
				seen.set(u,0);
			}
		}
	}
	
	int max_flow(){
		//height.assign(n, 0);
		//fill height with n int values, all of which equal 0;
		for(int i=0;i<n;i++){
			height.add(i, new Integer(0));
		}
		height.set(0,n);
		//flow.assign(n, vector<int>(n, 0));
		//fill flow with n <integer> arraylists of size n all full with 0;
		for(int i=0;i<n;i++){
			flow.add(i,new ArrayList<Integer>(n));
			for(int j=0;j<n;j++){
				flow.get(i).add(j, 0);
			}
		}
		//excess.assign(n, 0);
		//fill excess with n elements all of which are 0
		for(int i=0;i<n;i++){
			excess.add(i,0);
		}
		excess.set(0, inf);
		for(int i=1;i<n;i++){
			push(0,i);
		}
		//seen.assign(n, 0);
		//fill seen with n items that are all 0...
		for (int i=0;i<n;i++){
			seen.add(i,0);
		}
		while(!excess_vertices.isEmpty()){
			//System.out.println("dbug");
			int u = excess_vertices.peek();
			excess_vertices.remove();
			
			if(u!=0 && u!= n-1)
				discharge(u);
		}
		int max_flow=0;
		for(int i=0; i<n;i++)
			max_flow+=flow.get(0).get(i);
		return max_flow;
	}

}
