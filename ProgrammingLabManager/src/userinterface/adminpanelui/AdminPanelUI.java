package userinterface.adminpanelui;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import miscelleneous.Effects;
import miscelleneous.Shapes;
import networking.Server;
import utility.Database;
import utility.MCQExam;
import utility.MCQuestion;
import utility.Online;
import utility.Time;

public class AdminPanelUI extends Application
{
	Pane root=new Pane();
	Pane bodyPanel=new Pane();
	Pane softwareInfoPanel=null;
	Label logPanel=new Label();
	Group setupOnline,setupTest,sendFiles,receivedOffline,receivedProject,runningOnlines;
	
	private boolean isOpened=false;
	private Server server=null;
	private List<Online> availableOnlines=null;
	private MCQExam runningExam=null;
	
	
	@Override
	public void start(Stage stage) throws Exception
	{
		availableOnlines=Database.readOnlinesFromDatabase();
		runningExam=Database.readExamFileFromDatabase();
		
		if(stage.getTitle()==null) drawStage(stage);
		
		stage.show();
		isOpened=true;
	}
	
	private void drawStage(Stage stage)
	{
		Group navigationPanel=drawNavigationPanel();
		
		logPanel.setPrefSize(520,25);
		logPanel.setLayoutX(250);
		logPanel.setLayoutY(10);
		logPanel.setId("logPanel");
		try
		{
			logPanel.setText("Broadcasting IP address: "+server.getHost());
		}
		catch(Exception ex)
		{
			logPanel.setText("Failed to retrieve any broadcasting IP address");
		}
		logPanel.setOpacity(0.7);
		
		bodyPanel.setPrefSize(540,520);
		bodyPanel.setLayoutX(240);
		bodyPanel.setLayoutY(40);
		
		setupOnline=drawSetupOnline();
		setupTest=drawSetupTest();
		sendFiles=drawSendFiles();
		
		ImageView infoImg=new ImageView();
		infoImg.setFitHeight(25);
		infoImg.setFitWidth(25);
		infoImg.setLayoutX(20);
		infoImg.setLayoutY(565);
		infoImg.setId("infoImg");
		infoImg.setOnMouseEntered(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if(softwareInfoPanel!=null) root.getChildren().remove(softwareInfoPanel);
				softwareInfoPanel=drawSoftwareInfoPanel();
				root.getChildren().add(softwareInfoPanel);
			}
		});
		
		root.getChildren().addAll(navigationPanel,bodyPanel,logPanel,infoImg);
		
		Scene scene=new Scene(root,800,600);
		stage.setScene(scene);
		scene.getStylesheets().add(AdminPanelUI.class.getResource("adminpanelui.css").toExternalForm());
		stage.setResizable(false);
		stage.setTitle("Programming Lab Manager: Admin Panel");
		
		stage.setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent event)
			{
				isOpened=false;
			}
		});
		
	}
	
	Label activeNavigationItem=null;
	private Group drawNavigationPanel()
	{
		Group out=new Group();
		out.setLayoutX(20);
		out.setLayoutY(40);
		
		
		Rectangle navigationBG=new Rectangle(200,520);
		navigationBG.setId("navigationBG");
		navigationBG.setEffect(Effects.Blur(null));
		navigationBG.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				logPanel.setText("");
				bodyPanel.getChildren().clear();
				
				if(activeNavigationItem!=null) activeNavigationItem.setId("navigationItems");
				activeNavigationItem=null;
				
				try
				{
					logPanel.setText("Broadcasting IP address: "+server.getHost());
				}
				catch(Exception ex)
				{
					logPanel.setText("Unable to retrieve any broadcasting IP address");
				}
			}
		});
		
		Label setOnline=new Label("Setup an online");
		setOnline.setId("navigationItems");
		setOnline.setPrefSize(200,50);
		setOnline.setLayoutY(15);
		setOnline.setEffect(Effects.Light());
	
		setOnline.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if(activeNavigationItem==setOnline) return;
				if(activeNavigationItem!=null) activeNavigationItem.setId("navigationItems");
				activeNavigationItem=setOnline;
				setOnline.setId("navigationItems_Active");
				
				logPanel.setText("Setup an online");
				logPanel.setOpacity(0.7);
				logPanel.requestFocus();
				
				bodyPanel.getChildren().clear();
				bodyPanel.getChildren().add(setupOnline);
			}
		});
		
		Label setTest=new Label("Setup a Test");
		setTest.setId("navigationItems");
		setTest.setPrefSize(200,50);
		setTest.setLayoutY(60+5);
		setTest.setEffect(Effects.Light());
		
		setTest.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if(activeNavigationItem==setTest) return;
				if(activeNavigationItem!=null) activeNavigationItem.setId("navigationItems");
				activeNavigationItem=setTest;
				setTest.setId("navigationItems_Active");				
				
				logPanel.setText("Setup a test.");
				logPanel.setOpacity(0.7);
				logPanel.requestFocus();
				
				bodyPanel.getChildren().clear();
				bodyPanel.getChildren().add(setupTest);
				
				int remTime=0;
				if(runningExam!=null)
					remTime=runningExam.getDuration()-Time.parseToMinute(Time.getCurrentTimeInMilisec()-runningExam.getStartTime());
				
				if(remTime>0) 
				{
					bodyPanel.getChildren().clear();
					bodyPanel.getChildren().add(drawRunningTest());
					logPanel.setText("Currently an exam is running.");
				}
			}
		});
		
		Label sendFile=new Label("Send Files");
		sendFile.setId("navigationItems");
		sendFile.setPrefSize(200,50);
		sendFile.setLayoutY(110+5);
		sendFile.setEffect(Effects.Light());
		
		sendFile.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if(activeNavigationItem==sendFile) return;
				if(activeNavigationItem!=null) activeNavigationItem.setId("navigationItems");
				activeNavigationItem=sendFile;
				sendFile.setId("navigationItems_Active");
				
				logPanel.setText("Click ADD_FILE button to enlist files.");
				logPanel.setOpacity(0.7);
				logPanel.requestFocus();
				
				bodyPanel.getChildren().clear();
				bodyPanel.getChildren().add(sendFiles);
			}
		});
		
		Label recOffline=new Label("Received Offlines");
		recOffline.setId("navigationItems");
		recOffline.setPrefSize(200,50);
		recOffline.setLayoutY(160+5);
		recOffline.setEffect(Effects.Light());
		
		recOffline.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if(activeNavigationItem==recOffline) return;
				if(activeNavigationItem!=null) activeNavigationItem.setId("navigationItems");
				activeNavigationItem=recOffline;
				recOffline.setId("navigationItems_Active");				
				
				logPanel.setText("Showing received offlines.");
				logPanel.setOpacity(0.7);
				logPanel.requestFocus();
				
				receivedOffline=drawReceivedOffline();
				
				bodyPanel.getChildren().clear();
				bodyPanel.getChildren().add(receivedOffline);
			}
		});
		
		Label recProject=new Label("Received Projects");
		recProject.setId("navigationItems");
		recProject.setPrefSize(200,50);
		recProject.setLayoutY(210+5);
		recProject.setEffect(Effects.Light());
		
		recProject.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if(activeNavigationItem==recProject) return;
				if(activeNavigationItem!=null) activeNavigationItem.setId("navigationItems");
				activeNavigationItem=recProject;
				recProject.setId("navigationItems_Active");				

				logPanel.setText("Showing received projects.");
				logPanel.setOpacity(0.7);
				logPanel.requestFocus();
				
				receivedProject=drawReceivedProject();
				
				bodyPanel.getChildren().clear();
				bodyPanel.getChildren().add(receivedProject);
			}
		});
		
		Label runOnlines=new Label("Running Onlines");
		runOnlines.setId("navigationItems");
		runOnlines.setPrefSize(200,50);
		runOnlines.setLayoutY(260+5);
		runOnlines.setEffect(Effects.Light());
		
		runOnlines.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if(activeNavigationItem==runOnlines) return;
				if(activeNavigationItem!=null) activeNavigationItem.setId("navigationItems");
				activeNavigationItem=runOnlines;
				runOnlines.setId("navigationItems_Active");
				
				bodyPanel.getChildren().clear();
				
				try
				{
					availableOnlines=Database.readOnlinesFromDatabase();
					
					if(availableOnlines.isEmpty())
					{
						logPanel.setText("Currently no online assignment is running.");
						return;
					}
					
					logPanel.setText("Number of onlines running: "+availableOnlines.size());
					logPanel.setOpacity(0.7);
					
					runningOnlines=drawRunningOnlines();
					bodyPanel.getChildren().add(runningOnlines);
				}
				catch(Exception ex)
				{
					logPanel.setText("Fatal error occured while loading onlines.");
				}
							
			}
		});
		
		out.getChildren().addAll(navigationBG,setOnline,setTest,sendFile,recOffline,recProject,runOnlines);
		
		return out;
	}
	
	private Group initBodyPanel()
	{
		Group out=new Group();
		
		Rectangle bodyBG=new Rectangle(0,0,540,520);
		bodyBG.setId("navigationBG");
		bodyBG.setEffect(Effects.Blur(null));
		
		out.getChildren().add(bodyBG);
		
		return out;
	}  
	
	private Group drawSetupOnline()
	{
		Group out=initBodyPanel();
		
		TextField title=new TextField();
		title.setPrefSize(520,25);
		title.setLayoutX(10);
		title.setLayoutY(10);
		title.setPromptText("ONLINE TITLE (Maximum 32 characters)");
		
		title.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable,String oldValue,String newValue)
			{
				if(newValue.length()>32) title.setText(oldValue);
			}
		});
		
		TextArea description=new TextArea();
		description.setPrefSize(520,410);
		description.setLayoutX(10);
		description.setLayoutY(40);
		description.setPromptText("ONLINE DESCRIPTION");
		
		TextField startTime=new TextField();
		startTime.setPrefSize(220,25);
		startTime.setLayoutX(10);
		startTime.setLayoutY(455);
		startTime.setEditable(false);
		startTime.setText("START TIME: "+Time.getCurrentTime());
		
		Timeline t=new Timeline(new KeyFrame(Duration.seconds(1),new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				startTime.setText("START TIME: "+Time.getCurrentTime());
			}
		}));
		
		t.setCycleCount(Timeline.INDEFINITE);
		t.play();
		
		TextField duration=new TextField();
		duration.setPrefSize(220,25);
		duration.setLayoutX(10);
		duration.setLayoutY(485);
		duration.setPromptText("DURATION (in minute)");
		
		duration.textProperty().addListener(new ChangeListener<String>()
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
		
		TextField demoFile=new TextField();
		demoFile.setPrefSize(295,25);
		demoFile.setLayoutX(235);
		demoFile.setLayoutY(455);
		demoFile.setPromptText("FILE LINK");
		
		demoFile.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				FileChooser fc=new FileChooser();
				File f=fc.showOpenDialog(new Stage());
				if(f!=null) demoFile.setText(f.toPath().toString());
			}
		});
		
		Button startOnline=new Button("START ONLINE");
		startOnline.setPrefSize(145,25);
		startOnline.setLayoutX(235);
		startOnline.setLayoutY(485);
		
		startOnline.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				if(title.getText().trim().isEmpty() |
						description.getText().trim().isEmpty() | 
						startTime.getText().trim().isEmpty() | 
						duration.getText().trim().isEmpty())
				{
					logPanel.setText("Fields except FILE_LINK are mandatory");
					return;
				}
				
				Online online=new Online(title.getText(),description.getText(),
													Time.getCurrentTimeInMilisec(),duration.getText());
				online.setFile(demoFile.getText());
				
				try
				{
					availableOnlines.add(online);
					File f=Database.writeOnlinesToDatabase(availableOnlines);
					int count=server.send(f);
					logPanel.setText("Online successfully sent to "+count+" people.");
					
					title.setText("");
					description.setText("");
					duration.setText("");
					demoFile.setText("");
				}
				catch(Exception ex)
				{
					logPanel.setText("Fatal error occured while sending online!!!");
				}
			}
		});
		
		Button resetForm=new Button("RESET");
		resetForm.setPrefSize(145,25);
		resetForm.setLayoutX(385);
		resetForm.setLayoutY(485);
		
		resetForm.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				title.setText("");
				description.setText("");
				demoFile.setText("");
				duration.setText("");
			}
		});
		
		out.getChildren().addAll(description,title,startTime,duration,startOnline,resetForm,demoFile);
		return out;
	}
	
	private Group drawSetupTest()
	{
		Group out=initBodyPanel();
		
		TextField title=new TextField();
		title.setPrefSize(520,35);
		title.setLayoutX(10);
		title.setLayoutY(10);
		title.setAlignment(Pos.CENTER);
		title.setStyle("-fx-font-weight: bold;\n-fx-font-size: 18px;\n");
		title.setPromptText("TEST-TITLE");
		
		title.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable,String oldValue,String newValue)
			{
				if(newValue.length()>32) title.setText(oldValue);
			}
		});
		
		TextField startTime=new TextField("START TIME: "+Time.getCurrentTime());
		startTime.setPrefSize(200,25);
		startTime.setLayoutX(10);
		startTime.setLayoutY(50);
		startTime.setEditable(false);
		
		Timeline t=new Timeline(new KeyFrame(Duration.seconds(1),new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				startTime.setText("START TIME: "+Time.getCurrentTime());
			}
		}));
		
		t.setCycleCount(Timeline.INDEFINITE);
		t.play();
		
		TextField duration=new TextField();
		duration.setPrefSize(155,25);
		duration.setLayoutX(215);
		duration.setLayoutY(50);
		duration.setPromptText("DURATION (minutes)");
		
		duration.textProperty().addListener(new ChangeListener<String>()
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
		
		TextField totalQuestion=new TextField();
		totalQuestion.setPrefSize(155,25);
		totalQuestion.setLayoutX(375);
		totalQuestion.setLayoutY(50);
		totalQuestion.setPromptText("TOTAL QUESTIONS");
		
		totalQuestion.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable,String oldValue,String newValue)
			{
				if(newValue.isEmpty()) return;
				if(newValue.charAt(newValue.length()-1)<'0' |
						newValue.charAt(newValue.length()-1)>'9' |
						newValue.length()>2)
					totalQuestion.setText(oldValue);
				
				else if(Integer.parseInt(newValue)>30) totalQuestion.setText(""+30);
			}
		});
		
		Group questions=new Group();
		ScrollPane questionArea=new ScrollPane();
		questionArea.setPrefSize(520,400);
		questionArea.setLayoutX(10);
		questionArea.setLayoutY(80);
		questionArea.setContent(questions);
		
		Button proceed=new Button("NEXT");
		proceed.setPrefSize(145,25);
		proceed.setLayoutX(235);
		proceed.setLayoutY(485);
		
		List<MCQuestion> mcqList=new ArrayList();
		
		proceed.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				if(proceed.getText().equals("START TEST")) //otherwise it is "RESET"
				{
					if(title.getText().trim().isEmpty())
					{
						logPanel.setText("TITLE field cannot be empty.");
						return;
					}
					
					if(duration.getText().isEmpty())
					{
						logPanel.setText("DURATION field cannot be empty.");
						return;
					}
					
					if(Integer.parseInt(duration.getText())<=0)
					{
						logPanel.setText("Invalid duration.");
						duration.setText("");
						return;
					}
					
					int totalQ=mcqList.size();
					for(int i=0;i<totalQ;i++)
					{
						Group g=(Group)questions.getChildren().get(i);
						char ch=DrawInterface.optionEmpty(g);
						if(ch!=0)
						{
							if(ch=='q') logPanel.setText("Quesion "+(i+1)+" is empty!");
							else logPanel.setText("Option "+ch+" in question "+(i+1)+" is empty!");
							return;
						}
					}
					
					for(int i=0;i<totalQ;i++)
					{
						Group g=(Group)questions.getChildren().get(i);
						DrawInterface.populateQuestion(g,mcqList.get(i));
					}
					
					try
					{
						runningExam=new MCQExam(title.getText().trim(),mcqList,Time.getCurrentTimeInMilisec(),Integer.parseInt(duration.getText()));
						File f=Database.writeExamfileToDatabase(runningExam);
						server.send(f);
					}
					catch(Exception ex)
					{
						logPanel.setText("Error occured while sending test-files.");
						return;
					}
					
					logPanel.setText("Exam started.");
					
					setupTest=drawSetupTest();
					
					bodyPanel.getChildren().clear();
					bodyPanel.getChildren().add(drawRunningTest());
			
					return;
				}
				
				if(totalQuestion.getText().isEmpty())
				{
					logPanel.setText("You must fill TOTAL_QUESTIONS field to proceed.");
					return;
				}
				
				if(Integer.parseInt(totalQuestion.getText())<=0)
				{
					logPanel.setText("Invalid total question.");
					totalQuestion.setText("");
					return;
				}
				
				mcqList.clear();
				questions.getChildren().clear();
				int totalQ=Integer.parseInt(totalQuestion.getText());
			
				for(int i=0;i<totalQ;i++)
				{
					mcqList.add(new MCQuestion("","","","",""));
					Group temp=DrawInterface.drawMCQuestionComponents(mcqList.get(i),i+1,true);
					temp.setLayoutY(i*175);
					questions.getChildren().add(temp);
				}
				
				totalQuestion.setEditable(false);
				proceed.setText("START TEST");
				
				logPanel.setText("Fill-up the MCQ data. Click on choice characters to set correct answer.");
			}
		});
		
		Button resetForm=new Button("RESET");
		resetForm.setPrefSize(145,25);
		resetForm.setLayoutX(385);
		resetForm.setLayoutY(485);
		
		resetForm.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				duration.setText("");
				totalQuestion.setText("");
				totalQuestion.setEditable(true);
				title.setText("");
				proceed.setText("NEXT");
				mcqList.clear();
				questions.getChildren().clear();
				
				logPanel.setText("Setup an online MCQ exam.");
			}
		});
		
		out.getChildren().addAll(title,totalQuestion,duration,startTime,proceed,resetForm,questionArea);
		
		return out;
	}
	
	char solutions[];
	private Group drawRunningTest()
	{
		solutions=new char[runningExam.getQuestions().size()];
		
		Group out=initBodyPanel();
		
		ImageView editTest=new ImageView();
		ImageView cancelTest=new ImageView();
		Timeline t=new Timeline();
		Timeline t2=new Timeline();
		
		TextField title=new TextField(runningExam.getTitle().toUpperCase());
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
		
		Group questionComponents=new Group();
		ScrollPane questionPane=new ScrollPane();
		questionPane.setPrefSize(520,425);
		questionPane.setLayoutX(10);
		questionPane.setLayoutY(55);
		questionPane.setContent(questionComponents);
		questionPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		
		TextField resultLabel=new TextField("EXAM RESULT");
		resultLabel.setPrefSize(350,30);
		resultLabel.setLayoutX(10);
		resultLabel.setLayoutY(185);
		resultLabel.setStyle("-fx-alignment: CENTER;\n-fx-font-weight: bold;\n-fx-fong-size: 17px");
		resultLabel.setEditable(false);
		
		Button openTextFile=new Button("SHOW IN FILE");
		openTextFile.setPrefSize(165,30);
		openTextFile.setLayoutX(365);
		openTextFile.setLayoutY(480);
		openTextFile.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				File f=new File("TestResults/"+runningExam.getTitle()+".txt");
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							Desktop.getDesktop().open(f);
						}
						catch(IOException ex)
						{
							logPanel.setText("Failed to open "+f.getName());
						}
					}
				}).start();
			}
		});
		
		Button resetAll=new Button("RESET ALL");
		resetAll.setPrefSize(165,30);
		resetAll.setLayoutX(365);
		resetAll.setLayoutY(445);
		resetAll.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				t2.stop();
				runningExam=null;
				bodyPanel.getChildren().clear();
				bodyPanel.getChildren().add(drawSetupTest());
				
				logPanel.setText("Setup an MCQ Exam.");
				logPanel.requestFocus();
			}
		});
		
		
		Group results=new Group();
		ScrollPane resultPane=new ScrollPane();
		resultPane.setPrefSize(350,290);
		resultPane.setLayoutX(10);
		resultPane.setLayoutY(220);
		resultPane.setContent(results);
		resultPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		
		int totalQ=runningExam.getQuestions().size();
		for(int i=0;i<totalQ;i++)
		{
			Group temp=DrawInterface.drawMCQuestionComponents(runningExam.getQuestions().get(i),i+1,false);
			temp.setLayoutY(i*175);
			DrawInterface.setAnswerVisibility(temp,runningExam.getQuestions().get(i).getSolution(),true);
			questionComponents.getChildren().add(temp);
		}
		
		String tm=DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(runningExam.getStartTime()));
		TextField startTime=new TextField();
		startTime.setPrefSize(210,25);
		startTime.setLayoutX(10);
		startTime.setLayoutY(485);
		startTime.setText("STARTED AT: "+tm);
		startTime.setEditable(false);
		
		int remTime=runningExam.getDuration()-Time.parseToMinute(Time.getCurrentTimeInMilisec()-runningExam.getStartTime());
		if(remTime<0) remTime=0;
		
		TextField duration=new TextField("TIME REMAINING: "+remTime+" MIN");
		duration.setPrefSize(205,25);
		duration.setLayoutX(225);
		duration.setLayoutY(485);
		duration.setEditable(false);
		
		t.getKeyFrames().add(new KeyFrame(Duration.seconds(1),new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				int remTime=runningExam.getDuration()-Time.parseToMinute(Time.getCurrentTimeInMilisec()-runningExam.getStartTime());
				if(remTime<0) remTime=0;
				if(remTime==0)
				{
					logPanel.setText("Exam duration ended.");
					questionPane.setPrefHeight(120);
					out.getChildren().add(resultPane);
					out.getChildren().add(resultLabel);
					out.getChildren().add(openTextFile);
					out.getChildren().add(resetAll);
					out.getChildren().remove(editTest);
					out.getChildren().remove(cancelTest);
					out.getChildren().remove(startTime);
					out.getChildren().remove(duration);
					
					t2.play();
					t.stop();
				}
				duration.setText("TIME REMAINING: "+remTime+" MIN");
			}
		}));
		
		try
		{	
			File f=new File("TestResults/"+runningExam.getTitle()+".txt");
			f.createNewFile();
			BufferedReader bf=Files.newBufferedReader(f.toPath());
			t2.getKeyFrames().add(new KeyFrame(Duration.seconds(1),new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent event)
				{
					try
					{
						while(bf.ready())
						{
							String s=bf.readLine();
							TextField tf=new TextField(s);
							tf.setPrefSize(328,25);
							tf.setLayoutY(30*results.getChildren().size());
							tf.setEditable(false);

							results.getChildren().add(tf);
						}
					}
					catch(Exception ex) {}
				}
			}));
		}
		catch(Exception e)
		{
			logPanel.setText("Error occured while streaming result.");
			t2.stop();
		}
		
		t2.setCycleCount(Timeline.INDEFINITE);
		
		t.setCycleCount(Timeline.INDEFINITE);
		t.play();
		
		editTest.setFitHeight(35);
		editTest.setFitWidth(35);
		editTest.setLayoutX(445);
		editTest.setLayoutY(482);
		editTest.setId("editOnline");
		
		editTest.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				int totalQ=runningExam.getQuestions().size();
				
				if(editTest.getId().equals("applyChange"))
				{
					for(int i=0;i<totalQ;i++)
					{
						Group g=(Group)questionComponents.getChildren().get(i);
						DrawInterface.populateQuestion(g,runningExam.getQuestions().get(i));
						DrawInterface.setEditable(g,false);
					}
					
					try
					{
						File f=Database.writeExamfileToDatabase(runningExam);
						server.send(f);
					}
					catch(Exception ex)
					{
						logPanel.setText("Error occured while applying changes.");
						return;
					}
					
					logPanel.setText("Exam MCQs updated.");
					logPanel.requestFocus();
					
					editTest.setId("editOnline");
					cancelTest.setId("deleteOnline");
					
					return;
				}
				
				
				for(int i=0;i<totalQ;i++)
				{
					solutions[i]=runningExam.getQuestions().get(i).getSolution();
					Group g=(Group)questionComponents.getChildren().get(i);
					DrawInterface.setEditable(g,true);
				}
				
				editTest.setId("applyChange");
				cancelTest.setId("cancelChange");
				
				logPanel.setText("Type on MCQ fields to edit.");
				logPanel.requestFocus();
			}
		});
		
		cancelTest.setFitHeight(35);
		cancelTest.setFitWidth(35);
		cancelTest.setLayoutX(485);
		cancelTest.setLayoutY(482);
		cancelTest.setId("deleteOnline");
		
		cancelTest.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if(cancelTest.getId().equals("cancelChange"))
				{
					int totalQ=runningExam.getQuestions().size();
					for(int i=0;i<totalQ;i++)
					{
						runningExam.getQuestions().get(i).setSolution(solutions[i]);
						Group g=(Group)questionComponents.getChildren().get(i);
						DrawInterface.resetChangesToQuestion(g,runningExam.getQuestions().get(i));
						DrawInterface.setEditable(g,false);
						DrawInterface.setAnswerVisibility(g,runningExam.getQuestions().get(i).getSolution(),true);
					}
					
					editTest.setId("editOnline");
					cancelTest.setId("deleteOnline");
					logPanel.setText("");
					logPanel.requestFocus();
					
					return;
				}
				
				t.stop();
				runningExam=null;
				try
				{
					File f=Database.writeExamfileToDatabase(runningExam);
					server.send(f);
				}
				catch(Exception ex)
				{
					logPanel.setText("Error occured while informing students.");
					return;
				}
				
				logPanel.setText("Exam cancelled.");
				logPanel.requestFocus();
				
				bodyPanel.getChildren().clear();
				bodyPanel.getChildren().add(drawSetupTest());
			}
		});
		
		out.getChildren().addAll(title,questionPane,duration,startTime,editTest,cancelTest);
		return out;
	}
	
	private Group drawSendFiles()
	{
		Group out=initBodyPanel();
		List<File> files=new ArrayList();
		
		ScrollPane fileListPane=new ScrollPane();
		fileListPane.setPrefSize(520,470);
		fileListPane.setLayoutX(10);
		fileListPane.setLayoutY(10);
		fileListPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		
		Group fileList=new Group();
		fileListPane.setContent(fileList);
		
		Button addFile=new Button("ADD FILE");
		addFile.setPrefSize(145,25);
		addFile.setLayoutX(385);
		addFile.setLayoutY(485);
		
		addFile.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				FileChooser fc=new FileChooser();
				List<File>fls=fc.showOpenMultipleDialog(new Stage());
				
				if(fls==null) return;
				
				for(File f:fls)
				{
					TextField item=new TextField(f.getName());
					item.setPrefSize(498,25);
					item.setLayoutY(files.size()*30);
					item.setId("listItem");
					item.setOnMouseClicked(new EventHandler<MouseEvent>()
					{
						@Override
						public void handle(MouseEvent event)
						{
							int i=(int)((item.getLayoutY()+1)/30);
							files.remove(i);
							fileList.getChildren().remove(i);
							
							if(files.isEmpty())
							{
								fileListPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
								logPanel.setText("");
							}
							for(;i<fileList.getChildren().size();i++)
							{
								TextField tf=(TextField)fileList.getChildren().get(i);
								tf.setLayoutY(tf.getLayoutY()-30);
							}
							
						}
					});

					fileList.getChildren().add(item);
					files.add(f);
				}
				
				fileListPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
				logPanel.setText("Click on items to delete.");
			}
		});
		
		
		Button sendAll=new Button("SEND ALL");
		sendAll.setPrefSize(145,25);
		sendAll.setLayoutX(235);
		sendAll.setLayoutY(485);
		
		sendAll.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				if(files.isEmpty())
				{
					logPanel.setText("List is empty!");
					return;
				}
				
				int count=0;
				for(File f:files)
				{
					count=server.send(f); //Program will hang in case of
                                                              //bigger files and large number of clients.
				}
				logPanel.setText("Successfully sent files to "+count+" people.");
				files.clear();
				fileListPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
				fileList.getChildren().clear();
			}
		});
		
		out.getChildren().addAll(sendAll,addFile,fileListPane);
		return out;
	}
	
	private Group drawReceivedOffline()
	{
		Group out=initBodyPanel();
		List<String> fileNames;
		
		ScrollPane fileListPane=new ScrollPane();
		fileListPane.setPrefSize(520,470);
		fileListPane.setLayoutX(10);
		fileListPane.setLayoutY(10);
		fileListPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		
		Button refresh=new Button("RELOAD");
		refresh.setPrefSize(145,25);
		refresh.setLayoutX(235);
		refresh.setLayoutY(485);
		refresh.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				receivedOffline=drawReceivedOffline();
				bodyPanel.getChildren().clear();
				bodyPanel.getChildren().add(receivedOffline);
			}
		});
		
		Button openFolder=new Button("OPEN DIRECTORY");
		openFolder.setPrefSize(145,25);
		openFolder.setLayoutX(385);
		openFolder.setLayoutY(485);
		
		openFolder.setOnAction(new EventHandler<ActionEvent>()
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
							Desktop.getDesktop().open(new File("ReceivedOfflines/"));
						}
						catch(IOException ioe)
						{
							logPanel.setText("Unable to open directory: ReceivedOfflines/");
						}
					}
				}).start();
			}
		});
		
		try
		{
			fileNames=Database.readAllFilesFromDirectory("ReceivedOfflines");
			fileNames.sort(null);
			if(fileNames.isEmpty())
			{
				logPanel.setText("No offline assignment received.");
				fileListPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
				out.getChildren().addAll(refresh,openFolder);
				return out;
			}
		}
		catch(Exception ex)
		{
			logPanel.setText("Unable to read offline files.");
			return out;
		}
		
		
		Group fileList=new Group();
		fileListPane.setContent(fileList);
		
		for(int i=0;i<fileNames.size();i++)
		{
			TextField item=new TextField(fileNames.get(i));
			item.setPrefSize(498,25);
			item.setLayoutY(i*30);
			item.setEditable(false);
			item.setId("listItem");
			
			File f=new File(fileNames.get(i));
			item.setOnMouseClicked(new EventHandler<MouseEvent>()
			{
				@Override
				public void handle(MouseEvent event)
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
							catch(IOException ex)
							{
								logPanel.setText("Failed to open "+f.getName());
							}
						}
					}).start();
				}
			});
			
			fileList.getChildren().add(item);
		}
		
		
		logPanel.setText("Offline received from "+fileNames.size()+" people.");
		
		out.getChildren().addAll(fileListPane,refresh,openFolder);
		return out;
	}
	
	private Group drawReceivedProject()
	{
		Group out=initBodyPanel();
		
		List<String> fileNames;
		
		ScrollPane fileListPane=new ScrollPane();
		fileListPane.setPrefSize(520,470);
		fileListPane.setLayoutX(10);
		fileListPane.setLayoutY(10);
		fileListPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		
		Button refresh=new Button("RELOAD");
		refresh.setPrefSize(145,25);
		refresh.setLayoutX(235);
		refresh.setLayoutY(485);
		refresh.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				receivedProject=drawReceivedProject();
				bodyPanel.getChildren().clear();
				bodyPanel.getChildren().add(receivedProject);
			}
		});
		
		Button openFolder=new Button("OPEN DIRECTORY");
		openFolder.setPrefSize(145,25);
		openFolder.setLayoutX(385);
		openFolder.setLayoutY(485);
		
		openFolder.setOnAction(new EventHandler<ActionEvent>()
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
							Desktop.getDesktop().open(new File("ReceivedProjects/"));
						}
						catch(IOException ioe)
						{
							logPanel.setText("Unable to open directory: ReceivedProjects/");
						}
					}
				}).start();
			}
		});
		
		
		try
		{
			fileNames=Database.readAllFilesFromDirectory("ReceivedProjects");
			fileNames.sort(null);
			if(fileNames.isEmpty())
			{
				logPanel.setText("No project file received.");
				fileListPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
				out.getChildren().addAll(refresh,openFolder);
				return out;
			}
		}
		catch(Exception ex)
		{
			logPanel.setText("Unable to read project files.");
			return out;
		}
		
		
		Group fileList=new Group();
		fileListPane.setContent(fileList);
		
		for(int i=0;i<fileNames.size();i++)
		{
			TextField item=new TextField(fileNames.get(i));
			item.setPrefSize(498,25);
			item.setLayoutY(i*30);
			item.setEditable(false);
			item.setId("listItem");
			
			File f=new File(fileNames.get(i));
			item.setOnMouseClicked(new EventHandler<MouseEvent>()
			{
				@Override
				public void handle(MouseEvent event)
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
							catch(IOException ex)
							{
								logPanel.setText("Failed to open "+f.getName());
							}
						}
					}).start();
				}
			});
			
			fileList.getChildren().add(item);
		}
		
		
		logPanel.setText("Projects received from "+fileNames.size()+" people.");
		
		out.getChildren().addAll(fileListPane,refresh,openFolder);
		
		return out;
	}
	
	private int currentOnlineIndex=0;
	private Group drawRunningOnlines()
	{
		Group out=initBodyPanel();
		Group currentOnlineComponent=DrawInterface.drawOnlineComponents(availableOnlines.get(currentOnlineIndex),false);
		Polygon goLeft=Shapes.drawPolygonalDirectionSign(1),goRight=Shapes.drawPolygonalDirectionSign(1);
		
		goLeft.setLayoutX(16);
		goLeft.setLayoutY(13);
		goLeft.setRotate(180);
		goLeft.setId("polygonalDirection");
		goLeft.setEffect(Effects.Glow(Effects.Blur(null)));
		goLeft.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				currentOnlineIndex--;
				Group currentOnlineComponent=DrawInterface.drawOnlineComponents(availableOnlines.get(currentOnlineIndex),false);
				out.getChildren().set(1,currentOnlineComponent);
				
				if(currentOnlineIndex==0) goLeft.setDisable(true);
				if(currentOnlineIndex<availableOnlines.size()-1) goRight.setDisable(false);
				
				logPanel.setText("Available online(s): "+availableOnlines.size()+", Displaying online: "+(currentOnlineIndex+1));
			}
		});
		
		goRight.setLayoutX(514);
		goRight.setLayoutY(13);
//		goRight.setRotate(0.0);
		goRight.setId("polygonalDirection");
		goRight.setEffect(Effects.Glow(Effects.Blur(null)));
		goRight.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				currentOnlineIndex++;
				Group currentOnlineComponent=DrawInterface.drawOnlineComponents(availableOnlines.get(currentOnlineIndex),false);
				out.getChildren().set(1,currentOnlineComponent);
				
				if(currentOnlineIndex>0) goLeft.setDisable(false);
				if(currentOnlineIndex==availableOnlines.size()-1) goRight.setDisable(true);
				
				logPanel.setText("Available online(s): "+availableOnlines.size()+", Displaying online: "+(currentOnlineIndex+1));
			}
		});
		
		if(currentOnlineIndex==0) goLeft.setDisable(true);
		if(currentOnlineIndex<availableOnlines.size()-1) goRight.setDisable(false);
		if(currentOnlineIndex>0) goLeft.setDisable(false);
		if(currentOnlineIndex==availableOnlines.size()-1) goRight.setDisable(true);
		
		ImageView editOnline=new ImageView();
		ImageView deleteOnline=new ImageView();
		
		editOnline.setFitHeight(35);
		editOnline.setFitWidth(35);
		editOnline.setLayoutX(445);
		editOnline.setLayoutY(482);
		editOnline.setId("editOnline");
		
		editOnline.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if(editOnline.getId().equals("applyChange"))
				{
					goLeft.setVisible(true); goRight.setVisible(true);
					editOnline.setId("editOnline"); deleteOnline.setId("deleteOnline");
					
					TextField title=(TextField)((Group)out.getChildren().get(1)).getChildren().get(1);
					TextArea desc=(TextArea)((Group)out.getChildren().get(1)).getChildren().get(2);
					TextField extraMin=(TextField)((Group)out.getChildren().get(1)).getChildren().get(3);
					
					availableOnlines.get(currentOnlineIndex).setTitle(title.getText());
					availableOnlines.get(currentOnlineIndex).setDescription(desc.getText());
					availableOnlines.get(currentOnlineIndex).extendDuration(Integer.parseInt(extraMin.getText()));
					
					try
					{
						File f=Database.writeOnlinesToDatabase(availableOnlines);
						server.send(f);
						logPanel.setText("Online successfully updated.");
					}
					catch(Exception ex)
					{
						logPanel.setText("Failed to save online.");
					}
					
					Group currentOnlineComponent=DrawInterface.drawOnlineComponents(availableOnlines.get(currentOnlineIndex),false);
					out.getChildren().set(1,currentOnlineComponent);
					
					return;
				}
				
				Group currentOnlineComponent=DrawInterface.drawOnlineComponents(availableOnlines.get(currentOnlineIndex),true);
				out.getChildren().set(1,currentOnlineComponent);
				
				goLeft.setVisible(false); goRight.setVisible(false);
				editOnline.setId("applyChange"); deleteOnline.setId("cancelChange");
				
				logPanel.setText("Type on text to edit.");
			}
		});
		
		deleteOnline.setFitHeight(35);
		deleteOnline.setFitWidth(35);
		deleteOnline.setLayoutX(485);
		deleteOnline.setLayoutY(482);
		deleteOnline.setId("deleteOnline");
		
		deleteOnline.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if(deleteOnline.getId().equals("cancelChange"))
				{
					goLeft.setVisible(true); goRight.setVisible(true);
					editOnline.setId("editOnline"); deleteOnline.setId("deleteOnline");
					
					Group currentOnlineComponent=DrawInterface.drawOnlineComponents(availableOnlines.get(currentOnlineIndex),false);
					out.getChildren().set(1,currentOnlineComponent);
					
					logPanel.setText("Available online(s): "+availableOnlines.size()+", Displaying online: "+(currentOnlineIndex+1));
					
					return;
				}
				
				availableOnlines.remove(currentOnlineIndex);
				currentOnlineIndex=0;
				
				if(availableOnlines.isEmpty()) bodyPanel.getChildren().clear();
				else
				{
					Group currentOnlineComponent=DrawInterface.drawOnlineComponents(availableOnlines.get(currentOnlineIndex),false);
					out.getChildren().set(1,currentOnlineComponent);
					goLeft.setDisable(true);
					goRight.setDisable(availableOnlines.size()==1);
				}
				
				try
				{
					File f=Database.writeOnlinesToDatabase(availableOnlines);
					server.send(f);
					logPanel.setText("Online successfully deleted.");
				}
				catch(Exception ex)
				{
					logPanel.setText("Error occured while deleting online.");
				}
				
			}
		});
		
		out.getChildren().addAll(deleteOnline,editOnline,goRight,goLeft);
		
		out.getChildren().add(1,currentOnlineComponent);
		
		return out;
	}
	
	private Pane drawSoftwareInfoPanel()
	{
		Pane out=new Pane();
		out.setPrefSize(400,270);
		out.setLayoutX(10);
		out.setLayoutY(320);
		
		Rectangle bodyBG=new Rectangle(400,270);
		bodyBG.setId("navigationBG");
		bodyBG.setEffect(Effects.Blur(null));
		
		
		TextField label=new TextField("ABOUT US");
		label.setPrefSize(380,30);
		label.setLayoutX(10);
		label.setLayoutY(10);
		label.setEditable(false);
		label.setStyle("-fx-font-weight: bold;\n-fx-alignment: CENTER;-fx-font-size: 15px;");
		
		TextArea infoText=new TextArea();
		infoText.setPrefSize(380,215);
		infoText.setLayoutX(10);
		infoText.setLayoutY(45);
		infoText.setEditable(false);

		infoText.setText("This program is designed as an utility tool for "
				+ "programming labs in BUET. It offers a convenient way of performing "
				+ "required objectives in programming lab sessionals to both "
				+ "teachers and students.We developed this software as a "
				+ "Term Project of CSE-202 course (Object Oriented Programming "
				+ "Language Sessional) under the supervision of Mr. Siddhartha Das, Lecturer, "
				+ "Dept. of CSE, BUET.\n"
				+ "\n"
				+ "Sincerely,\n"
				+ "Ahmed Sayeed Wasif -1205056(aswasif007@gmail.com)\n"
				+ "&\n"
				+ "Nafi-us Sabbir -1205036(nafi.sabith.1993@gmail.com)\n"
				+ "\n"
				+ "\u00a92014"
				+ "\n");
		
		
		out.setOnMouseExited(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				root.getChildren().remove(softwareInfoPanel);
				softwareInfoPanel=null;
			}
		});
		
		out.getChildren().addAll(bodyBG,label,infoText);
		
		return out;
	}
	
	@Override
	public void stop()
	{
		try { super.stop(); } catch(Exception e) {}
	}
	
	public boolean isOpened()
	{
		return isOpened;
	}
	
	public void setOpened(boolean isOpened)
	{
		this.isOpened=isOpened;
	}
	
	public void setServer(Server server)
	{
		this.server=server;
	}
	
	public MCQExam getRunningExam()
	{
		return runningExam;
	}
}
