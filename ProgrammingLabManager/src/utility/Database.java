package utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import miscelleneous.Logger;

public class Database
{
	private static final File onlineFile=new File("Onlines/Onlines.plm");
	private static final File testFile=new File("Testfiles/Testfile.plm");
	
	public static File writeOnlinesToDatabase(List<Online> onlines) throws Exception
	{
		ObjectOutputStream ostream=new ObjectOutputStream(new FileOutputStream(onlineFile));
		ostream.writeObject(onlines);
		Logger.log("Database updated.");
		
		return onlineFile;
	}
	
	public static List<Online> readOnlinesFromDatabase() throws Exception
	{
		List<Online> out;
		
		ObjectInputStream istream=new ObjectInputStream(new FileInputStream(onlineFile));
		out=(ArrayList)istream.readObject();
		
		return out;
	}
	
	public static File writeExamfileToDatabase(MCQExam exam) throws Exception
	{
		ObjectOutputStream ostream=new ObjectOutputStream(new FileOutputStream(testFile));
		ostream.writeObject(exam);
		Logger.log("Database updated.");
		
		return testFile;
	}
	
	public static MCQExam readExamFileFromDatabase() throws Exception
	{
		MCQExam out;
		
		ObjectInputStream istream=new ObjectInputStream(new FileInputStream(testFile));
		out=(MCQExam)istream.readObject();
		
		return out;
	}
	
	public static List<String> readAllFilesFromDirectory(String directory) throws Exception
	{
		List<String> out=new ArrayList();
		
		DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory));
		for(Path p:directoryStream) out.add(p.toString());
		
		return out;
	}
	
	
	
	public static void main(String args[]) throws Exception
	{
		List<Online> o=new ArrayList();
		writeOnlinesToDatabase(o);
	}
}
