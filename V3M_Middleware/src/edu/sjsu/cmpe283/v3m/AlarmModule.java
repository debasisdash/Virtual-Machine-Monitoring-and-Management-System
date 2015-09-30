package edu.sjsu.cmpe283.v3m;

import java.rmi.RemoteException;
import com.vmware.vim25.AlarmSetting;
import com.vmware.vim25.AlarmSpec;
import com.vmware.vim25.AlarmState;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.StateAlarmExpression;
import com.vmware.vim25.StateAlarmOperator;
import com.vmware.vim25.mo.Alarm;
import com.vmware.vim25.mo.AlarmManager;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;
public class AlarmModule {
	
	ServiceInstance servInstance = null;
	String vmName = null;
	
	AlarmModule(ServiceInstance servInstanceParam, String vmNameParam) 
	{
		this.servInstance = servInstanceParam;
		this.vmName = vmNameParam;
	}

	public boolean getAlarmStatus() throws Exception
	{
		InventoryNavigator inv = new InventoryNavigator(servInstance.getRootFolder());
        VirtualMachine vm = (VirtualMachine)inv.searchManagedEntity("VirtualMachine", vmName);
    	Alarm[] alarms = servInstance.getAlarmManager().getAlarm(vm);    	
    	AlarmState[] alarmStates = vm.getTriggeredAlarmState();
    	
    	if(alarmStates == null)
    	{
    		System.out.println("No Alarm found for VM : " + vm.getName());
    		V3MLogger.log(vmName, "No Alarm found for VM");
        	return false;
    	}	
    	else if(alarms[0].getAlarmInfo().name.equals("VmPowerStateAlarm" + vm.getName()) && (alarmStates[0].overallStatus.name().equals("red")))
		{
    		System.out.println("Alarm found for VM : " + vm.getName());
    		V3MLogger.log(vmName, "Alarm found for VM");
    		return true;
		}
    	else
    	{
    		System.out.println("No Alarm Condition matched : " + vm.getName());
    		V3MLogger.log(vmName, "No Alarm Condition matched");
    		return false;
    	}
	}
		
	public void setAlarm() 
	{
		InventoryNavigator invNav = new InventoryNavigator(servInstance.getRootFolder());
		VirtualMachine vm;
	
		try{
			vm = (VirtualMachine)invNav.searchManagedEntity("VirtualMachine", vmName);
			AlarmManager alarmMgr = servInstance.getAlarmManager();
			
			AlarmSpec spec = new AlarmSpec();
			StateAlarmExpression expression = createStateAlarmExpression();
			spec.setExpression(expression);
			spec.setName("VmPowerStateAlarm" + vm.getName());
			spec.setDescription("Monitor VM state  " + "and power it ON if VM powers off");
			spec.setEnabled(true);    
	    
			AlarmSetting alarmConfig = new AlarmSetting();
			alarmConfig.setReportingFrequency(0); //as often as possible
			alarmConfig.setToleranceRange(0);
	    
			spec.setSetting(alarmConfig);
			alarmMgr.createAlarm(vm, spec);
			
			System.out.println("+++ALARM: Created Alarm for vm : " + vm.getName());
			V3MLogger.log(vm.getName().toString(), "Alarm created for VM");
			return;
	    
		  }catch (InvalidProperty e) {
			e.printStackTrace();
		} catch (RuntimeFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	static StateAlarmExpression createStateAlarmExpression()
	{
	    StateAlarmExpression expression = new StateAlarmExpression();
	    expression.setType("VirtualMachine");
		expression.setStatePath("runtime.powerState");
		expression.setOperator(StateAlarmOperator.isEqual);
		expression.setRed("poweredOff");
	    return expression;
	}
	
	public void removeAlarm(VirtualMachine vm) throws Exception 
    {		
		Alarm[] alarms = servInstance.getAlarmManager().getAlarm(vm);
    	for(Alarm alarm : alarms)
    	{
    		alarm.removeAlarm();
    		System.out.println("ALARM: Removed Alarm for vm : " + vm.getName());
    		V3MLogger.log(vm.getName().toString(), "Alarm removed for VM");
    		return;
    	}
    }
}
