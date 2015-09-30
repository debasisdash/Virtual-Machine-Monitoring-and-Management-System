package edu.sjsu.cmpe283.v3m;


public class V3MPing {
	public static boolean pingCommon(String ipAddr, String vmName)
	{
		boolean result= false;
		try
		{
			if(ipAddr!=null)
			{			
				boolean reachable = 
				   (java.lang.Runtime.getRuntime().exec("ping -n 1 "+ ipAddr).waitFor()==0);
				if(reachable == false)
				{
					System.out.println("Ping not successful for " + ipAddr);
					V3MLogger.log(vmName,"Ping not successful for " + ipAddr );
					result=false;					
				}
				else
				{
					System.out.println("Ping successful for " + ipAddr);
					V3MLogger.log(vmName,"Ping successful for " + ipAddr );
					result=true;
				}
			}  
			else 
			{
				System.out.println("IP " + ipAddr +" is not found!");
				V3MLogger.log(vmName, "IP " + ipAddr +" is not found!");
				result = false;
			} 
		} 
		catch(Exception e)
		{
			System.out.println(e.toString());
			V3MLogger.log(vmName, e.toString());
		}
		return result;
	}
}