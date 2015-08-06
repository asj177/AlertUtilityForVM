package demo;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

@Component
public class MessageScheduler {

	public static String timeStamp=date();
	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	
	public static String date() {
		TimeZone timeZone = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(timeZone);
		return (df.format(new Date()));
	}
	@Scheduled(fixedRate=10000)
	public void checkAlerts(){
		
		try{
		
		Client client = new TransportClient()
        .addTransportAddress(new InetSocketTransportAddress("54.153.36.109", 9300));
		SearchResponse response = client.prepareSearch("logstash-*")
		           .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
		            .setFrom(0).setSize(10).setExplain(true)
		            .setPostFilter(FilterBuilders.rangeFilter("@timestamp").from(timeStamp))
		            .execute()
		           .actionGet();
		if(response.getHits().getHits().length>0){
		SearchHit search=response.getHits().getHits()[response.getHits().getHits().length-1];
		System.out.println("**************************************************************************************");
		checkNotification(response);
		System.out.println(search);
		timeStamp=(String)search.getSource().get("@timestamp");
		System.out.println("**************************************************************************************");
		System.out.println("Time stampinto consideartion is "+timeStamp);
		System.out.println("**************************************************************************************");
		}}catch(Exception e ){
			e.printStackTrace();
		}
		
	}
	
	
	public void checkNotification(SearchResponse response){
		
		try{
		DB db = GetMongoConnections.getConnection();
		for(SearchHit search:response.getHits().getHits()){
			
			String userName=(String)search.getSource().get("UserName");
			System.out.println("Time stamp "+(String)search.getSource().get("@timestamp") + "user name is "+userName);
			DBCollection collection = db.getCollection("threshold");
			 BasicDBObject doc = new BasicDBObject();
			 doc.put("vmip",userName);
				DBCursor cursor = collection.find(doc);
				
				if(cursor.hasNext()){
					System.out.println("Threshold set for user "+userName);
					DBObject dobj = cursor.next();
				
					if(Integer.valueOf((String)dobj.get("flag"))!=1){
					String email=(String)dobj.get("email");
					if((String)dobj.get("cpu")!=null){
						
						int cpu=Integer.valueOf((String)dobj.get("cpu"));
						System.out.println("CPU threshold for user  is "+cpu);
						int cpufromsearch=(int)search.getSource().get("cpu");
						System.out.println("CPU search for user  is "+cpufromsearch);
						if(cpu<cpufromsearch){
							String message="Dear User, \n \n "+
						                  "Your vm has exceeded the cpu limit of "+(String)dobj.get("cpu") + " set by you and its current cpu value is "+
									      search.getSource().get("cpu")+
									      "\n To acknowledge please click on the below link or type the below URL on your browser \n "+
									      "http://10.189.5.39:8080/ack/"+userName+"/set";
							
							sendEmail(email,message,dobj,collection,doc,userName);
						}
					}
					
					if((String)dobj.get("net")!=null){
						int net=Integer.valueOf((String)dobj.get("net"));
						System.out.println("net threshold for user  is "+net);
						int netfromsearch=(int)search.getSource().get("net");
						System.out.println("Net search for user  is "+netfromsearch);
						if(net<netfromsearch){
							String message="Dear User, \n \n "+
					                  "Your vm has exceeded the network limit of "+(String)dobj.get("net") + " set by you and its current network value is "+
								      search.getSource().get("net")+
								      "\n To acknowledge please click on the below link or type the below URL on your browser \n "+
								      "http://10.189.5.39:8080/ack/"+userName+"/set";
						
						sendEmail(email,message,dobj,collection,doc,userName);
						}
					}
					
					
					if((String)dobj.get("mem")!=null){
						int mem=Integer.valueOf((String)dobj.get("mem")); 
						System.out.println("mem threshold for user  is "+mem);
						int memfromsearch=(int)search.getSource().get("mem");
						System.out.println("mem search for user  is "+memfromsearch);
						if(mem<memfromsearch){
							String message="Dear User, \n \n "+
					                  "Your vm has exceeded the memory limit of "+(String)dobj.get("mem") + " set by you and its current memory value is "+
								      search.getSource().get("mem")+
								      "\n To acknowledge please click on the below link or type the below URL on your browser \n "+
								      "http://10.189.5.39:8080/ack/"+userName+"/set";
						
						sendEmail(email,message,dobj,collection,doc,userName);
						}
					}
					
					if((String)dobj.get("diskRead")!=null){
						int diskread=Integer.valueOf((String)dobj.get("diskRead"));
						System.out.println("diskread threshold for user  is "+diskread);
						int diskreadfromsearch=(int)search.getSource().get("diskRead");
						System.out.println("diskread search for user  is "+diskreadfromsearch);
						if(diskread<diskreadfromsearch){
							String message="Dear User, \n \n "+
					                  "Your vm has exceeded the diskRead limit of "+(String)dobj.get("diskRead") + " set by you and its current diskRead value is "+
								      search.getSource().get("diskRead")+
								      "\n To acknowledge please click on the below link or type the below URL on your browser \n "+
								      "http://10.189.5.39:8080/ack/"+userName+"/set";
						
						sendEmail(email,message,dobj,collection,doc,userName);
						}
					}
					
					if((String)dobj.get("diskWrite")!=null){
						int diskwrite=Integer.valueOf((String)dobj.get("diskWrite"));
						System.out.println("diskWrite threshold for user  is "+diskwrite);
						int diskwritefromsearch=(int)search.getSource().get("diskWrite");
						System.out.println("diskWrite search for user  is "+diskwritefromsearch);
						if(diskwrite<diskwritefromsearch){
							String message="Dear User, \n \n "+
					                  "Your vm has exceeded the diskWrite limit of "+(String)dobj.get("diskWrite") + " set by you and its current diskWrite value is "+
								      search.getSource().get("diskWrite")+
								      "\n To acknowledge please click on the below link or type the below URL on your browser \n "+
								      "http://10.189.5.39:8080/ack/"+userName+"/set";
						
						sendEmail(email,message,dobj,collection,doc,userName);
						}
					}
					
				}
				}
		}
		
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public void sendEmail(String emailId,String alertmessage,DBObject dobj,DBCollection collection,BasicDBObject doc,String userIp){
		dobj.put("flag", "1");
		collection.update(doc, dobj);
		
		System.out.println("Sending email to "+emailId);
		System.out.println("Flag set to 1 for user"+emailId);
		final String username = "cmpe283team10@gmail.com";
		final String password = "TeamTen10";
 
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
 
		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		  });
 
		try {
 
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress("cmpe283team10@gmail.com"));
			message.setRecipients(javax.mail.Message.RecipientType.TO,
				InternetAddress.parse(emailId));
			message.setSubject("Alert from CMPE283 ADMIN");
			message.setText(alertmessage);
 
			Transport.send(message);
 
			//System.out.println("Done");
 
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
		
	}
	
}
