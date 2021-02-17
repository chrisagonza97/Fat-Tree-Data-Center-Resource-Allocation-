
/**
 * Write a description of class FatTree here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
import java.util.*;
import java.io.FileWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
public class FatTree
{
    public static void main (String args[]){
        
        int k=1;
        ArrayList<Node> tree = new ArrayList<Node>(); //array list for all switches and PMs
        Scanner scan = new Scanner(System.in);
        
        while(k%2!=0){
        System.out.println("Enter an even 'k' number for fat tree data center");
        k = scan.nextInt();
        }   
        System.out.println("Enter p number of original VMs that are randomly placed");
        final int p =scan.nextInt();
        System.out.println("Enter m initial storage capacity of each pm");
        int m = scan.nextInt();
        System.out.println("Enter number of R copies of each VM");
        int r = scan.nextInt();
        if(p>(k*k*k/4)){
            System.out.println("VMs can't be placed if P is greater than the number of PMs.");
            System.exit(0);
        }
        int id=0;
        //int inc=0;
        int agg=k; //index of first agg switch;
        for (int i=0;i<(k*k)/4;i++){  //create (k^2)/4 core switches
            tree.add(new Node(id,0));
            //for (int j=0;j<k;j++){
                //if(i>0 && i%(k/2)==0) // k/2 groups of k/2 core switches have edges to the same agg switches
                //agg++;
                //tree.get(id).addedge(tree.get(agg+j*(k/2)));
                
            //}
            id++;
            
        }
        int eid=0;
        for (int j=0;j<(k*k)/2;j++){ 
                
                tree.add(new Node(id,1));
                if(j==0)
                eid = id-1;//index before first aggr switch.
                id++;
                //create (k*k)/2 aggregation switch
        } 
        
        
        for(int x=0;x<(k*k)/2;x++){
                
                tree.add(new Node(id,2));
                id++; //create (k^2)/2 edge switches
            }
        int y;
        int count = ((k*k)/4+(k*k)/2)-1; //this should be the index before the first edge switch
                                     //which is the sum of the index of core and aggr switches.
        for(y=0;y<(k*k*k)/4;y++){
                
                if (y%(k/2)==0) //each edge switch gets k/2 PMs
                count++;
                
                tree.add(new Node(id,3));
                tree.get(id).setedgeid(count); //giving the id of the edge the PM belongs to
                
                tree.get(id).setesc(p);//setting the initial effective storage capacity p
                tree.get(count).addpmsofedge(tree.get(id)); //adding PM to edge
                id++; //create k^3/4 physical machines, k/2 machines for each edge switch
                }
         for (int i=0;i<(k*k)/4;i++){  
            //tree.add(new Node(id,0));
            if(i>0 && i%(k/2)==0) // k/2 groups of k/2 core switches have edges to the same agg switches
                agg++;
            for (int j=0;j<k;j++){
                //if(i>0 && i%(k/2)==0) // k/2 groups of k/2 core switches have edges to the same agg switches
                //agg++;
                tree.get(i).addedge(tree.get(agg+j*(k/2)));
                
            }
            //id++;
            
        }       
                
        
            
            
        
        System.out.println(tree);
        System.out.println();
        //now we create the PODs
        ArrayList<POD> plist = new ArrayList<POD>();
        int podcount = eid;
        for(int i=0;i<k;i++){
            plist.add(new POD(i,k));
            podcount=eid;//index of before first aggregate switch
            podcount += i*(k/2);//index before the first aggregate switch of podi
            for (int j =0; j<k/2;j++){
                podcount++;
                //System.out.println(i+" "+j);
                plist.get(i).addNode(tree.get(podcount)); //adding aggr switch to podi
            }
            podcount=eid + i *(k/2);//set index back to index before first aggr switch of pod i
            podcount += (k*k)/2; //set index to index before first edge switch
            for(int j=0;j<k/2;j++){
                
                podcount++;
                plist.get(i).addNode(tree.get(podcount)); //adding edge switch to podi
            }
        }
        //now we print the IDs of the PODS
        for (int i=0;i<k;i++){
            System.out.println(plist.get(i).toString());
        }
        System.out.println();
        int node1;
        int node2;
        /*System.out.println("Will output distance between two nodes");
        System.out.println("Enter the id of Node one");
        node1= scan.nextInt();
        System.out.println("Enter the id of Node two");
        node2 = scan.nextInt();
        int d = distance(tree,tree.get(node1),tree.get(node2),k);
        System.out.println("The distance between "+ tree.get(node1).toString() +" and " +tree.get(node2).toString()+ "is: " +d);
        */
        //first create random number to represent ids of PMs that have original VMs.
        int firstpm = (k*k)/4 + (k*k)/2 + (k*k)/2; //index of the first PM
        //System.out.println("index of first pm is " + firstpm);
        int lastpm = firstpm + (k*k*k)/4 -1;        //index of last PM.
        //System.out.println("index of last pm is " + lastpm);
        Random rand= new Random();
        int randnum;
        boolean flag;
        //VMs are just integer numbers here.
        ArrayList<VirtualM> vms = new ArrayList(); //list of original VMs 
                ArrayList<Integer> orgnl= new ArrayList<Integer>(0);
        for (int i=0; i<p;i++){
            do {
               randnum = rand.nextInt((lastpm -firstpm)+1)+ firstpm;
           
               flag=tree.get(randnum).addVM(i,m); //placing p VMs randomly into PMs
                                 //m is passed to make sure the a vm is not placed at a pm at capacity
               if(flag){
            	   tree.get(randnum).addOrigi();
               }
            } while(!flag);
            vms.add(new VirtualM(i,randnum));
            tree.get(randnum).decesc();
            tree.get(randnum).setactive();
            tree.get(randnum).setissource();
            orgnl.add(randnum);
            System.out.println("VM " + i+" Has been placed in PM with id of: "+ randnum);
        }
        
        int pmcount = lastpm-firstpm+1;//this is how many physical machines there are
        
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
        String supplyarcs="";
        int countnode=2;
        for (int i=0;i<p;i++){
            supplyarcs+="a 1 "+(countnode)+" 0 "+(r-1)+" 0 \n";
            countnode++;
        }
        int firstvm=countnode;
        
        int startv=2;
        //System.out.print(supplyarcs);
        String vmarcs ="";
        
        //make the arcs from the location of the orginal vms to every physical machine
        //except the one the VM is originally stored in.
        for (int i=0;i<p;i++){
            
            countnode=firstvm;
            for(int j=0;j<pmcount;j++){
                if (vms.get(i).getpmid()==j+firstpm){
                    countnode++;
                    continue;
                }
                vmarcs+= "a "+(i+2)+" "+(countnode)+" 0 "+1+" "+
                distance(tree,tree.get(vms.get(i).getpmid()),tree.get(j+firstpm),k)+"\n";
                countnode++;    
            }
        }
        
        //System.out.print(vmarcs);
        //now we make arcs from every physical machine to the destination node
        String pmarcs="";
        for(int i=0; i<pmcount;i++){
            pmarcs+="a "+(i+firstvm)+" "+(countnode)+" 0 "+tree.get(i+firstpm).getesc()+" "+"0 \n";
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
        if(p*r>m*pmcount){//if the amount of copies times the amount of original vms
                            //is more than m times the amount of pms, replication not possible
            System.out.println("replication of every VM not possible.");
        }
        System.out.println("mcf_replication.inp has been written to in project root file directory");
        //now we have to read from the file created from the cs2 program and 
        //determine where each VM copy was placed.
        //first create file object of cs2 output.
        String foo="";
        System.out.println("Enter anything after CS2 has been run and outpute file has been made.");
        foo += scan.next();
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
                    vms.get(vmnum).addcopy(pmnumid);
                    tree.get(pmnumid).setactive();
                    int cost = distance(tree,tree.get(vms.get(vmnum).getpmid()),tree.get(pmnumid),k);
                    tree.get(pmnumid).addVMc(vmnum,pmnumid,cost,m);
                    System.out.println("VM " +vmnum+ " copy has been placed in PM with id "+
                    pmnumid);
                }
               
            }
        
        }catch (Exception e){System.out.println("");}
        
        System.out.println("Placement of VMS before consolidation:");
        for (int i=firstpm;i<=lastpm;i++){
            String print = tree.get(i).printVms();
            if(print==null)
            continue;
            System.out.println(print);
        }
        //making input file for lpsolve.
        //first we start with the minimize objective
        String lpfile="minimize ";
        for (int i=0;i<pmcount;i++){
        	lpfile+="Y"+i;
        	if(i!=pmcount-1){
        		lpfile+="+";
        	}
        }
        
        //Algorithm for server consolidation.
        int IPM=0;
        for(int i =1; i<=m;i++){//line 1 of algorithm
            for (int j=0; j<pmcount;j++){//for each CPM with i copies
                boolean aflag = false;
                if (!(tree.get(j+firstpm).getactive()&& tree.get(j+firstpm).getissource()==false)){
                    continue;//if this PM is not a CPM continue
                }
                if(tree.get(j+firstpm).getreplicount()!=i){
                    continue; //if this CPM does not have i replicas continue;
                }
                for (int l=0;l<tree.get(j+firstpm).getvmclength();l++){//line 4
                    for (int h =0; h<pmcount;h++){
                       int thiscost= tree.get(j+firstpm).getvmcost(l);//cost of replicating this
                       int temp=distance (tree,tree.get(h+firstpm),tree.get(tree.get(j+firstpm).getcpm(l)),k);
                       if (!(thiscost==temp)){
                           //aflag=false;
                        continue; //if they do not have the same cost, it is not a TPM, continue
                       }
                       //System.out.println("0");
                       if(tree.get(h+firstpm).getactive()==false){
                           continue; //if PM is not active, it is not a TPM
                        }
                      /* if(tree.get(h+firstpm).getissource()==true){
                           //aflag=false;
                        continue;// if PM is a source pm, it is not a TPM
                       }*/
                       //System.out.println("1");
                       if(tree.get(h+firstpm).getvmcount()>=m){
                           //aflag=false;
                           continue;//if PM is at capacity, it is not a TPM
                       }
                       //System.out.println("2");
                       //check if PM has another replica of this VM replica
                       if(tree.get(h+firstpm).checkVm(tree.get(j+firstpm).getvmid(l))){
                           //aflag=false;
                           continue;//if PM has a copy of this VM it is not a TPM
                       }
                       //System.out.println("3");
                       //if loop hasn't continued, PM is a TPM
                       //move vm to PM, remove this vm copy
                       int movedvm = tree.get(j+firstpm).getvmid(l);
                       aflag=true;
                       System.out.println("VM "+tree.get(j+firstpm).getvmid(l)+ " from PM " +(j+firstpm)+" migrated to PM with id " + (h+firstpm));
                       tree.get(h+firstpm).addVMc(movedvm,h+firstpm,thiscost,m);
                       tree.get(j+firstpm).removeVMc(tree.get(j+firstpm).getvmid(l));
                       l--;
                       //System.out.println("VM "+tree.get(j+firstpm).getvmid(l)+ "from PM " +(j+firstpm)+"migrated to PM with id " + (h+firstpm));
                       
                       break;
                       
                    }
                    if(aflag==false){
                        break;
                    }
                    
                }
                if (aflag==true){
                        IPM++;
                }
                
            }
        }
        
      System.out.println("Placement of VMS after consolidation:");
        for (int i=firstpm;i<=lastpm;i++){
            String print = tree.get(i).printVms();
            if(print==null)
            continue;
            System.out.println(print);
        }  
        
        System.out.println("number of IPMs: "+ IPM);
    }
    
    public static int distance(ArrayList<Node> tree,Node node1,Node node2,int k){
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
                if(tree.get(node1.getedgeid()).getpodid()==tree.get(node2.getedgeid()).getpodid())//if both Pms belong to the same POD and not the same edge, dist is always 4
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
                    if(node1.getpodid()==tree.get(node2.getedgeid()).getpodid())
                    return 2; //if the POD of the aggr switch is the POD of the edge switch that the PM belongs to dist is 2
                }
                if(type2==1){
                    if(node2.getpodid()==tree.get(node1.getedgeid()).getpodid())
                    return 2; //if the POD of the aggr switch is the POD of the edge switch that the PM belongs to dist is 2
                }
                return 4; //otherwise distance is always 4
            }
            if((type1==2 && type2==3)||(type1==3 && type2==2)){//if one is an edge switch //and other is pm
                if (type1==2){
                    if (id1==node2.getedgeid())
                        return 1; //if the PM belongs to the edge switch
                    if (node1.getpodid()==tree.get(node2.getedgeid()).getpodid())
                        return 3; //if the POD of the edge switch is the POD of the edge switch that the PM belongs to is the same dist is 3
                    return 5; //otherwise distance is always 5;
                }
                if(type2==2){
                    if(id2==node1.getedgeid())
                        return 1;
                    if (node2.getpodid()==tree.get(node1.getedgeid()).getpodid())
                        return 3;
                    return 5;
                }
            }
            return 0; //this should never happen.
}
}
