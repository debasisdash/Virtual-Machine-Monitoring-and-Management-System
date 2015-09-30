package edu.sjsu.cmpe283.v3m;

import java.rmi.RemoteException;

import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class V3MVMFinder {

	ServiceInstance servInstance;
	int isFolder;
	int isExsi;
	Folder rootFolder;
	int numOfHost;
	int numOfVms;
	String[] folder ;
	public static VirtualMachine[] vmArr = new VirtualMachine[10];
	
	
	public VirtualMachine[] getVMList()
	{
		return vmArr;
	}
	
	public int getVMNumList()
	{
		return numOfVms;
	}

	V3MVMFinder(ServiceInstance servInstanceParam, int isFolderParam, int isESxiParam, String folderParam []) 
	{
		this.servInstance = servInstanceParam;
		this.isFolder = isFolderParam;
		this.isExsi = isESxiParam;
		this.folder = folderParam;	
	}

	void readPath(){

		ManagedEntity[] child = null;
		Folder VMS = null;
		
		//We Passed VM Name so no need to 
		//Parse the inventory path
		if (isFolder == 0)
			return ;
		
		rootFolder = servInstance.getRootFolder();
		try {
			child = rootFolder.getChildEntity();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//We received path of Exsi 
		if(isExsi == 1){
			//Getting the DataCenter
			for(int i=0; i<child.length; i++)
			{
				if(child[i] instanceof Datacenter)
				{
					Datacenter dc = (Datacenter) child[i];
					int cnt =0;
					try {
						VMS = dc.getVmFolder(); //Getting the folder
						ManagedEntity[] VMD = VMS.getChildEntity(); //Read the child entry of the folder

						for(int j = 0; j < VMD.length; j++)
						{
							ManagedEntity vm =  VMD[j];

							if(!vm.getName().toString().contains("-template")){
								vmArr[cnt] = (VirtualMachine) vm;

								++numOfVms;
								++cnt;
							}
						}
					}catch (RemoteException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		else
		{
			
			//Getting the DataCenter
			for(int i=0; i<child.length; i++)
			{
				if(child[i] instanceof Datacenter)
				{
					Datacenter dc = (Datacenter) child[i];
					try {
						VMS = dc.getVmFolder(); //Getting the folder
						ManagedEntity[] VMD = VMS.getChildEntity(); //Read the child entry of the folder

						for(int j=0; j<VMD.length; j++)
						{
							ManagedEntity vm =  VMD[j];
							//System.out.println(("VM:" + vm.getName()));

							ManagedEntity[] DBS = ((Folder) vm).getChildEntity();
							for(int k=0; k<DBS.length; k++)
							{
								ManagedEntity vm1 =  DBS[k];
								//System.out.println(("VM1:" + vm1.getName()));

								ManagedEntity[] JBS = ((Folder) vm1).getChildEntity();
								for(int l=0; l<JBS.length; l++)
								{
									//Here I am matching for my personal folder only
									if(JBS[l].getName().equalsIgnoreCase(folder[3])){
										//System.out.println(("VM2:" + JBS[l].getName()));
										//System.out.println(("VM folder = " + JBS[l].getName() + "\n"));
										ManagedEntity[] myvms = ((Folder) JBS[l]).getChildEntity();
										int cnt = 0;
										for(int m=0; m < myvms.length; m++){
											if(myvms[m] instanceof VirtualMachine){
												VirtualMachine myvm = (VirtualMachine) myvms[m]; 
												if(!myvm.getName().toString().contains("-template")){
													//System.out.println(("VM3:" + myvm.getName().toString()));
													vmArr[cnt] = myvm;
													++numOfVms;
													++cnt;
												}//Check for only VM	  
											}//Check of Instance of VirtualMachine
										} //End of For Loop for Virtual Machine
									}//choose the my folder
								}//For Loop to get the WorkSpace
							}//For Loop to get the CMPE283 SEC3
						}//For loop to read all the datacenter details

					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}

}
