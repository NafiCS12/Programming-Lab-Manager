package userinterface;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import utility.MCQuestion;
import utility.Online;
import utility.Time;

public class DrawInterface
{
	public static char solution[];
	public static Group drawOnlineComponents(Online online)
	{
		Group onlineComponents=new Group();
		
		TextField title=new TextField(online.getTitle().toUpperCase());
		title.setPrefSize(520,40);
		title.setLayoutX(10);
		title.setLayoutY(10);
		title.setEditable(false);
		title.setId("onlineTitle");
		title.requestFocus();
		title.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable,String oldValue,String newValue)
			{
				if(newValue.length()>32) title.setText(oldValue);
			}
		});
		
		TextArea description=new TextArea(online.getDescription());
		description.setPrefSize(520,425);
		description.setLayoutX(10);
		description.setLayoutY(55);
		description.setEditable(false);
		
		String tm=DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(online.getStartTimeInMilisec()));
		TextField startTime=new TextField();
		startTime.setPrefSize(210,25);
		startTime.setLayoutX(10);
		startTime.setLayoutY(485);
		startTime.setText("STARTED AT: "+tm);
		startTime.setEditable(false);
		
		int remTime=online.getDuration()-Time.parseToMinute(Time.getCurrentTimeInMilisec()-online.getStartTimeInMilisec());
		if(remTime<0) remTime=0;
		
		TextField duration=new TextField("TIME REMAINING: "+remTime+" MIN");
		duration.setPrefSize(205,25);
		duration.setLayoutX(225);
		duration.setLayoutY(485);
		duration.setEditable(false);
		
		Timeline t=new Timeline();
		t.getKeyFrames().add(new KeyFrame(Duration.seconds(1),new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				int remTime=online.getDuration()-Time.parseToMinute(Time.getCurrentTimeInMilisec()-online.getStartTimeInMilisec());
				if(remTime<0) remTime=0;
				if(remTime==0) t.stop();
				duration.setText("TIME REMAINING: "+remTime+" MIN");
			}
		}));
		
		t.setCycleCount(Timeline.INDEFINITE);
		t.play();
		
		if(online.fileAvailable())
		{
			try
			{
				File f=online.getFile();
				Button openFile=new Button("OPEN FILE");
				openFile.setPrefSize(95,25);
				openFile.setLayoutX(435);
				openFile.setLayoutY(485);
				openFile.setOnAction(new EventHandler<ActionEvent>()
				{
					@Override
					public void handle(ActionEvent event)
					{
						new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								try
								{
									Desktop.getDesktop().open(f);
								}
								catch(IOException ex) {}
							}
						}).start();
						
					}
				});
				onlineComponents.getChildren().add(openFile);
			}
			catch(Exception e) {System.out.println("FDASDFASF");}
		}
		
		onlineComponents.getChildren().addAll(startTime,duration);
		onlineComponents.getChildren().add(1,title);
		onlineComponents.getChildren().add(2,description);
		
		return onlineComponents;
	}

	public static Group drawMCQuestionComponents(MCQuestion mcq,int qNo,boolean editable)
	{
		Group out=new Group();
		
		TextField label=new TextField("Q: "+(qNo+1));
		label.setPrefSize(52,30);
		label.setLayoutY(20);
		label.setEditable(false);
		label.setStyle("-fx-font-weight: bold;\n-fx-font-size: 12px;");
		
		TextField question=new TextField(mcq.getQuestion());
		question.setPrefSize(440,30);
		question.setLayoutX(57);
		question.setLayoutY(20);
		question.setEditable(editable);
		question.setPromptText("QUESTION NO "+(qNo+1));
		question.setStyle("-fx-font-weight: bold;\n-fx-font-size: 15px;");
		
		out.getChildren().add(question);
		out.getChildren().add(label);

		TextField options[]=new TextField[4];
		
		for(char seq='a';seq<='d';seq++)
		{
			Group option=drawOption(mcq,seq,editable);
			option.setLayoutY(55+(seq-'a')*30);
			out.getChildren().add(seq-'a'+2,option);
			
			options[seq-'a']=(TextField)option.getChildren().get(0);
		}
		
		options[0].setOnMouseClicked(new EventHandler<Event>()
		{
			@Override
			public void handle(Event event)
			{
				for(int j=0;j<4;j++) if(j!=0) options[j].setId("listItem");
				options[0].setId("listItemActive");
				
				solution[qNo]='a';
			}
		});
		
		options[1].setOnMouseClicked(new EventHandler<Event>()
		{
			@Override
			public void handle(Event event)
			{
				for(int j=0;j<4;j++) if(j!=1) options[j].setId("listItem");
				options[1].setId("listItemActive");
				
				solution[qNo]='b';
			}
		});
		
		options[2].setOnMouseClicked(new EventHandler<Event>()
		{
			@Override
			public void handle(Event event)
			{
				for(int j=0;j<4;j++) if(j!=2) options[j].setId("listItem");
				options[2].setId("listItemActive");
				
				solution[qNo]='c';
			}
		});
		
		options[3].setOnMouseClicked(new EventHandler<Event>()
		{
			@Override
			public void handle(Event event)
			{
				for(int j=0;j<4;j++) if(j!=3) options[j].setId("listItem");
				options[3].setId("listItemActive");
				
				solution[qNo]='d';
			}
		});
		
		if(solution[qNo]!=0) options[solution[qNo]-'a'].setId("listItemActive");
		
		return out;
	}
	
	public static int checkValid(MCQuestion mcq,int qNo)
	{
		if(mcq.getSolution()==solution[qNo]) return 1;
		return 0;
	}
	
	public static void freezeMCQ(Group mcqComponent)
	{
		for(char seq='a';seq<='d';seq++)
		{
			Group g=(Group)mcqComponent.getChildren().get(seq-'a'+2);
			((TextField)g.getChildren().get(0)).setDisable(true);
		}
	}
	
	private static Group drawOption(MCQuestion mcq,char seq,boolean editable)
	{
		Group out=new Group();
		
		TextField label=new TextField(""+seq);
		label.setPrefSize(35,25);
		label.setEditable(false);
		label.setAlignment(Pos.CENTER);
		label.setId("listItem");
		
		TextField field=new TextField(mcq.getAnswers()[seq-'a']);
		field.setPrefSize(440,25);
		field.setLayoutX(40);
		field.setEditable(editable);
		
		out.getChildren().add(field);
		out.getChildren().add(0,label);
		
		return out;
	}
}
