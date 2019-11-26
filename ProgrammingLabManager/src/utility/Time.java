package utility;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;

public class Time
{	
	public static String getCurrentTime()
	{
		Date d=Date.from(Instant.now());
		DateFormat df=DateFormat.getTimeInstance(DateFormat.MEDIUM);
		return df.format(d);
	}
	
	public static long getCurrentTimeInMilisec()
	{
		Date d=Date.from(Instant.now());
		return d.getTime();
	}
	
	public static int parseToMinute(long mils)
	{
		return (int)(mils/(1000*60));
	}
	public static void main(String args[])
	{
		System.out.println(getCurrentTime());
	}
	
}
