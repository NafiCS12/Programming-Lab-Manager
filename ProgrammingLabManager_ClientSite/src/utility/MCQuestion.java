package utility;

import java.io.Serializable;

public class MCQuestion implements Serializable
{
	private String question;
	private String a,b,c,d;
	private char solution=0;
	
	public MCQuestion(String question,String a,String b,String c,String d)
	{
		this.question=question;
		this.a=a;
		this.b=b;
		this.c=c;
		this.d=d;
	}
	
	public void setSolution(char sol)
	{
		solution=sol;
	}
	
	public char getSolution()
	{
		return solution;
	}
	
	public void setQuestion(String question)
	{
		this.question=question;
	}
	
	public String getQuestion()
	{
		return question;
	}
	
	public void setAnswers(String a,String b,String c,String d)
	{
		this.a=a;
		this.b=b;
		this.c=c;
		this.d=d;
	}
	
	public String[] getAnswers()
	{
		String out[]={a,b,c,d};
		return out;
	}
	
}
