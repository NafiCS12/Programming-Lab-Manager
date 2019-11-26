package utility;

import java.io.Serializable;
import java.util.List;

public class MCQExam implements Serializable
{
	private String title=null;
	private List<MCQuestion> questions=null;
	private int duration;
	private long startTime;
	
	public MCQExam(String title,List<MCQuestion> questions,long startTime,int duration)
	{
		this.title=title;
		this.questions=questions;
		this.duration=duration;
		this.startTime=startTime;
	}
	
	public void setTitle(String title)
	{
		this.title=title;
	}
	
	public void setQuestions(List<MCQuestion> questions)
	{
		this.questions=questions;
	}
	
	public void setDuration(int duration)
	{
		this.duration=duration;
	}
	
	public void setStartTime(long startTime)
	{
		this.startTime=startTime;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public List<MCQuestion> getQuestions()
	{
		return questions;
	}
	
	public int getDuration()
	{
		return duration;
	}
	
	public long getStartTime()
	{
		return startTime;
	}
}
