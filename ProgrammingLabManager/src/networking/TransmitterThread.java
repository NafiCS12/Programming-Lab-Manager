package networking;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.file.Files;
import miscelleneous.Logger;

public final class TransmitterThread extends Thread
{
	private Socket socket=null;
	private ObjectInputStream istream=null;
	private ObjectOutputStream ostream=null;
	
	private String clientName=null;
	
	public TransmitterThread(Socket socket) throws Exception
	{
		this.socket=socket;
	
		startConnection();
		clientName=(String)istream.readObject();
			
		send(new File("Onlines/Onlines.plm"));
		send(new File("Testfiles/Testfile.plm"));
			
		start();
	}
	
	private void startConnection() throws Exception
	{
		istream=new ObjectInputStream(socket.getInputStream());
		ostream=new ObjectOutputStream(socket.getOutputStream());
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			try
			{
				String fileName=(String)istream.readObject();
				System.out.println(fileName);
				if(fileName.startsWith("project"))
				{
					File f=new File("ReceivedProjects/"+fileName.replaceFirst("project",""));
					Files.write(f.toPath(),(byte[])istream.readObject());
					Logger.log("Project received from "+clientName);
				}
				else if(fileName.startsWith("offline"))
				{
					File f=new File("ReceivedOfflines/"+fileName.replaceFirst("offline",""));
					Files.write(f.toPath(),(byte[])istream.readObject());
					Logger.log("Offline received from "+clientName);
				}
				else
				{
					File f=new File("TestResults/"+fileName+".txt");
					String score=(String)istream.readObject();
					PrintStream ps=new PrintStream(f);
					ps.println(clientName+":  "+score); //FileName is the testResult
				}
			}
			catch(Exception e)
			{
				Logger.log(clientName+" disconnected.");
				break;
			}
		}
	}
	
	public void send(File file) throws IOException
	{
		ostream.writeObject(file.getName());
		ostream.writeObject(Files.readAllBytes(file.toPath()));
		ostream.flush();
	}
	
	
	public void Stop() throws IOException
	{
		istream.close();
		ostream.close();
		stop();
	}
	
	public String getClientName()
	{
		return clientName;
	}
}
