package miscelleneous;

import java.io.PrintStream;

public class Logger
{
	private static PrintStream logWriter;
	
	public static void log(String msg)
	{
		System.out.println(msg);
		logWriter.println(msg);
	}
	
	public static void prepareLogger(PrintStream logger)
	{
		logWriter=logger;
	}
}
