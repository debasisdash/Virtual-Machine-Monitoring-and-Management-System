package edu.sjsu.cmpe283.v3m;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class V3MLogger {
	
	public static void log(String vmName, String logMsg)
	{
		//Connecting to the Cloud Based Mongo DB
		MongoClientURI uri  = 
		  new MongoClientURI("mongodb://Team_4:team4@ds031802.mongolab.com:31802/cmpe283_vmmm"); 
		MongoClient client = null;
		try {
			client = new MongoClient(uri);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		DB db = client.getDB(uri.getDatabase());
		
		DBCollection collection1 = db.getCollection("V3M_LOGS");
		
		//Updating the VM_LOGS
		BasicDBObject document1 = new BasicDBObject();
		document1.put("name", vmName);
		
		//Recording the TimeStamp
		SimpleDateFormat stdTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		document1.put("datetime",stdTime.format(new Date()));
		
		//Storing the LOG String
		document1.put("logMsg",logMsg);
		
		// save it into collection named "MyCollection"
		collection1.insert(document1);
		
		BasicDBObject searchQuery1 = new BasicDBObject();
        searchQuery1.put("id", 1);
        
        // query it
        DBCursor cursor1 = collection1.find(searchQuery1);
        
        // loop over the cursor and display the retrieved result
        while (cursor1.hasNext()) {
            System.out.println(cursor1.next());
        }	
	}
}
