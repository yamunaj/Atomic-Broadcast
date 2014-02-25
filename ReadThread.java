import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet4Address;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
@SuppressWarnings("restriction")
public class ReadThread implements Runnable
{
	Thread t;
	SctpChannel clientSocket;
	int nodeId;
	int msgCount =0;
	public ReadThread(SctpChannel clientSocket, int nodeId)
	{
		this.clientSocket = clientSocket;
		this.nodeId = nodeId;
	t = new Thread(this,"ServerThread");
	t.setPriority(10);
	t.start();
	}
	@SuppressWarnings({ "unused" })
	@Override
	public void run() 
	{
		while(true)
		{
			System.out.println("Server waiting for message ...");
		boolean applFlag = false;
		ByteBuffer byteBuffer;
		byteBuffer = ByteBuffer.allocate(64000);
		byteBuffer.clear();
		
		try {
			MessageInfo msgInfo = clientSocket.receive(byteBuffer, null, null);
		
		System.out.println("Server got one msg");
		applFlag = displayMsg(byteBuffer,nodeId);
		
		if(applFlag == true )
		{
	
			String name = Inet4Address.getLocalHost().getHostName();
			String timeStamp = BiddingSystem.clock.toString();


		String msg = name+","+timeStamp;

		BiddingSystem.write(clientSocket, msg);

		System.out.println("Control msg send from server "+name);
			
		}
		else
		{

					String msg = BiddingSystem.deliverMsg();

					FileWriter outFile = new FileWriter("/home/004/y/yx/yxj122030/AOS/Project1/Log/log"+Inet4Address.getLocalHost().getHostName()+".txt",true);
					//FileWriter outFile = new FileWriter("/home/yj-jbp/Documents/AOS/Project1/log.txt",true);
					BufferedWriter outStream = new BufferedWriter(outFile);
					outStream.write(msg);
					outStream.write("\n");

					outStream.close();

					BiddingSystem.setDelivered(true);

					System.out.println("Delivered : "+msg);

					BiddingSystem.remove(msg);
		}
		;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean displayMsg(ByteBuffer byteBuffer,int nodeid )
	{
		boolean applFlag = false;
		String Msg =  BiddingSystem.byteToString(byteBuffer);
		System.out.println("Message received by server : "+Msg);
		String[] str = Msg.split(",");
		int len = str.length;
		if(len == 4)
		{
			//server recive application message
			msgCount++;
			applFlag = true;

		Integer timeStamp = BiddingSystem.receiveClock(nodeid, Integer.parseInt(str[len-1].trim()));

		BiddingSystem.putMsg(str[1]+msgCount+","+str[2],timeStamp );

		System.out.println("Server receives application message:"+str[1]+","+str[2]);
		}
		else
		{
			//server receive the max timestamp from client
			//server delivers message
			applFlag =false;
			BiddingSystem.setreceiveMaxTS(Integer.parseInt(str[len-1].trim()));
			Entry e = BiddingSystem.checkExists(str[1]+msgCount);

			System.out.println("entry : "+e);
				if(!(e.equals(null)))
				{
					System.out.println("inside : "+e);
					BiddingSystem.putMsg(e.getKey().toString(), BiddingSystem.getreceiveMaxTS());
					
					Map<String, Integer> lhm=  sortByValues(BiddingSystem.getHM());
					BiddingSystem.setLHM(lhm);
					
				}

			System.out.println("Server receives MaxTS message");

		}
		return applFlag;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static java.util.Map sortByValues(Map<String, Integer> hm) {

		List list = new LinkedList(hm.entrySet());

		// sort list based on comparator
		Collections.sort(list, new Comparator() {
		public int compare(Object o1, Object o2) {
		return ((Comparable) ( ((Entry) o1)).getValue())
		.compareTo(( ((Entry) o2)).getValue());
		}


		});

		java.util.Map sortedMap = new LinkedHashMap();
		Iterator it = list.iterator();
		while (it.hasNext()) 
		{
		Entry entry = (Entry) it.next();
		sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
		}
}
