package edu.sjsu.cmpe283.v3m;

import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class PingThread extends Thread {
	ServiceInstance servInstance = null;
	int duration;
	VirtualMachine[] vmArr = new VirtualMachine[10];
	int numOfVMs;
	int isEsxi;
	int isFolder;
	V3MVMFinder vmFinder;

	PingThread(ServiceInstance servInstance) 
	{
		this.servInstance = servInstance;
	}

	//Constructor with ServiceInstance and Period
	PingThread(ServiceInstance servInstance, int period) 
	{
		this.servInstance = servInstance;
		this.duration = period;
	}

	//This also pass the list VM which we need to 
	PingThread(ServiceInstance servInstance, int period, VirtualMachine[] vmList, int numVM) 
	{
		this.servInstance = servInstance;
		this.duration = period;
		this.vmArr = vmList;
		this.numOfVMs = numVM;
	}

	PingThread(ServiceInstance servInstance, int time, V3MVMFinder vmFinderParam) 
	{
		this.servInstance = servInstance;
		this.duration = time;
		this.vmFinder = vmFinderParam;
	}

	public void run() 
	{
		String ip = null;

		while (true) 
		{
			//Collecting the data
			vmFinder.readPath();
			VirtualMachine[] vmArr = vmFinder.getVMList();
			numOfVMs = vmFinder.getVMNumList();

			try 
			{
				Folder rootFolder = servInstance.getRootFolder();
				ManagedEntity[] mesHost = 
						new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");

				for (int j = 0; j < mesHost.length; j++) 
				{
					//boolean isHostActive = false;
					HostSystem hs = (HostSystem) mesHost[j];

					for (int i = 0; i < numOfVMs; i++) 
					{
						VirtualMachine vm = vmArr[i];
						System.out.println("VM name : " + vm.getName() + " IP : " 
								+ vm.getGuest().getIpAddress() );
						V3MLogger.log(vm.getName().toString(), "Going To Ping The Server");

						String hostip = hs.getName();
						System.out.println("Hostip : " + hs.getName() );
						V3MLogger.log(hs.getName().toString(), "Hosted Server Server");

						AlarmModule alarm = new AlarmModule(servInstance,vm.getName());
						//alarm.setAlarm();
						if (vm.getSummary().runtime.powerState.toString().equals("poweredOn")
								&& (vm.getGuestHeartbeatStatus().toString().equals("green") ))
						{ 

							System.out.println("VM " + vm.getName() + " is in powered ON");
							ip = vm.getGuest().getIpAddress();
							try 
							{
								if ((ip != null) && V3MPing.pingCommon(ip, vm.getName().toString())) 
								{
									System.out.println("VM : " + vm.getName() + " Reachable");
									V3MLogger.log(vm.getName().toString(), "VM is Reachable");
								} 
								else 
								{
									System.out.println("VM :" +  vm.getName() + " UnReachable");
									V3MLogger.log(vm.getName().toString(), "VM is UnReachable");

									if (hostip != null && V3MPing.pingCommon(hostip,hostip)) 
									{	
										if(alarm.getAlarmStatus())
										{
											System.out.println("[Alarm] User has switched off VM  : "+ vm.getName());
											V3MLogger.log(hostip.toString(), "User has switched off VM " + vm.getName().toString());
										}
										else 
										{
											System.out.println("Ping to VM hostes Host : " + hs.getName()	+ " Successful"); 
											System.out.println("Started disaster recovery Thread for VM : " + vm.getName().toString());
											V3MLogger.log(hs.getName().toString(), "Ping to VM hostes Host");
											V3MLogger.log(hs.getName().toString(), "Started disaster recovery Thread for VM : " + vm.getName().toString());

											//Going to recover the host
											new V3MRecoveryModule(vm).start();
										}
									} 
									else 
									{
										VirtualMachine vHostVM = RecoverHost.getvHostFromAdminVCenter(hs.getName().substring(11, hs.getName().length()),servInstance);
										Task taskHost = vHostVM.revertToCurrentSnapshot_Task(null);
										if (taskHost.getTaskInfo().getState() == taskHost.getTaskInfo().getState().success) {
											System.out.println("Recovery Handler: vHost has been recovered on the admin vCenter..");
											V3MLogger.log(hs.getName().toString(), "Recovery Handler: vHost has been recovered on the admin vCenter");
										}
										if(RecoverHost.reconnectHostandRecoverVM(vm,hs, servInstance)){
											System.out.println("Recovery Handler: Host reconnected");
											V3MLogger.log(vm.getName().toString(), "Recovery Handler: Host reconnected");
										}
										else
										{
											System.out.println("No Active hosts not found..");
											System.out.println("Recovery Aborted");
											V3MLogger.log(vm.getName().toString(), "No Active hosts not found.");
											V3MLogger.log(vm.getName().toString(), "Recovery Aborted");
										}	
									}//else
								}//else
							}//try
							catch (Exception e) 
							{
								System.out.println(e.getStackTrace());
								V3MLogger.log("Exception", e.getStackTrace().toString());

							}
						}//if
						else
						{
							System.out.println(vm.getName() + " is in powered off state");
							V3MLogger.log(vm.getName().toString(), "VM is in powered off state");
						}
						//alarm.removeAlarm(vm);
					}//for
				}//for
				System.out.println("Sleep Ping thread for " + duration + " minutes");
				System.out.println();
				Thread.sleep(duration * 60 * 1000);
			}//try 
			catch (Exception e) 
			{
				System.out.println(e.getStackTrace());
				V3MLogger.log("Exception", e.getStackTrace().toString());
			}
		}//while
	}//run
}