import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.*;
public class RandMTree implements Serializable{
	int arrayindex=0;
	int arryvindex=0;
	int arryoindex=0;
	int vclistindex=0;
	int k=1;
	int p;//number of original VMs
	int m;//inital storage capacity
	int r;//How many total copies of each VM
	int firstpm;
	int lastpm;
	int pmcount;
	VirtualM vms[];
	Integer orgnl[];
	Integer randMs[];
	Node tree[];
	Node fftree[];
	Node itree[];
	CopyVM vmcList[];
	ArrayList <Integer> onPMs = new ArrayList <Integer>(0);	
	
	public RandMTree(){
		
	}
	public void buildTree(){
		int treesize = (k*k/4)+(k/2*k)+(k/2*k)+ (k*k*k/4);
		tree= new Node[treesize];//tree for servcons
		fftree=new Node[treesize];//tree for FF
		itree=new Node[treesize];//tree for improvedservcons
		randMs=new Integer[pmcount];
		
		Random rando = new Random();
		int totspace=0;
		while(totspace<p*r){
			totspace=0;
			for(int i=0;i<pmcount;i++){
				int randnum = rando.nextInt((m -1)+1)+ 1;
				randMs[i]=randnum;
				totspace+=randnum;
			}
		}
		for (int i=0;i<pmcount;i++){
			System.out.println("PM with id "+(i+firstpm)+" has a capacity of: "+randMs[i]);
		}
		vmcList = new CopyVM[p*(r-1)];
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
                //tree0.add(new Node(id,3));
                //tree0.get(id).setedgeid(count);
                
                int set = Math.min(p,randMs[id-firstpm]);//the smaller between number of original
                						//and the capacity m is the ESC.
                tree[id].setesc(set);//setting the initial effective storage capacity p
                tree[count].addpmsofedge(tree[id]); //adding PM to edge
                
                itree[id].setesc(set);//setting the initial effective storage capacity p
                itree[count].addpmsofedge(tree[id]); //adding PM to edge
                
                fftree[id].setesc(set);
                fftree[count].addpmsofedge(fftree[id]);
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
        int podcount = eid;
        for(int i=0;i<k;i++){
            plist.add(new POD(i,k));
            iplist.add(new POD(i,k));
            plist0.add(new POD(i,k));
            podcount=eid;//index of before first aggregate switch
            podcount += i*(k/2);//index before the first aggregate switch of podi
            for (int j =0; j<k/2;j++){
                podcount++;
                //System.out.println(i+" "+j);
                plist.get(i).addNode(tree[podcount]); //adding aggr switch to podi
                iplist.get(i).addNode(itree[podcount]);
                plist0.get(i).addNode(fftree[podcount]);
                //plist0.get(i).addNode(tree0.get(podcount));
            }
            podcount=eid + i *(k/2);//set index back to index before first aggr switch of pod i
            podcount += (k*k)/2; //set index to index before first edge switch
            for(int j=0;j<k/2;j++){
                
                podcount++;
                plist.get(i).addNode(tree[podcount]); //adding edge switch to podi
                iplist.get(i).addNode(itree[podcount]);
                plist0.get(i).addNode(fftree[podcount]);
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
			tree[j+firstpm].addVM(i, randMs[j]);
			tree[j+firstpm].decesc();
            tree[j+firstpm].setactive();
            tree[j+firstpm].setissource();
            tree[j+firstpm].addOrigi();
            addV(i,j+firstpm);
			
            fftree[j+firstpm].addVM(i,randMs[j]);
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
           
               flag=tree[randnum].addVM(i,randMs[randnum-firstpm]); //placing p VMs randomly into PMs
               fftree[randnum].addVM(i,randMs[randnum-firstpm]);
               itree[randnum].addVM(i,randMs[randnum-firstpm]);
               //if(i==0){
            	   //tree[randnum].addVM(1,m);
               //}
               //flag=tree0.get(randnum).addVM(i,m);
               if(flag){
            	   itree[randnum].addOrigi();
            	   tree[randnum].addOrigi();
            	   fftree[randnum].addOrigi();
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
                       if(tree[h+firstpm].getvmcount()>=randMs[h]){
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
                       boolean gflag =tree[h+firstpm].addVMc(movedvm,h+firstpm,thiscost,randMs[h]);
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
                       if(itree[h+firstpm].getvmcount()>=randMs[h]){
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
                		boolean gflagt =itree[newpm].addVMc(movmid, newpm, ncost, randMs[newpm]);
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
                        if(itree[h+firstpm].getvmcount()>=randMs[h]){
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
                       if(itree[h+firstpm].getvmcount()>=randMs[h]){
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
                		boolean gflagt =itree[newpm].addVMc(movmid, newpm, ncost, randMs[newpm-firstpm]);
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
            System.out.println("capacity: "+ randMs[i-firstpm]);
            System.out.println(itree[i].vmcount);
        }  
        
        System.out.println("Number of IPMs for improvedServCons: "+ IPM0);
        System.out.println("Number of active PMS for improvedServCons: "+actpm);
	}
	public void bubbleSort(){
		//sorting algorithm before FF
		//sorting vmcList by cost
		int n= vmcList.length;
		for(int i=0;i<n-1;i++){
			for(int j=0;j<n-i-1;j++){
				if(vmcList[j].getcost()> vmcList[j+1].getcost()){
					CopyVM temp = vmcList[j];
					vmcList[j]=vmcList[j+1];
					vmcList[j+1]=temp;
				}
			}
		}
		
	}
	public void addToVMclist(int vmnum,int pmnumid,int cost){
		vmcList[vclistindex]= new CopyVM(vmnum,pmnumid,cost);
		vclistindex++;
	}
	public void add(int id, int t){
		tree[arrayindex]=new Node(id,t);
		fftree[arrayindex]=new Node(id,t);
		itree[arrayindex]=new Node(id,t);
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
}
