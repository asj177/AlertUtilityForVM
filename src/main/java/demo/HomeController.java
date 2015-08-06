package demo;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

@RestController
public class HomeController {

	@RequestMapping(value = "/ack/{vmName}/{set}", method = RequestMethod.GET)
	public @ResponseBody String ackuser(@PathVariable("vmName")String vmName,@PathVariable("set")String set) {
		
		
		try{
			
			 DB db = GetMongoConnections.getConnection();
			 DBCollection collection = db.getCollection("threshold");
			 BasicDBObject doc = new BasicDBObject();
			 doc.put("vmip",vmName);
			 DBCursor cursor = collection.find(doc);
			 if(cursor.hasNext()){
					
					DBObject dobj = cursor.next();
					dobj.put("flag", "0");
					collection.update(doc, dobj);
					
			 }
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return "Thank you for your acknowledgement,we have set your flag back,incase if your VM goes above threshold a mail will again be sent to you";
	}
}
