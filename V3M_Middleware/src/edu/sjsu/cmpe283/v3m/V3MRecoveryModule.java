package edu.sjsu.cmpe283.v3m;

//import com.vmware.vim25.ManagedObjectReference;
//import com.vmware.vim25.VirtualMachineSnapshotInfo;
//import com.vmware.vim25.VirtualMachineSnapshotTree;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;
//import com.vmware.vim25.mo.VirtualMachineSnapshot;

public class V3MRecoveryModule extends Thread 
{
	VirtualMachine vm = null;
	String snapshotname = "recent";

	V3MRecoveryModule(VirtualMachine vm) 
	{
		this.vm = vm;
	}

	public void run() 
	{ 
		try{		
			Task task = vm.revertToCurrentSnapshot_Task(null);
			if (task.waitForTask() == Task.SUCCESS) 
			{
				System.out.println("V3M Installing the Snapshot: " + vm.getSnapshot().toString() );

				//Lets make thread sleep for 5 Secs
				Thread.sleep(5000);

				//Going to start the VM
				task = vm.powerOnVM_Task(null);
				if(task.waitForTask()==Task.SUCCESS)
				{
					System.out.println("V3M  Reverted " + vm.getName());
					Thread.sleep(5000);
				}
				else
				{
					System.out.println("V3M could not power on VM: " + vm.getName());
				}
			}
			else
			{
				System.out.println(" Couldn't get the VM snapshot:" + snapshotname);
			}
		}//try
		catch (Exception e) 
		{
			System.out.println(" V3M Couldn't revert the VM:" + snapshotname);
		}		
	}
}