import java.util.Scanner;


public class MCFVNF {

	public static void main(String[] args) {
		FTreeFF tree = new FTreeFF();
		getInput(tree);
		tree.buildTreeB();
		tree.placeVNFs();
		tree.placeVMpairs();
		
		tree.vmbMigration();//method for migration for benefit algorithm 1(VMBASED)
							//method calculates total cost in the fat tree
		
		tree.mbbMigration();//method for migration for benefit algorithm 2 (MBBASED)
		
		tree.VNFMCFOutput();
		waitForCs2();
		tree.readMCFVNFOutput();//this method reads mcf output and calculates total cost based on placement
		
		tree.printMigrationOutputs();
		

	}
	public static void getInput(FTreeFF tree){
		 
        
        Scanner scan = new Scanner(System.in);
        
        while(tree.k%2!=0){
        System.out.println("Enter an even 'k' number for fat tree data center");
        tree.k = scan.nextInt();
        }   
        System.out.println("Enter p number of pairs that are randomly placed");
        int temp=scan.nextInt();
        tree.p =temp*2;
        tree.vmpairs=temp;
        /*System.out.println("Enter number of VM pairs to be placed");
        */
        System.out.println("Enter m initial storage capacity of each pm");
        tree.m = scan.nextInt();
        
        System.out.println("Enter number of VNFs");
        tree.VNFs = scan.nextInt();
        System.out.println("Enter lower bound for traffic rate");
        tree.trafficlow = scan.nextInt();
        System.out.println("Enter upper bound for traffic rate");
        tree.trafficup = scan.nextInt();
        System.out.println("Enter migration coefficient mu: ");
        tree.mu = scan.nextInt();
        
        /*System.out.println("Enter number of R replica copies for each VM");
        tree.r = scan.nextInt()+1;
        */
        /*System.out.println("Enter number of random incomptatabilities for each VM");
        tree.randInc = scan.nextInt();*/
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
		System.out.println("Enter anything after CS2 output has been saved to 'output2.txt'");
		scan.hasNextInt();
	}
}
