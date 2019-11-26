package utility;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;

public class Online implements Serializable
{
	private String fileName=null;
	private byte[] fileData=null;
	private String title=null;
	private String description=null;
	
	private final long startTime;
	private int duration;
	
	public Online(String title,String description,long startTime,String duration)
	{
		this.title=title;
		this.description=description;
		this.startTime=startTime;
		this.duration=Integer.parseInt(duration);
	}
	
	public void setFile(String filePath)
	{
		if(filePath==null || filePath.equals("")) return;
		try
		{
			File f=new File(filePath);
			fileName=f.getName();
			fileData=Files.readAllBytes(f.toPath());
		}
		catch(IOException ex){}
	}
	
	public void setTitle(String title)
	{
		this.title=title;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void setDescription(String str)
	{
		this.description=str;
	}
	public String getDescription()
	{
		return description;
	}
	
	public long getStartTimeInMilisec()
	{
		return startTime;
	}
	
	public int getDuration()
	{
		return duration;
	}
	
	public void extendDuration(int min)
	{
		duration+=min;
	}
	
	public boolean fileAvailable()
	{
		return fileName!=null;
	}
	
	public File getFile() throws Exception
	{
		File out=new File("ReceivedFiles/"+fileName);
		Files.write(out.toPath(),fileData);
		return out;
	}
}
