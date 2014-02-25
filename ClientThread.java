import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import java.util.ArrayList;
import java.util.Scanner;

import com.sun.nio.sctp.SctpChannel;


@SuppressWarnings("restriction")
public class ClientThread implements Runnable
{

	Thread t;
	Integer nodeCount;
	ArrayList<String> ips = new ArrayList<String>();
	ArrayList<Integer> port = new ArrayList<Integer>();
	ArrayList<SctpChannel> sockets = new ArrayList<SctpChannel>();
	int nodeId;
	static ArrayList<Integer> receiveBackTS = new ArrayList<Integer>();
	ClientThread(Integer nc, ArrayList<String> ip, ArrayList<Integer> prt, int nodeid)
	{

		this.nodeId = nodeid;
		this.nodeCount = nc;
		this.ips.addAll(ip);
		this.port.addAll(prt);
		t = new Thread(this,"ServerThread");
		t.start();
	}
	@SuppressWarnings("resource")
	@Override
	public void run() {
		try{

			//connecting to all servers in different nodes
			for(int i =0;i<nodeCount; i++)
			{
				System.out.println("Thread Started: "+t+", "+t.getId()+", "+ips.get(i)+", "+port.get(i));
				SctpChannel clientSocket;
				SocketAddress serverAddr = new InetSocketAddress(ips.get(i),port.get(i));
				clientSocket = SctpChannel.open();
				//clientSocket.configureBlocking(false);
				System.out.println("Client Open: "+ clientSocket.isOpen());
				clientSocket.connect(serverAddr);
				System.out.println("Client Connected ");
				sockets.add(clientSocket);
			}
			System.out.println("Enter bidAmount at any point of time");
			BiddingSystem.setBidAmount(0);
			while(true)
			{
			
			int amt = 0;
			do
			{
				//Getting Bid Amount from user
			Scanner scan = new Scanner(System.in);
			amt = scan.nextInt();
			System.out.println(amt);
			if(amt > BiddingSystem.getBidAmount())
			{
				BiddingSystem.putBidValue(amt);
			 //BiddingSystem.setBidAmount(amt);
			 break;
			}
			else if(amt <= BiddingSystem.getBidAmount());
			{
			System.out.println("Bidding amount must be greater than "+BiddingSystem.getBidAmount());
			}
			}
			while(amt <= BiddingSystem.getBidAmount());
			BiddingSystem.setBidAmount(BiddingSystem.getBidValue());
			System.out.println("new bidAmount"+BiddingSystem.getBidAmount());
			boolean sendMax = false;
			//broadcasting the bid amount to all nodes/machines
			for(SctpChannel clientSock: sockets)
			{
				sendMax = false;

				write(clientSock,Thread.currentThread().getName()+","+Inet4Address.getLocalHost().getHostName(),nodeId, sendMax,0);

			}
			System.out.println("After sending ......");
			BiddingSystem.removeBidValue();
			Thread.sleep(5000);
			//getting the timestamp of the msg received at the other end
			for(SctpChannel clientSock: sockets)
			{
			receiveBack(clientSock);
			}
			//calculating the max timestamp
			for(SctpChannel clientSock: sockets)
			{

				sendMax = true;
				write(clientSock,Thread.currentThread().getName()+","+Inet4Address.getLocalHost().getHostName(),nodeId, sendMax,0);

				System.out.println("Resend MaxTM complete"+Inet4Address.getLocalHost().getHostName());

			}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public static void write(SctpChannel clientSocket, String name, int nodeid, boolean sendMax,int timeStamp) throws IOException
	{
		/*ByteBuffer sendBuffer = ByteBuffer.allocate(64000);
		final MessageInfo msgInfo = MessageInfo.createOutgoing(null, 0);*/
		String msg = null;
		if(sendMax == false)
		{
			Integer tStamp = BiddingSystem.clockIncrement(nodeid);
			msg = "Client "+name+",BidAmount: $"+BiddingSystem.getBidAmount()+","+tStamp;
			System.out.println("Application message send from client "+name);
		}
		else
		{
			msg = "Client "+name+","+getMaxTS();
			System.out.println("MaxTM send from client "+name);
			//Thread.currentThread().stop();
		}
		BiddingSystem.write(clientSocket, msg);
		
}
	public static void receiveBack(SctpChannel clientSock) throws IOException
	{

	String Msg =  BiddingSystem.receive(clientSock);
	String[] str = Msg.split(",");
	int len = str.length;

	
	receiveBackTS.add(Integer.parseInt(str[len-1].trim()));
	System.out.println("Client recieves timestamp from : "+clientSock.getRemoteAddresses());

	}
	public static synchronized Integer getMaxTS()
	{
		Integer max = new Integer(0);
		for(Integer i: receiveBackTS)
		{
			max = Math.max(max, i);
		}
		return max;
	}
}
