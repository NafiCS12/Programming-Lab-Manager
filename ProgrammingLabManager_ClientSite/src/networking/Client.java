package networking;

import java.io.File;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;

public class Client
{
	private Socket socket=null;
	private ObjectOutputStream ostream=null;
	private ReceiverThread receiver=null;
	private String clientID=null;
	
	public Client(String clientID,String host,int port) throws Exception
	{
		this.clientID=clientID;
		socket=new Socket(host,port);
		ostream=new ObjectOutputStream(socket.getOutputStream());
		
		ostream.writeObject(clientID);
		ostream.flush();
		
		receiver=new ReceiverThread(socket);
		receiver.start();
	}
	
	public void send(File file) throws Exception
	{
		ostream.writeObject(file.getName());
		ostream.writeObject(Files.readAllBytes(file.toPath()));
	}
	
	public void send(File file,String fileName) throws Exception
	{
		ostream.writeObject(fileName);
		ostream.writeObject(Files.readAllBytes(file.toPath()));
	}
	
	public void send(String value,String title) throws Exception
	{
		ostream.writeObject(title);
		ostream.writeObject(value);
	}
	
	public String getClientID()
	{
		return clientID;
	}
	
	public void close() throws Exception
	{
		socket.close();
		ostream.close();
		receiver.Stop();
	}
	
	public static void main(String args[]) throws Exception	
	{
		Client c=new Client("Guest","localhost",10004);
		System.out.println("Done");
		Thread.sleep(10000);
		c.send(new File("a.txt"));
	}
}
