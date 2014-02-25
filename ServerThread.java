import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;


@SuppressWarnings("restriction")
public class ServerThread implements Runnable
{ 
	Thread t;
	Integer nodeCount;
	String ipadd = new String();
	Integer port;
	int nodeId;
	ServerThread(Integer nc, String ip, Integer prt, int nodeid)
	{
		this.nodeCount = nc;
		this.ipadd = ip;
		this.port = prt;
		this.nodeId = nodeid;
		t = new Thread(this,"ServerThread");
		t.setPriority(10);
		t.start();
	}
	@Override
	public void run() {
		try
		{
		SctpServerChannel serverSock;
		SctpChannel clientSock;
		System.out.println("Thread Started: "+t+", "+t.getId());

		serverSock = SctpServerChannel.open();

		System.out.println("Server Wating ... "+ serverSock.isOpen());

		SocketAddress serverAddr = new InetSocketAddress(port);
		serverSock.bind(serverAddr);
		while(true)
		{
		clientSock = serverSock.accept();
		new ReadThread(clientSock,nodeId);
		}
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
