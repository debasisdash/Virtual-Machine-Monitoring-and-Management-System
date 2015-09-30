package edu.sjsu.cmpe283.v3m;

import java.net.UnknownHostException;
import java.util.HashMap;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.vmware.vim25.mo.ServiceInstance;

public class V3MMongoDBProxy {
	
	private HashMap<String, String> infoList = new HashMap<String, String>();
	private HashMap<String, String>  vmDetail = new HashMap<String, String>();
	ServiceInstance servInstance;
	
	V3MMongoDBProxy() 
	{
		//Do Nothing
	}
	
	public void  updateMongoDB(ServiceInstance servInstanceParam,  HashMap<String, String> infoListParam,  HashMap<String, String> vmDetailsparam, int newEntry)
	{
		this.servInstance = servInstanceParam;
		this.infoList = infoListParam;
		this.vmDetail = vmDetailsparam;
		
		try {

			//Connecting to the Cloud Based Mongo DB
			MongoClientURI uri  = 
					new MongoClientURI("mongodb://Team_4:team4@ds031802.mongolab.com:31802/cmpe283_vmmm"); 
			MongoClient client = new MongoClient(uri);
			DB db = client.getDB(uri.getDatabase());

			//Creating the Mongo Database Connector
			DBCollection collection1 = db.getCollection("DC_AUSTIN_6547_VM_Details");
			DBCollection collection2 = db.getCollection("DC_AUSTIN_6547_VM_Statistics");
			
			//DBCollection collection1 = db.getCollection("DC_IRIVINE_5648_VM_Details");
			//DBCollection collection2 = db.getCollection("DC_IRIVINE_5648_VM_Statistics");
			
			//Dropping the table
			if (newEntry == 1)
				collection1.drop();
			
			//Updating the VM_Details Table
			BasicDBObject document1 = new BasicDBObject();
			for(String key : vmDetail.keySet()){				
				document1.put(key, vmDetail.get(key));
			}
			
			//Updating the VM_Statistics Table
			BasicDBObject document2 = new BasicDBObject();
			for(String key : infoList.keySet()){				
				document2.put(key, infoList.get(key));
			}
			
			// save it into collection named "MyCollection"
			collection1.insert(document1);
			collection2.insert(document2);
			
			// search query
            BasicDBObject searchQuery1 = new BasicDBObject();
            searchQuery1.put("Name","VM3_Details");
            
            BasicDBObject searchQuery2 = new BasicDBObject();
            searchQuery2.put("id", 1);
            
            BasicDBObject searchQuery3 = new BasicDBObject();
            searchQuery2.put("id", 1);
            
            BasicDBObject searchQuery4 = new BasicDBObject();
            searchQuery4.put("id", 1);
            
            // query it
            DBCursor cursor1 = collection1.find(searchQuery1);
            DBCursor cursor2 = collection1.find(searchQuery2);
            DBCursor cursor3= collection2.find(searchQuery3);
            DBCursor cursor4= collection2.find(searchQuery4);
            
            // loop over the cursor and display the retrieved result
            while (cursor1.hasNext()) {
                System.out.println(cursor1.next());
            }
            
            while (cursor2.hasNext()) {
                System.out.println(cursor2.next());
            }
            
            while (cursor3.hasNext()) {
                System.out.println(cursor3.next());
            }
           
            while (cursor4.hasNext()) {
                System.out.println(cursor4.next());
            }
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		}

	}
}
