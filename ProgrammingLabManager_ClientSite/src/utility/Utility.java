package utility;

import java.util.StringTokenizer;

public class Utility
{
	public static boolean validIP(String IP) throws NumberFormatException
	{
		StringTokenizer t=new StringTokenizer(IP,".");
		if(t.countTokens()!=4) return false;
		while(t.hasMoreTokens())
			if(Integer.parseInt(t.nextToken())<0 | Integer.parseInt(t.nextToken())>255) return false;
		return true;
	}
}
