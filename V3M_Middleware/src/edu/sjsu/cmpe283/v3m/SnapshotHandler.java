package edu.sjsu.cmpe283.v3m;

import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;


public class SnapshotHandler extends Thread {
	ServiceInstance servInstance = null;
	int duration;
	VirtualMachine[] vmArr = new VirtualMachine[10];
	int arrSize = 0;
	int numOfVMs;
	int isEsxi;
	int isFolder;
	V3MVMFinder vmFinder;
	
	/*
	 * Overloaded Constructors
	 */
	SnapshotHandler(ServiceInstance servInstance, int time) 
	{
		this.servInstance = servInstance;
		this.duration = time;
	}
	
	SnapshotHandler(ServiceInstance servInstance, int time, V3MVMFinder vmFinderParam) 
	{
		this.servInstance = servInstance;
		this.duration = time;
		this.vmFinder = vmFinderParam;
	}
	
	SnapshotHandler(ServiceInstance servInstance, int time, int isFolderParam, int inEsxiParam) 
	{
		this.servInstance = servInstance;
		this.isEsxi = inEsxiParam;
		this.servInstance = servInstance;
		this.duration = time;
	}
	
	SnapshotHandler(ServiceInstance servInstance) 
	{
		this.servInstance = servInstance;
	}
	
	public void run() {
		
		/*This will be running all the time and keep taking snapshot in 
		a configured amount of time duration*/
		while (true) 
		{
			try 
			{
				createVMSnapshot();
				System.out.println("Snashot Therad Sleeping for " + duration +" minutes");
				Thread.sleep(1000 * 60 * duration); 
				
			}catch (Exception e){
				System.out.println(e.getStackTrace());
			}
		}
	}
	
	public void createVMSnapshot()
	{
		try
		{		   
		   String desc ;
		   String snapshotname;
		   
		   //Collecting the data
		   vmFinder.readPath();
		   VirtualMachine[] vmArr = vmFinder.getVMList();
		   numOfVMs = vmFinder.getVMNumList();
		   
		   for (int cnt = 0; cnt < numOfVMs ; cnt++) 
		   {
			   VirtualMachine vm = vmArr[cnt];
			   V3MLogger.log(vm.getName().toString(),"Power State: " + vm.getSummary().runtime.powerState.toString() );
			   
			   if (vm.getSummary().runtime.powerState.toString().equals("poweredOn") && 
					   (vm.getGuest().getIpAddress() != null)                       && 
					   V3MPing.pingCommon(vm.getGuest().getIpAddress(), vm.getName().toString()))
			   {
				   //Removing the snapshot
				   Task task1 = vm.removeAllSnapshots_Task();

				   if(task1.waitForTask()==Task.SUCCESS){
					   System.out.println("[V3M] Removed the existing Snapshot" + vm.getName());
					   V3MLogger.log(vm.getName().toString(), "Removed the existing Snapshot");
				   }
				   else{
					   System.out.println("[V3M] No Available Snapshots for VM : "+ vm.getName());
					   V3MLogger.log(vm.getName().toString(), "No Available Snapshots for VM");
				   }

				   snapshotname = vm.getName() + "_V3M_Snapshot";
				   desc = "This is a snapshot for " + vm.getName();
				   Task task = vm.createSnapshot_Task(snapshotname, desc,false, false);
				   if(task.waitForTask()==Task.SUCCESS){
					   System.out.println("[V3M] Created Snapshot for VM: "+ vm.getName());
					   V3MLogger.log(vm.getName().toString(), "Created Snapshot for VM");
				   }
				   else
				   {
					   System.out.println("[V3M] Created Snapshot FAILED for VM: "+ vm.getName());
					   V3MLogger.log(vm.getName().toString(), "Failed to create Snapshot for VM");
				   }
			   }//if block
			   else
			   {
				   System.out.println("[V3M] Snapshot Handler " +vm.getName() + " is in powered off state.");
				   V3MLogger.log(vm.getName().toString(), "VM is in powered off state");
			   }
		   }//for loop
		}catch(Exception e)
		{
			System.out.println("Exception" +e);
			V3MLogger.log("Exception", e.toString());
		}
	}
}