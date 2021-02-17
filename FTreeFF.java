import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.*;
public class FTreeFF implements Serializable{
	int arrayindex=0;
	int arryvindex=0;
	int arryoindex=0;
	int vclistindex=0;
	int k=1;
	int vmpairs;
	int VNFs;
	int trafficlow;
	int trafficup;
	int p;//number of original VMs
	int m;//inital storage capacity
	int r;//How many total copies of each VM
	int randInc;
	int firstpm;
	int lastpm;
	int pmcount;
	int regflo;
	VirtualM vms[];
	Integer orgnl[];
	Integer orgnlsize[];
	Integer rvm[];
	Integer rn[];
	Node tree[];
	Node fftree[];
	Node itree[];
	Node htree[];
	//ArrayList<Node>
	CopyVM vmcList[];
	CopyVM hvmcList[];
	ArrayList <VNF> VNFlist = new ArrayList<VNF> (0);
	ArrayList<VMpair> Pairlist = new ArrayList<VMpair>(0);
	ArrayList <Integer> onPMs = new ArrayList <Integer>(0);	
	ArrayList<CompNode> igraph;
	public int mu;
	public int vmsizel;
	public int vmsizeu;
	
	public FTreeFF(){
		
	}
	public void buildTree(){
		int treesize = (k*k/4)+(k/2*k)+(k/2*k)+ (k*k*k/4);
		//the different Node arrays are for different consolidation alorithms, they are all essentially the same tree
		tree= new Node[treesize];//tree for servcons
		fftree=new Node[treesize];//tree for FF
		itree=new Node[treesize];//tree for improvedservcons
		htree = new Node [treesize];//tree for two stage heur. algo.
		vmcList = new CopyVM[p*(r-1)];
		hvmcList = new CopyVM[p*(r-1)];
		int id=0;
        //int inc=0;
        int agg=k; //index of first agg switch;
        for (int i=0;i<(k*k)/4;i++){  //create (k^2)/4 core switches
            add(id,0);
            //tree0.add(new Node(id,0));
            //for (int j=0;j<k;j++){
                //if(i>0 && i%(k/2)==0) // k/2 groups of k/2 core switches have edges to the same agg switches
                //agg++;
                //tree.get(id).addedge(tree.get(agg+j*(k/2)));
                
            //}
            id++;
            
        }
        int eid=0;
        for (int j=0;j<(k*k)/2;j++){ 
                
                add(id,1);
                //tree0.add(new Node(id,1));
                if(j==0)
                eid = id-1;//index before first aggr switch.
                id++;
                //create (k*k)/2 aggregation switch
        } 
        
        
        for(int x=0;x<(k*k)/2;x++){
                
                add(id,2);
                //tree0.add(new Node(id,2));
                id++; //create (k^2)/2 edge switches
            }
        int y;
        int count = ((k*k)/4+(k*k)/2)-1; //this should be the index before the first edge switch
                                     //which is the sum of the index of core and aggr switches.
        for(y=0;y<(k*k*k)/4;y++){
                
                if (y%(k/2)==0) //each edge switch gets k/2 PMs
                count++;
                
                add(id,3);
                tree[id].setedgeid(count); //giving the id of the edge the PM belongs to
                fftree[id].setedgeid(count);
                itree[id].setedgeid(count);
                htree[id].setedgeid(count);
                //tree0.add(new Node(id,3));
                //tree0.get(id).setedgeid(count);
                
                int set = Math.min(p,m);//the smaller between number of original
                						//and the capacity m is the ESC.
                tree[id].setesc(set);//setting the initial effective storage capacity p
                tree[count].addpmsofedge(tree[id]); //adding PM to edge
                
                itree[id].setesc(set);//setting the initial effective storage capacity p
                itree[count].addpmsofedge(itree[id]); //adding PM to edge
                
                fftree[id].setesc(set);
                fftree[count].addpmsofedge(fftree[id]);
                
                htree[id].setesc(set);//setting the initial effective storage capacity p
                htree[count].addpmsofedge(htree[id]); //adding PM to edge
                
                //tree0.get(id).setesc(set);//setting the initial effective storage capacity p
                //tree0.get(count).addpmsofedge(tree.get(id));
                
                
                
                id++; //create k^3/4 physical machines, k/2 machines for each edge switch
                }
        for(int i=0;i<=lastpm;i++){
        	tree[i].esc=Math.min(p,m);
        }
         for (int i=0;i<(k*k)/4;i++){  
            //tree.add(new Node(id,0));
            if(i>0 && i%(k/2)==0) // k/2 groups of k/2 core switches have edges to the same agg switches
                agg++;
            for (int j=0;j<k;j++){
                //if(i>0 && i%(k/2)==0) // k/2 groups of k/2 core switches have edges to the same agg switches
                //agg++;
            	itree[i].addedge(tree[agg+j*(k/2)]);
                tree[i].addedge(tree[agg+j*(k/2)]);
                fftree[i].addedge(fftree[agg+j*(k/2)]);
                htree[i].addedge(tree[agg+j*(k/2)]);
                //tree0.get(i).addedge(tree.get(agg+j*(k/2)));
            }
            //id++;
            
        }                               
        for(int i =0;i<tree.length;i++){
        	System.out.println(tree[i]);
        }
        
        System.out.println();
        //now we create the PODs
        ArrayList<POD> plist = new ArrayList<POD>();
        ArrayList<POD> iplist = new ArrayList<POD>();
        ArrayList<POD> plist0 = new ArrayList<POD>();
        ArrayList<POD> hplist = new ArrayList<POD>();
        int podcount = eid;
        for(int i=0;i<k;i++){
            plist.add(new POD(i,k));
            iplist.add(new POD(i,k));
            plist0.add(new POD(i,k));
            hplist.add(new POD(i,k));
            podcount=eid;//index of before first aggregate switch
            podcount += i*(k/2);//index before the first aggregate switch of podi
            for (int j =0; j<k/2;j++){
                podcount++;
                //System.out.println(i+" "+j);
                plist.get(i).addNode(tree[podcount]); //adding aggr switch to podi
                iplist.get(i).addNode(itree[podcount]);
                plist0.get(i).addNode(fftree[podcount]);
                hplist.get(i).addNode(htree[podcount]);
                //plist0.get(i).addNode(tree0.get(podcount));
            }
            podcount=eid + i *(k/2);//set index back to index before first aggr switch of pod i
            podcount += (k*k)/2; //set index to index before first edge switch
            for(int j=0;j<k/2;j++){
                
                podcount++;
                plist.get(i).addNode(tree[podcount]); //adding edge switch to podi
                iplist.get(i).addNode(itree[podcount]);
                plist0.get(i).addNode(fftree[podcount]);
                hplist.get(i).addNode(htree[podcount]);
                //plist0.get(i).addNode(tree0.get(podcount));
            }
        }
        //now we print the IDs of the PODS
        for (int i=0;i<k;i++){
            System.out.println(plist.get(i).toString());
        }
	}
	public void buildTreeB(){
		int treesize = (k*k/4)+(k/2*k)+(k/2*k)+ (k*k*k/4);
		//the different Node arrays are for different consolidation alorithms, they are all essentially the same tree
		tree= new Node[treesize];//tree for servcons
		fftree=new Node[treesize];//tree for FF
		itree=new Node[treesize];//tree for improvedservcons
		htree = new Node [treesize];//tree for two stage heur. algo.
		//vmcList = new CopyVM[p*(r-1)];
		//hvmcList = new CopyVM[p*(r-1)];
		
		int id=0;
        //int inc=0;
        int agg=k; //index of first agg switch;
        for (int i=0;i<(k*k)/4;i++){  //create (k^2)/4 core switches
            add(id,0);
            //tree0.add(new Node(id,0));
            //for (int j=0;j<k;j++){
                //if(i>0 && i%(k/2)==0) // k/2 groups of k/2 core switches have edges to the same agg switches
                //agg++;
                //tree.get(id).addedge(tree.get(agg+j*(k/2)));
                
            //}
            id++;
            
        }
        int eid=0;
        for (int j=0;j<(k*k)/2;j++){ 
                
                add(id,1);
                //tree0.add(new Node(id,1));
                if(j==0)
                eid = id-1;//index before first aggr switch.
                id++;
                //create (k*k)/2 aggregation switch
        } 
        
        
        for(int x=0;x<(k*k)/2;x++){
                
                add(id,2);
                //tree0.add(new Node(id,2));
                id++; //create (k^2)/2 edge switches
            }
        int y;
        int count = ((k*k)/4+(k*k)/2)-1; //this should be the index before the first edge switch
                                     //which is the sum of the index of core and aggr switches.
        for(y=0;y<(k*k*k)/4;y++){
                
                if (y%(k/2)==0) //each edge switch gets k/2 PMs
                count++;
                
                add(id,3);
                tree[id].setedgeid(count); //giving the id of the edge the PM belongs to
                fftree[id].setedgeid(count);
                itree[id].setedgeid(count);
                htree[id].setedgeid(count);
                //tree0.add(new Node(id,3));
                //tree0.get(id).setedgeid(count);
                
                int set = Math.min(p,m);//the smaller between number of original
                						//and the capacity m is the ESC.
                tree[id].setesc(set);//setting the initial effective storage capacity p
                tree[count].addpmsofedge(tree[id]); //adding PM to edge
                
                itree[id].setesc(set);//setting the initial effective storage capacity p
                itree[count].addpmsofedge(itree[id]); //adding PM to edge
                
                fftree[id].setesc(set);
                fftree[count].addpmsofedge(fftree[id]);
                
                htree[id].setesc(set);//setting the initial effective storage capacity p
                htree[count].addpmsofedge(htree[id]); //adding PM to edge
                
                //tree0.get(id).setesc(set);//setting the initial effective storage capacity p
                //tree0.get(count).addpmsofedge(tree.get(id));
                
                
                
                id++; //create k^3/4 physical machines, k/2 machines for each edge switch
                }
         for (int i=0;i<(k*k)/4;i++){  
            //tree.add(new Node(id,0));
            if(i>0 && i%(k/2)==0) // k/2 groups of k/2 core switches have edges to the same agg switches
                agg++;
            for (int j=0;j<k;j++){
                //if(i>0 && i%(k/2)==0) // k/2 groups of k/2 core switches have edges to the same agg switches
                //agg++;
            	itree[i].addedge(tree[agg+j*(k/2)]);
                tree[i].addedge(tree[agg+j*(k/2)]);
                fftree[i].addedge(fftree[agg+j*(k/2)]);
                htree[i].addedge(tree[agg+j*(k/2)]);
                //tree0.get(i).addedge(tree.get(agg+j*(k/2)));
            }
            //id++;
            
        }                               
        for(int i =0;i<tree.length;i++){
        	System.out.println(tree[i]);
        }
        
        System.out.println();
        //now we create the PODs
        ArrayList<POD> plist = new ArrayList<POD>();
        ArrayList<POD> iplist = new ArrayList<POD>();
        ArrayList<POD> plist0 = new ArrayList<POD>();
        ArrayList<POD> hplist = new ArrayList<POD>();
        int podcount = eid;
        for(int i=0;i<k;i++){
            plist.add(new POD(i,k));
            iplist.add(new POD(i,k));
            plist0.add(new POD(i,k));
            hplist.add(new POD(i,k));
            podcount=eid;//index of before first aggregate switch
            podcount += i*(k/2);//index before the first aggregate switch of podi
            for (int j =0; j<k/2;j++){
                podcount++;
                //System.out.println(i+" "+j);
                plist.get(i).addNode(tree[podcount]); //adding aggr switch to podi
                iplist.get(i).addNode(itree[podcount]);
                plist0.get(i).addNode(fftree[podcount]);
                hplist.get(i).addNode(htree[podcount]);
                //plist0.get(i).addNode(tree0.get(podcount));
            }
            podcount=eid + i *(k/2);//set index back to index before first aggr switch of pod i
            podcount += (k*k)/2; //set index to index before first edge switch
            for(int j=0;j<k/2;j++){
                
                podcount++;
                plist.get(i).addNode(tree[podcount]); //adding edge switch to podi
                iplist.get(i).addNode(itree[podcount]);
                plist0.get(i).addNode(fftree[podcount]);
                hplist.get(i).addNode(htree[podcount]);
                //plist0.get(i).addNode(tree0.get(podcount));
            }
        }
        //now we print the IDs of the PODS
        for (int i=0;i<k;i++){
            System.out.println(plist.get(i).toString());
        }
	}
	public void placeOriginalVms(int x){
		//int i=0;
		orgnl = new Integer[p];
		vms=new VirtualM[p];
		//int j = 0;
		int placed=0;
		int j=0;
		for (int i=0;i<p;i++){
			tree[j+firstpm].addVM(i, m);
			tree[j+firstpm].decesc();
            tree[j+firstpm].setactive();
            tree[j+firstpm].setissource();
            tree[j+firstpm].addOrigi();
            addV(i,j+firstpm);
			
            fftree[j+firstpm].addVM(i,m);
            fftree[j+firstpm].decesc();
            fftree[j+firstpm].setactive();
            fftree[j+firstpm].setissource();
            fftree[j+firstpm].addOrigi();
            turnOnPM(j+firstpm);
            System.out.println("VM " + i+" Has been placed in PM with id of: "+ (j+firstpm));
            addO(j+firstpm);
			placed++;
			
			if(placed%m==0){
				j++;
			}
			
		}
	}
	public void placeOriginalVms(){
		Random rand= new Random();
        int randnum;
        boolean flag;
        //VMs are just integer numbers here.
        vms=new VirtualM[p];
        orgnl=new Integer[p];
        //ArrayList<VirtualM> vms = new ArrayList(); //list of original VMs 
        //ArrayList<Integer> orgnl= new ArrayList<Integer>(0);
        
        //ArrayList<VirtualM> vms0 = new ArrayList(); //list of original VMs 
        //ArrayList<Integer> orgnl0= new ArrayList<Integer>(0);
        
        
        for (int i=0; i<p;i++){
            do {
               randnum = rand.nextInt((lastpm -firstpm)+1)+ firstpm;
           
               flag=tree[randnum].addVM(i,m); //placing p VMs randomly into PMs
               fftree[randnum].addVM(i,m);
               itree[randnum].addVM(i,m);
               htree[randnum].addVM(i,m);
               //if(i==0){
            	   //tree[randnum].addVM(1,m);
               //}
               //flag=tree0.get(randnum).addVM(i,m);
               if(flag){
            	   itree[randnum].addOrigi();
            	   tree[randnum].addOrigi();
            	   fftree[randnum].addOrigi();
            	   htree[randnum].addOrigi();
            	   //tree0.get(randnum).addOrigi();
            	   //if(i==0){
            		//   tree[randnum].addOrigi();
            		   
            	 //  }
               }
                                 //m is passed to make sure the a vm is not placed at a pm at capacity
               
            } while(!flag);
            addV(i,randnum);
            //vms0.add(new VirtualM(i,randnum));
            turnOnPM(randnum);
            //onPMs.add(randnum);
            tree[randnum].decesc();
            tree[randnum].setactive();
            tree[randnum].setissource();
            
            itree[randnum].decesc();
            itree[randnum].setactive();
            itree[randnum].setissource();
            
            htree[randnum].decesc();
            htree[randnum].setactive();
            htree[randnum].setissource();
            
            fftree[randnum].decesc();
            fftree[randnum].setactive();
            fftree[randnum].setissource();
            
            addO(randnum);
            //if(i==0){
            	//addV(1,randnum);
            	//tree[randnum].decesc();
            	//addO(randnum);
            //}
            
            //tree0.get(randnum).decesc();
            //tree0.get(randnum).setactive();
            //tree0.get(randnum).setissource();
            //orgnl0.add(randnum);
            System.out.println("VM " + i+" Has been placed in PM with id of: "+ randnum);
            //if(i==0)
            	//i++;
        }
	}
	public void placeOriginalVms112020(){
		Random rand= new Random();
        int randnum;
        boolean flag;
        //VMs are just integer numbers here.
        vms=new VirtualM[p];
        orgnl=new Integer[p];
        //ArrayList<VirtualM> vms = new ArrayList(); //list of original VMs 
        //ArrayList<Integer> orgnl= new ArrayList<Integer>(0);
        
        //ArrayList<VirtualM> vms0 = new ArrayList(); //list of original VMs 
        //ArrayList<Integer> orgnl0= new ArrayList<Integer>(0);
        
        
        for (int i=0; i<p;i++){
            do {
               randnum = rand.nextInt((lastpm -firstpm)+1)+ firstpm;
           
               flag=tree[randnum].addVM(i,m); //placing p VMs randomly into PMs
               fftree[randnum].addVM(i,m);
               itree[randnum].addVM(i,m);
               htree[randnum].addVM(i,m);
               
               flag=tree[randnum].addVM112020(i,m); //placing p VMs randomly into PMs
               fftree[randnum].addVM112020(i,m);
               itree[randnum].addVM112020(i,m);
               htree[randnum].addVM112020(i,m);
               
             /*  tree[randnum].vmcount-=1;
               fftree[randnum].vmcount-=1;
               itree[randnum].vmcount-=1;
               htree[randnum].vmcount-=1;*/
               //if(i==0){
            	   //tree[randnum].addVM(1,m);
               //}
               //flag=tree0.get(randnum).addVM(i,m);
               if(flag){
            	   itree[randnum].addOrigi();
            	   tree[randnum].addOrigi();
            	   fftree[randnum].addOrigi();
            	   htree[randnum].addOrigi();
            	   //tree0.get(randnum).addOrigi();
            	   //if(i==0){
            		//   tree[randnum].addOrigi();
            		   
            	 //  }
               }
                                 //m is passed to make sure the a vm is not placed at a pm at capacity
               
            } while(!flag);
            addV(i,randnum);
            //vms0.add(new VirtualM(i,randnum));
            turnOnPM(randnum);
            //onPMs.add(randnum);
            tree[randnum].decesc();
            tree[randnum].setactive();
            tree[randnum].setissource();
            
            itree[randnum].decesc();
            itree[randnum].setactive();
            itree[randnum].setissource();
            
            htree[randnum].decesc();
            htree[randnum].setactive();
            htree[randnum].setissource();
            
            fftree[randnum].decesc();
            fftree[randnum].setactive();
            fftree[randnum].setissource();
            
            addO(randnum);
            //if(i==0){
            	//addV(1,randnum);
            	//tree[randnum].decesc();
            	//addO(randnum);
            //}
            
            //tree0.get(randnum).decesc();
            //tree0.get(randnum).setactive();
            //tree0.get(randnum).setissource();
            //orgnl0.add(randnum);
            System.out.println("VM " + i+" Has been placed in PM with id of: "+ randnum);
            //if(i==0)
            	//i++;
        }
        //now add rand comps
        for(int i=0;i<orgnl.length;i++){
        	for(int g=0; g<randInc;g++){
        		//System.out.println("yetg");
        	//}
        	int randomNum;
        	boolean iflag=false;
        	do{
        		
        		 randomNum = rand.nextInt((lastpm - firstpm) + 1) + firstpm;
        		 //System.out.println(randomNum+" "+i+"iorgnl is "+orgnl[i]);
        		 //if randomnum is already incomp or if it the orgnl reroll.
        		 iflag=false;
        		 for(int j=0 ;j<tree[orgnl[i]].vmlist2.size();j++){
        			 if(tree[orgnl[i]].vmlist2.get(j).vmid!=i){
        				 System.out.println("vmid "+ tree[orgnl[i]].vmlist2.get(j).vmid);
        				 continue;
        			 }
        			 for(int l=0;l<tree[orgnl[i]].vmlist2.get(j).randIncs.size();l++){
        				 if (randomNum==tree[orgnl[i]].vmlist2.get(j).randIncs.get(l)){
        					// System.out.println("   "+tree[orgnl[i]].vmlist2.get(j).randIncs.get(l));
        					 iflag=true;
        				 }
        			 }
        		 }
        		 if(iflag){
        			 randomNum=orgnl[i];
        		 }
            }while(randomNum==orgnl[i]);
        		for(int j=0 ;j<tree[orgnl[i]].vmlist2.size();j++){		
        			if(tree[orgnl[i]].vmlist2.get(j).vmid!=i){
        				continue;
        			}
        			tree[orgnl[i]].vmlist2.get(j).randIncs.add(randomNum);
        			htree[orgnl[i]].vmlist2.get(j).randIncs.add(randomNum);
        			fftree[orgnl[i]].vmlist2.get(j).randIncs.add(randomNum);
        			itree[orgnl[i]].vmlist2.get(j).randIncs.add(randomNum);
        			
        		}
        		
        	}
        }
        System.out.println("Incompats:");
        for (int i=0; i<orgnl.length;i++){
        	System.out.println(""+i+":");
        	for(int j=0;j< tree[orgnl[i]].vmlist2.size();j++){
        		if(tree[orgnl[i]].vmlist2.get(j).vmid!=i){
        			continue;
        		}
        		for(int l=0; l<tree[orgnl[i]].vmlist2.get(j).randIncs.size();l++){
        			System.out.println("	"+tree[orgnl[i]].vmlist2.get(j).randIncs.get(l));
        		}
        		
        	}
        }
        	
	}
	public void placeOriginalVms112020B(){
		Random rand= new Random();
        int randnum;
        boolean flag;
        //VMs are just integer numbers here.
        vms=new VirtualM[p];
        orgnl=new Integer[p];
        //ArrayList<VirtualM> vms = new ArrayList(); //list of original VMs 
        //ArrayList<Integer> orgnl= new ArrayList<Integer>(0);
        
        //ArrayList<VirtualM> vms0 = new ArrayList(); //list of original VMs 
        //ArrayList<Integer> orgnl0= new ArrayList<Integer>(0);
        
        
        for (int i=0; i<p;i++){
            do {
               randnum = rand.nextInt((lastpm -firstpm)+1)+ firstpm;
           
               flag=tree[randnum].addVM(i,m); //placing p VMs randomly into PMs
               fftree[randnum].addVM(i,m);
               itree[randnum].addVM(i,m);
               htree[randnum].addVM(i,m);
               
               flag=tree[randnum].addVM112020(i,m); //placing p VMs randomly into PMs
               fftree[randnum].addVM112020(i,m);
               itree[randnum].addVM112020(i,m);
               htree[randnum].addVM112020(i,m);
               
             /*  tree[randnum].vmcount-=1;
               fftree[randnum].vmcount-=1;
               itree[randnum].vmcount-=1;
               htree[randnum].vmcount-=1;*/
               //if(i==0){
            	   //tree[randnum].addVM(1,m);
               //}
               //flag=tree0.get(randnum).addVM(i,m);
               if(flag){
            	   itree[randnum].addOrigi();
            	   tree[randnum].addOrigi();
            	   fftree[randnum].addOrigi();
            	   htree[randnum].addOrigi();
            	   //tree0.get(randnum).addOrigi();
            	   //if(i==0){
            		//   tree[randnum].addOrigi();
            		   
            	 //  }
               }
                                 //m is passed to make sure the a vm is not placed at a pm at capacity
               
            } while(!flag);
            addV(i,randnum);
            //vms0.add(new VirtualM(i,randnum));
            turnOnPM(randnum);
            //onPMs.add(randnum);
            tree[randnum].decesc();
            tree[randnum].setactive();
            tree[randnum].setissource();
            
            itree[randnum].decesc();
            itree[randnum].setactive();
            itree[randnum].setissource();
            
            htree[randnum].decesc();
            htree[randnum].setactive();
            htree[randnum].setissource();
            
            fftree[randnum].decesc();
            fftree[randnum].setactive();
            fftree[randnum].setissource();
            
            addO(randnum);
            //if(i==0){
            	//addV(1,randnum);
            	//tree[randnum].decesc();
            	//addO(randnum);
            //}
            
            //tree0.get(randnum).decesc();
            //tree0.get(randnum).setactive();
            //tree0.get(randnum).setissource();
            //orgnl0.add(randnum);
            System.out.println("VM " + i+" Has been placed in PM with id of: "+ randnum);
            //if(i==0)
            	//i++;
        }
        //now add rand comps
        rn = new Integer[p];
        for (int i=0;i<p;i++){
        	rn[i]=rand.nextInt((5 - 1) + 1) + 1;
        }
        
        for(int i=0;i<orgnl.length;i++){
        	for(int g=0; g<rn[i];g++){
        		//System.out.println("yetg");
        	//}
        	int randomNum;
        	boolean iflag=false;
        	do{
        		
        		 randomNum = rand.nextInt((lastpm - firstpm) + 1) + firstpm;
        		 //System.out.println(randomNum+" "+i+"iorgnl is "+orgnl[i]);
        		 //if randomnum is already incomp or if it the orgnl reroll.
        		 iflag=false;
        		 for(int j=0 ;j<tree[orgnl[i]].vmlist2.size();j++){
        			 if(tree[orgnl[i]].vmlist2.get(j).vmid!=i){
        				 System.out.println("vmid "+ tree[orgnl[i]].vmlist2.get(j).vmid);
        				 continue;
        			 }
        			 for(int l=0;l<tree[orgnl[i]].vmlist2.get(j).randIncs.size();l++){
        				 if (randomNum==tree[orgnl[i]].vmlist2.get(j).randIncs.get(l)){
        					// System.out.println("   "+tree[orgnl[i]].vmlist2.get(j).randIncs.get(l));
        					 iflag=true;
        				 }
        			 }
        		 }
        		 if(iflag){
        			 randomNum=orgnl[i];
        		 }
            }while(randomNum==orgnl[i]);
        		for(int j=0 ;j<tree[orgnl[i]].vmlist2.size();j++){		
        			if(tree[orgnl[i]].vmlist2.get(j).vmid!=i){
        				continue;
        			}
        			tree[orgnl[i]].vmlist2.get(j).randIncs.add(randomNum);
        			htree[orgnl[i]].vmlist2.get(j).randIncs.add(randomNum);
        			fftree[orgnl[i]].vmlist2.get(j).randIncs.add(randomNum);
        			itree[orgnl[i]].vmlist2.get(j).randIncs.add(randomNum);
        			
        		}
        		
        	}
        }
        System.out.println("Incompats:");
        for (int i=0; i<orgnl.length;i++){
        	System.out.println(""+i+":");
        	for(int j=0;j< tree[orgnl[i]].vmlist2.size();j++){
        		if(tree[orgnl[i]].vmlist2.get(j).vmid!=i){
        			continue;
        		}
        		for(int l=0; l<tree[orgnl[i]].vmlist2.get(j).randIncs.size();l++){
        			System.out.println("	"+tree[orgnl[i]].vmlist2.get(j).randIncs.get(l));
        		}
        		
        	}
        }
        //now make random number [5,10] of r bins for each original VM
        //save random number in array for each original VM, indexed by VM ID (much like the orgnl[] array)
        //whenever r (number of VM copy for a VM) is used in a for loop, use rvm[]
        rvm= new Integer[p];

        
        

        for (int i=0;i<p;i++){
        	rvm[i]=rand.nextInt((10 - 5) + 1) + 5;
        }
        int sizeofarrs=0;
		for (int i=0;i<p;i++){
			sizeofarrs+=rvm[i];
		}
		vmcList = new CopyVM[sizeofarrs];
		hvmcList = new CopyVM[sizeofarrs];
        	
	}
	private void turnOnPM(int randnum) {
		boolean flag = true;
		for(int i=0; i<onPMs.size();i++){
			if(randnum==onPMs.get(i).intValue()){
				flag=false;
			}
		}
		if(flag==true){
			onPMs.add(randnum);
		}
		
	}
	public void createMCFFile(){
		
		int arccount= (p*pmcount-p);//this is how many edges there are in the middle
        arccount+= p;// there are p edges from the supply node to the original vms
        arccount+=pmcount;//the number of edges between the PMs and the demand node is 
                         //the number of physical machines.
        int nodecount=p+pmcount+2;
        String firstline = "p min "+ nodecount+" "+arccount+"\n";
        String secline = "c min-cost flow problem with "+nodecount+" nodes and "+arccount+ " arcs \n";
        //System.out.println(firstline +secline);
        String thirdline ="n 1 "+ p*(r-1)+ "\n";//supply is p times r-1
        String fourthln="c supply of "+p*(r-1)+" at node 1 \n"; 
        String fifthln = "n "+nodecount+" "+-1*p*(r-1)+"\n";
        String sixthln = "c demand of "+-1*p*(r-1)+" at node "+nodecount+"\n";
        String sevln = "c arc list follows \n";
        String eithln = "c arc has <tail> <head> <capacity l.b.> <capacity u.b> <cost> \n";
        String firstlns=firstline+secline+thirdline+fourthln+fifthln+sixthln+sevln+eithln;
        //System.out.print(firstlns);
        StringBuilder supplyarcs = new StringBuilder("");
        
        //String supplyarcs="";
        int countnode=2;
        for (int i=0;i<p;i++){
        	supplyarcs.append("a 1 "+(countnode)+" 0 "+(r-1)+" 0 \n");
            //supplyarcs+="a 1 "+(countnode)+" 0 "+(r-1)+" 0 \n";
            countnode++;
        }
        int firstvm=countnode;
        
        int startv=2;
        //System.out.print(supplyarcs);
        StringBuilder vmarcs=new StringBuilder("");
        //String vmarcs ="";
        
        //make the arcs from the location of the orginal vms to every physical machine
        //except the one the VM is originally stored in.
        for (int i=0;i<p;i++){
            
            countnode=firstvm;
            for(int j=0;j<pmcount;j++){
                if (vms[i].getpmid()==j+firstpm){
                    countnode++;
                    continue;
                }
                vmarcs.append("a "+(i+2)+" "+(countnode)+" 0 "+1+" "+
                distance(tree,tree[vms[i].getpmid()],tree[j+firstpm],k)+"\n");
                //vmarcs+= "a "+(i+2)+" "+(countnode)+" 0 "+1+" "+
                //distance(tree,tree[vms[i].getpmid()],tree[j+firstpm],k)+"\n";
                countnode++;    
            }
        }
        
        //System.out.print(vmarcs);
        //now we make arcs from every physical machine to the destination node
        StringBuilder pmarcs = new StringBuilder("");
        //String pmarcs="";
        for(int i=0; i<pmcount;i++){
        	pmarcs.append("a "+(i+firstvm)+" "+(countnode)+" 0 "+tree[i+firstpm].getesc()+" "+"0 \n");
            //pmarcs+="a "+(i+firstvm)+" "+(countnode)+" 0 "+tree[i+firstpm].getesc()+" "+"0 \n";
        }
        
        String output= firstlns+supplyarcs+vmarcs+pmarcs;
        //System.out.print(output);
       ;
        try{
            //this is where i am writing my file to.
            File file = new File("mcf_replication.inp");
            file.createNewFile();
            FileWriter fw = new FileWriter("mcf_replication.inp");
            fw.write(output);
            fw.flush();
            fw.close();
        }catch (Exception e){System.out.println("failed 1");}
        //int checkif = Math.min(m, p);
        if(p*r>m*pmcount){//if the amount of copies times the amount of original vms
                            //is more than m times the amount of pms, replication not possible
            System.out.println("replication of every VM not possible.");
        }
        System.out.println("mcf_replication.inp has been written to in project root file directory");
	}
	public void createMCFFile112020(){
		int arccount= (p*pmcount-p);//this is how many edges there are in the middle
        arccount+= p;// there are p edges from the supply node to the original vms
        arccount+=pmcount;//the number of edges between the PMs and the demand node is 
                         //the number of physical machines.
        //change amount of arcs to account for random incompat
        arccount= arccount- (p*randInc);
        int nodecount=p+pmcount+2;
        String firstline = "p min "+ nodecount+" "+arccount+"\n";
        String secline = "c min-cost flow problem with "+nodecount+" nodes and "+arccount+ " arcs \n";
        //System.out.println(firstline +secline);
        String thirdline ="n 1 "+ p*(r-1)+ "\n";//supply is p times r-1
        String fourthln="c supply of "+p*(r-1)+" at node 1 \n"; 
        String fifthln = "n "+nodecount+" "+-1*p*(r-1)+"\n";
        String sixthln = "c demand of "+-1*p*(r-1)+" at node "+nodecount+"\n";
        String sevln = "c arc list follows \n";
        String eithln = "c arc has <tail> <head> <capacity l.b.> <capacity u.b> <cost> \n";
        String firstlns=firstline+secline+thirdline+fourthln+fifthln+sixthln+sevln+eithln;
        //System.out.print(firstlns);
        StringBuilder supplyarcs = new StringBuilder("");
        
        //String supplyarcs="";
        int countnode=2;
        for (int i=0;i<p;i++){
        	supplyarcs.append("a 1 "+(countnode)+" 0 "+(r-1)+" 0 \n");
            //supplyarcs+="a 1 "+(countnode)+" 0 "+(r-1)+" 0 \n";
            countnode++;
        }
        int firstvm=countnode;
        
        int startv=2;
        //System.out.print(supplyarcs);
        StringBuilder vmarcs=new StringBuilder("");
        //String vmarcs ="";
        
        //make the arcs from the location of the orginal vms to every physical machine
        //except the one the VM is originally stored in.
        for (int i=0;i<p;i++){
            
            countnode=firstvm;
            for(int j=0;j<pmcount;j++){
                if (vms[i].getpmid()==j+firstpm){
                    countnode++;
                    continue;
                }
                boolean isflag=false;
                for(int l=0;l<tree[orgnl[i]].vmlist2.size();l++){
                	if(tree[orgnl[i]].vmlist2.get(l).vmid!=i){
                		continue;
                	}
                	for(int x=0;x<tree[orgnl[i]].vmlist2.get(l).randIncs.size();x++){
                		if(tree[orgnl[i]].vmlist2.get(l).randIncs.get(x)==j+firstpm){
                			isflag=true;
                		}
                	}
                		
                }
                if(isflag){
                	countnode++;
                	continue;
                }
                	
                vmarcs.append("a "+(i+2)+" "+(countnode)+" 0 "+1+" "+
                distance(tree,tree[vms[i].getpmid()],tree[j+firstpm],k)+"\n");
                //vmarcs+= "a "+(i+2)+" "+(countnode)+" 0 "+1+" "+
                //distance(tree,tree[vms[i].getpmid()],tree[j+firstpm],k)+"\n";
                countnode++;    
            }
        }
        
        //System.out.print(vmarcs);
        //now we make arcs from every physical machine to the destination node
        StringBuilder pmarcs = new StringBuilder("");
        //String pmarcs="";
        for(int i=0; i<pmcount;i++){
        	pmarcs.append("a "+(i+firstvm)+" "+(countnode)+" 0 "+tree[i+firstpm].getesc()+" "+"0 \n");
            //pmarcs+="a "+(i+firstvm)+" "+(countnode)+" 0 "+tree[i+firstpm].getesc()+" "+"0 \n";
        }
        
        String output= firstlns+supplyarcs+vmarcs+pmarcs;
        //System.out.print(output);
       ;
        try{
            //this is where i am writing my file to.
            File file = new File("mcf_replication.inp");
            file.createNewFile();
            FileWriter fw = new FileWriter("mcf_replication.inp");
            fw.write(output);
            fw.flush();
            fw.close();
        }catch (Exception e){System.out.println("failed 1");}
        //int checkif = Math.min(m, p);
        if(p*r>m*pmcount){//if the amount of copies times the amount of original vms
                            //is more than m times the amount of pms, replication not possible
            System.out.println("replication of every VM not possible.");
        }
        System.out.println("mcf_replication.inp has been written to in project root file directory");
	}
	public void createMCFFile112020B(){
		int arccount= (p*pmcount-p);//this is how many edges there are in the middle
        arccount+= p;// there are p edges from the supply node to the original vms
        arccount+=pmcount;//the number of edges between the PMs and the demand node is 
                         //the number of physical machines.
        //change amount of arcs to account for random incompat
       // arccount= arccount- (p*randInc);//fix after doing [5,10]
        for(int i=0;i<p;i++){
        	arccount = arccount - rn[i];
        }
        int nodecount=p+pmcount+2;
        String firstline = "p min "+ nodecount+" "+arccount+"\n";
        String secline = "c min-cost flow problem with "+nodecount+" nodes and "+arccount+ " arcs \n";
        //System.out.println(firstline +secline);
        int supply=0;
        
        for(int i=0;i<p;i++){
        	supply+=rvm[i];
        }
        
        String thirdline ="n 1 "+ supply+ "\n";//supply is p times r-1
        String fourthln="c supply of "+p*(supply)+" at node 1 \n"; 
        String fifthln = "n "+nodecount+" "+-1*supply+"\n";
        String sixthln = "c demand of "+-1*supply+" at node "+nodecount+"\n";
        String sevln = "c arc list follows \n";
        String eithln = "c arc has <tail> <head> <capacity l.b.> <capacity u.b> <cost> \n";
        String firstlns=firstline+secline+thirdline+fourthln+fifthln+sixthln+sevln+eithln;
        //System.out.print(firstlns);
        StringBuilder supplyarcs = new StringBuilder("");
        
        //String supplyarcs="";
        int countnode=2;
        for (int i=0;i<p;i++){
        	supplyarcs.append("a 1 "+(countnode)+" 0 "+(rvm[i])+" 0 \n");
            //supplyarcs+="a 1 "+(countnode)+" 0 "+(r-1)+" 0 \n";
            countnode++;
        }
        int firstvm=countnode;
        
        int startv=2;
        //System.out.print(supplyarcs);
        StringBuilder vmarcs=new StringBuilder("");
        //String vmarcs ="";
        
        //make the arcs from the location of the orginal vms to every physical machine
        //except the one the VM is originally stored in.
        for (int i=0;i<p;i++){
            
            countnode=firstvm;
            for(int j=0;j<pmcount;j++){
                if (vms[i].getpmid()==j+firstpm){
                    countnode++;
                    continue;
                }
                boolean isflag=false;
                for(int l=0;l<tree[orgnl[i]].vmlist2.size();l++){
                	if(tree[orgnl[i]].vmlist2.get(l).vmid!=i){
                		continue;
                	}
                	for(int x=0;x<tree[orgnl[i]].vmlist2.get(l).randIncs.size();x++){
                		if(tree[orgnl[i]].vmlist2.get(l).randIncs.get(x)==j+firstpm){
                			isflag=true;
                		}
                	}
                		
                }
                if(isflag){
                	countnode++;
                	continue;
                }
                	
                vmarcs.append("a "+(i+2)+" "+(countnode)+" 0 "+1+" "+
                distance(tree,tree[vms[i].getpmid()],tree[j+firstpm],k)+"\n");
                //vmarcs+= "a "+(i+2)+" "+(countnode)+" 0 "+1+" "+
                //distance(tree,tree[vms[i].getpmid()],tree[j+firstpm],k)+"\n";
                countnode++;    
            }
        }
        
        //System.out.print(vmarcs);
        //now we make arcs from every physical machine to the destination node
        StringBuilder pmarcs = new StringBuilder("");
        //String pmarcs="";
        for(int i=0; i<pmcount;i++){
        	pmarcs.append("a "+(i+firstvm)+" "+(countnode)+" 0 "+tree[i+firstpm].getesc()+" "+"0 \n");
            //pmarcs+="a "+(i+firstvm)+" "+(countnode)+" 0 "+tree[i+firstpm].getesc()+" "+"0 \n";
        }
        
        String output= firstlns+supplyarcs+vmarcs+pmarcs;
        //System.out.print(output);
       ;
        try{
            //this is where i am writing my file to.
            File file = new File("mcf_replication.inp");
            file.createNewFile();
            FileWriter fw = new FileWriter("mcf_replication.inp");
            fw.write(output);
            fw.flush();
            fw.close();
        }catch (Exception e){System.out.println("failed 1");}
        //int checkif = Math.min(m, p);
      /*  if(p*r>m*pmcount){//if the amount of copies times the amount of original vms
                            //is more than m times the amount of pms, replication not possible
            System.out.println("replication of every VM not possible.");
        }*/
        System.out.println("mcf_replication.inp has been written to in project root file directory");
	}
	public void createMCFFile112020Bsize(){
		int arccount= (p*pmcount-p);//this is how many edges there are in the middle
        arccount+= p;// there are p edges from the supply node to the original vms
        arccount+=pmcount;//the number of edges between the PMs and the demand node is 
                         //the number of physical machines.
        //change amount of arcs to account for random incompat
       // arccount= arccount- (p*randInc);//fix after doing [5,10]
        for(int i=0;i<p;i++){
        	arccount = arccount - rn[i];
        }
        int nodecount=p+pmcount+2;
        String firstline = "p min "+ nodecount+" "+arccount+"\n";
        String secline = "c min-cost flow problem with "+nodecount+" nodes and "+arccount+ " arcs \n";
        //System.out.println(firstline +secline);
        int supply=0;
        
        for(int i=0;i<p;i++){
        	supply+=rvm[i];
        }
        
        String thirdline ="n 1 "+ supply+ "\n";//supply is p times r-1
        String fourthln="c supply of "+p*(supply)+" at node 1 \n"; 
        String fifthln = "n "+nodecount+" "+-1*supply+"\n";
        String sixthln = "c demand of "+-1*supply+" at node "+nodecount+"\n";
        String sevln = "c arc list follows \n";
        String eithln = "c arc has <tail> <head> <capacity l.b.> <capacity u.b> <cost> \n";
        String firstlns=firstline+secline+thirdline+fourthln+fifthln+sixthln+sevln+eithln;
        //System.out.print(firstlns);
        StringBuilder supplyarcs = new StringBuilder("");
        
        //String supplyarcs="";
        int countnode=2;
        for (int i=0;i<p;i++){
        	supplyarcs.append("a 1 "+(countnode)+" 0 "+(rvm[i])+" 0 \n");
            //supplyarcs+="a 1 "+(countnode)+" 0 "+(r-1)+" 0 \n";
            countnode++;
        }
        int firstvm=countnode;
        
        int startv=2;
        //System.out.print(supplyarcs);
        StringBuilder vmarcs=new StringBuilder("");
        //String vmarcs ="";
        
        //make the arcs from the location of the orginal vms to every physical machine
        //except the one the VM is originally stored in.
        for (int i=0;i<p;i++){
            
            countnode=firstvm;
            for(int j=0;j<pmcount;j++){
                if (vms[i].getpmid()==j+firstpm){
                    countnode++;
                    continue;
                }
                boolean isflag=false;
                for(int l=0;l<tree[orgnl[i]].vmlist2.size();l++){
                	if(tree[orgnl[i]].vmlist2.get(l).vmid!=i){
                		continue;
                	}
                	for(int x=0;x<tree[orgnl[i]].vmlist2.get(l).randIncs.size();x++){
                		if(tree[orgnl[i]].vmlist2.get(l).randIncs.get(x)==j+firstpm){
                			isflag=true;
                		}
                	}
                		
                }
                if(isflag){
                	countnode++;
                	continue;
                }
                	
                vmarcs.append("a "+(i+2)+" "+(countnode)+" 0 "+1+" "+
                distance(tree,tree[vms[i].getpmid()],tree[j+firstpm],k)+"\n");
                //vmarcs+= "a "+(i+2)+" "+(countnode)+" 0 "+1+" "+
                //distance(tree,tree[vms[i].getpmid()],tree[j+firstpm],k)+"\n";
                countnode++;    
            }
        }
        
        //System.out.print(vmarcs);
        //now we make arcs from every physical machine to the destination node
        StringBuilder pmarcs = new StringBuilder("");
        //String pmarcs="";
        for(int i=0; i<pmcount;i++){
        	pmarcs.append("a "+(i+firstvm)+" "+(countnode)+" 0 "+(m-tree[i+firstpm].vmcount)+" "+"0 \n");
            //pmarcs+="a "+(i+firstvm)+" "+(countnode)+" 0 "+tree[i+firstpm].getesc()+" "+"0 \n";
        }
        
        String output= firstlns+supplyarcs+vmarcs+pmarcs;
        //System.out.print(output);
       ;
        try{
            //this is where i am writing my file to.
            File file = new File("mcf_replication.inp");
            file.createNewFile();
            FileWriter fw = new FileWriter("mcf_replication.inp");
            fw.write(output);
            fw.flush();
            fw.close();
        }catch (Exception e){System.out.println("failed 1");}
        //int checkif = Math.min(m, p);
      /*  if(p*r>m*pmcount){//if the amount of copies times the amount of original vms
                            //is more than m times the amount of pms, replication not possible
            System.out.println("replication of every VM not possible.");
        }*/
        System.out.println("mcf_replication.inp has been written to in project root file directory");
	}
	public void readFromFile(){
		
		File out = new File("output.txt");
        try{
            BufferedReader br = new BufferedReader(new FileReader(out));
            String line;
            //int c=0;
            while((line=br.readLine())!=null){
               if(line.charAt(0)=='c'||line.charAt(0)=='s')
                    continue;//ignoring comment lines
               int c=1;//index of string after first character
               String firstnum=""; //first number of the line
               while(line.charAt(c)==' '){
                  c++;
               }
               while(line.charAt(c)!=' '){
                   firstnum+=line.charAt(c);//reading the first number of the line
                   c++;
                }
               if(Integer.parseInt(firstnum)==1)
               continue;//also ignoring the supply arc lines
               int vmnum=Integer.parseInt(firstnum)-2; //id of original vm
               String secnum="";
               while(line.charAt(c)==' '){
                  c++;
               }
               while(line.charAt(c)!=' '){
                   secnum+=line.charAt(c);//reading the second number of the line
                   c++;
                }
               int pmnumid = Integer.parseInt(secnum)-(p+2)+firstpm; //id of pm
               
               String thirdnum="";
                while(line.charAt(c)==' '){
                  c++;
               }
               //while(line.charAt(c)!=' '){
                   thirdnum+=line.charAt(c);//reading the third number of the line
                   c++;
                //}
                int ifplaced = Integer.parseInt(thirdnum);
                
                if(ifplaced >0){
                    vms[vmnum].addcopy(pmnumid);
                    //vms0.get(vmnum).addcopy(pmnumid);
                    
                    tree[pmnumid].setactive();
                    itree[pmnumid].setactive();
                    //tree0.get(pmnumid).setactive();
                    
                    int cost = distance(tree,tree[vms[vmnum].getpmid()],tree[pmnumid],k);
                    itree[pmnumid].addVMc(vmnum,pmnumid,cost,m);
                    tree[pmnumid].addVMc(vmnum,pmnumid,cost,m);
                    //tree0.get(pmnumid).addVMc(vmnum,pmnumid,cost,m);
                    addToVMclist(vmnum,pmnumid,cost); //list that has every vm copy
                    System.out.println("VM " +vmnum+ " copy has been placed in PM with id "+
                    pmnumid);
                    vms[vmnum].copyAdd(cost);
                }
               
            }
        
        }catch (Exception e){System.out.println("");}
        
        System.out.println("Placement of VMS before consolidation:");
        int actvsi=0;
        
        for (int i=firstpm;i<=lastpm;i++){
            String print = tree[i].printVms();
            if(tree[i].getactive())
            actvsi++;
            //String print0 = tree0.get(i).printVms();
            //if(!(print.equals(tree0.get(i).printVms()))){
            //System.out.println("mistakes");
            //System.exit(0);
            //}
            
            if(print==null)
            continue;
            //print+=" "+tree[i].getreplicount();
            System.out.println(print);
            
            //System.out.println(print0 +"l");
        }
        System.out.println("initial active: "+actvsi);
        System.out.println("initial off: "+ (pmcount-actvsi));
	}
	public void createIncGraph(){
		//IncompGraph igraph = new IncompGraph(hvmcList);
		igraph = new ArrayList<CompNode>(0);
		int index=0;
		for(int i=0;i<hvmcList.length;i++){
			CopyVM temp = hvmcList[i]; 
			igraph.add(new CompNode(temp.vmid,temp.pmid, temp.cost));
			igraph.get(index).index=index;
			index++;
		}
		igraph.trimToSize();
		int firstbin = igraph.size();
		for(int i=0;i<pmcount;i++){
			igraph.add(new CompNode(firstpm+i));
			igraph.trimToSize();
			igraph.get(igraph.size()-1).index=index;
			index++;
		}
		igraph.trimToSize();
		//graph is now populated with nodes, now make edges
		//ie add to the CompNode Objects neighbors list where the neighbors list is full of indeces.
		for(int i=0;i<igraph.size();i++){
			CompNode temp = igraph.get(i);
			for(int j=0;j<igraph.size();j++){
				if(i==j){
					continue;
				}
				if (temp.vmid==(0-1) && igraph.get(j).vmid==(0-1)){
					continue;
				}
				//if vmid are the same for VMs
				if (temp.vmid!=(0-1)&&igraph.get(j).vmid!=(0-1)){
					if(temp.vmid!=(0-1)&&temp.vmid==igraph.get(j).vmid){
						temp.nbors.add(j);
						igraph.get(j).nbors.add(i);
						continue;
					}
					continue;
				}
				// if cost is not the same then it is not compatible
				//or if this PM has original copy
				if(temp.vmid!=(0-1)&&igraph.get(j).vmid==(0-1)){
					int vmnum=temp.vmid;
					int pmnum=temp.pmid;
					int cost=temp.cost;
					int tempdistance = distance(htree,htree[igraph.get(j).pmid],htree[orgnl[vmnum]],k);
					if(cost!=tempdistance){
						temp.nbors.add(j);
						igraph.get(j).nbors.add(i);
					}
					if(htree[igraph.get(j).pmid].checkVm(vmnum)==true){
						temp.nbors.add(j);
						igraph.get(j).nbors.add(i);
					}
					
				}
				if(temp.vmid==(0-1)&&igraph.get(j).vmid!=(0-1)){
					int vmnum=igraph.get(j).vmid;
					int pmnum=igraph.get(j).pmid;
					int cost= igraph.get(j).cost;
					int tempdistance = distance(htree,htree[temp.pmid],htree[orgnl[vmnum]],k);
					if(cost!=tempdistance){
						temp.nbors.add(j);
						igraph.get(j).nbors.add(i);
					}
					if(htree[temp.pmid].checkVm(igraph.get(j).vmid)==true){
						temp.nbors.add(j);
						igraph.get(j).nbors.add(i);
					}
				}
			}
		}
	}
	public void createIncGraph112020(){
		//IncompGraph igraph = new IncompGraph(hvmcList);
		igraph = new ArrayList<CompNode>(0);
		int index=0;
		for(int i=0;i<hvmcList.length;i++){
			CopyVM temp = hvmcList[i]; 
			igraph.add(new CompNode(temp.vmid,temp.pmid, temp.cost));
			igraph.get(index).index=index;
			index++;
		}
		igraph.trimToSize();
		int firstbin = igraph.size();
		
		for(int i=0;i<pmcount;i++){
			boolean isflag=false;
			if(htree[firstpm+i].vmcount>0){
				isflag=true;
			}
			for(int j=0;j<orgnl.length;j++){
				for(int l=0;l<htree[orgnl[j]].vmlist2.size();l++){
					for(int z=0;z<htree[orgnl[j]].vmlist2.get(l).randIncs.size();z++){
						if(htree[orgnl[j]].vmlist2.get(l).randIncs.get(z)==firstpm+i){
							isflag=true;
						}
					}
				}
			}
			if(isflag){
				igraph.add(new CompNode(firstpm+i));
				igraph.trimToSize();
				igraph.get(igraph.size()-1).index=index;
				index++;
			}
		}
		igraph.trimToSize();
		//graph is now populated with nodes, now make edges
		//ie add to the CompNode Objects neighbors list where the neighbors list is full of indeces.
		for(int i=0;i<igraph.size();i++){
			CompNode temp = igraph.get(i);
			for(int j=0;j<igraph.size();j++){
				if(i==j){
					continue;
				}
				if (temp.vmid==(0-1) && igraph.get(j).vmid==(0-1)){
					continue;
				}
				//if vmid are the same for VMs
				if (temp.vmid!=(0-1)&&igraph.get(j).vmid!=(0-1)){
					if(temp.vmid==igraph.get(j).vmid){
						temp.nbors.add(j);
						igraph.get(j).nbors.add(i);
						continue;
					}
					continue;
				}
				// if cost is not the same then it is not compatible
				//or if this PM has original copy
				if(temp.vmid!=(0-1)&&igraph.get(j).vmid==(0-1)){
					int vmnum=temp.vmid;
					int pmnum=temp.pmid;
					int cost=temp.cost;
					int tempdistance = distance(htree,htree[igraph.get(j).pmid],htree[orgnl[vmnum]],k);
					if(cost!=tempdistance){
						//temp.nbors.add(j);
						//igraph.get(j).nbors.add(i);
					}
					if(htree[igraph.get(j).pmid].checkVm(vmnum)==true){
						temp.nbors.add(j);
						igraph.get(j).nbors.add(i);
					}
					//if temp i vmnum has incompat to igraph j pmnum
					//tempnbors add j
					//igraph get j nbors add i
					boolean isaflag=false;
					for(int l=0;l<htree[orgnl[vmnum]].vmlist2.size();l++){
						if(htree[orgnl[vmnum]].vmlist2.get(l).vmid!=vmnum){
							continue;
						}
						for(int g=0; g<htree[orgnl[vmnum]].vmlist2.get(l).randIncs.size();g++ ){
							if(htree[orgnl[vmnum]].vmlist2.get(l).randIncs.get(g)==igraph.get(j).pmid){
								isaflag=true;
							}
						}
					}
					if (isaflag){
						temp.nbors.add(j);
						igraph.get(j).nbors.add(i);
					}
					
				}
				if(temp.vmid==(0-1)&&igraph.get(j).vmid!=(0-1)){
					int vmnum=igraph.get(j).vmid;
					int pmnum=igraph.get(j).pmid;
					int cost= igraph.get(j).cost;
					int tempdistance = distance(htree,htree[temp.pmid],htree[orgnl[vmnum]],k);
					if(cost!=tempdistance){
						//temp.nbors.add(j);
						//igraph.get(j).nbors.add(i);
					}
					if(htree[temp.pmid].checkVm(igraph.get(j).vmid)==true){
						temp.nbors.add(j);
						igraph.get(j).nbors.add(i);
					}
					//if igraph(j)vmnum has incompat to temp(i)pmid
					//tempnbors addj
					//igraph get j nbors add i
					boolean isa=false;
					for(int l=0;l<htree[orgnl[vmnum]].vmlist2.size();l++){
						if(htree[orgnl[vmnum]].vmlist2.get(l).vmid!=vmnum){
							continue;
						}
						for(int g=0; g<htree[orgnl[vmnum]].vmlist2.get(l).randIncs.size();g++ ){
							if(htree[orgnl[vmnum]].vmlist2.get(l).randIncs.get(g)==temp.pmid){
								isa=true;
							}
						}
					}
					if (isa){
						temp.nbors.add(j);
						igraph.get(j).nbors.add(i);
					}
				}
			}
		}
	}
	public void colorGraph(){
		//maintain two sorted lists with precolored and uncolored vertices by descending order
		ArrayList<CompNode> unlist = new ArrayList<CompNode>(0);
		ArrayList<CompNode> prelist = new ArrayList<CompNode>(0);
		for(int i=0;i<igraph.size();i++){
			/*if(igraph.get(i).vmid==(0-1)){
				System.out.println(i+" PM"+igraph.get(i).pmid+ " has nbors: ");
			}
			else{
				System.out.println(i+ " VM"+igraph.get(i).vmid+ " has nbors: ");
			}
			for(int j=0;j<igraph.get(i).nbors.size();j++){
				System.out.println("    "+igraph.get(i).nbors.get(j));
				
			}*/
		}
		int color =1;
		for(int i=0;i<igraph.size();i++){
			if(igraph.get(i).vmid==(0-1)){
				igraph.get(i).color=color;
				htree[igraph.get(i).pmid].color=color;//give all pms a color... all pms that have conflict
				color++;
				prelist.add(igraph.get(i));
			}
			else{
				unlist.add(igraph.get(i));
			}
		}
		
		bSort(unlist,prelist);
		int tcolor = 0;
		for(int i=0;i<prelist.size();i++){
			tcolor = prelist.get(i).color;{
				for(int j=0; j<unlist.size();j++){
					
					//if (unlist.checkColor(tcolor)==false){
						
					//}
					boolean flag= false;
					if(unlist.get(j).color==tcolor){
						flag=true;
					}
					for(int c=0;c<unlist.get(j).nbors.size();c++){
						if(igraph.get(igraph.get(unlist.get(j).index).nbors.get(c)).color==tcolor){
							flag=true;
							
						}
						/*if(htree[unlist.get(j).nbors.get(c)].color==tcolor){
							flag=true;
						}*/
					}
					if(flag==false){
						unlist.get(j).color=tcolor;
						unlist.remove(j);
						unlist.trimToSize();
						j--;
						//continue;
					}
				}
			}
		}
		tcolor++;//the newest color
		//now we have traversed through precolored
		//do the regular welsh powell heuristic for the rest of the uncolored.
		for(int i=0;i<unlist.size();i++){
			if(unlist.get(i).color!=(0-1)){
				continue;
			}
			boolean flag= false;
			if(unlist.get(i).color==tcolor){
				flag=true;
			}
			for(int c=0;c<unlist.get(i).nbors.size();c++){
				if(igraph.get(igraph.get(unlist.get(i).index).nbors.get(c)).color==tcolor){
					flag=true;
				}
				/*if(htree[unlist.get(i).nbors.get(c)].color==tcolor)
					flag=true;*/
			}
			if(flag==false){
				unlist.get(i).color=tcolor;
				unlist.remove(i);
				unlist.trimToSize();
				
				i--;
				continue;
			}
			if(flag==true){
				tcolor++;
				unlist.get(i).color=tcolor;
				unlist.remove(i);
				unlist.trimToSize();
				i--;
			}
		}
		
		for(int i=0;i<igraph.size();i++){
			if(igraph.get(i).vmid==(0-1))
				continue;
			System.out.println("VM"+igraph.get(i).vmid+" has color: "+igraph.get(i).color);
			
		}
		for(int i=0;i<prelist.size();i++){
			/*if(igraph.get(i).vmid==(0-1))
				continue;*/
			System.out.println("PM"+prelist.get(i).pmid+" has color: "+prelist.get(i).color);
			
		}
	}
	
	public void colorGraph112020(){
		//maintain two sorted lists with precolored and uncolored vertices by descending order
		ArrayList<CompNode> unlist = new ArrayList<CompNode>(0);
		ArrayList<CompNode> prelist = new ArrayList<CompNode>(0);
		for(int i=0;i<igraph.size();i++){
			if(igraph.get(i).vmid==-1){
				System.out.println(i+" PM"+igraph.get(i).pmid+ " has nbors: ");
			}
			else{
				System.out.println(i+ " VM"+igraph.get(i).vmid+ " has nbors: ");
			}
			for(int j=0;j<igraph.get(i).nbors.size();j++){
				System.out.println("    "+igraph.get(i).nbors.get(j));
				
			}
		}
		int color =1;
		for(int i=0;i<igraph.size();i++){
			if(igraph.get(i).vmid==(0-1)){
				//give all PMs n+1 color (signified by color =(0-1))
				igraph.get(i).color=(0-1);
				htree[igraph.get(i).pmid].color=(0-1);
				//IF PM HAS CONFLICT TO IT (INCLUDING ORIGINAL VMs)ADD COLOR
				//SO CHECK EVERY ORIGINAL VMS INCOMPATS TO SEE IF COLOR SHOULD BE ADDED
				boolean isflag=false;
				System.out.println(htree[igraph.get(i).pmid].vmcount);
				if(htree[igraph.get(i).pmid].vmcount!=0){
					isflag=true;
				}
				for(int j=0;j<orgnl.length;j++){
					for(int l=0;l<htree[orgnl[j]].vmlist2.size();l++){
						for(int z=0;z<htree[orgnl[j]].vmlist2.get(l).randIncs.size();z++){
							//todo: add to check if it is original
							if(htree[orgnl[j]].vmlist2.get(l).randIncs.get(z)==igraph.get(i).pmid){
								isflag=true;
							}
							//if(h)
						}
					}
				}
				if(isflag){
					igraph.get(i).color=color;
					htree[igraph.get(i).pmid].color=color;
					color++;
					prelist.add(igraph.get(i));
				}
				//color++;
				//prelist.add(igraph.get(i));
			}
			else{
				unlist.add(igraph.get(i));
			}
		}
		
		bSort(unlist,prelist);
		int tcolor = 0;
		for(int i=0;i<prelist.size();i++){
			tcolor = prelist.get(i).color;{
				for(int j=0; j<unlist.size();j++){
					
					//if (unlist.checkColor(tcolor)==false){
						
					//}
					boolean flag= false;
					/*if(unlist.get(j).color==tcolor){
						flag=true;
					}*/
					for(int c=0;c<unlist.get(j).nbors.size();c++){
						int nborcolor=igraph.get(igraph.get(unlist.get(j).index).nbors.get(c)).color;
						if(nborcolor==tcolor){
							flag=true;
							
						}
						/*if(htree[unlist.get(j).nbors.get(c)].color==tcolor){
							flag=true;
						}*/
					}
					if(flag==false){
						unlist.get(j).color=tcolor;
						unlist.remove(j);
						unlist.trimToSize();
						j--;
						//continue;
					}
				}
			}
		}
		tcolor++;//the newest color
		//now we have traversed through precolored
		//do the regular welsh powell heuristic for the rest of the uncolored.
		for(int i=0;i<unlist.size();i++){
			if(unlist.get(i).color!=(0-1)){
				continue;
			}
			boolean flag= false;
			if(unlist.get(i).color==tcolor){
				flag=true;
			}
			for(int c=0;c<unlist.get(i).nbors.size();c++){
				if(igraph.get(igraph.get(unlist.get(i).index).nbors.get(c)).color==tcolor){
					flag=true;
				}
				/*if(htree[unlist.get(i).nbors.get(c)].color==tcolor)
					flag=true;*/
			}
			if(flag==false){
				unlist.get(i).color=tcolor;
				unlist.remove(i);
				unlist.trimToSize();
				
				i--;
				continue;
			}
			if(flag==true){
				tcolor++;
				unlist.get(i).color=tcolor;
				unlist.remove(i);
				unlist.trimToSize();
				i--;
			}
		}
		
		for(int i=0;i<igraph.size();i++){
			if(igraph.get(i).vmid==(0-1))
				continue;
			System.out.println("VM"+igraph.get(i).vmid+" has color: "+igraph.get(i).color);
			
		}
		for(int i=0;i<prelist.size();i++){
			/*if(igraph.get(i).vmid==(0-1))
				continue;*/
			System.out.println("PM"+prelist.get(i).pmid+" has color: "+prelist.get(i).color);
			
		}
	}
	public void allocVms(){
		
		//printing neighbors
		/*for(int i=0;i<igraph.size();i++){
			System.out.println(i+" VM" +igraph.get(i).vmid+" PM "+igraph.get(i).pmid+"nbors:");
			for(int j=0;j<igraph.get(i).nbors.size();j++){
				System.out.println("   "+""+igraph.get(i).nbors.get(j));
			}
		} */
		ArrayList<Node> onPMs = new ArrayList<Node>(0);
		
		//first sort bins by decreasing order of ESC
		ArrayList<Node> bins = new ArrayList<Node> (0);
		for (int i=0;i<pmcount;i++){
			bins.add(htree[i+firstpm]);
		}
		for (int i=0;i<pmcount;i++){
			if(htree[i+firstpm].getactive()==true){
				onPMs.add(htree[i+firstpm]);
			}
		}
		bins.trimToSize();
		// sort bins by ESC
		sortBins(bins);
		sortBins(onPMs);
		//sort items by decreasing order of size (n/a)
		//put items in list from the igraph
		ArrayList<CompNode> items = new ArrayList<CompNode>(0);
		for(int i=0;i<igraph.size();i++){
			if(igraph.get(i).vmid!=(0-1)){
				items.add(igraph.get(i));
			}
		}
		
		/*for(int i=0;i<items.size();i++){
			System.out.println(items.get(i).color+" item color");
		}*/
		for(int i=0;i<items.size();i++){
			int tempcolor = items.get(i).color;
			int tempvmid = items.get(i).vmid;
			int tempcost = items.get(i).cost;
			boolean placed=false;
			for(int j=0;j<onPMs.size();j++){
				//System.out.println("check: " + tempcolor+" "+ onPMs.get(j).color);
				if(onPMs.get(j).color==tempcolor){//if they have the same color they are compatible
					if(onPMs.get(j).addVMc(tempvmid, onPMs.get(j).getid(), tempcost, m)==false){
						//System.exit(0);
						//System.out.println("2SH: "+tempvmid+" copy could not be placed in PM" + onPMs.get(j).getid() );
					}
					else{
						onPMs.get(j).setactive();//
						System.out.println("2SH: "+tempvmid+" copy placed in PM" + onPMs.get(j).getid() );
						placed=true;
						break;
					}
				}
				
			}
			if(placed==true)
			continue;
			boolean isplaced=false;
			for(int j=0;j<bins.size();j++){
				
				if(bins.get(j).color==tempcolor){
					boolean placedflag=bins.get(j).addVMc(tempvmid, bins.get(j).getid(), tempcost, m);
					if (placedflag==false){
						continue;
					}
					//bins.get(j).color=tempcolor;
					System.out.println("2SH: "+tempvmid+" copy placed in PM" + bins.get(j).getid() );
					onPMs.add(bins.get(j));
					isplaced=true;
					break;
					
				}
			}
			if(isplaced==false){
				for(int j=0;j<bins.size();j++){
					if(bins.get(j).color==(0-1)){
						boolean placedflag=bins.get(j).addVMc(tempvmid, bins.get(j).getid(), tempcost, m);
						if (placedflag==false){
							continue;
						}
						//bins.get(j).addVMc(tempvmid, bins.get(j).getid(), tempcost, m);
						bins.get(j).color=tempcolor;
						System.out.println("2SH: "+tempvmid+" copy placed in PM" + bins.get(j).getid() );
						onPMs.add(bins.get(j));
						isplaced=true;
						break;
					}
				}
				
			}
			if(isplaced==false){
				System.out.println("2SH: "+tempvmid+" copy could not be placed in PM" );
			}
		}
		//active pms
		System.out.println("Placement of VMS after 2 stage heur consolidation:");
	      int actpms=0;
	        for (int i=firstpm;i<=lastpm;i++){
	            String print = htree[i].printVms();
	            if(htree[i].getactive()){
	            	actpms++;
	            }
	            if(print==null)
	            continue;
	            System.out.println(print);
	        }
	        System.out.println("Number of active using 2SH: "+actpms);
	        //System.out.println("Number of IPMS: "+(pmcount-actpms));
	}
public void allocVms112020size(){
		int notplaced=0;
		int numplaced=0;
		//printing neighbors
		/*for(int i=0;i<igraph.size();i++){
			System.out.println(i+" VM" +igraph.get(i).vmid+" PM "+igraph.get(i).pmid+"nbors:");
			for(int j=0;j<igraph.get(i).nbors.size();j++){
				System.out.println("   "+""+igraph.get(i).nbors.get(j));
			}
		} */
		ArrayList<Node> bin_counter = new ArrayList<Node>(0);
		
		//first sort bins by decreasing order of ESC
		ArrayList<Node> bins = new ArrayList<Node> (0);
		for (int i=0;i<pmcount;i++){
			bins.add(htree[i+firstpm]);
		}
		for (int i=0;i<pmcount;i++){
			if(htree[i+firstpm].color!=-1){
				bin_counter.add(htree[i+firstpm]);
			}
		}
		bins.trimToSize();
		// sort bins by ESC
		sortBins(bins);
		sortBins(bin_counter);
		//sort items by decreasing order of size (n/a)
		//put items in list from the igraph
		ArrayList<CompNode> items = new ArrayList<CompNode>(0);
		for(int i=0;i<igraph.size();i++){
			if(igraph.get(i).vmid!=(0-1)){
				items.add(igraph.get(i));
			}
		}
		sortItems(items);
		//make data structure for :
				//data structure for unplaced VMs
					//no data struct for unplaced Vms
				//data structure for new(hypothetical PMs
				//how many active PMs in fat tree?(+how many total active outside of fat tree
				//how much total cost for VMs in fat tree?
		ArrayList<Node> newPMs = new ArrayList<Node>(0);
		/*for(int i=0;i<items.size();i++){
			System.out.println(items.get(i).color+" item color");
		}*/
		for(int i=0;i<items.size();i++){
			int tempcolor = items.get(i).color;
			int tempvmid = items.get(i).vmid;
			int tempcost = items.get(i).cost;
			int tempsize = orgnlsize[tempvmid];
			boolean placed=false;
			//first try to place VMs in PMs id by bincounter
			for(int j=0;j<bin_counter.size();j++){
				//System.out.println("check: " + tempcolor+" "+ onPMs.get(j).color);
				if(bin_counter.get(j).color==tempcolor){//if they have the same color they are compatible
					int truecost= distance(htree, htree[bin_counter.get(j).getid()], htree[orgnl[tempvmid]], k);
					if(bin_counter.get(j).addVMc(tempvmid, bin_counter.get(j).getid(), truecost, m, tempsize)==false){
					//	System.out.println(bin_counter.get(j).esc);
						//System.out.println(bin_counter.get(j).vmcount);
						//System.exit(0);
						//System.out.println("2SH: "+tempvmid+" copy could not be placed in PM" + onPMs.get(j).getid() );
					}
					else{
						numplaced++;
						//notplaced--;
						bin_counter.get(j).setactive();//
						System.out.println("2SH: "+tempvmid+" copy placed in PM" + bin_counter.get(j).getid() );
						placed=true;
						break;
					}
				}
				
			}
			if(placed==true)
			continue;
			//if not placed in PMs id by bincounter, open another PM, and give it the color of VM placed 
				
			boolean isplaced=false;
			for(int j=0;j<bins.size();j++){
				
				if(bins.get(j).color==-1){
					int truecost= distance(htree, htree[bins.get(j).getid()], htree[orgnl[tempvmid]], k);
					boolean placedflag=bins.get(j).addVMc(tempvmid, bins.get(j).getid(), truecost, m,tempsize);
					if (placedflag==false){
						continue;
					}
					//bins.get(j).color=tempcolor;
					System.out.println("2SH: "+tempvmid+" copy placed in PM" + bins.get(j).getid() );
					numplaced++;
					//notplaced--;
					bin_counter.add(bins.get(j));
					bins.get(j).color=tempcolor;
					bins.get(j).setactive();
					isplaced=true;
					break;
					
				}
			}
			if(isplaced==false){
				//numplaced--;
				//nstead of VMs not getting placed, try to place in new PM structure
				//^if not placed, try to place in newPM data structure
				for (int j=0;j<newPMs.size();j++){
					if(newPMs.get(j).color==tempcolor){
						//int truecost= distance(htree, htree[bins.get(j).getid()], htree[orgnl[tempvmid]], k);
						int truecost=6;
						boolean placedflag=newPMs.get(j).addVMc(tempvmid, newPMs.get(j).getid(), truecost, m,tempsize);
						if (placedflag==false){
							continue;
						}
						//bins.get(j).color=tempcolor;
						System.out.println("2SH: "+tempvmid+" copy placed in PM" + newPMs.get(j).getid() );
						//numplaced++;
						//notplaced--;
						//bin_counter.add(bins.get(j));
						newPMs.get(j).color=tempcolor;
						newPMs.get(j).setactive();
						isplaced=true;
						break;
						
					}
				}
				notplaced++;
				System.out.println("2SH: "+tempvmid+" copy could not be placed in PM" );
			}
			if(isplaced==false){//if not placed, finally open a new PM in newPMs
				newPMs.add(new Node());
				int index = newPMs.size()-1;
				newPMs.get(index).type=3;
				newPMs.get(index).m=m;
				newPMs.get(index).id=-99;
				newPMs.get(index).addVMc(tempvmid, -99, 6, m,tempsize);
				newPMs.get(index).color=tempcolor;
				newPMs.get(index).setactive();
				
				
			}
			
		}
		//active pms
		System.out.println("Placement of VMS after 2 stage heur consolidation:");
	      int actpms=0;
	        for (int i=firstpm;i<=lastpm;i++){
	            String print = htree[i].printVms();
	            if(htree[i].getactive()){
	            	actpms++;
	            }
	            if(print==null)
	            continue;
	            System.out.println(print);
	        }
	        for(int i=0;i<newPMs.size();i++){
	        	String print = newPMs.get(i).printVms();
	            if(newPMs.get(i).getactive()){
	            	//actpms++;
	            }
	            if(print==null)
	            continue;
	            System.out.println(print);
	        }
	        System.out.println("Number of active using 2SH: "+actpms);
	        int repcost=0;
	        for(int i=0;i<pmcount;i++){
	        	for(int j=0; j<htree[i+firstpm].vmclist.size();j++){
	        		repcost+=htree[i+firstpm].vmclist.get(j).cost;
	        	}
	        }
	        //number of active(with newPMs)
	        int newrepcost=0;
	        newPMs.trimToSize();
	        actpms+=newPMs.size();
	        System.out.println("Number of active using 2SH with new PMs:"+ actpms);
	        for(int i=0;i<newPMs.size();i++){
	        	for(int j=0;j<newPMs.get(i).vmclist.size();j++){
	        		newrepcost+=newPMs.get(i).vmclist.get(j).cost;
	        	}
	        }
	        System.out.println("Total replication cost of 2SH: "+repcost);
	        System.out.println("Total replication cost of 2SH(with new PMs: "+(repcost+newrepcost));
	        System.out.println("required flow: "+regflo);
	        System.out.println("Vms not placed by 2SH: "+notplaced);
	        System.out.println("Vms placed byt 2SH: "+ numplaced);
	        System.out.println("debug: "+(notplaced+numplaced));
	        for(int i=0;i<pmcount;i++){
	        	System.out.println("PM"+htree[i+firstpm].getid()+" color: "+htree[i+firstpm].color);
	        }
	        //System.out.println("Number of IPMS: "+(pmcount-actpms));
	}
public void allocVms112020(){
	int notplaced=0;
	int numplaced=0;
	//printing neighbors
	/*for(int i=0;i<igraph.size();i++){
		System.out.println(i+" VM" +igraph.get(i).vmid+" PM "+igraph.get(i).pmid+"nbors:");
		for(int j=0;j<igraph.get(i).nbors.size();j++){
			System.out.println("   "+""+igraph.get(i).nbors.get(j));
		}
	} */
	ArrayList<Node> bin_counter = new ArrayList<Node>(0);
	
	//first sort bins by decreasing order of ESC
	ArrayList<Node> bins = new ArrayList<Node> (0);
	for (int i=0;i<pmcount;i++){
		bins.add(htree[i+firstpm]);
	}
	for (int i=0;i<pmcount;i++){
		if(htree[i+firstpm].color!=-1){
			bin_counter.add(htree[i+firstpm]);
		}
	}
	bins.trimToSize();
	// sort bins by ESC
	sortBins(bins);
	sortBins(bin_counter);
	//sort items by decreasing order of size (n/a)
	//put items in list from the igraph
	ArrayList<CompNode> items = new ArrayList<CompNode>(0);
	for(int i=0;i<igraph.size();i++){
		if(igraph.get(i).vmid!=(0-1)){
			items.add(igraph.get(i));
		}
	}
	//sortItems(items);
	//make data structure for :
			//data structure for unplaced VMs
				//no data struct for unplaced Vms
			//data structure for new(hypothetical PMs
			//how many active PMs in fat tree?(+how many total active outside of fat tree
			//how much total cost for VMs in fat tree?
	ArrayList<Node> newPMs = new ArrayList<Node>(0);
	/*for(int i=0;i<items.size();i++){
		System.out.println(items.get(i).color+" item color");
	}*/
	for(int i=0;i<items.size();i++){
		int tempcolor = items.get(i).color;
		int tempvmid = items.get(i).vmid;
		int tempcost = items.get(i).cost;
		boolean placed=false;
		//first try to place VMs in PMs id by bincounter
		for(int j=0;j<bin_counter.size();j++){
			//System.out.println("check: " + tempcolor+" "+ onPMs.get(j).color);
			if(bin_counter.get(j).color==tempcolor){//if they have the same color they are compatible
				int truecost= distance(htree, htree[bin_counter.get(j).getid()], htree[orgnl[tempvmid]], k);
				if(bin_counter.get(j).addVMc(tempvmid, bin_counter.get(j).getid(), truecost, m)==false){
				//	System.out.println(bin_counter.get(j).esc);
					//System.out.println(bin_counter.get(j).vmcount);
					//System.exit(0);
					//System.out.println("2SH: "+tempvmid+" copy could not be placed in PM" + onPMs.get(j).getid() );
				}
				else{
					numplaced++;
					//notplaced--;
					bin_counter.get(j).setactive();//
					System.out.println("2SH: "+tempvmid+" copy placed in PM" + bin_counter.get(j).getid() );
					placed=true;
					break;
				}
			}
			
		}
		if(placed==true)
		continue;
		//if not placed in PMs id by bincounter, open another PM, and give it the color of VM placed 
			
		boolean isplaced=false;
		for(int j=0;j<bins.size();j++){
			
			if(bins.get(j).color==-1){
				int truecost= distance(htree, htree[bins.get(j).getid()], htree[orgnl[tempvmid]], k);
				boolean placedflag=bins.get(j).addVMc(tempvmid, bins.get(j).getid(), truecost, m);
				if (placedflag==false){
					continue;
				}
				//bins.get(j).color=tempcolor;
				System.out.println("2SH: "+tempvmid+" copy placed in PM" + bins.get(j).getid() );
				numplaced++;
				//notplaced--;
				bin_counter.add(bins.get(j));
				bins.get(j).color=tempcolor;
				bins.get(j).setactive();
				isplaced=true;
				break;
				
			}
		}
		if(isplaced==false){
			//numplaced--;
			//nstead of VMs not getting placed, try to place in new PM structure
			//^if not placed, try to place in newPM data structure
			for (int j=0;j<newPMs.size();j++){
				if(newPMs.get(j).color==tempcolor){
					//int truecost= distance(htree, htree[bins.get(j).getid()], htree[orgnl[tempvmid]], k);
					int truecost=6;
					boolean placedflag=newPMs.get(j).addVMc(tempvmid, newPMs.get(j).getid(), truecost, m);
					if (placedflag==false){
						continue;
					}
					//bins.get(j).color=tempcolor;
					System.out.println("2SH: "+tempvmid+" copy placed in PM" + newPMs.get(j).getid() );
					//numplaced++;
					//notplaced--;
					//bin_counter.add(bins.get(j));
					newPMs.get(j).color=tempcolor;
					newPMs.get(j).setactive();
					isplaced=true;
					break;
					
				}
			}
			notplaced++;
			System.out.println("2SH: "+tempvmid+" copy could not be placed in PM" );
		}
		if(isplaced==false){//if not placed, finally open a new PM in newPMs
			newPMs.add(new Node());
			int index = newPMs.size()-1;
			newPMs.get(index).type=3;
			newPMs.get(index).m=m;
			newPMs.get(index).id=-99;
			newPMs.get(index).addVMc(tempvmid, -99, 6, m);
			newPMs.get(index).color=tempcolor;
			newPMs.get(index).setactive();
			
			
		}
		
	}
	//active pms
	System.out.println("Placement of VMS after 2 stage heur consolidation:");
      int actpms=0;
        for (int i=firstpm;i<=lastpm;i++){
            String print = htree[i].printVms();
            if(htree[i].getactive()){
            	actpms++;
            }
            if(print==null)
            continue;
            System.out.println(print);
        }
        for(int i=0;i<newPMs.size();i++){
        	String print = newPMs.get(i).printVms();
            if(newPMs.get(i).getactive()){
            	//actpms++;
            }
            if(print==null)
            continue;
            System.out.println(print);
        }
        System.out.println("Number of active using 2SH: "+actpms);
        int repcost=0;
        for(int i=0;i<pmcount;i++){
        	for(int j=0; j<htree[i+firstpm].vmclist.size();j++){
        		repcost+=htree[i+firstpm].vmclist.get(j).cost;
        	}
        }
        //number of active(with newPMs)
        int newrepcost=0;
        newPMs.trimToSize();
        actpms+=newPMs.size();
        System.out.println("Number of active using 2SH with new PMs:"+ actpms);
        for(int i=0;i<newPMs.size();i++){
        	for(int j=0;j<newPMs.get(i).vmclist.size();j++){
        		newrepcost+=newPMs.get(i).vmclist.get(j).cost;
        	}
        }
        System.out.println("Total replication cost of 2SH: "+repcost);
        System.out.println("Total replication cost of 2SH(with new PMs: "+(repcost+newrepcost));
        System.out.println("required flow: "+regflo);
        System.out.println("Vms not placed by 2SH: "+notplaced);
        System.out.println("Vms placed byt 2SH: "+ numplaced);
        System.out.println("debug: "+(notplaced+numplaced));
        for(int i=0;i<pmcount;i++){
        	System.out.println("PM"+htree[i+firstpm].getid()+" color: "+htree[i+firstpm].color);
        }
        //System.out.println("Number of IPMS: "+(pmcount-actpms));
}
	public void sortBins(ArrayList<Node> bins){
		int n = bins.size();
		for(int i=0;i<n-1;i++){
			for(int j=0;j<n-i-1;j++){
				if (bins.get(j).getesc()<bins.get(j+1).getesc()){
					Node temp = bins.get(j);
					bins.set(j,bins.get(j+1));
					bins.set(j+1, temp);
				}
			}
		}
	}
	public void sortItems(ArrayList<CompNode> bins){
		int n = bins.size();
		for(int i=0;i<n-1;i++){
			for(int j=0;j<n-i-1;j++){
				if (orgnlsize[bins.get(j).vmid]<orgnlsize[bins.get(j+1).vmid]){
					CompNode temp = bins.get(j);
					bins.set(j,bins.get(j+1));
					bins.set(j+1, temp);
				}
			}
		}
	}

	public void createLPFile(){
		System.out.println("waiting for lpsolve file to be created... \n might take a few minutes");
		StringBuilder lpfile= new StringBuilder("");
		 lpfile.append("min: ");
        for (int i=0;i<pmcount;i++){
        	lpfile.append("Y"+i);
        	//lpfile+="Y"+i;
        	if(i!=pmcount-1){
        		lpfile.append(" + ");
        		//lpfile+=" + ";
        	}
        	else{
        		lpfile.append("; \n");
        		//lpfile+="; \n";
        	}
        }
        //all PMs with original VMs must be on (Yi=1)
        for (int i =0;i<pmcount;i++){
        	if(tree[i+firstpm].getissource()==true){
        		lpfile.append("Y"+i+"=1; \n");
        		//lpfile+="Y"+i+"=1; \n";
        	}
        }
        
        
        //if at least one replica is assigned to it, its on (CPMs)
        for(int i=0;i<pmcount;i++){//loop for pm
        	if(tree[i+firstpm].getissource()==false){
        		for(int j=0;j<p;j++){
        			for(int l=0;l<r-1;l++){
        				lpfile.append("Y"+i+" >= "+" X"+j+"_"+(l+1)+"_"+i+"; \n");
        				//lpfile+="Y"+i+" >= "+" X"+j+"_"+(l+1)+"_"+i+"; \n";
        			}
        		}
        	}
        }
        for (int i=0;i<pmcount;i++){
        	lpfile.append("Y"+i+" <= 1; \n");
        }
        
        //VM replicas can't go to the same PM
        for(int i=0;i<p;i++){
        	for(int j=0;j<pmcount;j++){
        		for(int l=0;l<r-1;l++){
        			lpfile.append("X"+i+"_"+(l+1)+"_"+j);
        			//lpfile+="X"+i+"_"+(l+1)+"_"+j;
        			if(l==r-2){
        				lpfile.append(" <= 1; \n");
        				//lpfile+=" <= 1; \n";
        			}
        			else{
        				lpfile.append(" + ");
        				//lpfile+=" + ";
        			}
        		}
        	}
        }
        for(int i=0;i<p;i++){
        	for(int j=0;j<r-1;j++){
        		for(int l=0;l<pmcount;l++){
        			int origipm= vms[i].getpmid();//pm of original vm
        			int copycost=vms[i].getCCost(j);
        			int tempd=distance(tree,tree[origipm],tree[l+firstpm],k);
        			//tempd is distance between original node and pm l
        			if(copycost!=tempd){
        				lpfile.append("X"+i+"_"+(j+1)+"_"+l+" = 0; \n");
        				//lpfile+="X"+i+"_"+(j+1)+"_"+l+" = 0; \n";
        			}
        			else if(origipm==l+firstpm){
        				lpfile.append("X"+i+"_"+(j+1)+"_"+l+" = 0; \n");
        			}
        		}
        	}
        }
      //each vm replica must be assigned to one PM
        for(int i=0;i<p;i++){//loop for every original
        	for(int j=0;j<r-1;j++){//loop for every copy
        		for(int l=0;l<pmcount;l++){//loop for every pm
        			//int origipm= vms[i].getpmid();//pm of original vm
        			//int copycost=vms[i].getCCost(j);
        			//int tempd=distance(tree,tree[origipm],tree[l+firstpm],k);
        			//tempd is distance between original node and pm l
        			//if(copycost!=tempd){
        				//continue;
        			//}
        			//else if(origipm==l+firstpm){
        				//continue;
        			//}
        			lpfile.append("X"+i+"_"+(j+1)+"_"+l);
        			//lpfile+="X"+i+"_"+(j+1)+"_"+l;
        			
        			if(l!=pmcount-1){
        				lpfile.append(" + ");
        				//lpfile+=" + ";
        			}
        			else{
        				lpfile.append(" = 1; \n");
        				//lpfile+=" = 1; \n";
        			}
        		}
        	}
        }
      //memory capacity constraint of all the PMs (ESC)
        for (int i=0;i<pmcount;i++){
        	int esc=tree[i+firstpm].getesc();
        	if(esc==0){
        		lpfile.append("Y"+i+" - Y"+i+">= ");
        	}
        	for (int h=0;h<esc;h++){
        		lpfile.append("Y"+i);
        		//lpfile+="Y"+i;
        			if(h==esc-1){
        				lpfile.append(">= ");
        				//lpfile+= ">= ";
        			}
        			else{
        				lpfile.append(" + ");
        				//lpfile+=" + ";
        			}
        	}
			//lpfile+="Y"+i+" * "+esc+">= ";
			//lpfile+="Y"+i+" "+esc+">= ";
        	for(int j=0; j<p;j++){
        		for(int l=0;l<r-1;l++){
        			lpfile.append(" X"+j+"_"+(l+1)+"_"+i);
        			//lpfile+=" X"+p+"_"+(l+1)+"_"+i;
        			if(l==r-2 && j==p-1){
        				lpfile.append("; \n");
        				//lpfile+="; \n";
        				
        				
        			}
        			else{
        				lpfile.append(" + ");
        				//.lpfile+=" + ";
        			}
        		}
        	}
        }
      //cpms can be potentially turned off
        for(int i =0;i<pmcount;i++){
        	if(tree[i+firstpm].getissource()==false){
        		 lpfile.append("int Y"+i+"; \n");
        		//lpfile.append("Y"+i+" <=1; \n");
        		//lpfile+="int Y"+i+"; \n";
        		//lpfile+="0 <= Y"+i+" <= 1; \n";
        	}
        }
        for(int i=0;i<p;i++){//declaring variables
        	for(int j=0;j<r-1;j++){
        		for(int l=0;l<pmcount;l++){
        			 lpfile.append("int X"+i+"_"+(j+1)+"_"+l+"; \n");
        			//lpfile.append("X"+i+" "+(j+1)+"_"+l+"<=1; \n");
        			//lpfile+="int X"+i+"_"+(j+1)+"_"+l+"; \n";
        			//lpfile+= "0 <= X"+i+"_"+(j+1)+"_"+l+" <=1; \n";
        			
        		}
        	}
        }
        
        try{
            //this is where i am writing my file to.
        	String out =lpfile.toString();
            File file = new File("lpsolve_input.lp");
            file.createNewFile();
            FileWriter fw = new FileWriter("lpsolve_input.lp");
            fw.write(out);
            fw.flush();
            fw.close();
        }catch (Exception e){System.out.println("failed 1");}
	}
	public void createLPFile112020(){
		System.out.println("waiting for lpsolve file to be created... \n might take a few minutes");
		StringBuilder lpfile= new StringBuilder("");
		 lpfile.append("min: ");
        for (int i=0;i<pmcount;i++){
        	lpfile.append("Y"+i);
        	//lpfile+="Y"+i;
        	if(i!=pmcount-1){
        		lpfile.append(" + ");
        		//lpfile+=" + ";
        	}
        	else{
        		lpfile.append("; \n");
        		//lpfile+="; \n";
        	}
        }
        //all PMs with original VMs must be on (Yi=1)
        for (int i =0;i<pmcount;i++){
        	if(tree[i+firstpm].getissource()==true){
        		lpfile.append("Y"+i+"=1; \n");
        		//lpfile+="Y"+i+"=1; \n";
        	}
        }
        
        
        //if at least one replica is assigned to it, its on (CPMs)
        for(int i=0;i<pmcount;i++){//loop for pm
        	if(tree[i+firstpm].getissource()==false){
        		for(int j=0;j<p;j++){
        			for(int l=0;l<r-1;l++){
        				lpfile.append("Y"+i+" >= "+" X"+j+"_"+(l+1)+"_"+i+"; \n");
        				//lpfile+="Y"+i+" >= "+" X"+j+"_"+(l+1)+"_"+i+"; \n";
        			}
        		}
        	}
        }
        for (int i=0;i<pmcount;i++){
        	lpfile.append("Y"+i+" <= 1; \n");
        }
        
        //VM replicas can't go to the same PM
        for(int i=0;i<p;i++){
        	for(int j=0;j<pmcount;j++){
        		for(int l=0;l<r-1;l++){
        			lpfile.append("X"+i+"_"+(l+1)+"_"+j);
        			//lpfile+="X"+i+"_"+(l+1)+"_"+j;
        			if(l==r-2){
        				lpfile.append(" <= 1; \n");
        				//lpfile+=" <= 1; \n";
        			}
        			else{
        				lpfile.append(" + ");
        				//lpfile+=" + ";
        			}
        		}
        	}
        }
        for(int i=0;i<p;i++){
        	for(int j=0;j<r-1;j++){
        		for(int l=0;l<pmcount;l++){
        			int origipm= vms[i].getpmid();//pm of original vm
        			int copycost=vms[i].getCCost(j);
        			int tempd=distance(tree,tree[origipm],tree[l+firstpm],k);
        			//tempd is distance between original node and pm l
        			if(copycost!=tempd){
        				//lpfile.append("X"+i+"_"+(j+1)+"_"+l+" = 0; \n");
        				//lpfile+="X"+i+"_"+(j+1)+"_"+l+" = 0; \n";
        			}
        			else if(origipm==l+firstpm){
        				lpfile.append("X"+i+"_"+(j+1)+"_"+l+" = 0; \n");
        			}
        			
        		}
        	}
        }
        //new to this method:=!
        for(int i=0;i<p;i++){
        	for(int l=0;l<tree[orgnl[i]].vmlist2.size();l++){
        		for(int x=0;x<tree[orgnl[i]].vmlist2.get(l).randIncs.size();x++){
        			for(int j=0;j<r-1;j++){
            			lpfile.append("X"+i+"_"+(j+1)+"_"+(tree[orgnl[i]].vmlist2.get(l).randIncs.get(x)-firstpm)+" = 0; \n");
        			}
        		}
        			
        	}
        	
        	
        }
      //each vm replica must be assigned to one PM
        for(int i=0;i<p;i++){//loop for every original
        	for(int j=0;j<r-1;j++){//loop for every copy
        		for(int l=0;l<pmcount;l++){//loop for every pm
        			//int origipm= vms[i].getpmid();//pm of original vm
        			//int copycost=vms[i].getCCost(j);
        			//int tempd=distance(tree,tree[origipm],tree[l+firstpm],k);
        			//tempd is distance between original node and pm l
        			//if(copycost!=tempd){
        				//continue;
        			//}
        			//else if(origipm==l+firstpm){
        				//continue;
        			//}
        			lpfile.append("X"+i+"_"+(j+1)+"_"+l);
        			//lpfile+="X"+i+"_"+(j+1)+"_"+l;
        			
        			if(l!=pmcount-1){
        				lpfile.append(" + ");
        				//lpfile+=" + ";
        			}
        			else{
        				lpfile.append(" = 1; \n");
        				//lpfile+=" = 1; \n";
        			}
        		}
        	}
        }
      //memory capacity constraint of all the PMs (ESC)
        for (int i=0;i<pmcount;i++){
        	int esc=tree[i+firstpm].getesc();
        	if(esc==0){
        		lpfile.append("Y"+i+" - Y"+i+">= ");
        	}
        	for (int h=0;h<esc;h++){
        		lpfile.append("Y"+i);
        		//lpfile+="Y"+i;
        			if(h==esc-1){
        				lpfile.append(">= ");
        				//lpfile+= ">= ";
        			}
        			else{
        				lpfile.append(" + ");
        				//lpfile+=" + ";
        			}
        	}
			//lpfile+="Y"+i+" * "+esc+">= ";
			//lpfile+="Y"+i+" "+esc+">= ";
        	for(int j=0; j<p;j++){
        		for(int l=0;l<r-1;l++){
        			lpfile.append(" X"+j+"_"+(l+1)+"_"+i);
        			//lpfile+=" X"+p+"_"+(l+1)+"_"+i;
        			if(l==r-2 && j==p-1){
        				lpfile.append("; \n");
        				//lpfile+="; \n";
        				
        				
        			}
        			else{
        				lpfile.append(" + ");
        				//.lpfile+=" + ";
        			}
        		}
        	}
        }
      //cpms can be potentially turned off
        for(int i =0;i<pmcount;i++){
        	if(tree[i+firstpm].getissource()==false){
        		 lpfile.append("int Y"+i+"; \n");
        		//lpfile.append("Y"+i+" <=1; \n");
        		//lpfile+="int Y"+i+"; \n";
        		//lpfile+="0 <= Y"+i+" <= 1; \n";
        	}
        }
        for(int i=0;i<p;i++){//declaring variables
        	for(int j=0;j<r-1;j++){
        		for(int l=0;l<pmcount;l++){
        			 lpfile.append("int X"+i+"_"+(j+1)+"_"+l+"; \n");
        			//lpfile.append("X"+i+" "+(j+1)+"_"+l+"<=1; \n");
        			//lpfile+="int X"+i+"_"+(j+1)+"_"+l+"; \n";
        			//lpfile+= "0 <= X"+i+"_"+(j+1)+"_"+l+" <=1; \n";
        			
        		}
        	}
        }
        
        try{
            //this is where i am writing my file to.
        	String out =lpfile.toString();
            File file = new File("lpsolve_input.lp");
            file.createNewFile();
            FileWriter fw = new FileWriter("lpsolve_input.lp");
            fw.write(out);
            fw.flush();
            fw.close();
        }catch (Exception e){System.out.println("failed 1");}
	}
	public void createLPFile112020B(){
		System.out.println("waiting for lpsolve file to be created... \n might take a few minutes");
		StringBuilder lpfile= new StringBuilder("");
		 lpfile.append("min: ");
        for (int i=0;i<pmcount;i++){
        	lpfile.append("Y"+i);
        	//lpfile+="Y"+i;
        	if(i!=pmcount-1){
        		lpfile.append(" + ");
        		//lpfile+=" + ";
        	}
        	else{
        		lpfile.append("; \n");
        		//lpfile+="; \n";
        	}
        }
        //all PMs with original VMs must be on (Yi=1)
        for (int i =0;i<pmcount;i++){
        	if(tree[i+firstpm].getissource()==true){
        		lpfile.append("Y"+i+"=1; \n");
        		//lpfile+="Y"+i+"=1; \n";
        	}
        }
        
        
        //if at least one replica is assigned to it, its on (CPMs)
        for(int i=0;i<pmcount;i++){//loop for pm
        	if(tree[i+firstpm].getissource()==false){
        		for(int j=0;j<p;j++){
        			for(int l=0;l<rvm[j];l++){
        				lpfile.append("Y"+i+" >= "+" X"+j+"_"+(l+1)+"_"+i+"; \n");
        				//lpfile+="Y"+i+" >= "+" X"+j+"_"+(l+1)+"_"+i+"; \n";
        			}
        		}
        	}
        }
        for (int i=0;i<pmcount;i++){
        	lpfile.append("Y"+i+" <= 1; \n");
        }
        
        //VM replicas can't go to the same PM
        for(int i=0;i<p;i++){
        	for(int j=0;j<pmcount;j++){
        		for(int l=0;l<rvm[i];l++){
        			lpfile.append("X"+i+"_"+(l+1)+"_"+j);
        			//lpfile+="X"+i+"_"+(l+1)+"_"+j;
        		//	if(l==r-2){
        			if(l==rvm[i]-1){
        				lpfile.append(" <= 1; \n");
        				//lpfile+=" <= 1; \n";
        			}
        			else{
        				lpfile.append(" + ");
        				//lpfile+=" + ";
        			}
        		}
        	}
        }
        for(int i=0;i<p;i++){
        	for(int j=0;j<rvm[i];j++){
        		for(int l=0;l<pmcount;l++){
        			int origipm= vms[i].getpmid();//pm of original vm
        			int copycost=vms[i].getCCost(j);
        			int tempd=distance(tree,tree[origipm],tree[l+firstpm],k);
        			//tempd is distance between original node and pm l
        			if(copycost!=tempd){
        				//lpfile.append("X"+i+"_"+(j+1)+"_"+l+" = 0; \n");
        				//lpfile+="X"+i+"_"+(j+1)+"_"+l+" = 0; \n";
        			}
        			else if(origipm==l+firstpm){
        				lpfile.append("X"+i+"_"+(j+1)+"_"+l+" = 0; \n");
        			}
        			
        		}
        	}
        }
        //new to this method:=!
        for(int i=0;i<p;i++){
        	for(int l=0;l<tree[orgnl[i]].vmlist2.size();l++){
        		for(int x=0;x<tree[orgnl[i]].vmlist2.get(l).randIncs.size();x++){
        			for(int j=0;j<r-1;j++){
            			lpfile.append("X"+i+"_"+(j+1)+"_"+(tree[orgnl[i]].vmlist2.get(l).randIncs.get(x)-firstpm)+" = 0; \n");
        			}
        		}
        			
        	}
        	
        	
        }
      //each vm replica must be assigned to one PM
        for(int i=0;i<p;i++){//loop for every original
        	for(int j=0;j<rvm[i];j++){//loop for every copy
        		for(int l=0;l<pmcount;l++){//loop for every pm
        			//int origipm= vms[i].getpmid();//pm of original vm
        			//int copycost=vms[i].getCCost(j);
        			//int tempd=distance(tree,tree[origipm],tree[l+firstpm],k);
        			//tempd is distance between original node and pm l
        			//if(copycost!=tempd){
        				//continue;
        			//}
        			//else if(origipm==l+firstpm){
        				//continue;
        			//}
        			lpfile.append("X"+i+"_"+(j+1)+"_"+l);
        			//lpfile+="X"+i+"_"+(j+1)+"_"+l;
        			
        			if(l!=pmcount-1){
        				lpfile.append(" + ");
        				//lpfile+=" + ";
        			}
        			else{
        				lpfile.append(" = 1; \n");
        				//lpfile+=" = 1; \n";
        			}
        		}
        	}
        }
      //memory capacity constraint of all the PMs (ESC)
        for (int i=0;i<pmcount;i++){
        	int esc=tree[i+firstpm].getesc();
        	if(esc==0){
        		lpfile.append("Y"+i+" - Y"+i+">= ");
        	}
        	for (int h=0;h<esc;h++){
        		lpfile.append("Y"+i);
        		//lpfile+="Y"+i;
        			if(h==esc-1){
        				lpfile.append(">= ");
        				//lpfile+= ">= ";
        			}
        			else{
        				lpfile.append(" + ");
        				//lpfile+=" + ";
        			}
        	}
			//lpfile+="Y"+i+" * "+esc+">= ";
			//lpfile+="Y"+i+" "+esc+">= ";
        	for(int j=0; j<p;j++){
        		for(int l=0;l<rvm[j];l++){
        			lpfile.append(" X"+j+"_"+(l+1)+"_"+i);
        			//lpfile+=" X"+p+"_"+(l+1)+"_"+i;
        			if(l==rvm[j]-1 && j==p-1){
        				lpfile.append("; \n");
        				//lpfile+="; \n";
        				
        				
        			}
        			else{
        				lpfile.append(" + ");
        				//.lpfile+=" + ";
        			}
        		}
        	}
        }
      //cpms can be potentially turned off
        for(int i =0;i<pmcount;i++){
        	if(tree[i+firstpm].getissource()==false){
        		 lpfile.append("int Y"+i+"; \n");
        		//lpfile.append("Y"+i+" <=1; \n");
        		//lpfile+="int Y"+i+"; \n";
        		//lpfile+="0 <= Y"+i+" <= 1; \n";
        	}
        }
        for(int i=0;i<p;i++){//declaring variables
        	for(int j=0;j<rvm[i];j++){
        		for(int l=0;l<pmcount;l++){
        			 lpfile.append("int X"+i+"_"+(j+1)+"_"+l+"; \n");
        			//lpfile.append("X"+i+" "+(j+1)+"_"+l+"<=1; \n");
        			//lpfile+="int X"+i+"_"+(j+1)+"_"+l+"; \n";
        			//lpfile+= "0 <= X"+i+"_"+(j+1)+"_"+l+" <=1; \n";
        			
        		}
        	}
        }
        
        try{
            //this is where i am writing my file to.
        	String out =lpfile.toString();
            File file = new File("lpsolve_input.lp");
            file.createNewFile();
            FileWriter fw = new FileWriter("lpsolve_input.lp");
            fw.write(out);
            fw.flush();
            fw.close();
        }catch (Exception e){System.out.println("failed 1");}
	}
	public void suitablePMs(){
		for (int i=0;i<vclistindex;i++){
			int vmnum = vmcList[i].getvmid();
			int pmnum = vmcList[i].getpmid();
			int cost  = vmcList[i].getcost();
			for (int j=0;j<pmcount;j++){
				int temp = distance(fftree,fftree[j+firstpm],fftree[orgnl[vmnum]],k);
				if (cost!=temp){
					continue;
				}
				if (fftree[j+firstpm].checkVm(vmnum)==true){
					continue;
				}
				vmcList[i].suitpms.add(new Integer(j+firstpm));
			}
		}
		System.out.println("Order of VM replicas and cost:");
		for (int i=0;i<vclistindex;i++){
			int vmnum = vmcList[i].getvmid();
			int cost = vmcList[i].getcost();
			System.out.println("VM "+vmnum+" replica with cost: "+cost);
			//System.out.println("VM "+vmnum+" replica with a cost of "+cost+" can be placed in:");
			for (int j=0;j<vmcList[i].suitpms.size();j++){
				//System.out.println("	PM: "+vmcList[i].suitpms.get(j));
			}
		}
	}
	public void suitablePMs112020(){
		for (int i=0;i<vclistindex;i++){
			int vmnum = vmcList[i].getvmid();
			int pmnum = vmcList[i].getpmid();
			int cost  = vmcList[i].getcost();
			for (int j=0;j<pmcount;j++){
				int temp = distance(fftree,fftree[j+firstpm],fftree[orgnl[vmnum]],k);
				if (cost!=temp){
					//continue;
				}
				boolean isflag=false;
                for(int l=0;l<tree[orgnl[vmnum]].vmlist2.size();l++){
                	if(tree[orgnl[vmnum]].vmlist2.get(l).vmid!=vmnum){
                		continue;
                	}
                	for(int x=0;x<tree[orgnl[vmnum]].vmlist2.get(l).randIncs.size();x++){
                		if(tree[orgnl[vmnum]].vmlist2.get(l).randIncs.get(x)==j+firstpm){
                			isflag=true;
                		}
                	}
                		
                }
                if(isflag){
                	//countnode++;
                	continue;
                }
				if (fftree[j+firstpm].checkVm(vmnum)==true){
					continue;
				}
				vmcList[i].suitpms.add(new Integer(j+firstpm));
			}
		}
		System.out.println("Order of VM replicas and cost:");
		for (int i=0;i<vclistindex;i++){
			int vmnum = vmcList[i].getvmid();
			int cost = vmcList[i].getcost();
			System.out.println("VM "+vmnum+" replica with cost: "+cost);
			//System.out.println("VM "+vmnum+" replica with a cost of "+cost+" can be placed in:");
			for (int j=0;j<vmcList[i].suitpms.size();j++){
				//System.out.println("	PM: "+vmcList[i].suitpms.get(j));
			}
		}
	}
	public void ffServerConsAlgo(){
		int placedcount=0;
		for (int i=0;i<vclistindex;i++){
			int vmid=vmcList[i].getvmid();
			
			int cost=vmcList[i].getcost();
			 
			boolean flag=false;
			for (int j=0;j<onPMs.size();j++){
				int onPM = onPMs.get(j);
				if(vmcList[i].checkIfPossible(onPM)==false){
					continue;
				}
				if(fftree[onPM].getvmcount()>=m){
					continue;
				}
				if(fftree[onPM].checkVm(vmid)==true){
					continue;
				}
				fftree[onPM].addVMc(vmid, onPM, cost, m);
				flag=true;
				//System.out.println(i);
				placedcount++;
				break; 
						
					
					
				
				
			}
			
			if (flag==false){
				boolean aflag=false;
				for (int j=0;j<vmcList[i].suitpms.size();j++){
					int thispm = vmcList[i].suitpms.get(j).intValue();
					if(fftree[thispm].getvmcount()<m){
						if(fftree[thispm].checkVm(vmid)==false){
							fftree[thispm].addVMc(vmid, thispm, cost, m);
							turnOnPM(thispm);
							aflag = true;
							//onPMs.add(thispm);
							//System.out.println(i);
							placedcount++;
							break;
						}
					}
				}
				if (aflag==false){
					System.out.println("VM "+vmid+" with a cost of "+cost+" could not be placed");
				}
			}
			
		}
		//Scanner scan = new Scanner(System.in);
		//int one=scan.nextInt();
		//int two=scan.nextInt();
		//int printh=distance(fftree,fftree[one],fftree[two],k);
		//System.out.println(printh);
		//printh=distance(tree,tree[one],tree[two],k);
		//System.out.println(printh);
		System.out.println("Amount placed from FF: "+placedcount);
		
		System.out.println("Placement of VMS after FF consolidation:");
	      int actpms=0;
	        for (int i=firstpm;i<=lastpm;i++){
	            String print = fftree[i].printVms();
	            if(fftree[i].getactive()){
	            	actpms++;
	            }
	            if(print==null)
	            continue;
	            System.out.println(print);
	        }
	        System.out.println("Number of active using FF: "+actpms);
	        System.out.println("Number of IPMS: "+(pmcount-actpms));
	}
	public void ffServerConsAlgo112020(){
		int placedcount=0;
		for (int i=0;i<vclistindex;i++){
			int vmid=vmcList[i].getvmid();
			
			int cost=vmcList[i].getcost();
			 
			boolean flag=false;
			for (int j=0;j<onPMs.size();j++){
				int onPM = onPMs.get(j);
				if(vmcList[i].checkIfPossible(onPM)==false){
					continue;
				}
				if(fftree[onPM].getvmcount()>=m){
					continue;
				}
				if(fftree[onPM].checkVm(vmid)==true){
					continue;
				}
				int tempcost=distance(fftree,fftree[orgnl[vmid]],fftree[onPM],k);
				fftree[onPM].addVMc(vmid, onPM, tempcost, m);
				flag=true;
				//System.out.println(i);
				placedcount++;
				break; 
						
					
					
				
				
			}
			
			if (flag==false){
				boolean aflag=false;
				for (int j=0;j<vmcList[i].suitpms.size();j++){
					int thispm = vmcList[i].suitpms.get(j).intValue();
					if(fftree[thispm].getvmcount()<m){
						if(fftree[thispm].checkVm(vmid)==false){
							if(vmcList[i].checkIfPossible(thispm)==true){
								int tempcost= distance(fftree,fftree[orgnl[vmid]],fftree[thispm],k);
								fftree[thispm].addVMc(vmid, thispm, tempcost, m);
								turnOnPM(thispm);
								aflag = true;
								//onPMs.add(thispm);
								//System.out.println(i);
								placedcount++;
								break;
							}
						}
					}
				}
				if (aflag==false){
					System.out.println("VM "+vmid+" with a cost of "+cost+" could not be placed");
				}
			}
			
		}
		//Scanner scan = new Scanner(System.in);
		//int one=scan.nextInt();
		//int two=scan.nextInt();
		//int printh=distance(fftree,fftree[one],fftree[two],k);
		//System.out.println(printh);
		//printh=distance(tree,tree[one],tree[two],k);
		//System.out.println(printh);
		System.out.println("Amount placed from FF: "+placedcount);
		
		System.out.println("Placement of VMS after FF consolidation:");
	      int actpms=0;
	        for (int i=firstpm;i<=lastpm;i++){
	            String print = fftree[i].printVms();
	            if(fftree[i].getactive()){
	            	actpms++;
	            }
	            if(print==null)
	            continue;
	            System.out.println(print);
	        }
	        System.out.println("Number of active using FF: "+actpms);
	        System.out.println("Number of IPMS: "+(pmcount-actpms));
	        int repcost=0;
	        for(int i=0;i<pmcount;i++){
	        	for(int j=0; j<fftree[i+firstpm].vmclist.size();j++){
	        		repcost+=fftree[i+firstpm].vmclist.get(j).cost;
	        	}
	        }
	        System.out.println("Total replication cost of greedy: "+repcost);
	}
	public void serverConsAlgo(){
		int IPM0=0;
        for(int i =1; i<=m;i++){//line 1 of algorithm
            for (int j=0; j<pmcount;j++){//for each CPM with i copies
                boolean aflag = true;
                //System.out.println("n "+i+" "+j+" ");
                if ((!(tree[j+firstpm].getactive())|| tree[j+firstpm].getissource()==true)){
                    
                	continue;//if this PM is not a CPM continue
                }
                //System.out.println("a "+i+" "+j+" ");
                if(tree[j+firstpm].getreplicount()!=i){
                	
                    continue; //if this CPM does not have i replicas continue;
                }
                //System.out.println("b "+i+" "+j+" ");
                for (int l=0;l<tree[j+firstpm].getvmclength();l++){//line 4
                    for (int h =0; h<pmcount;h++){
                       int thiscost= tree[j+firstpm].getvmcost(l);//cost of replicating this
                       int temp=distance (tree,tree[h+firstpm],tree[orgnl[tree[j+firstpm].getvmid(l)].intValue()],k);
                       //System.out.println("-1 "+i+" "+j+" "+l+" "+h);
                       //System.out.println("this cost "+thiscost+" temp "+temp);
                       if ((thiscost!=temp)){
                           //aflag=false;
                    	   if(h==pmcount-1){
                    		   aflag=false;
                    	   }
                        continue; //if they do not have the same cost, it is not a TPM, continue
                       }
                       //System.out.println("0 "+i+" "+j+" "+l+" "+h);
                       if(tree[h+firstpm].getactive()==false){
                    	   if(h==pmcount-1){
                    		   aflag=false;
                    	   }
                           continue; //if PM is not active, it is not a TPM
                        }
                      /* if(tree.get(h+firstpm).getissource()==true){
                           //aflag=false;
                        continue;// if PM is a source pm, it is not a TPM
                       }*/
                       //System.out.println("1 "+i+" "+j+" "+l+" "+h);
                       if(tree[h+firstpm].getvmcount()>=m){
                           //aflag=false;
                    	   if(h==pmcount-1){
                    		   aflag=false;
                    	   }
                           continue;//if PM is at capacity, it is not a TPM
                       }
                       //System.out.println("2 "+i+" "+j+" "+l+" "+h);
                       //check if PM has another replica of this VM replica
                       if(tree[h+firstpm].checkVm(tree[j+firstpm].getvmid(l))){
                           //aflag=false;
                    	   if(h==pmcount-1){
                    		   aflag=false;
                    	   }
                           continue;//if PM has a copy of this VM it is not a TPM
                       }
                       //System.out.println("3 "+i+" "+j+" "+l+" "+h);
                       //if loop hasn't continued, PM is a TPM
                       //move vm to PM, remove this vm copy
                       int movedvm = tree[j+firstpm].getvmid(l);
                       //aflag=true;
                       System.out.println("--VM "+tree[j+firstpm].getvmid(l)+ " from PM " +(j+firstpm)+" migrated to PM with id " + (h+firstpm));
                       boolean gflag =tree[h+firstpm].addVMc(movedvm,h+firstpm,thiscost,m);
                       if(gflag==false){
                    	   System.out.print("SCERR");
                       }
                       tree[j+firstpm].removeVMc(tree[j+firstpm].getvmid(l));
                       l--;
                       //System.out.println("VM "+tree.get(j+firstpm).getvmid(l)+ "from PM " +(j+firstpm)+"migrated to PM with id " + (h+firstpm));
                       
                       break;
                       
                    }
                    if(aflag==false){
                        break;
                    }
                    
                }
                if (aflag==true){
                        IPM0++;
                }
                
            }
        }
      System.out.println("Placement of VMS after ServCons:");
      int actpm=0;
        for (int i=firstpm;i<=lastpm;i++){
            String print = tree[i].printVms();
            if(tree[i].getactive()){
            	actpm++;
            }
            if(print==null)
            continue;
            System.out.println(print);
        }  
        
        System.out.println("Number of IPMs for ServCons: "+ IPM0);
        System.out.println("Number of active PMS for ServCons: "+actpm);
        System.out.println("lpsolve file has been created");
        
        
	}
	public void serverConsAlgo112020(){
		int IPM0=0;
        for(int i =1; i<=m;i++){//line 1 of algorithm
            for (int j=0; j<pmcount;j++){//for each CPM with i copies
                boolean aflag = true;
                //System.out.println("n "+i+" "+j+" ");
                if ((!(tree[j+firstpm].getactive())|| tree[j+firstpm].getissource()==true)){
                    
                	continue;//if this PM is not a CPM continue
                }
                //System.out.println("a "+i+" "+j+" ");
                if(tree[j+firstpm].getreplicount()!=i){
                	
                    continue; //if this CPM does not have i replicas continue;
                }
                //System.out.println("b "+i+" "+j+" ");
                for (int l=0;l<tree[j+firstpm].getvmclength();l++){//line 4
                    for (int h =0; h<pmcount;h++){
                       int thiscost= tree[j+firstpm].getvmcost(l);//cost of replicating this
                       int temp=distance (tree,tree[h+firstpm],tree[orgnl[tree[j+firstpm].getvmid(l)].intValue()],k);
                       //System.out.println("-1 "+i+" "+j+" "+l+" "+h);
                       //System.out.println("this cost "+thiscost+" temp "+temp);
                       if ((thiscost!=temp)){
                           //aflag=false;
                    	   if(h==pmcount-1){
                    		   aflag=false;
                    	   }
                        continue; //if they do not have the same cost, it is not a TPM, continue
                       }
                       //System.out.println("0 "+i+" "+j+" "+l+" "+h);
                       if(tree[h+firstpm].getactive()==false){
                    	   if(h==pmcount-1){
                    		   aflag=false;
                    	   }
                           continue; //if PM is not active, it is not a TPM
                        }
                      // TODO: IF PM is in INCOMP LIST IT IS NOT TPM< COntinue
                       boolean isflag=false;
                       for(int x=0;x<tree[orgnl[tree[j+firstpm].getvmid(l)]].vmlist2.size();x++){
                    	   if(tree[orgnl[tree[j+firstpm].getvmid(l)]].vmlist2.get(x).vmid!=tree[j+firstpm].getvmid(l)){
                    		   continue;
                    	   }
                    	   for(int z=0;z<tree[orgnl[tree[j+firstpm].getvmid(l)]].vmlist2.get(x).randIncs.size();z++){
                    		   if (tree[orgnl[tree[j+firstpm].getvmid(l)]].vmlist2.get(x).randIncs.get(z)==h+firstpm){
                    			   isflag=true;
                    		   }
                    	   }
                       }
                       if(isflag)
                    	   continue;
                       ////////////////////////////////////////////////////////////////
                      /* if(tree.get(h+firstpm).getissource()==true){
                           //aflag=false;
                        continue;// if PM is a source pm, it is not a TPM
                       }*/
                       //System.out.println("1 "+i+" "+j+" "+l+" "+h);
                       if(tree[h+firstpm].getvmcount()>=m){
                           //aflag=false;
                    	   if(h==pmcount-1){
                    		   aflag=false;
                    	   }
                           continue;//if PM is at capacity, it is not a TPM
                       }
                       //System.out.println("2 "+i+" "+j+" "+l+" "+h);
                       //check if PM has another replica of this VM replica
                       if(tree[h+firstpm].checkVm(tree[j+firstpm].getvmid(l))){
                           //aflag=false;
                    	   if(h==pmcount-1){
                    		   aflag=false;
                    	   }
                           continue;//if PM has a copy of this VM it is not a TPM
                       }
                       //System.out.println("3 "+i+" "+j+" "+l+" "+h);
                       //if loop hasn't continued, PM is a TPM
                       //move vm to PM, remove this vm copy
                       int movedvm = tree[j+firstpm].getvmid(l);
                       //aflag=true;
                       System.out.println("--VM "+tree[j+firstpm].getvmid(l)+ " from PM " +(j+firstpm)+" migrated to PM with id " + (h+firstpm));
                       boolean gflag =tree[h+firstpm].addVMc(movedvm,h+firstpm,thiscost,m);
                       if(gflag==false){
                    	   System.out.print("SCERR");
                       }
                       tree[j+firstpm].removeVMc(tree[j+firstpm].getvmid(l));
                       l--;
                       //System.out.println("VM "+tree.get(j+firstpm).getvmid(l)+ "from PM " +(j+firstpm)+"migrated to PM with id " + (h+firstpm));
                       
                       break;
                       
                    }
                    if(aflag==false){
                        break;
                    }
                    
                }
                if (aflag==true){
                        IPM0++;
                }
                
            }
        }
      System.out.println("Placement of VMS after ServCons:");
      int actpm=0;
        for (int i=firstpm;i<=lastpm;i++){
            String print = tree[i].printVms();
            if(tree[i].getactive()){
            	actpm++;
            }
            if(print==null)
            continue;
            System.out.println(print);
        }  
        
        System.out.println("Number of IPMs for ServCons: "+ IPM0);
        System.out.println("Number of active PMS for ServCons: "+actpm);
        System.out.println("lpsolve file has been created");
        int repcost=0;
        for(int i=0;i<pmcount;i++){
        	for(int j=0; j<tree[i+firstpm].vmclist.size();j++){
        		repcost+=tree[i+firstpm].vmclist.get(j).cost;
        	}
        }
        System.out.println("Total replication cost of Servcons: "+repcost);
        
        
	}
	public void improvedServCons(){
		int IPM0=0;
        for(int i =1; i<=m;i++){//line 1 of algorithm
            for (int j=0; j<pmcount;j++){//for each CPM with i copies
                boolean aflag = true;
                //System.out.println("n "+i+" "+j+" ");
                if ((!(itree[j+firstpm].getactive())|| itree[j+firstpm].getissource()==true)){
                    
                	continue;//if this PM is not a CPM continue
                }
                //System.out.println("a "+i+" "+j+" ");
                if(itree[j+firstpm].getreplicount()!=i){
                	
                    continue; //if this CPM does not have i replicas continue;
                }
                //System.out.println("b "+i+" "+j+" ");
                ArrayList<MiVM> movVMs= new ArrayList<MiVM>(0);
                for (int l=0;l<itree[j+firstpm].getvmclength();l++){//line 4
                    for (int h =0; h<pmcount;h++){
                       int thiscost= itree[j+firstpm].getvmcost(l);//cost of replicating this
                       int temp=distance (itree,itree[h+firstpm],itree[orgnl[itree[j+firstpm].getvmid(l)].intValue()],k);
                       //System.out.println("-1 "+i+" "+j+" "+l+" "+h);
                       //System.out.println("this cost "+thiscost+" temp "+temp);
                       if ((thiscost!=temp)){
                           //aflag=false;
                    	   if(h==pmcount-1){
                    		   aflag=false;
                    	   }
                        continue; //if they do not have the same cost, it is not a TPM, continue
                       }
                       //System.out.println("0 "+i+" "+j+" "+l+" "+h);
                       if(itree[h+firstpm].getactive()==false){
                    	   if(h==pmcount-1){
                    		   aflag=false;
                    	   }
                           continue; //if PM is not active, it is not a TPM
                        }
                      /* if(tree.get(h+firstpm).getissource()==true){
                           //aflag=false;
                        continue;// if PM is a source pm, it is not a TPM
                       }*/
                       //System.out.println("1 "+i+" "+j+" "+l+" "+h);
                       if(itree[h+firstpm].getvmcount()>=m){
                           //aflag=false;
                    	   if(h==pmcount-1){
                    		   aflag=false;
                    	   }
                           continue;//if PM is at capacity, it is not a TPM
                       }
                       //System.out.println("2 "+i+" "+j+" "+l+" "+h);
                       //check if PM has another replica of this VM replica
                       if(itree[h+firstpm].checkVm(itree[j+firstpm].getvmid(l))){
                           //aflag=false;
                    	   if(h==pmcount-1){
                    		   aflag=false;
                    	   }
                           continue;//if PM has a copy of this VM it is not a TPM
                       }
                       //System.out.println("3 "+i+" "+j+" "+l+" "+h);
                       //if loop hasn't continued, PM is a TPM
                       //move vm to PM, remove this vm copy
                       
                       int movedvm = itree[j+firstpm].getvmid(l);
                       movVMs.add(new MiVM(itree,j+firstpm,h+firstpm,thiscost,movedvm));
                       int lastindex=movVMs.size()-1;
                       //movVMs.get(lastindex).decEsc();
                       //aflag=true;
                       //System.out.println("--VM "+tree[j+firstpm].getvmid(l)+ " from PM " +(j+firstpm)+" migrated to PM with id " + (h+firstpm));
                       //tree[h+firstpm].addVMc(movedvm,h+firstpm,thiscost,m);
                       //tree[j+firstpm].removeVMc(tree[j+firstpm].getvmid(l));
                       //l--;
                       //System.out.println("VM "+tree.get(j+firstpm).getvmid(l)+ "from PM " +(j+firstpm)+"migrated to PM with id " + (h+firstpm));
                       
                       break;
                       
                    }
                    if(aflag==false){
                    	
                        break;
                    }
                    
                }
                if (aflag==false){
                	for (int y=0;y<movVMs.size();y++){
                		movVMs.get(y).reverse();
                	}
                }
                if (aflag==true){
                	//if this flag is true that means every Vm replica is able to move, so move the VMs here
                	for (int y=0;y<movVMs.size();y++){
                		movVMs.get(y).reverse();
                	}
                	for (int y=0;y<movVMs.size();y++){
                		int prevpm=movVMs.get(y).prevpm;
                		int newpm=movVMs.get(y).newpm;
                		int ncost=movVMs.get(y).cost;
                		int movmid=movVMs.get(y).vmid;
                		boolean gflagt =itree[newpm].addVMc(movmid, newpm, ncost, m);
                		itree[prevpm].removeVMc(movmid);
                		if (gflagt==false){
                			System.out.println("You made an error");
                		}
                		System.out.println("++VM "+movmid+" from PM "+prevpm+" migrated to PM with id: "+newpm);
                	}
                        IPM0++;
                }
                
            }
        }
      System.out.println("Placement of VMS after improvedServCons:");
      int actpm=0;
        for (int i=firstpm;i<=lastpm;i++){
            String print = itree[i].printVms();
            if(itree[i].getactive()){
            	actpm++;
            }
            if(print==null)
            continue;
            System.out.println(print);
            System.out.println(itree[i].vmcount);
        }  
        
        System.out.println("Number of IPMs for improvedServCons: "+ IPM0);
        System.out.println("Number of active PMS for improvedServCons: "+actpm);
	}
	public void improvedServConsv2(){
		int IPM0=0;
        for(int i =1; i<=m;i++){//line 1 of algorithm
            for (int j=0; j<pmcount;j++){//for each CPM with i copies
                boolean aflag = true;
                //System.out.println("n "+i+" "+j+" ");
                if ((!(itree[j+firstpm].getactive())|| itree[j+firstpm].getissource()==true)){
                    
                	continue;//if this PM is not a CPM continue
                }
                //System.out.println("a "+i+" "+j+" ");
                if(itree[j+firstpm].getreplicount()!=i){
                	
                    continue; //if this CPM does not have i replicas continue;
                }
                //System.out.println("b "+i+" "+j+" ");
                ArrayList<MiVM> movVMs= new ArrayList<MiVM>(0);
                
                for (int l=0;l<itree[j+firstpm].getvmclength();l++){//line 4
                	boolean origiplaced =false;
                	for (int h =0; h<pmcount;h++){//loop for finding a PM with original VM
                        int thiscost= itree[j+firstpm].getvmcost(l);//cost of replicating this
                        int temp=distance (itree,itree[h+firstpm],itree[orgnl[itree[j+firstpm].getvmid(l)].intValue()],k);
                        //System.out.println("-1 "+i+" "+j+" "+l+" "+h);
                        //System.out.println("this cost "+thiscost+" temp "+temp);
                        if (itree[h+firstpm].getissource()==false){
                        	continue;
                        }
                        if ((thiscost!=temp)){
                            //aflag=false;
                     	   
                     	   //if(h==pmcount-1){
                     		   //aflag=false;
                     	   //}
                         continue; //if they do not have the same cost, it is not a TPM, continue
                        }
                        //System.out.println("0 "+i+" "+j+" "+l+" "+h);
                        if(itree[h+firstpm].getactive()==false){
                     	   //if(h==pmcount-1){
                     		   //aflag=false;
                     	   //}
                            continue; //if PM is not active, it is not a TPM
                         }
                       /* if(tree.get(h+firstpm).getissource()==true){
                            //aflag=false;
                         continue;// if PM is a source pm, it is not a TPM
                        }*/
                        //System.out.println("1 "+i+" "+j+" "+l+" "+h);
                        if(itree[h+firstpm].getvmcount()>=m){
                            //aflag=false;
                     	   //if(h==pmcount-1){
                     		   //aflag=false;
                     	   //}
                            continue;//if PM is at capacity, it is not a TPM
                        }
                        //System.out.println("2 "+i+" "+j+" "+l+" "+h);
                        //check if PM has another replica of this VM replica
                        if(itree[h+firstpm].checkVm(itree[j+firstpm].getvmid(l))){
                            //aflag=false;
                     	   //if(h==pmcount-1){
                     		 //  aflag=false;
                     	//   }
                            continue;//if PM has a copy of this VM it is not a TPM
                        }
                        //System.out.println("3 "+i+" "+j+" "+l+" "+h);
                        //if loop hasn't continued, PM is a TPM
                        //move vm to PM, remove this vm copy
                        
                        int movedvm = itree[j+firstpm].getvmid(l);
                        movVMs.add(new MiVM(itree,j+firstpm,h+firstpm,thiscost,movedvm));
                        origiplaced=true;
                        
                        //int lastindex=movVMs.size()-1;
                        //movVMs.get(lastindex).decEsc();
                        //aflag=true;
                        //System.out.println("--VM "+tree[j+firstpm].getvmid(l)+ " from PM " +(j+firstpm)+" migrated to PM with id " + (h+firstpm));
                        //tree[h+firstpm].addVMc(movedvm,h+firstpm,thiscost,m);
                        //tree[j+firstpm].removeVMc(tree[j+firstpm].getvmid(l));
                        //l--;
                        //System.out.println("VM "+tree.get(j+firstpm).getvmid(l)+ "from PM " +(j+firstpm)+"migrated to PM with id " + (h+firstpm));
                        
                        break;
                        
                     }
                    for (int h =0; h<pmcount;h++){
                    	if(origiplaced==true){
                    		break;
                    	}
                       int thiscost= itree[j+firstpm].getvmcost(l);//cost of replicating this
                       int temp=distance (itree,itree[h+firstpm],itree[orgnl[itree[j+firstpm].getvmid(l)].intValue()],k);
                       //System.out.println("-1 "+i+" "+j+" "+l+" "+h);
                       //System.out.println("this cost "+thiscost+" temp "+temp);
                       
                       if ((thiscost!=temp)){
                           //aflag=false;
                    	   
                    	   if(h==pmcount-1){
                    		   aflag=false;
                    	   }
                        continue; //if they do not have the same cost, it is not a TPM, continue
                       }
                       //System.out.println("0 "+i+" "+j+" "+l+" "+h);
                       if(itree[h+firstpm].getactive()==false){
                    	   if(h==pmcount-1){
                    		   aflag=false;
                    	   }
                           continue; //if PM is not active, it is not a TPM
                        }
                      /* if(tree.get(h+firstpm).getissource()==true){
                           //aflag=false;
                        continue;// if PM is a source pm, it is not a TPM
                       }*/
                       //System.out.println("1 "+i+" "+j+" "+l+" "+h);
                       if(itree[h+firstpm].getvmcount()>=m){
                           //aflag=false;
                    	   if(h==pmcount-1){
                    		   aflag=false;
                    	   }
                           continue;//if PM is at capacity, it is not a TPM
                       }
                       //System.out.println("2 "+i+" "+j+" "+l+" "+h);
                       //check if PM has another replica of this VM replica
                       if(itree[h+firstpm].checkVm(itree[j+firstpm].getvmid(l))){
                           //aflag=false;
                    	   if(h==pmcount-1){
                    		   aflag=false;
                    	   }
                           continue;//if PM has a copy of this VM it is not a TPM
                       }
                       //System.out.println("3 "+i+" "+j+" "+l+" "+h);
                       //if loop hasn't continued, PM is a TPM
                       //move vm to PM, remove this vm copy
                       
                       int movedvm = itree[j+firstpm].getvmid(l);
                       movVMs.add(new MiVM(itree,j+firstpm,h+firstpm,thiscost,movedvm));
                       //int lastindex=movVMs.size()-1;
                       //movVMs.get(lastindex).decEsc();
                       //aflag=true;
                       //System.out.println("--VM "+tree[j+firstpm].getvmid(l)+ " from PM " +(j+firstpm)+" migrated to PM with id " + (h+firstpm));
                       //tree[h+firstpm].addVMc(movedvm,h+firstpm,thiscost,m);
                       //tree[j+firstpm].removeVMc(tree[j+firstpm].getvmid(l));
                       //l--;
                       //System.out.println("VM "+tree.get(j+firstpm).getvmid(l)+ "from PM " +(j+firstpm)+"migrated to PM with id " + (h+firstpm));
                       
                       break;
                       
                    }
                    if(aflag==false){
                    	
                        break;
                    }
                    
                }
                if (aflag==false){
                	for (int y=0;y<movVMs.size();y++){
                		movVMs.get(y).reverse();
                	}
                }
                if (aflag==true){
                	//if this flag is true that means every Vm replica is able to move, so move the VMs here
                	for (int y=0;y<movVMs.size();y++){
                		movVMs.get(y).reverse();
                	}
                	for (int y=0;y<movVMs.size();y++){
                		int prevpm=movVMs.get(y).prevpm;
                		int newpm=movVMs.get(y).newpm;
                		int ncost=movVMs.get(y).cost;
                		int movmid=movVMs.get(y).vmid;
                		boolean gflagt =itree[newpm].addVMc(movmid, newpm, ncost, m);
                		itree[prevpm].removeVMc(movmid);
                		if (gflagt==false){
                			System.out.println("You made an error");
                		}
                		System.out.println("++VM "+movmid+" from PM "+prevpm+" migrated to PM with id: "+newpm);
                	}
                        IPM0++;
                }
                
            }
        }
      System.out.println("Placement of VMS after improvedServCons:");
      int actpm=0;
        for (int i=firstpm;i<=lastpm;i++){
            String print = itree[i].printVms();
            if(itree[i].getactive()){
            	actpm++;
            }
            if(print==null)
            continue;
            System.out.println(print);
            System.out.println(itree[i].vmcount);
        }  
        
        System.out.println("Number of IPMs for improvedServCons: "+ IPM0);
        System.out.println("Number of active PMS for improvedServCons: "+actpm);
	}
	public void improvedServConsv2112020(){
		int IPM0=0;
        for(int i =1; i<=m;i++){//line 1 of algorithm
            for (int j=0; j<pmcount;j++){//for each CPM with i copies
                boolean aflag = true;
                //System.out.println("n "+i+" "+j+" ");
                if ((!(itree[j+firstpm].getactive())|| itree[j+firstpm].getissource()==true)){
                    
                	continue;//if this PM is not a CPM continue
                }
                //System.out.println("a "+i+" "+j+" ");
                if(itree[j+firstpm].getreplicount()!=i){
                	
                    continue; //if this CPM does not have i replicas continue;
                }
                //System.out.println("b "+i+" "+j+" ");
                ArrayList<MiVM> movVMs= new ArrayList<MiVM>(0);
                
                for (int l=0;l<itree[j+firstpm].getvmclength();l++){//line 4
                	boolean origiplaced =false;
                	for (int h =0; h<pmcount;h++){//loop for finding a PM with original VM
                        int thiscost= itree[j+firstpm].getvmcost(l);//cost of replicating this
                        int temp=distance (itree,itree[h+firstpm],itree[orgnl[itree[j+firstpm].getvmid(l)].intValue()],k);
                        //System.out.println("-1 "+i+" "+j+" "+l+" "+h);
                        //System.out.println("this cost "+thiscost+" temp "+temp);
                        if (itree[h+firstpm].getissource()==false){
                        	continue;
                        }
                        if ((thiscost!=temp)){
                            //aflag=false;
                     	   
                     	   //if(h==pmcount-1){
                     		   //aflag=false;
                     	   //}
                         continue; //if they do not have the same cost, it is not a TPM, continue
                        }
                     // TODO: IF PM is in INCOMP LIST IT IS NOT TPM< COntinue
                        boolean isflag=false;
                        for(int x=0;x<itree[orgnl[itree[j+firstpm].getvmid(l)]].vmlist2.size();x++){
                     	   if(itree[orgnl[itree[j+firstpm].getvmid(l)]].vmlist2.get(x).vmid!=itree[j+firstpm].getvmid(l)){
                     		   continue;
                     	   }
                     	   for(int z=0;z<itree[orgnl[itree[j+firstpm].getvmid(l)]].vmlist2.get(x).randIncs.size();z++){
                     		   if (itree[orgnl[itree[j+firstpm].getvmid(l)]].vmlist2.get(x).randIncs.get(z)==h+firstpm){
                     			   isflag=true;
                     		   }
                     	   }
                        }
                        if(isflag)
                     	   continue;
                        ////////////////////////////////////////////////////////////////
                        //System.out.println("0 "+i+" "+j+" "+l+" "+h);
                        if(itree[h+firstpm].getactive()==false){
                     	   //if(h==pmcount-1){
                     		   //aflag=false;
                     	   //}
                            continue; //if PM is not active, it is not a TPM
                         }
                       /* if(tree.get(h+firstpm).getissource()==true){
                            //aflag=false;
                         continue;// if PM is a source pm, it is not a TPM
                        }*/
                        //System.out.println("1 "+i+" "+j+" "+l+" "+h);
                        if(itree[h+firstpm].getvmcount()>=m){
                            //aflag=false;
                     	   //if(h==pmcount-1){
                     		   //aflag=false;
                     	   //}
                            continue;//if PM is at capacity, it is not a TPM
                        }
                        //System.out.println("2 "+i+" "+j+" "+l+" "+h);
                        //check if PM has another replica of this VM replica
                        if(itree[h+firstpm].checkVm(itree[j+firstpm].getvmid(l))){
                            //aflag=false;
                     	   //if(h==pmcount-1){
                     		 //  aflag=false;
                     	//   }
                            continue;//if PM has a copy of this VM it is not a TPM
                        }
                        //System.out.println("3 "+i+" "+j+" "+l+" "+h);
                        //if loop hasn't continued, PM is a TPM
                        //move vm to PM, remove this vm copy
                        
                        int movedvm = itree[j+firstpm].getvmid(l);
                        movVMs.add(new MiVM(itree,j+firstpm,h+firstpm,thiscost,movedvm));
                        origiplaced=true;
                        
                        //int lastindex=movVMs.size()-1;
                        //movVMs.get(lastindex).decEsc();
                        //aflag=true;
                        //System.out.println("--VM "+tree[j+firstpm].getvmid(l)+ " from PM " +(j+firstpm)+" migrated to PM with id " + (h+firstpm));
                        //tree[h+firstpm].addVMc(movedvm,h+firstpm,thiscost,m);
                        //tree[j+firstpm].removeVMc(tree[j+firstpm].getvmid(l));
                        //l--;
                        //System.out.println("VM "+tree.get(j+firstpm).getvmid(l)+ "from PM " +(j+firstpm)+"migrated to PM with id " + (h+firstpm));
                        
                        break;
                        
                     }
                    for (int h =0; h<pmcount;h++){
                    	if(origiplaced==true){
                    		break;
                    	}
                       int thiscost= itree[j+firstpm].getvmcost(l);//cost of replicating this
                       int temp=distance (itree,itree[h+firstpm],itree[orgnl[itree[j+firstpm].getvmid(l)].intValue()],k);
                       //System.out.println("-1 "+i+" "+j+" "+l+" "+h);
                       //System.out.println("this cost "+thiscost+" temp "+temp);
                       
                       if ((thiscost!=temp)){
                           //aflag=false;
                    	   
                    	   if(h==pmcount-1){
                    		   aflag=false;
                    	   }
                        continue; //if they do not have the same cost, it is not a TPM, continue
                       }
                       //System.out.println("0 "+i+" "+j+" "+l+" "+h);
                       if(itree[h+firstpm].getactive()==false){
                    	   if(h==pmcount-1){
                    		   aflag=false;
                    	   }
                           continue; //if PM is not active, it is not a TPM
                        }
                      /* if(tree.get(h+firstpm).getissource()==true){
                           //aflag=false;
                        continue;// if PM is a source pm, it is not a TPM
                       }*/
                       //System.out.println("1 "+i+" "+j+" "+l+" "+h);
                       if(itree[h+firstpm].getvmcount()>=m){
                           //aflag=false;
                    	   if(h==pmcount-1){
                    		   aflag=false;
                    	   }
                           continue;//if PM is at capacity, it is not a TPM
                       }
                    // TODO: IF PM is in INCOMP LIST IT IS NOT TPM< COntinue
                       boolean isflag=false;
                       for(int x=0;x<itree[orgnl[itree[j+firstpm].getvmid(l)]].vmlist2.size();x++){
                    	   if(itree[orgnl[itree[j+firstpm].getvmid(l)]].vmlist2.get(x).vmid!=itree[j+firstpm].getvmid(l)){
                    		   continue;
                    	   }
                    	   for(int z=0;z<itree[orgnl[itree[j+firstpm].getvmid(l)]].vmlist2.get(x).randIncs.size();z++){
                    		   if (itree[orgnl[itree[j+firstpm].getvmid(l)]].vmlist2.get(x).randIncs.get(z)==h+firstpm){
                    			   isflag=true;
                    		   }
                    	   }
                       }
                       if(isflag)
                    	   continue;
                       ////////////////////////////////////////////////////////////////
                       //System.out.println("2 "+i+" "+j+" "+l+" "+h);
                       //check if PM has another replica of this VM replica
                       if(itree[h+firstpm].checkVm(itree[j+firstpm].getvmid(l))){
                           //aflag=false;
                    	   if(h==pmcount-1){
                    		   aflag=false;
                    	   }
                           continue;//if PM has a copy of this VM it is not a TPM
                       }
                       //System.out.println("3 "+i+" "+j+" "+l+" "+h);
                       //if loop hasn't continued, PM is a TPM
                       //move vm to PM, remove this vm copy
                       
                       int movedvm = itree[j+firstpm].getvmid(l);
                       movVMs.add(new MiVM(itree,j+firstpm,h+firstpm,thiscost,movedvm));
                       //int lastindex=movVMs.size()-1;
                       //movVMs.get(lastindex).decEsc();
                       //aflag=true;
                       //System.out.println("--VM "+tree[j+firstpm].getvmid(l)+ " from PM " +(j+firstpm)+" migrated to PM with id " + (h+firstpm));
                       //tree[h+firstpm].addVMc(movedvm,h+firstpm,thiscost,m);
                       //tree[j+firstpm].removeVMc(tree[j+firstpm].getvmid(l));
                       //l--;
                       //System.out.println("VM "+tree.get(j+firstpm).getvmid(l)+ "from PM " +(j+firstpm)+"migrated to PM with id " + (h+firstpm));
                       
                       break;
                       
                    }
                    if(aflag==false){
                    	
                        break;
                    }
                    
                }
                if (aflag==false){
                	for (int y=0;y<movVMs.size();y++){
                		movVMs.get(y).reverse();
                	}
                }
                if (aflag==true){
                	//if this flag is true that means every Vm replica is able to move, so move the VMs here
                	for (int y=0;y<movVMs.size();y++){
                		movVMs.get(y).reverse();
                	}
                	for (int y=0;y<movVMs.size();y++){
                		int prevpm=movVMs.get(y).prevpm;
                		int newpm=movVMs.get(y).newpm;
                		int ncost=movVMs.get(y).cost;
                		int movmid=movVMs.get(y).vmid;
                		boolean gflagt =itree[newpm].addVMc(movmid, newpm, ncost, m);
                		itree[prevpm].removeVMc(movmid);
                		if (gflagt==false){
                			System.out.println("You made an error");
                		}
                		System.out.println("++VM "+movmid+" from PM "+prevpm+" migrated to PM with id: "+newpm);
                	}
                        IPM0++;
                }
                
            }
        }
      System.out.println("Placement of VMS after improvedServCons:");
      int actpm=0;
        for (int i=firstpm;i<=lastpm;i++){
            String print = itree[i].printVms();
            if(itree[i].getactive()){
            	actpm++;
            }
            if(print==null)
            continue;
            System.out.println(print);
            System.out.println(itree[i].vmcount);
        }  
        
        System.out.println("Number of IPMs for improvedServCons: "+ IPM0);
        System.out.println("Number of active PMS for improvedServCons: "+actpm);
        int repcost=0;
        for(int i=0;i<pmcount;i++){
        	for(int j=0; j<itree[i+firstpm].vmclist.size();j++){
        		repcost+=itree[i+firstpm].vmclist.get(j).cost;
        	}
        }
        System.out.println("Total replication cost of improved Servcons: "+repcost);
	}
	public void bubbleSort(){
		//sorting algorithm before FF
		//sorting vmcList by descending number of incompats.
		int n= vmcList.length;
		for(int i=0;i<n-1;i++){
			for(int j=0;j<n-i-1;j++){
				if(vmcList[j].suitpms.size()> vmcList[j+1].suitpms.size()){
					CopyVM temp = vmcList[j];
					vmcList[j]=vmcList[j+1];
					vmcList[j+1]=temp;
				}
			}
		}
		
	}
	public void bSort(ArrayList<CompNode> unlist, ArrayList<CompNode> prelist){
		int n = unlist.size();
		for(int i=0;i<n-1;i++){
			for(int j=0;j<n-i-1;j++){
				if (unlist.get(j).nbors.size()<unlist.get(j+1).nbors.size()){
					CompNode temp = unlist.get(j);
					unlist.set(j,unlist.get(j+1));
					unlist.set(j+1, temp);
				}
			}
		}
		n = prelist.size();
		for(int i=0;i<n-1;i++){
			for(int j=0;j<n-i-1;j++){
				if (prelist.get(j).nbors.size()<prelist.get(j+1).nbors.size()){
					CompNode temp = prelist.get(j);
					prelist.set(j,prelist.get(j+1));
					prelist.set(j+1, temp);
				}
			}
		}
	}
	public void addToVMclist(int vmnum,int pmnumid,int cost){
		vmcList[vclistindex]= new CopyVM(vmnum,pmnumid,cost);
		hvmcList[vclistindex]= new CopyVM(vmnum,pmnumid,cost);
		vclistindex++;
	}
	public void add(int id, int t){
		tree[arrayindex]=new Node(id,t);
		fftree[arrayindex]=new Node(id,t);
		itree[arrayindex]=new Node(id,t);
		htree[arrayindex]=new Node(id,t);
		arrayindex++;
	}
	public void addV(int i, int rand){
		vms[arryvindex]=new VirtualM(i,rand);
		arryvindex++;
	}
	public void addO(int rand){
		orgnl[arryoindex]=rand;
		arryoindex++;
	}
	public void addO(int rand,int randsize){
		orgnl[arryoindex]=rand;
		orgnlsize[arryoindex]=randsize;
		arryoindex++;
	}
	 public static int distance(Node[] tree,Node node1,Node node2,int k){
         int type1 = node1.gettype();
         int type2 = node2.gettype();
         int id1 = node1.getid();
         int id2 = node2.getid();
         if(type1==0 && type2==0){ //if both are core switches
             if(node1.getedge(0)==node2.getedge(0))//distance between any two core switches is either 2 or 4
                 return 2;
             else
                 return 4; //if core switches dont have edges to the same agg switch, dist is always 4.
             
         }
         if(type1==1 && type2==1){//if both are aggr switches
             if (node1.getpodid()==node2.getpodid())
             return 2; //if two aggr switches are in the same pod dist is always 2.
             for(int i=0;i<k;i++){
                 if (id1==id2+i*(k/2)||id2==id1+i*(k/2)) //when they have the same core switches have edges to them 
                 return 2;
             }
             return 4; //otherwise, the distance will always be 4.
         }
         if(type1==2 && type2==2){//if they are both edge switches
             if(node1.getpodid()==node2.getpodid())//if they are in the same POD dist is always 2
                 return 2;
             return 4; //otherwise distance is always 4
         }
         if(type1==3 && type2==3){// if they are both PMs
             if(node1.getedgeid()==node2.getedgeid())//if both PMs belong to the same edge switch dist is always 2
                 return 2;
             if(tree[node1.getedgeid()].getpodid()==tree[node2.getedgeid()].getpodid())//if both Pms belong to the same POD and not the same edge, dist is always 4
                 return 4;
             return 6; //otherwise dist is always 6.
         }
         if((type1==0 && type2==1)||(type1==1 && type2==0)){//if one is core and the other is aggr
             if (type1==0){
                 for(int i=0;i<k;i++){
                     if (node1.getedge(i)==id2) //if the core switch has an edge directly to the agg switch dist is 1
                     return 1;
                 }
             }
             if(type2==0){
                 for(int i=0;i<k;i++){
                     if (node2.getedge(i)==id1)
                     return 1;
                 }
             }
             return 3; //otherwise distance is always 3
         }
         if((type1==0 && type2== 2)||(type1==2 &&type2==0)){//if one is core and the other is edge
             return 2; //distance between any core switch and any edge switch is always 2
         }
         if((type1==0 && type2==3)||(type1==3 && type2==0)){//if one is a core and the other is a PM
             return 3; //distance between any core switch and any PM is always 3
         }
         if((type1==1 && type2==2)||(type1==2 && type2==1)){//if one is a agg switch and the other is a edge switch
             if(node1.getpodid()==node2.getpodid())
                 return 1; //if they are in the same POD dist is always 1
             return 3; //otherwise distance is always 3.
         }
         if((type1==1 && type2==3)||(type1==3 && type2==1)){//if one is an aggr and the other is a PM
             if(type1==1){
                 if(node1.getpodid()==tree[node2.getedgeid()].getpodid())
                 return 2; //if the POD of the aggr switch is the POD of the edge switch that the PM belongs to dist is 2
             }
             if(type2==1){
                 if(node2.getpodid()==tree[node1.getedgeid()].getpodid())
                 return 2; //if the POD of the aggr switch is the POD of the edge switch that the PM belongs to dist is 2
             }
             return 4; //otherwise distance is always 4
         }
         if((type1==2 && type2==3)||(type1==3 && type2==2)){//if one is an edge switch //and other is pm
             if (type1==2){
                 if (id1==node2.getedgeid())
                     return 1; //if the PM belongs to the edge switch
                 if (node1.getpodid()==tree[node2.getedgeid()].getpodid())
                     return 3; //if the POD of the edge switch is the POD of the edge switch that the PM belongs to is the same dist is 3
                 return 5; //otherwise distance is always 5;
             }
             if(type2==2){
                 if(id2==node1.getedgeid())
                     return 1;
                 if (node2.getpodid()==tree[node1.getedgeid()].getpodid())
                     return 3;
                 return 5;
             }
         }
         return 0; //this should never happen.
}
	public void maxFlow() {
		//System.out.println("test");
		//first initialize and populate capacity[][]
		ArrayList<ArrayList<Boolean>> compatible =  new ArrayList<ArrayList<Boolean>> (p);
		for(int i=0;i<p;i++){
			compatible.add(i, new ArrayList <Boolean>(pmcount));
			for(int j=0;j<pmcount;j++){
				compatible.get(i).add(j,false);
			}
		}
		//System.out.println("test");
		makeCompatible(compatible);
		int n = 1+p+(k*k*k/4)+1;
		int sourceind=0;
		int firstvm=1;
		int cfirstpm=p+1;
		int destinode=n-1;//cfirstpm+(k*k*k/4);
		ArrayList<ArrayList<Integer>> capacity = new ArrayList<ArrayList<Integer>> (n);
		for(int i=0;i<n;i++){
			capacity.add(i,new ArrayList<Integer>(n));
			for(int j=0;j<n;j++){
				capacity.get(i).add(j, 0);
			}
		}
		int V=n;
		Graph g = new Graph(V);
		
		//System.out.println("test");
		//first make edges from source node to original VMs
		//capacity.set(0, new ArrayList<Integer>(n));
		
		for (int i=1;i<=p;i++){
			g.addEdge(0, i, r-1);
		}
		//next make edge with capacity 1 from VMs  to every compatible PM
		for (int i=1;i<cfirstpm;i++){
			for(int j=0;j<pmcount;j++){
				// if VMi is compatable with PMj, capacity.get(i).set(j,1)
				if(compatible.get(i-1).get(j)){
					//capacity.get(i).set(j+cfirstpm,1);
					g.addEdge(i, j+cfirstpm, 1);
				}
				else{
					//capacity.get(i).set(j+cfirstpm,0);
					
				}
					
			}
			
		}
		
		//next make edge from PM to destination
		int pmin=0;
		for(int i=cfirstpm;i<destinode;i++){
			//capacity.get(i).set(destinode,fftree[pmin+firstpm].getesc() );
			g.addEdge(i,destinode,fftree[pmin+firstpm].getesc());
			pmin++;
		}
		
		//MaxFlow mflow = new MaxFlow(capacity,k,p,r);
		
		int maxflow = g.getMaxFlow(0,destinode); //mflow.max_flow();
		int flowreq = (r-1)*p;
		System.out.println("Maximum flow, required flow: "+maxflow+", "+flowreq);
		if(flowreq>maxflow){
			System.out.println("FT-VMP not feasible: Max flow is: "+maxflow);
			System.out.println("Flow required: "+flowreq);
			//System.exit(0);
		}
		else {
			System.out.println("FTVMP is feasible");
		}
	}
	public void maxFlow112020() {
		//System.out.println("test");
		//first initialize and populate capacity[][]
		ArrayList<ArrayList<Boolean>> compatible =  new ArrayList<ArrayList<Boolean>> (p);
		for(int i=0;i<p;i++){
			compatible.add(i, new ArrayList <Boolean>(pmcount));
			for(int j=0;j<pmcount;j++){
				compatible.get(i).add(j,false);
			}
		}
		//System.out.println("test");
		makeCompatible112020(compatible);
		int n = 1+p+(k*k*k/4)+1;
		int sourceind=0;
		int firstvm=1;
		int cfirstpm=p+1;
		int destinode=n-1;//cfirstpm+(k*k*k/4);
		ArrayList<ArrayList<Integer>> capacity = new ArrayList<ArrayList<Integer>> (n);
		for(int i=0;i<n;i++){
			capacity.add(i,new ArrayList<Integer>(n));
			for(int j=0;j<n;j++){
				capacity.get(i).add(j, 0);
			}
		}
		int V=n;
		Graph g = new Graph(V);
		
		//System.out.println("test");
		//first make edges from source node to original VMs
		//capacity.set(0, new ArrayList<Integer>(n));
		
		for (int i=1;i<=p;i++){
			g.addEdge(0, i, r-1);
		}
		//next make edge with capacity 1 from VMs  to every compatible PM
		for (int i=1;i<cfirstpm;i++){
			for(int j=0;j<pmcount;j++){
				// if VMi is compatable with PMj, capacity.get(i).set(j,1)
				if(compatible.get(i-1).get(j)){
					//capacity.get(i).set(j+cfirstpm,1);
					g.addEdge(i, j+cfirstpm, 1);
				}
				else{
					//capacity.get(i).set(j+cfirstpm,0);
					
				}
					
			}
			
		}
		
		//next make edge from PM to destination
		int pmin=0;
		for(int i=cfirstpm;i<destinode;i++){
			//capacity.get(i).set(destinode,fftree[pmin+firstpm].getesc() );
			g.addEdge(i,destinode,fftree[pmin+firstpm].getesc());
			pmin++;
		}
		
		//MaxFlow mflow = new MaxFlow(capacity,k,p,r);
		
		int maxflow = g.getMaxFlow(0,destinode); //mflow.max_flow();
		int flowreq = (r-1)*p;
		System.out.println("Maximum flow, required flow: "+maxflow+", "+flowreq);
		if(flowreq>maxflow){
			System.out.println("FT-VMP not feasible: Max flow is: "+maxflow);
			System.out.println("Flow required: "+flowreq);
			//System.exit(0);
		}
		else {
			System.out.println("FTVMP is feasible");
		}
	}
	public void maxFlow112020B() {
		//System.out.println("test");
		//first initialize and populate capacity[][]
		ArrayList<ArrayList<Boolean>> compatible =  new ArrayList<ArrayList<Boolean>> (p);
		for(int i=0;i<p;i++){
			compatible.add(i, new ArrayList <Boolean>(pmcount));
			for(int j=0;j<pmcount;j++){
				compatible.get(i).add(j,false);
			}
		}
		//System.out.println("test");
		makeCompatible112020(compatible);
		int n = 1+p+(k*k*k/4)+1;
		int sourceind=0;
		int firstvm=1;
		int cfirstpm=p+1;
		int destinode=n-1;//cfirstpm+(k*k*k/4);
		ArrayList<ArrayList<Integer>> capacity = new ArrayList<ArrayList<Integer>> (n);
		for(int i=0;i<n;i++){
			capacity.add(i,new ArrayList<Integer>(n));
			for(int j=0;j<n;j++){
				capacity.get(i).add(j, 0);
			}
		}
		int V=n;
		Graph g = new Graph(V);
		
		//System.out.println("test");
		//first make edges from source node to original VMs
		//capacity.set(0, new ArrayList<Integer>(n));
		
		for (int i=1;i<=p;i++){
			g.addEdge(0, i, rvm[i-1]);
		}
		//next make edge with capacity 1 from VMs  to every compatible PM
		for (int i=1;i<cfirstpm;i++){
			for(int j=0;j<pmcount;j++){
				// if VMi is compatable with PMj, capacity.get(i).set(j,1)
				if(compatible.get(i-1).get(j)){
					//capacity.get(i).set(j+cfirstpm,1);
					g.addEdge(i, j+cfirstpm, 1);
				}
				else{
					//capacity.get(i).set(j+cfirstpm,0);
					
				}
					
			}
			
		}
		
		//next make edge from PM to destination
		int pmin=0;
		for(int i=cfirstpm;i<destinode;i++){
			//capacity.get(i).set(destinode,fftree[pmin+firstpm].getesc() );
			g.addEdge(i,destinode,fftree[pmin+firstpm].getesc());
			pmin++;
		}
		
		//MaxFlow mflow = new MaxFlow(capacity,k,p,r);
		
		int maxflow = g.getMaxFlow(0,destinode); //mflow.max_flow();
		int flowreq=0;// = (r-1)*p;
		for (int i =0;i<p;i++){
			flowreq+=rvm[i];
		}
		regflo=flowreq;
		//flowreq=flowreq*p;
		
		System.out.println("Maximum flow, required flow: "+maxflow+", "+flowreq);
		if(flowreq>maxflow){
			System.out.println("FT-VMP not feasible: Max flow is: "+maxflow);
			System.out.println("Flow required: "+flowreq);
			//System.exit(0);
		}
		else {
			System.out.println("FTVMP is feasible");
		}
	}
	 void makeCompatible112020(ArrayList <ArrayList<Boolean>> compatible ) {
		
		 for (int i=0;i<p;i++){
			 for(int j=0;j<pmcount;j++){
				 int thisorgnl =orgnl[i];
				 if(j+firstpm==thisorgnl){
					 compatible.get(i).set(j, false);
					 continue;
				 }
				 compatible.get(i).set(j,true);
				 
			 }
			 
		 }
		 for(int i=0;i<pmcount;i++){
			 for(int j=0;j<tree[i+firstpm].vmlist2.size();j++){
				for(int l=0;l<tree[i+firstpm].vmlist2.get(j).randIncs.size();l++){
					compatible.get(tree[i+firstpm].vmlist2.get(j).vmid).set(tree[i+firstpm].vmlist2.get(j).randIncs.get(l)-firstpm,false);
				}
			 }
		 }
		
		//return compatible;
	}
	 void makeCompatible(ArrayList <ArrayList<Boolean>> compatible ) {
			
		 for (int i=0;i<p;i++){
			 for(int j=0;j<pmcount;j++){
				 int thisorgnl =orgnl[i];
				 if(j+firstpm==thisorgnl){
					 compatible.get(i).set(j, false);
					 continue;
				 }
				 compatible.get(i).set(j,true);
				 
			 }
			 
		 }
		
		//return compatible;
	}
	public void readLP() {
		// TODO Auto-generated method stub
		//read from lpfile to calculate cost from lp result.
		Scanner scan = new Scanner(System.in);
		System.out.println("When lpfile result is ready enter anything: ");
		String useless=scan.next();
		
		File out = new File("lpresult99.txt");
		int totalcost=0;
		try{
			BufferedReader br = new BufferedReader(new FileReader(out));
			String line;
			//int totalcost=0;
			while((line=br.readLine())!=null){
				if(line.charAt(0)=='V'||line.charAt(0)==';'||line.charAt(0)=='Y'){
					continue;
				}
				//int c=1;
				if(line.charAt(0)=='X'){
					int c=1;
					
						int vmid=-99;
						char fstchar=line.charAt(c);
						c++;
						char secchar=line.charAt(c);
						c++;
						char thirchar=line.charAt(c);
						c++;
						char fthchar=line.charAt(c);
						c++;
						if(line.charAt(2)=='_'){
							String temp=""+fstchar;
							vmid=Integer.parseInt(temp);
							c=3;
						}
						if(line.charAt(3)=='_'){
							String temp=""+fstchar;
							vmid=Integer.parseInt(temp);
							c=4;
						}
						
						while(line.charAt(c)!='_'){
							c++;
						}
						c++;
						ArrayList<String> chars= new ArrayList<String>(0);
						//array has digit of the original VM
						while(line.charAt(c)!=';'){
							chars.add(""+line.charAt(c));
							c++;
						}
						String placPM="";
						for(int i=0;i<chars.size();i++){
							placPM+=""+chars.get(i);
						}
						int pm=Integer.parseInt(placPM);
						c++;
						if(line.charAt(c)=='1'){
							totalcost+=distance(tree,tree[orgnl[vmid]], tree[pm+firstpm], k);
							
						}
						
					}
				
			}
		}catch (Exception e){System.out.println("");}
		System.out.println("total replication cost of ILP Placement: "+ totalcost);
		
	
	}
	public void placeVNFs() {
		//randomly place VNFs in any Node that is not a PM
		Random rand = new Random();
		for(int i=0; i<VNFs;i++){
			

			boolean flag;
			int randomNum;
			do{ 
				flag=false;
				randomNum = rand.nextInt(((firstpm-1) - 0) + 1) + 0;
		    
		    for(int j=0;j<VNFlist.size();j++){
		    	if (VNFlist.get(j).locationid==randomNum){
		    		flag=true;
		    	}
		    }
			}while(flag==true);
			VNFlist.add(new VNF(randomNum,i));
			
		}
		
		
	}
	public void placeVMpairs() {
		//place VMs in fat tree randomly by pairs.
		//place each vm in pair in random PM
		//vms in the same pair shair an id
		 Random rand = new Random();
		for(int i=0;i<vmpairs;i++){
			//first vm
			boolean flag;
			int firstrand=0;
			int secondrand=0;
			do{
				flag=false;
			firstrand = rand.nextInt((lastpm - firstpm) + 1) + firstpm;
			
			if (tree[firstrand].esc <1){
				flag=true;
			}
			}while(flag==true);
			tree[firstrand].esc--;
			do{
				flag=false;
				secondrand = rand.nextInt((lastpm - firstpm) + 1) + firstpm;
				
				if (tree[secondrand].esc <1){
					flag=true;
				}
			}while(flag==true);
			tree[secondrand].esc--;
			
			//PairList.add(new VMpair(i,firstrand,secondrand,))
			
			int vnfrand=0;
			do{
				vnfrand = rand.nextInt(((VNFs-1) - 0) + 1) + 0;
				flag=false;
				if(VNFlist.get(vnfrand).VMpairs.size()>=(vmpairs/VNFs+((vmpairs%VNFs==0?0:1)))){
					flag=true;
				}
			}while(flag==true);
			VNFlist.get(vnfrand).VMpairs.add(i);
			VNFlist.get(vnfrand).VMpairs.trimToSize();
			int randcomrate = rand.nextInt(((trafficup) - trafficlow) + 1) + trafficlow;
			Pairlist.add(new VMpair(i,firstrand,secondrand,vnfrand,randcomrate));
		}
		/*//output communication cost from random placement 
		int totcommuncost=0;
		for(int i=0;i<Pairlist.size();i++){
			int temppm1;
			int temppm2;
			int vnfloc;
			int tempcomcost;
			tempcomcost=Pairlist.get(i).comrate;
			temppm1=Pairlist.get(i).vm1pm;
			temppm2=Pairlist.get(i).vm2pm;
			vnfloc = VNFlist.get(Pairlist.get(i).VNFid).locationid;
			totcommuncost += distance(tree, tree[temppm1], tree[vnfloc], k)*tempcomcost;
			totcommuncost += distance(tree,tree[vnfloc],tree[temppm2],k)*tempcomcost;
		}
		System.out.println("total communication cost before MCF: "+totcommuncost);
		
			*/
		
		
	}
	public void VNFMCFOutput() {
		int arccount= vmpairs;
		arccount+= vmpairs*VNFs;
		arccount+=VNFs;
		int nodecount=vmpairs+VNFs+2;
		String firstline = "p min "+nodecount+" "+arccount+"\n";
		String secline = "c min-cost flow problem with "+ nodecount+" nodes and "+arccount+ " arcs \n";
		String thirdline = "n 1 "+ vmpairs+" \n";
		String fourthln = "c supply of "+ vmpairs+ " at node 1 \n";
		String fifthln = "n "+nodecount+" "+(vmpairs*-1)+" \n";
		String sixthln = "c demand of "+(vmpairs*-1) +" at node "+nodecount+" \n";
		String sevln = "c arc list follows \n";
		String eithln = "c arc has <tail> <head> <capacity l.b.> <capacity u.b> <cost> \n";
        String firstlns=firstline+secline+thirdline+fourthln+fifthln+sixthln+sevln+eithln;
        //System.out.print(firstlns);
        StringBuilder supplyarcs = new StringBuilder(""); 
        
		int countnode = 2;
		for(int i=0;i<vmpairs;i++){
			supplyarcs.append("a 1 "+(countnode)+" 0 "+1+" 0 \n");
			countnode++;
		}
		int firstvm = countnode;
		int startv = 2;
		StringBuilder vmarcs = new StringBuilder("");
		for(int i=0;i<vmpairs;i++){
			countnode=firstvm;
			for(int j=0;j<VNFs;j++){
				//edges from vm pair to vnf
				int comcost = Pairlist.get(i).comrate;
				int tempdist;
				if(Pairlist.get(i).VNFid==j){
					tempdist = 0;
				}
				else{
					tempdist = distance(tree,tree[VNFlist.get(Pairlist.get(i).VNFid).locationid],tree[VNFlist.get(j).locationid],k)*mu;
				}
				
				vmarcs.append("a "+(i+2)+" "+countnode+ " 0 "+1+" "+
				(tempdist+(distance(tree,tree[Pairlist.get(i).vm1pm],tree[VNFlist.get(j).locationid],k)*comcost)+(distance(tree,tree[Pairlist.get(i).vm2pm],tree[VNFlist.get(j).locationid],k)*comcost))+"\n");
				countnode++;
			}
			
		}
		//now we make arcs from the VNFs to the sink
		StringBuilder pmarcs = new StringBuilder("");
		for (int i=0;i<VNFs;i++){
			pmarcs.append("a " +(i+firstvm)+" "+(countnode)+" 0 "+(vmpairs/VNFs+((vmpairs%VNFs==0?0:1)))+" "+"0 \n");
		}
		String output = firstlns+supplyarcs+vmarcs+pmarcs;
		;
		try{
            //this is where i am writing my file to.
            File file = new File("vnf_placement.inp");
            file.createNewFile();
            FileWriter fw = new FileWriter("vnf_placement.inp");
            fw.write(output);
            fw.flush();
            fw.close();
        }catch (Exception e){System.out.println("failed 1");}
		System.out.println("vnf_placement.inp created");
	}
	public void readMCFVNFOutput() {
		
		File out = new File("output2.txt");
        try{
        	BufferedReader br = new BufferedReader(new FileReader(out));
            String line;
            //int c=0;
            while((line=br.readLine())!=null){
            	//process here
            	if(line.charAt(0)=='c'||line.charAt(0)=='s')
                    continue;//ignoring comment lines
               int c=1;//index of string after first character
               String firstnum=""; //first number of the line
               while(line.charAt(c)==' '){
                  c++;
               }
               while(line.charAt(c)!=' '){
                   firstnum+=line.charAt(c);//reading the first number of the line
                   c++;
                }
               if(Integer.parseInt(firstnum)==1)
               continue;//also ignoring the supply arc lines
               int vmpairnum=Integer.parseInt(firstnum)-2; //id of original vm
               String secnum="";
               while(line.charAt(c)==' '){
                   c++;
                }
                while(line.charAt(c)!=' '){
                    secnum+=line.charAt(c);//reading the second number of the line
                    c++;
                 }
                int vnfid = Integer.parseInt(secnum)-(vmpairs+2); //id of the vnf
                if(Integer.parseInt(secnum)==(2+vmpairs+VNFs)){
                	continue;
                	//ignore sink node flow
                }
                String thirdnum="";
                while(line.charAt(c)==' '){
                    c++;
                 }
                 //while(line.charAt(c)!=' '){
                     thirdnum+=line.charAt(c);//reading the third number of the line
                     c++;
                  //}
                  int ifplaced = Integer.parseInt(thirdnum);
                  
                  if(ifplaced >0){
                	  //if number is greater than 0, than thagt means that the vm pair is assigned this VNF
                	  Pairlist.get(vmpairnum).mcfVNFid=vnfid;
                	  VNFlist.get(vnfid).mcfVMpairs.add(vmpairnum);
                	  VNFlist.get(vnfid).mcfpairs++;
                  }
                
            }
        }catch (Exception e){System.out.println("");}
        //now print comcost after mcf
      /*  int totcommuncost=0;
		for(int i=0;i<Pairlist.size();i++){
			int temppm1;
			int temppm2;
			int vnfloc;
			int tempcomcost;
			int migrcost;
			//add the 
			tempcomcost=Pairlist.get(i).comrate;
			temppm1=Pairlist.get(i).vm1pm;
			temppm2=Pairlist.get(i).vm2pm;
			vnfloc = VNFlist.get(Pairlist.get(i).mcfVNFid).locationid;
			totcommuncost += distance(tree, tree[temppm1], tree[vnfloc], k)*tempcomcost;
			totcommuncost += distance(tree,tree[vnfloc],tree[temppm2],k)*tempcomcost;
			if(vnfloc==VNFlist.get(Pairlist.get(i).VNFid).locationid){
				
			}
			else{
				totcommuncost+=distance(tree,tree[vnfloc],tree[VNFlist.get(Pairlist.get(i).VNFid).locationid],k)*mu;//this is where you multiply by mu
			}
		}
		System.out.println("total communication cost after MCF: "+totcommuncost);
            */
            
		
	}
	public void placeOriginalVms112020Bsize() {
		//this method is modified to give Sizes that aren't 1.
		// TODO Auto-generated method stub
		Random rand= new Random();
        int randnum;
        int randsize;
        boolean flag;
        //VMs are just integer numbers here.
        vms=new VirtualM[p];
        orgnl=new Integer[p];
        orgnlsize = new Integer[p];
        //ArrayList<VirtualM> vms = new ArrayList(); //list of original VMs 
        //ArrayList<Integer> orgnl= new ArrayList<Integer>(0);
        
        //ArrayList<VirtualM> vms0 = new ArrayList(); //list of original VMs 
        //ArrayList<Integer> orgnl0= new ArrayList<Integer>(0);
        
        
        for (int i=0; i<p;i++){
            do {
               randnum = rand.nextInt((lastpm -firstpm)+1)+ firstpm;
               randsize = rand.nextInt((vmsizeu -vmsizel)+1)+ vmsizel;
           
               
               flag=tree[randnum].addVM112020size(i,m,randsize); //placing p VMs randomly into PMs
               fftree[randnum].addVM112020size(i,m,randsize);
               itree[randnum].addVM112020size(i,m,randsize);
               htree[randnum].addVM112020size(i,m,randsize);
               if(flag){
               flag=tree[randnum].addVM(i,m,randsize); //placing p VMs randomly into PMs
               fftree[randnum].addVM(i,m,randsize);
               itree[randnum].addVM(i,m,randsize);
               htree[randnum].addVM(i,m,randsize);
               }
               
             /*  tree[randnum].vmcount-=1;
               fftree[randnum].vmcount-=1;
               itree[randnum].vmcount-=1;
               htree[randnum].vmcount-=1;*/
               //if(i==0){
            	   //tree[randnum].addVM(1,m);
               //}
               //flag=tree0.get(randnum).addVM(i,m);
               if(flag){
            	   itree[randnum].addOrigi();
            	   tree[randnum].addOrigi();
            	   fftree[randnum].addOrigi();
            	   htree[randnum].addOrigi();
            	   //tree0.get(randnum).addOrigi();
            	   //if(i==0){
            		//   tree[randnum].addOrigi();
            		   
            	 //  }
               }
                                 //m is passed to make sure the a vm is not placed at a pm at capacity
               //System.out.println("Thanks "+i);
            } while(!flag);
            addV(i,randnum);
            //vms0.add(new VirtualM(i,randnum));
            turnOnPM(randnum);
            //onPMs.add(randnum);
            tree[randnum].decesc(randsize);
            tree[randnum].setactive();
            tree[randnum].setissource();
            
            itree[randnum].decesc(randsize);
            itree[randnum].setactive();
            itree[randnum].setissource();
            
            htree[randnum].decesc(randsize);
            htree[randnum].setactive();
            htree[randnum].setissource();
            
            fftree[randnum].decesc(randsize);
            fftree[randnum].setactive();
            fftree[randnum].setissource();
            
            addO(randnum,randsize);
            //if(i==0){
            	//addV(1,randnum);
            	//tree[randnum].decesc();
            	//addO(randnum);
            //}
            
            //tree0.get(randnum).decesc();
            //tree0.get(randnum).setactive();
            //tree0.get(randnum).setissource();
            //orgnl0.add(randnum);
            System.out.println("VM " + i+" Has been placed in PM with id of: "+ randnum);
            //if(i==0)
            	//i++;
        }
        //now add rand comps
        rn = new Integer[p];
        for (int i=0;i<p;i++){
        	rn[i]=rand.nextInt((3 - 1) + 1) + 1;
        }
        
        for(int i=0;i<orgnl.length;i++){
        	for(int g=0; g<rn[i];g++){
        		//System.out.println("yetg");
        	//}
        	int randomNum;
        	boolean iflag=false;
        	do{
        		
        		 randomNum = rand.nextInt((lastpm - firstpm) + 1) + firstpm;
        		 //System.out.println(randomNum+" "+i+"iorgnl is "+orgnl[i]);
        		 //if randomnum is already incomp or if it the orgnl reroll.
        		 iflag=false;
        		 for(int j=0 ;j<tree[orgnl[i]].vmlist2.size();j++){
        			 if(tree[orgnl[i]].vmlist2.get(j).vmid!=i){
        				 System.out.println("vmid "+ tree[orgnl[i]].vmlist2.get(j).vmid);
        				 continue;
        			 }
        			 for(int l=0;l<tree[orgnl[i]].vmlist2.get(j).randIncs.size();l++){
        				 if (randomNum==tree[orgnl[i]].vmlist2.get(j).randIncs.get(l)){
        					// System.out.println("   "+tree[orgnl[i]].vmlist2.get(j).randIncs.get(l));
        					 iflag=true;
        				 }
        			 }
        		 }
        		 if(iflag){
        			 randomNum=orgnl[i];
        		 }
            }while(randomNum==orgnl[i]);
        		for(int j=0 ;j<tree[orgnl[i]].vmlist2.size();j++){		
        			if(tree[orgnl[i]].vmlist2.get(j).vmid!=i){
        				continue;
        			}
        			tree[orgnl[i]].vmlist2.get(j).randIncs.add(randomNum);
        			htree[orgnl[i]].vmlist2.get(j).randIncs.add(randomNum);
        			fftree[orgnl[i]].vmlist2.get(j).randIncs.add(randomNum);
        			itree[orgnl[i]].vmlist2.get(j).randIncs.add(randomNum);
        			
        		}
        		
        	}
        }
        System.out.println("Incompats:");
        for (int i=0; i<orgnl.length;i++){
        	System.out.println(""+i+":");
        	for(int j=0;j< tree[orgnl[i]].vmlist2.size();j++){
        		if(tree[orgnl[i]].vmlist2.get(j).vmid!=i){
        			continue;
        		}
        		for(int l=0; l<tree[orgnl[i]].vmlist2.get(j).randIncs.size();l++){
        			System.out.println("	"+tree[orgnl[i]].vmlist2.get(j).randIncs.get(l));
        		}
        		
        	}
        }
        //now make random number [5,10] of r bins for each original VM
        //save random number in array for each original VM, indexed by VM ID (much like the orgnl[] array)
        //whenever r (number of VM copy for a VM) is used in a for loop, use rvm[]
        //CHANGE THIS LATER SUCH THAT EACH VM WILL HAVE THE SAME AMOUNT OF COPIES
        	//THIS IS SO THAT WE CAN SEE HOW MUCH IS THE LIMIT
        rvm= new Integer[p];
       // System.out.println("thanks");
        
        

        for (int i=0;i<p;i++){
        	rvm[i]=rand.nextInt((3 - 3) + 1) + 3;
        }
        int sizeofarrs=0;
		for (int i=0;i<p;i++){
			sizeofarrs+=rvm[i];
		}
		vmcList = new CopyVM[sizeofarrs];
		hvmcList = new CopyVM[sizeofarrs];
        	
		
	}
	public void maxFlow112020Bsize() {
		
		// TODO Auto-generated method stub
		//System.out.println("test");
				//first initialize and populate capacity[][]
				ArrayList<ArrayList<Boolean>> compatible =  new ArrayList<ArrayList<Boolean>> (p);
				for(int i=0;i<p;i++){
					compatible.add(i, new ArrayList <Boolean>(pmcount));
					for(int j=0;j<pmcount;j++){
						compatible.get(i).add(j,false);
					}
				}
				//System.out.println("test");
				makeCompatible112020(compatible);
				int n = 1+p+(k*k*k/4)+1;
				int sourceind=0;
				int firstvm=1;
				int cfirstpm=p+1;
				int destinode=n-1;//cfirstpm+(k*k*k/4);
				ArrayList<ArrayList<Integer>> capacity = new ArrayList<ArrayList<Integer>> (n);
				for(int i=0;i<n;i++){
					capacity.add(i,new ArrayList<Integer>(n));
					for(int j=0;j<n;j++){
						capacity.get(i).add(j, 0);
					}
				}
				int V=n;
				Graph g = new Graph(V);
				
				//System.out.println("test");
				//first make edges from source node to original VMs
				//capacity.set(0, new ArrayList<Integer>(n));
				
				for (int i=1;i<=p;i++){
					g.addEdge(0, i, rvm[i-1]);
				}
				//next make edge with capacity 1 from VMs  to every compatible PM
				for (int i=1;i<cfirstpm;i++){
					for(int j=0;j<pmcount;j++){
						// if VMi is compatable with PMj, capacity.get(i).set(j,1)
						if(compatible.get(i-1).get(j)){
							//capacity.get(i).set(j+cfirstpm,1);
							//g.addEdge(i, j+cfirstpm, 1);
							g.addEdge(i, j+cfirstpm, orgnlsize[i-1]);
						}
						else{
							//capacity.get(i).set(j+cfirstpm,0);
							
						}
							
					}
					
				}
				
				//next make edge from PM to destination
				int pmin=0;
				for(int i=cfirstpm;i<destinode;i++){
					//capacity.get(i).set(destinode,fftree[pmin+firstpm].getesc() );
					g.addEdge(i,destinode,fftree[pmin+firstpm].getesc());
					pmin++;
				}
				
				//MaxFlow mflow = new MaxFlow(capacity,k,p,r);
				System.out.println("thanks1");
				int maxflow = g.getMaxFlow(0,destinode); //mflow.max_flow();
				System.out.println("thanksa");
				int flowreq=0;// = (r-1)*p;
				for (int i =0;i<p;i++){
					flowreq+=rvm[i];
				}
				regflo=flowreq;
				//flowreq=flowreq*p;
				
				System.out.println("Maximum flow, required flow: "+maxflow+", "+flowreq);
				if(flowreq>maxflow){
					System.out.println("FT-VMP not feasible: Max flow is: "+maxflow);
					System.out.println("Flow required: "+flowreq);
					//System.exit(0);
				}
				else {
					System.out.println("FTVMP is feasible");
				}
		
	}
	public void vmbMigration() {
		//this method uses vm based algorithm to migrate vm pairs to different VNFs
		ArrayList<VMpair> Pairlistcopy = new ArrayList<VMpair>(0);
		for(int i=0;i<Pairlist.size();i++){
			Pairlistcopy.add(Pairlist.get(i));
		}
		//then we sort the copy of the list in non-ascending order of comm freq.
		sortPairs(Pairlistcopy);
	for(int i=0; i<Pairlistcopy.size();i++){
		int oldVNFid = Pairlistcopy.get(i).VNFid;
		int tempvmpid = Pairlistcopy.get(i).pairid;
		//distance from vm1 to VNF
		int oldcost=distance(tree, tree[Pairlistcopy.get(i).vm1pm], tree[VNFlist.get(Pairlistcopy.get(i).VNFid).locationid], k);
		//distance from VNF to vm2
		oldcost+=distance(tree, tree[VNFlist.get(Pairlistcopy.get(i).VNFid).locationid], tree[Pairlistcopy.get(i).vm2pm], k);
		//multiplied by communication frequency is the communication cost of this vm pair
		oldcost= oldcost*Pairlistcopy.get(i).comrate;
		int bestbenefit=0;
		int bestVNF = oldVNFid;
		//keep an array of benefits so that if the VNF with the highest benefit is full, the algo will try to place in the next one
		ArrayList<PairBenefit> benefitarr = new ArrayList<PairBenefit>(0);
		benefitarr.add(new PairBenefit(bestbenefit,bestVNF));
		for(int j=0;j<VNFlist.size();j++){
			if(VNFlist.get(j).vmbpairs>=(vmpairs/VNFs+((vmpairs%VNFs==0?0:1)))){
				continue;
				//if this statement is true this VNF is at capacity so we will go to the next VNF
			}
			int benefit;
			/*if(VNFlist.get(j).vnfid==oldVNFid){
				continue;
				//if this is the old vnf, continue;
			}*/
			//distance from vm1 to VNF j
			int tempcost = distance(tree, tree[Pairlistcopy.get(i).vm1pm], tree[VNFlist.get(j).locationid], k)*Pairlistcopy.get(i).comrate;
			//distance from VNF j to vm2
			tempcost+=distance(tree, tree[VNFlist.get(j).locationid], tree[Pairlistcopy.get(i).vm2pm], k)*Pairlistcopy.get(i).comrate;
			
			//multiplied by com freq. is the communication cost of this vm pair
			//tempcost = tempcost*Pairlistcopy.get(i).comrate;
			//plus the cost from the old vnf to the new vnf multiplied by coeff. mu
			if(VNFlist.get(j).vnfid!=oldVNFid){
				tempcost+=distance(tree,tree[VNFlist.get(Pairlistcopy.get(i).VNFid).locationid],tree[VNFlist.get(j).locationid],k)*mu;
				//if this is the old vnf, continue;
			}
			benefit=oldcost-tempcost;
			//tempcost+=distance(tree,tree[VNFlist.get(Pairlistcopy.get(i).VNFid).locationid],tree[VNFlist.get(j).locationid],k)*mu;
			benefitarr.add(new PairBenefit(benefit,VNFlist.get(j).vnfid));
		/*	if (benefit>bestbenefit){
				bestVNF = VNFlist.get(j).vnfid;
				//System.out.println("here look: "+VNFlist.get(j).vnfid+" "+j);
				bestbenefit = benefit;
			}*/
			
		}
		//if(bestVNF !=oldVNFid ){
			//if the VM pair is to be migrated...
		//now we sort the list of VNF and benefits by descending order 
		sortList(benefitarr);
		/*System.out.println("test there first");
		for(int l=0;l<benefitarr.size();l++) {
			System.out.println(benefitarr.get(l).benefit);
		}*/
		int l=0;
		
		while(VNFlist.get(benefitarr.get(l).vmpid).vmbpairs>=(vmpairs/VNFs+((vmpairs%VNFs==0?0:1)))) {
			l++;
		}
		/*System.out.println("test there");
		System.out.println(benefitarr.get(l).benefit);*/
			//Pairlistcopy.get(i).vmbVNFid=bestVNF;
			Pairlistcopy.get(i).vmbVNFid=benefitarr.get(l).vmpid;
			//VNFlist.get(bestVNF).vmbVMpairs.add(tempvmpid);
			VNFlist.get(benefitarr.get(l).vmpid).vmbVMpairs.add(tempvmpid);
			VNFlist.get(benefitarr.get(l).vmpid).vmbpairs++;
			/*if(VNFlist.get(bestVNF).vmbpairs>101) {
				System.out.println(VNFlist.get(bestVNF).vmbpairs);
				System.out.println("error found");
				System.out.println((vmpairs/VNFs+((vmpairs%VNFs==0?0:1))));
			System.exit(0);
			}*/
			//we have both old and new vnfids saved in the vm pair object so we dont need to update anything else in terms of calculating total cost later.
			
		//}
		//if the vm pair isnt migrated, still make the vmbbnf id the old one;
		/*if(bestVNF==oldVNFid){
			Pairlistcopy
		}*/
	}
		
	}
	private void sortPairs(ArrayList<VMpair> pairlistcopy) {
		//simple bubble sort to sort the pairs in this list by non ascending order of communcation frequency
		int n = pairlistcopy.size() ;
        for (int i = 0; i < n-1; i++) 
            for (int j = 0; j < n-i-1; j++) 
                if (pairlistcopy.get(j).comrate < pairlistcopy.get(j+1).comrate) 
                { 
                    // swap arr[j+1] and arr[j] 
                    VMpair temp = pairlistcopy.get(j); 
                    pairlistcopy.set(j, pairlistcopy.get(j+1));
                   // arr[j] = arr[j+1]; 
                    pairlistcopy.set(j+1, temp);
                   // arr[j+1] = temp; 
                } 
		
	}
	public void mbbMigration() {
		//the first loop will be used to iteraate by MBs
		//make data structure so that each VNF can have a list of VM pairs it can be assigned to
		//so that after the j loop ends, it will be sorted by descending order of benefit
		//after, assign the first kappa VM pairs to the middleboxes
		// TODO Auto-generated method stub
		
		//ArrayList<PairBenefit> templist = new ArrayList<PairBenefit>(0);
		int totbenefit=0;
		for(int i=0;i<VNFlist.size();i++) {
			ArrayList<PairBenefit> templist = new ArrayList<PairBenefit>(0);
			for(int j=0;j<Pairlist.size();j++) {
				if(Pairlist.get(j).mbbmigrated) {
					continue;
					
				}
				//what if vnfj is the vm pairs original VNF
				//distance from vm1 to VNF i
				int originlcost;
				originlcost=distance(tree, tree[Pairlist.get(j).vm1pm],tree[VNFlist.get(Pairlist.get(j).VNFid).locationid],k);
				originlcost+=distance(tree,tree[VNFlist.get(Pairlist.get(j).VNFid).locationid], tree[Pairlist.get(j).vm2pm],k);
				originlcost= originlcost*Pairlist.get(j).comrate;
				
				int tempcost = distance(tree, tree[Pairlist.get(j).vm1pm], tree[VNFlist.get(i).locationid], k);
				//distance from VNF i to vm2
				tempcost+=distance(tree, tree[VNFlist.get(i).locationid], tree[Pairlist.get(j).vm2pm], k);
				//multiplied by com freq. is the communication cost of this vm pair
				tempcost = tempcost*Pairlist.get(j).comrate;
				//plus the cost from the old vnf to the new vnf multiplied by coeff. mu
				if(Pairlist.get(j).VNFid!=VNFlist.get(i).vnfid) {
					tempcost+=distance(tree,tree[VNFlist.get(Pairlist.get(j).VNFid).locationid],tree[VNFlist.get(i).locationid],k)*mu;
					//if true then this VM pair has already been assigned a VNF
				}     
				int benefit = originlcost-tempcost;
				templist.add(new PairBenefit(benefit,Pairlist.get(j).pairid));
			}
			templist.trimToSize();
			//now assign the most beneficial vm pairs to the middleboxes
			//sort the temp list by descending benefit
			//TODO
			sortList(templist);
			System.out.println("test here");
			for(int j=0;j<templist.size();j++) {
				System.out.println(templist.get(j).benefit);
				
			}
			/*System.out.println("test here2: "+templist.size());*/
			
			for(int j=0;j<(vmpairs/VNFs+((vmpairs%VNFs==0?0:1)))&&j<templist.size();j++) {
				
				VNFlist.get(i).mbbpairs++;
				/*if(VNFlist.get(i).mbbpairs>(vmpairs/VNFs+((vmpairs%VNFs==0?0:1)))) {
					System.out.println("nother error");
					System.out.println(VNFlist.get(i).mbbpairs);
					System.out.println(vmpairs/VNFs+((vmpairs%VNFs==0?0:1)));
					System.exit(0);
				}*/
				VNFlist.get(i).mbbVMpairs.add(templist.get(j).vmpid);
				Pairlist.get(templist.get(j).vmpid).mbbVNFid=VNFlist.get(i).vnfid;
				Pairlist.get(templist.get(j).vmpid).mbbmigrated=true;
				totbenefit+=templist.get(j).benefit;
				
			
			}
			
		}
		//System.out.println("debug: benefit: "+totbenefit);
		
	}
	private void sortList(ArrayList<PairBenefit> pairlistcopy) {
		//simple bubble sort to sort the pairs in this list by descending order of benefit
		int n = pairlistcopy.size() ;
        for (int i = 0; i < n-1; i++) 
            for (int j = 0; j < n-i-1; j++) 
                if (pairlistcopy.get(j).benefit < pairlistcopy.get(j+1).benefit) 
                { 
                    // swap arr[j+1] and arr[j] 
                    PairBenefit temp = pairlistcopy.get(j); 
                    pairlistcopy.set(j, pairlistcopy.get(j+1));
                   // arr[j] = arr[j+1]; 
                    pairlistcopy.set(j+1, temp);
                   // arr[j+1] = temp; 
                } 
		
	}
	public void printMigrationOutputs() {
		// TODO Auto-generated method stub
		//output communication cost from random placement 
		int totcommuncost=0;
		int placed=0;
		for(int i=0;i<Pairlist.size();i++){
			int temppm1;
			int temppm2;
			int vnfloc;
			int tempcomcost;
			tempcomcost=Pairlist.get(i).comrate;
			temppm1=Pairlist.get(i).vm1pm;
			temppm2=Pairlist.get(i).vm2pm;
			vnfloc = VNFlist.get(Pairlist.get(i).VNFid).locationid;
			totcommuncost += distance(tree, tree[temppm1], tree[vnfloc], k)*tempcomcost;
			totcommuncost += distance(tree,tree[vnfloc],tree[temppm2],k)*tempcomcost;
			
		}
		System.out.println("total communication cost after random placement: "+totcommuncost);
		
		for(int i=0;i<VNFlist.size();i++) {
			VNFlist.get(i).VMpairs.trimToSize();
			placed+=VNFlist.get(i).VMpairs.size();
		}
		System.out.println("placed: "+placed);
		  //now print comcost after mcf
        totcommuncost=0;
        placed=0;
		for(int i=0;i<Pairlist.size();i++){
			int temppm1;
			int temppm2;
			int vnfloc;
			int tempcomcost;
			int migrcost;
			//add the 
			tempcomcost=Pairlist.get(i).comrate;
			temppm1=Pairlist.get(i).vm1pm;
			temppm2=Pairlist.get(i).vm2pm;
			vnfloc = VNFlist.get(Pairlist.get(i).mcfVNFid).locationid;
			totcommuncost += distance(tree, tree[temppm1], tree[vnfloc], k)*tempcomcost;
			totcommuncost += distance(tree,tree[vnfloc],tree[temppm2],k)*tempcomcost;
			if(vnfloc==VNFlist.get(Pairlist.get(i).VNFid).locationid){
				
			}
			else{
				totcommuncost+=distance(tree,tree[vnfloc],tree[VNFlist.get(Pairlist.get(i).VNFid).locationid],k)*mu;//this is where you multiply by mu
			}
		}
		System.out.println("total communication cost after MCF: "+totcommuncost);
		int tempplac=0;
		for(int i=0;i<VNFlist.size();i++) {
			placed+=VNFlist.get(i).mcfVMpairs.size();
			tempplac+=VNFlist.get(i).mcfpairs;
		}
		System.out.println("placed: "+placed+" "+tempplac);
		
		//now print comcost after vm based (algo 1)
        totcommuncost=0;
        placed=0;
		for(int i=0;i<Pairlist.size();i++){
			int temppm1;
			int temppm2;
			int vnfloc;
			int tempcomcost;
			int migrcost;
			//add the 
			tempcomcost=Pairlist.get(i).comrate;
			temppm1=Pairlist.get(i).vm1pm;
			temppm2=Pairlist.get(i).vm2pm;
			vnfloc = VNFlist.get(Pairlist.get(i).vmbVNFid).locationid;
			totcommuncost += distance(tree, tree[temppm1], tree[vnfloc], k)*tempcomcost;
			totcommuncost += distance(tree,tree[vnfloc],tree[temppm2],k)*tempcomcost;
			if(vnfloc==VNFlist.get(Pairlist.get(i).VNFid).locationid){
				
			}
			else{
				totcommuncost+=distance(tree,tree[vnfloc],tree[VNFlist.get(Pairlist.get(i).VNFid).locationid],k)*mu;//this is where you multiply by mu
			}
		}
		System.out.println("total communication cost after VM Based Migration: "+totcommuncost);
		tempplac=0;
		for(int i=0;i<VNFlist.size();i++) {
			placed+=VNFlist.get(i).vmbVMpairs.size();
			tempplac+=VNFlist.get(i).vmbpairs;
		}
		System.out.println("placed: "+placed+" "+tempplac);
		
		//now print comcost after mb based (algo 2)
        totcommuncost=0;
        placed=0;
		for(int i=0;i<Pairlist.size();i++){
			int temppm1;
			int temppm2;
			int vnfloc;
			int tempcomcost;
			int migrcost;
			//add the 
			tempcomcost=Pairlist.get(i).comrate;
			temppm1=Pairlist.get(i).vm1pm;
			temppm2=Pairlist.get(i).vm2pm;
			vnfloc = VNFlist.get(Pairlist.get(i).mbbVNFid).locationid;
			totcommuncost += distance(tree, tree[temppm1], tree[vnfloc], k)*tempcomcost;
			totcommuncost += distance(tree,tree[vnfloc],tree[temppm2],k)*tempcomcost;
			if(vnfloc==VNFlist.get(Pairlist.get(i).VNFid).locationid){
				
			}
			else{
				totcommuncost+=distance(tree,tree[vnfloc],tree[VNFlist.get(Pairlist.get(i).VNFid).locationid],k)*mu;//this is where you multiply by mu
			}
		}
		System.out.println("total communication cost after MB Based Migration: "+totcommuncost);
		tempplac=0;
		for(int i=0;i<VNFlist.size();i++) {
			placed+=VNFlist.get(i).mbbVMpairs.size();
			tempplac+=VNFlist.get(i).vmbpairs;
		}
		System.out.println("placed: "+placed+" "+tempplac);
	}
}
