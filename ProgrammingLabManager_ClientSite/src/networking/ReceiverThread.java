package networking;

import java.io.File;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.nio.file.Files;
import miscelleneous.Logger;

public class ReceiverThread extends Thread
{
	private Socket socket=null;
	private ObjectInputStream istream=null;
	
	public ReceiverThread(Socket socket) throws Exception
	{
		this.socket=socket;
		istream=new ObjectInputStream(socket.getInputStream());
	}
	
	@Override
	public void run()
	{	
		while(true)
		{
			try
			{
				String fileName=(String)istream.readObject();
			
				if(fileName.equals("Onlines.plm"))
				{
					File f=new File("Onlines/"+fileName);
					Files.write(f.toPath(),(byte[])istream.readObject());
					Logger.log("Onlines updated.");
				}
				else if(fileName.equals("Testfile.plm"))
				{
					File f=new File("Testfiles/"+fileName);
					Files.write(f.toPath(),(byte[])istream.readObject());
					Logger.log("Exam database updated.");
				}
				else
				{
					File f=new File("ReceivedFiles/"+fileName);
					Files.write(f.toPath(),(byte[])istream.readObject());
					Logger.log("Files received.");
				}
			}
			catch(Exception ex)
			{
				Logger.log("Service disconnected.");
				break;
			}
		}
	}
	
	public void Stop() throws Exception
	{
		istream.close();
		stop();
	}
}
