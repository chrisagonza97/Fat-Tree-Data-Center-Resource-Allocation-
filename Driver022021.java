import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
public class Driver022021 implements Serializable {
	public static void main (String args[]){
		FTreeFF tree = new FTreeFF();
		getInput(tree);//getting user input
		//tree.buildTree();//building the tree
		tree.buildTreeB();
		//checkIfPossible(tree); this method was previously used instead of
		//the push relabel max flow algorithm
		 //this is where feasibility is checked.
		//tree.placeOriginalVms();
	//	tree.placeOriginalVms112020();
		//tree.placeOriginalVms112020B();//old
		tree.placeOriginalVms112020Bsize();
		//tree.maxFlow()4;
		//tree.maxFlow112020();
		//tree.maxFlow112020B();
		tree.maxFlow112020Bsize();
		//tree.placeOriginalVms(1);
	//	tree.createMCFFile();
		//tree.createMCFFile112020();
		//tree.createMCFFile112020B
		tree.createMCFFile112020Bsize();
		waitForCs2();
		
		tree.readFromFile();
		
		//tree.createIncGraph();//gupta et al heuristic
		//tree.colorGraph();
		//tree.allocVms();
		
		tree.createIncGraph112020();
		tree.colorGraph112020();
		//tree.allocVms112020();
		//tree.allocVms();
		tree.allocVms112020size();
		
		//tree.createLPFile();
		//tree.createLPFile112020();
		//tree.createLPFile112020B();
		
	//	tree.suitablePMs();
		tree.suitablePMs112020();
		tree.bubbleSort();
		//tree.ffServerConsAlgo();
		tree.ffServerConsAlgo112020();
		//tree.serverConsAlgo();
		tree.serverConsAlgo112020();
		//tree.improvedServCons();
		//tree.improvedServConsv2();
		tree.improvedServConsv2112020();
		
		//tree.readLP();
		
		
	}
	public static void getInput(FTreeFF tree){
		 
	        
	        Scanner scan = new Scanner(System.in);
	        
	        while(tree.k%2!=0){
	        System.out.println("Enter an even 'k' number for fat tree data center");
	        tree.k = scan.nextInt();
	        }   
	        System.out.println("Enter p number of original VMs that are randomly placed");
	        tree.p =scan.nextInt();
	        System.out.println("Enter m initial storage capacity of each pm");
	        tree.m = scan.nextInt();
	        System.out.println("Enter lower bound for VM size");
	        tree.vmsizel = scan.nextInt();
	        System.out.println("Enter upper bound for VM size");
	        tree.vmsizeu = scan.nextInt();
	        System.out.println("Enter number of R replica copies for each VM");
	        tree.r = scan.nextInt()+1;
	        System.out.println("Enter number of random incomptatabilities for each VM");
	        tree.randInc = scan.nextInt();
	        /*if(tree.p>(tree.k*tree.k*tree.k/4)){
	            System.out.println("VMs can't be placed if P is greater than the number of PMs.");
	            System.exit(0);
	        }*/
	        
	        
	        tree.firstpm = (tree.k*tree.k)/4 + (tree.k*tree.k)/2 + (tree.k*tree.k)/2; //index of the first PM
	        tree.lastpm = tree.firstpm + (tree.k*tree.k*tree.k)/4 -1;        //index of last PM.
	        tree.pmcount = tree.lastpm-tree.firstpm+1;//this is how many physical machines there are
	        
	}
	public static void waitForCs2(){
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter anything after CS2 output has been saved to 'output.txt'");
		scan.hasNextInt();
	}
	public static void checkIfPossible(FTreeFF tree) {
		int esc = Math.min(tree.m,tree.p);
		if(esc*tree.pmcount<tree.r*tree.p) {
        	System.out.println("total amount of VMs + copies is greater than total storage capacity; cannot place Vms");
        	System.exit(0);
        }
	}
	
}
