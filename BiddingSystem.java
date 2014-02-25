import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;

@SuppressWarnings("restriction")
public class BiddingSystem {

	static Integer nodeCount;
	static ArrayList<String> ips = new ArrayList<String>();
	static ArrayList<Integer> port = new ArrayList<Integer>();
	static Integer clock = new Integer(0);
	static boolean delivered = false;
	static Map<String, Integer> hm = Collections.synchronizedMap(new HashMap<String,Integer>());
	static Map<String, Integer> lhm = Collections.synchronizedMap(new LinkedHashMap<String, Integer>());
	static Map<String, Integer> permlhm = Collections.synchronizedMap(new LinkedHashMap<String, Integer>());
	static Integer receiveMaxTS;
	static Integer bidAmount; 
	static ArrayList<Integer> inputBid = new ArrayList<Integer>();
	@SuppressWarnings({ "unused" })
	public static void main(String[] args) {
		try
		{
			File f = new File(args[0]);
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line;
			int lineCount =0;
			while ((line = br.readLine()) != null) {
				String [] str = line.split("#");
				if(!(str[0].equals(null)))
				{
					String[] s = str[0].split(" ");
					if(lineCount == 0 && !(s[0].equals("")))
					{
						nodeCount = Integer.parseInt(s[0]);
						lineCount++;
					}
					else if(!(s[0].equals("")) && !(s[0].equals(null)))
					{
						ips.add(s[1]);
						port.add(Integer.parseInt(s[2]));
						lineCount++;
					}


				}
			}

			
			br.close();
			
			int i = 0;
			for(String s:ips)
			{
				if(Inet4Address.getLocalHost().getHostName().equals(s))
				{
					i = ips.indexOf(s);

				}
			}
			//start server thread
			ServerThread st = new ServerThread(nodeCount,ips.get(i),port.get(i),i+1);
			Thread.sleep(20000);
			//start client thread
			ClientThread ct = new ClientThread(nodeCount,ips,port,i+1);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	public static synchronized Integer clockIncrement(int ivalue)
	{
		return clock+=ivalue;
	}
	public static synchronized Integer receiveClock(int ivalue, Integer tm)
	{
		clock = Math.max(clock, tm);
		return clock+=ivalue;
	}
	public static synchronized void write(SctpChannel socket,String msg)
	{
		ByteBuffer sendBuffer = ByteBuffer.allocate(64000);
		final MessageInfo msgInfo = MessageInfo.createOutgoing(null, 0);
		sendBuffer.put(msg.getBytes());
		sendBuffer.flip();
		try {
			socket.send(sendBuffer, msgInfo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static synchronized String receive(SctpChannel socket)
	{
		ByteBuffer byteBuffer;
		byteBuffer = ByteBuffer.allocate(64000);
		byteBuffer.clear();
		try {
			@SuppressWarnings("unused")
			MessageInfo msgInfo = socket.receive(byteBuffer, null, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String Msg =  byteToString(byteBuffer);
		return Msg;
	}
	public static synchronized String byteToString(ByteBuffer byteBuffer)
	{
		byteBuffer.position(0);
		byteBuffer.limit(512);
		byte[] bufArr = new byte[byteBuffer.remaining()];
		byteBuffer.get(bufArr);
		return new String(bufArr);
	}
	public static synchronized void setBidAmount(Integer bidAmt)
	{
		bidAmount = bidAmt;
	}
	public static synchronized Integer getBidAmount()
	{
		return bidAmount;
	}
	@SuppressWarnings("rawtypes")
	public static synchronized String deliverMsg()
	{
		String msg = null;
		int count =0;
		for(Entry e : permlhm.entrySet())
		{
			if(count == 0)
			{
				msg = e.getKey()+","+e.getValue();
				count++;
			}
		}
		return msg;
	}
	public static synchronized void remove(String msg)
	{

		String[] str = msg.split(",");
		StringTokenizer s = new StringTokenizer(str[1].trim(), "$");
		s.nextToken();
		String a = s.nextToken().trim();
		BiddingSystem.setBidAmount(Integer.parseInt(a));

		lhm.remove(str[0]+","+str[1]);
		hm.remove(str[0]+","+str[1]);

		permlhm.clear();
		permlhm.putAll(lhm);

	}
	public static synchronized void putMsg(String key, Integer value)
	{
		hm.put(key, value);

	}
	public static synchronized void setLHM(Map<String, Integer> lhm2)
	{
		lhm.clear();
		permlhm.clear();

		lhm2.putAll(lhm2);
		permlhm.clear();

		permlhm.putAll(lhm2);

	}
	public static synchronized Map<String, Integer> getLHM()
	{
		return permlhm;
	}
	public static synchronized Map<String, Integer> getHM()
	{
		return hm;
	}
	@SuppressWarnings("rawtypes")
	public static synchronized Entry checkExists(String str)
	{
		Entry out = null;
		boolean flag = false;
		for(Entry e : BiddingSystem.hm.entrySet())
		{
			String[] s = e.getKey().toString().split(",");
			if(s[0].equals(str))
			{
				flag = true;
				out = e;
			}
		}
		if(flag == false)
			return null;
		else
			return out;
	}
	public static synchronized void setreceiveMaxTS(Integer TS)
	{
		receiveMaxTS = TS;
	}
	public static synchronized Integer getreceiveMaxTS()
	{
		return receiveMaxTS;
	}
	public static synchronized void setDelivered(boolean d)
	{
		delivered = d;
	}
	public static synchronized boolean getDelivered()
	{
		return delivered;
	}
	public static synchronized void putBidValue(Integer bid)
	{
		inputBid.add(bid);
	}
	public static synchronized Integer getBidValue()
	{
		return inputBid.get(0);
	}
	public static synchronized void removeBidValue()
	{
		inputBid.remove(0);
	}
}
