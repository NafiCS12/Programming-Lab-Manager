package networking;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import miscelleneous.Logger;

public class Server implements Runnable
{
	private final List<TransmitterThread> clients=new ArrayList();
	private ServerSocket server=null;
	private Socket socket=null;
	private Thread thread=null;
	
	public Server(int port) throws IOException
	{
		server=new ServerSocket(port);
		thread=new Thread(this);
		thread.start();
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			try
			{
				socket=server.accept();
				TransmitterThread tt=new TransmitterThread(socket);
				clients.add(tt);
				
				Logger.log("Connected to "+tt.getClientName());
			}
			catch(Exception io)
			{
				Logger.log("Failed to connect to an incoming client.");
			}
		}
	}
	
	public int send(File file)
	{
		int count=0;
		List<TransmitterThread> rem=new ArrayList();
		
		for(TransmitterThread tt: clients)
		{
			try
			{
				tt.send(file);
				count++;
			}
			catch(IOException ex)
			{
				rem.add(tt);
			}
		}
		
		clients.removeAll(rem);
		
		return count;
	}
	
	public void deleteClient(String clientID) throws Exception
	{
		for(TransmitterThread t: clients)
		{
			if(!t.getClientName().equals(clientID)) continue;
			t.Stop();
			clients.remove(t);
			return;
		}
	}
	
	public void close() throws Exception
	{
		for(TransmitterThread t: clients) t.Stop();
		clients.clear();
		thread.stop();
		server.close();
	}
	
	public int peopleOnline()
	{
		return clients.size();
	}
	
	public String getHost() throws Exception
	{
		String out="";
		
		Enumeration e = NetworkInterface.getNetworkInterfaces();
		while(e.hasMoreElements())
		{
			NetworkInterface n = (NetworkInterface) e.nextElement();
			Enumeration ee = n.getInetAddresses();
			while (ee.hasMoreElements())
			{
				InetAddress i = (InetAddress) ee.nextElement();
				if(i.toString().startsWith("/192") |
					i.toString().startsWith("/172") |
					i.toString().startsWith("/10"))
						
					out+=i.toString().substring(1)+", ";
			}
		}
		
		return out+"127.0.0.1";
	}
	
	public static void main(String[] args)
	{
		try
		{
			Server s=new Server(1234);
			Thread.sleep(5000);
			s.close();
		}
		catch(Exception ex)
		{
			
		}
	}
}
