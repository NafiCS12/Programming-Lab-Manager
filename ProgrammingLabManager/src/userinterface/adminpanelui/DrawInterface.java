package userinterface.adminpanelui;

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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import utility.MCQuestion;
import utility.Online;
import utility.Time;

public class DrawInterface
{
	public static Group drawOnlineComponents(Online online,boolean editable)
	{
		Group onlineComponents=new Group();
		
		TextField title=new TextField(online.getTitle().toUpperCase());
		title.setPrefSize(520,40);
		title.setLayoutX(10);
		title.setLayoutY(10);
		title.setEditable(editable);
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
		description.setEditable(editable);
		
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
		duration.setVisible(!editable);
		
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
		
		
		TextField extendTime=new TextField();
		extendTime.setPrefSize(205,25);
		extendTime.setLayoutX(225);
		extendTime.setLayoutY(485);
		extendTime.setEditable(true);
		extendTime.setVisible(editable);
		extendTime.setPromptText("ADD EXTRA MINUTES");
		extendTime.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable,String oldValue,String newValue)
			{
				if(newValue.isEmpty()) return;
				if(newValue.charAt(newValue.length()-1)<'0' |
						newValue.charAt(newValue.length()-1)>'9' |
						newValue.length()>3)
					duration.setText(oldValue);
			}
		});
		
		
		onlineComponents.getChildren().addAll(startTime,duration);
		onlineComponents.getChildren().add(1,title);
		onlineComponents.getChildren().add(2,description);
		onlineComponents.getChildren().add(3,extendTime);
		
		return onlineComponents;
	}
	
	public static Group drawMCQuestionComponents(MCQuestion mcq,int qNo,boolean editable)
	{
		Group out=new Group();
		
		TextField label=new TextField("Q: "+qNo);
		label.setPrefSize(52,30);
		label.setLayoutY(20);
		label.setEditable(false);
		label.setStyle("-fx-font-weight: bold;\n-fx-font-size: 12px;");
		
		TextField question=new TextField(mcq.getQuestion());
		question.setPrefSize(440,30);
		question.setLayoutX(57);
		question.setLayoutY(20);
		question.setEditable(editable);
		question.setPromptText("QUESTION NO "+qNo);
		question.setStyle("-fx-font-weight: bold;\n-fx-font-size: 12px;");
		
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
				
				mcq.setSolution('a');
			}
		});
		
		options[1].setOnMouseClicked(new EventHandler<Event>()
		{
			@Override
			public void handle(Event event)
			{
				for(int j=0;j<4;j++) if(j!=1) options[j].setId("listItem");
				options[1].setId("listItemActive");
				
				mcq.setSolution('b');
			}
		});
		
		options[2].setOnMouseClicked(new EventHandler<Event>()
		{
			@Override
			public void handle(Event event)
			{
				for(int j=0;j<4;j++) if(j!=2) options[j].setId("listItem");
				options[2].setId("listItemActive");
				
				mcq.setSolution('c');
			}
		});
		
		options[3].setOnMouseClicked(new EventHandler<Event>()
		{
			@Override
			public void handle(Event event)
			{
				for(int j=0;j<4;j++) if(j!=3) options[j].setId("listItem");
				options[3].setId("listItemActive");
				
				mcq.setSolution('d');
			}
		});

		return out;
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
	
	public static char optionEmpty(Group mcqComponent)
	{
		if(((TextField)(mcqComponent.getChildren().get(0))).getText().trim().isEmpty()) return 'q';
		
		for(char seq='a';seq<='d';seq++)
		{
			Group option=(Group)mcqComponent.getChildren().get(seq-'a'+2);
			if(((TextField)(option.getChildren().get(1))).getText().trim().isEmpty()) return seq;
		}
		
		return 0;
	}
	
	public static void populateQuestion(Group mcqComponent,MCQuestion mcq)
	{
		String qes=((TextField)(mcqComponent.getChildren().get(0))).getText().trim();
		mcq.setQuestion(qes);
		
		String op[]=new String[4];
		for(char seq='a';seq<='d';seq++)
		{
			Group option=(Group)mcqComponent.getChildren().get(seq-'a'+2);
			op[seq-'a']=((TextField)(option.getChildren().get(1))).getText().trim();
		}
		mcq.setAnswers(op[0],op[1],op[2],op[3]);
	}
	
	public static void resetChangesToQuestion(Group mcqComponent,MCQuestion mcq)
	{
		((TextField)(mcqComponent.getChildren().get(0))).setText(mcq.getQuestion());
		
		String sol[]=mcq.getAnswers();
		for(char seq='a';seq<='d';seq++)
		{
			Group option=(Group)mcqComponent.getChildren().get(seq-'a'+2);
			((TextField)(option.getChildren().get(1))).setText(sol[seq-'a']);
			
			if(seq!=mcq.getSolution()) ((TextField)(option.getChildren().get(0))).setId("listItem");
			else ((TextField)(option.getChildren().get(0))).setId("listItemActive");
		}
	}
	
	public static void setEditable(Group mcqComponent,boolean editable)
	{
		((TextField)(mcqComponent.getChildren().get(0))).setEditable(editable);
		for(char seq='a';seq<='d';seq++)
		{
			Group option=(Group)mcqComponent.getChildren().get(seq-'a'+2);
			((TextField)(option.getChildren().get(1))).setEditable(editable);
			((TextField)(option.getChildren().get(0))).setDisable(!editable);
			
		}
	}
	
	public static void setAnswerVisibility(Group mcqComponent,char ans,boolean visible)
	{
		for(char seq='a';seq<='d';seq++)
		{
			Group option=(Group)mcqComponent.getChildren().get(seq-'a'+2);
			if(seq!=ans) ((TextField)(option.getChildren().get(0))).setId("listItem");
			else ((TextField)(option.getChildren().get(0))).setId("listItemActive");
			((TextField)(option.getChildren().get(0))).setDisable(visible);
		}
	}
}
