package edu.sjsu.cmpe283.v3m;

import java.util.HashMap;
import java.util.Map;
import com.vmware.vim25.mo.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class V3MServer {

	private
		String ipAddr;
		String userName;
		String passWord;
		String myVM;
		String folder [];
		ServiceInstance servInstance;
		Folder rootFolder;
		int numOfHost;
		int numOfVms;
		int isFolder;
		int isExsi;
		int pingInterval;
		int snapshotInterval;
		Map<String, String> hostDetails;
		ManagedEntity[] hostSystems;
		ManagedEntity[] vms;
		ManagedEntity[] childEntity;
		VirtualMachine[] vmArr = new VirtualMachine[10];
		V3MVMFinder vmFinder;
				
	//Method to load the login parameters for the ESXi
	void loadParameter(String[] info){
		ipAddr ="https://" + info[0] +"/sdk";
		userName = info[1];
		passWord = info[2];
		myVM = info[3];
		numOfVms = 0;
		folder = new String[10];
		isExsi = 1;
		
		if(myVM.contains("/")){
			folder = myVM.split("/");
			isFolder = 1;
			if(folder.length > 0)
				isExsi = 0;
		}
		else
		{
			isFolder = 0;
			isExsi = 9999;
		}
	}
	
	//This method will be used to connect to the ESXi
	void connectToESXI(){
		
		try {
			servInstance = new ServiceInstance(new URL(ipAddr),userName,passWord, true);
		} catch (RemoteException | MalformedURLException e) {
			e.printStackTrace();
		}
		
		//Getting the root folder
		rootFolder = servInstance.getRootFolder();
		try {
			childEntity  = rootFolder.getChildEntity();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	//This method will create all the Managed Entities object
	void getManagedEntities(){
		try {
			hostSystems = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
			vms = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	//This method will collect all the system related Details print it out
	void collectAndHostSystemDetails(){
		int hostCounter = 0;
		
		hostDetails = new HashMap<String, String>();
	
		for(ManagedEntity host: hostSystems)
        {	
			System.out.println("Host["+hostCounter+"]:");
        	
			HostSystem hostSystem = (HostSystem) host;
        	System.out.println("Name = "+hostSystem.getName());
        	System.out.println("Product FullName = "+hostSystem.getConfig().product.fullName + "\n");
        	
        	//Update the Map for the future use
        	hostDetails.put(hostSystem.toString().substring(11, 18),hostSystem.getName().toString());
        	
        	hostCounter += 1;
        }		
		numOfHost = hostCounter;
	}
	
	public void threadHandler()
	{
		pingInterval = 1;		
		vmFinder = new V3MVMFinder(servInstance, isFolder, isExsi, folder);
		 
		//System.out.println("Starting V3M PingThread ...");
		new PingThread(servInstance, pingInterval, vmFinder).start();	
		
		//System.out.println("Starting V3M StatCollector Thread ...");
		new V3MStatsCollector(servInstance, pingInterval, vmFinder).start();
				
		//System.out.println("Starting V3M Snapshot ..."); 
		new SnapshotHandler(servInstance, pingInterval, vmFinder).start();
	}
	
	//This is the main method
	public static void main(String[] args) {
		
		V3MServer lab = null;
		
		//Checking for the All the required arguments
		if(args.length < 4){
			System.out.println("Usage:");
			System.out.println("<ESXi IP address> <userName> <passWord> <vmName or FullPath>");
			
			//Exiting from the service
			System.exit(1);
		}
		else
		{
			//Loading the ESXi login parameters
			lab = new V3MServer();
			lab.loadParameter(args);
		}		
		
		//Connecting to the ESXi server
		lab.connectToESXI();
		
		//Create all the Managed Entity Types
		lab.getManagedEntities();
		
		//Collect all the host systems
		lab.collectAndHostSystemDetails();
		
		//Starting all the thread
		lab.threadHandler();
	}
}