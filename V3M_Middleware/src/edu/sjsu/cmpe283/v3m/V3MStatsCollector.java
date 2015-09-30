package edu.sjsu.cmpe283.v3m;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.PerfQuerySpec;


import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class V3MStatsCollector extends Thread{
	
	static int counter = 0;
	int duration = 0;
	int numOfVMs;
	int isEsxi;
	int isFolder;
	V3MVMFinder vmFinder;
	ServiceInstance servInstance = null;
	VirtualMachine[] vmArr = new VirtualMachine[10];
	V3MMongoDBProxy mongoProxy;
			
	private static final int SELECTED_COUNTER_ID = 6;
	static Integer[] VmCounterIds = { 5, 23, 124, 142, 156 };
	static String[] VmCounterInfo = {"memUsage","cpuUsage","diskUsage","netUsage","sysUsage"};
	private static HashMap<String, String> infoList_ = new HashMap<String, String>();
	private static HashMap<String, String> vmDetails = new HashMap<String, String>();
	
	V3MStatsCollector(ServiceInstance servInstance, int time, V3MVMFinder vmFinderParam) 
	{
		this.servInstance = servInstance;
		this.duration = time;
		this.vmFinder = vmFinderParam;
		mongoProxy = new V3MMongoDBProxy();
	}
	
	public void run(){		
		int newEntry = 1;
		
		while (true) 
		{
			//Collecting the data
			vmFinder.readPath();
			VirtualMachine[] vmArr = vmFinder.getVMList();
			numOfVMs = vmFinder.getVMNumList();
			
			try 
			{
				for(int cnt =  0; cnt < numOfVMs; cnt++)
				{
					vmDetailsCollector(vmArr[cnt]);
					vmDataCollector(vmArr[cnt].getName().toString());
					mongoProxy.updateMongoDB(servInstance, infoList_, vmDetails, newEntry);
					infoList_.clear();
					vmDetails.clear();
					newEntry = 0;
				}				
				newEntry = 1;
				System.out.println("vmDataCollector Therad Sleeping for 1 minute");
				Thread.sleep(1000 * 60 * duration); 				
			}catch (Exception e){
				System.out.println(e.getStackTrace());
			}
		}
	}
	
	public void vmDataCollector(String vmName){
		try {
			
			ManagedEntity vm =  
					new InventoryNavigator(servInstance.getRootFolder()).searchManagedEntity("VirtualMachine", vmName);
			
			PerformanceManager perfMgr = servInstance.getPerformanceManager();
			System.out.println("perf name" + perfMgr);
			PerfProviderSummary summary = perfMgr.queryPerfProviderSummary(vm);
			int perfInterval = summary.getRefreshRate();
			 
			PerfMetricId[] queryAvailablePerfMetric = 
					perfMgr.queryAvailablePerfMetric(vm, null, null,perfInterval);
			
			ArrayList<PerfMetricId> list = new ArrayList<PerfMetricId>();
			
			for (int i = 0; i < queryAvailablePerfMetric.length; i++) 
			{
				PerfMetricId perfMetricId = queryAvailablePerfMetric[i];
				if (SELECTED_COUNTER_ID == perfMetricId.getCounterId()) 
				{
					list.add(perfMetricId);
				}
			}
			
			PerfMetricId[] pmis = list.toArray(new PerfMetricId[list.size()]);
			
			// creating a query Spec
			PerfQuerySpec qSpec = new PerfQuerySpec();
			qSpec.setEntity(vm.getMOR());
			qSpec.setMetricId(pmis);
			qSpec.intervalId = perfInterval;

			// Perf metric base
			PerfEntityMetricBase[] pembs = perfMgr.queryPerf(new PerfQuerySpec[] { qSpec });
			for (int i = 0; pembs != null && i < pembs.length; i++) 
			{
				PerfEntityMetricBase val = pembs[i];
				PerfEntityMetric pem = (PerfEntityMetric) val;
				PerfMetricSeries[] vals = pem.getValue();
				
				//Adding the VM Name
				infoList_.put("VM_Name", vmDetails.get("VM_Name").toString());
				
				//Recording the TimeStamp
				SimpleDateFormat stdTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				infoList_.put("datetime",stdTime.format(new Date()));
			 
				for (int j = 0; vals != null && j < vals.length; ++j) 
				{
					PerfMetricIntSeries val1 = (PerfMetricIntSeries) vals[j];
					long[] longs = val1.getValue();
					for (int k : VmCounterIds) 
					{
						infoList_.put(VmCounterInfo[counter],String.valueOf(longs[k]));
						counter++;
					}
					counter = 0;					 
				}
			}						 
		}
		catch(Exception e){
		}
	}

	public void vmDetailsCollector(VirtualMachine vm)
	{
		VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vm.getRuntime();		
		vmDetails.put("VM_Name", vm.getName().toString());
		vmDetails.put("VM_IPAddress", vm.getSummary().getGuest().getIpAddress().toString());
		vmDetails.put("Max_CPU_Usage", vmri.maxCpuUsage.toString());
		vmDetails.put("Max_Memory", vmri.maxMemoryUsage.toString());
		vmDetails.put("Number_Of_NICs", vm.getSummary().config.getNumEthernetCards().toString());
		vmDetails.put("Number_Of_CPUs", vm.getSummary().config.getNumCpu().toString());
		vmDetails.put("Number_Of_Disks", vm.getSummary().config.getNumVirtualDisks().toString());
		vmDetails.put("UpTime", vm.getSummary().quickStats.uptimeSeconds.toString());
		vmDetails.put("MemUsage", vm.getSummary().quickStats.hostMemoryUsage.toString());	
	}
}
