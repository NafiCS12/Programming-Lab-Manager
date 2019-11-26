package userinterface;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import miscelleneous.Effects;
import miscelleneous.Logger;
import miscelleneous.Shapes;
import networking.Client;
import utility.Database;
import utility.MCQExam;
import utility.Online;
import utility.Time;
import utility.Utility;

public class ClientPanelUI extends Application
{
	Pane root=new Pane();
	Pane bodyPanel=new Pane();
	Pane softwareInfoPanel=null,settingsPanel=null;
	
	Label logPanel=new Label();

	Group runningOnlines,runningTest,submitOffline,submitProject,receivedFiles;
	
	
	private List<Online> availableOnlines=new ArrayList();
	private MCQExam runningExam=null;
	private final char examAns[]=new char[30];
	private Client client=null;
	
	private String HOST=null;
	private final int PORT=10004;
	private String clientID;
	
	private Timeline logStreamChecker;
	private final File logFile=new File("Client.log");
	private final File CONFIG=new File("config.inf");
	
	@Override
	public void start(Stage stage) throws Exception
	{
		HOST=new DataInputStream(new FileInputStream(new File("config.inf"))).readLine();
		for(int i=0;i<30;i++) examAns[i]=0;
		DrawInterface.solution=examAns;
		
		logPanel.setPrefSize(520,25);
		logPanel.setLayoutX(250);
		logPanel.setLayoutY(10);
		logPanel.setId("logPanel");
		logPanel.setOpacity(0.7);
		logPanel.setText("Welcome to Programming Lab Manager!");
		
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
				softwareInfoPanel=drawSoftwareInfoPanel();
				root.getChildren().add(softwareInfoPanel);
			}
		});
		
		root.getChildren().add(drawLoginPanel());
		root.getChildren().add(logPanel);
		root.getChildren().add(infoImg);
		
		PrintStream logWriter=new PrintStream(new FileOutputStream(logFile));
		BufferedReader logReader=Files.newBufferedReader(logFile.toPath());
		Logger.prepareLogger(logWriter);
		
		logStreamChecker=new Timeline(new KeyFrame(Duration.seconds(2),new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				try
				{
					if(!logReader.ready()) return;
					String s=logReader.readLine();
					
					logPanel.setText(s);
					
					if(s.equals("Service disconnected."))
					{
						client.close();
						client=null;
					}
					
					if(bodyPanel.getChildren().isEmpty()) return;
					
					switch (s)
					{
					case "Onlines updated.":
						if(bodyPanel.getChildren().get(0)!=runningOnlines) return;
						bodyPanel.getChildren().clear();
						try
						{
							availableOnlines=Database.readOnlinesFromDatabase();

							if(availableOnlines.isEmpty())
							{
								logPanel.setText("Currently no online assignment is running.");
								runningOnlines=new Group();
								bodyPanel.getChildren().add(runningOnlines);
								return;
							}

							logPanel.setText("Number of onlines running: "+availableOnlines.size());
							logPanel.setOpacity(0.7);
							logPanel.requestFocus();
							
							if(currentOnlineIndex>=availableOnlines.size()) currentOnlineIndex=0;
							runningOnlines=drawRunningOnline();
							bodyPanel.getChildren().add(runningOnlines);
						}
						catch(Exception ex)
						{
							logPanel.setText("Fatal error occured while loading onlines.");
						}
						break;
						
					case "Files received.":
						if(bodyPanel.getChildren().get(0)!=receivedFiles) return;
						receivedFiles=drawReceivedFiles();
						bodyPanel.getChildren().set(0,receivedFiles);
						break;
						
					case "Exam database updated.":
						if(bodyPanel.getChildren().get(0)!=runningTest) return;
						bodyPanel.getChildren().clear();
						try
						{
							runningExam=Database.readExamFileFromDatabase();
							int remTime=0;
							if(runningExam!=null) remTime=runningExam.getDuration()-Time.parseToMinute(Time.getCurrentTimeInMilisec()-runningExam.getStartTime());
					
							if(runningExam==null | remTime<=0)
							{
								for(int i=0;i<30;i++) examAns[i]=0;
								DrawInterface.solution=examAns;
								runningTest=new Group();
								bodyPanel.getChildren().add(runningTest);
								logPanel.setText("Currently no online exam running.");
								return;
							}
							runningTest=drawRunningTest();
							bodyPanel.getChildren().add(runningTest);
						}
						catch(IOException e)
						{
							logPanel.setText("Fatal error occured while updating exam database.");
						}
					}
				}
				catch(Exception ex)
				{
					logPanel.setText("Auto-synchronization failed.");
				}
			}
		}));
		
		logStreamChecker.setCycleCount(Timeline.INDEFINITE);
		
		Scene scene=new Scene(root,800,600);
		stage.setScene(scene);
		scene.getStylesheets().add(ClientPanelUI.class.getResource("clientpanelui.css").toExternalForm());
		stage.setResizable(false);
		stage.setTitle("Programming Lab Manager");
		
		stage.setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent event)
			{
				logStreamChecker.stop();
				
				if(client!=null) try
				{
					client.close();
					client=null;
				}
				catch(Exception ex) {}
			}
		});
		
		stage.show();
	}
	
	private Pane drawLoginPanel()
	{
		Pane loginPanel=new Pane();
                
		TextField label=new TextField("PLEASE ENTER YOUR STUDENT ID");
		label.setPrefSize(350,30);
		label.setLayoutX(225);
		label.setLayoutY(220);
		label.setDisable(true);
		label.setId("loginPanelLabel");
		
		TextField raw=new TextField();
		raw.setPrefSize(0,0);
		raw.setOpacity(0);
		raw.setLayoutX(-20);
		
		TextField sID=new TextField();
		sID.setPrefSize(350,30);
		sID.setLayoutX(225);
		sID.setLayoutY(255);
		sID.setPromptText("example: 1201001");
		sID.setId("loginPanelID");
		sID.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable,String oldValue,String newValue)
			{
				if(newValue.isEmpty()) return;
				if(newValue.charAt(newValue.length()-1)<'0' |
						newValue.charAt(newValue.length()-1)>'9' |
						newValue.length()>7)
					sID.setText(oldValue);
			}
		});
		
                Timeline tryConnectionThread=new Timeline(new KeyFrame(Duration.millis(250), new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        try
                        {
                            clientID=sID.getText();
                            client=new Client(clientID,HOST,PORT);
                            root.getChildren().set(0,drawMainPanel());
                            logPanel.setText("Connected to "+HOST+" using ID: "+clientID);
                        }
                        catch (Exception ex)
                        {
                            logPanel.setText("No response from "+HOST);
                        }
                    }
                }));
                
		Button proceed=new Button("SIGN IN");
		proceed.setPrefSize(100,30);
		proceed.setLayoutX(350);
		proceed.setLayoutY(290);
		proceed.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				if(sID.getText().length()!=7)
				{
					logPanel.setText("Wrong student ID!");
					sID.requestFocus();
					return;
				}
                                
                                if(tryConnectionThread.getStatus().equals(Status.RUNNING)) return;

                                logPanel.setText("Connecting to "+HOST+", please wait.");
                                tryConnectionThread.playFromStart();
			}
		});
		
		ImageView setting=new ImageView();
		setting.setFitHeight(25);
		setting.setFitWidth(25);
		setting.setLayoutX(20);
		setting.setLayoutY(10);
		setting.setId("settings");
		
		setting.setOnMouseEntered(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if(settingsPanel!=null) root.getChildren().remove(settingsPanel);
				settingsPanel=drawSettingsPanel();
				root.getChildren().add(settingsPanel);
			}
		});
		
		loginPanel.getChildren().addAll(setting,label,sID,proceed);
		return loginPanel;
	}
	
	private Pane drawMainPanel()
	{
		Pane mainPanel=new Pane();
		
		Group navigationPanel=drawNavigationPanel();
		
		ImageView logOut=new ImageView();
		logOut.setFitHeight(25);
		logOut.setFitWidth(25);
		logOut.setLayoutX(20);
		logOut.setLayoutY(10);
		logOut.setId("logOut");
		
		logOut.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				try
				{
					client.close();
				}
				catch(Exception ex) {}
				
				client=null;
				root.getChildren().set(0,drawLoginPanel());
				
				logStreamChecker.stop();
				logPanel.setText("Disconnected.");
			}
		});
		
		ImageView refresh=new ImageView();
		refresh.setFitHeight(25);
		refresh.setFitWidth(25);
		refresh.setLayoutX(50);
		refresh.setLayoutY(10);
		refresh.setId("refresh");
		
		refresh.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				try
				{
					if(client!=null)
					{
						client.close();
						client=null;
					}
					client=new Client(clientID,HOST,PORT);
					bodyPanel.getChildren().clear();
					root.getChildren().set(0,drawMainPanel());
					logPanel.setText("Connected to "+HOST+" using ID: "+clientID);
				}
				catch(Exception ex)
				{
					logPanel.setText("Failed to connect to "+HOST);
				}
			}
		});
		
		logPanel.setPrefSize(520,25);
		logPanel.setLayoutX(250);
		logPanel.setLayoutY(10);
		logPanel.setId("logPanel");
		logPanel.setOpacity(0.7);
		
		bodyPanel.setPrefSize(540,520);
		bodyPanel.setLayoutX(240);
		bodyPanel.setLayoutY(40);
		
		submitOffline=drawSubmitOffline();
		submitProject=drawSubmitProject();
		
		mainPanel.getChildren().addAll(logOut,refresh,navigationPanel,bodyPanel);
		
		logStreamChecker.play();
		
		return mainPanel;
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
				if(client==null) logPanel.setText("Service disconnected. Click refresh to reconnect.");
				else logPanel.setText("Connected to "+HOST+" using ID: "+clientID);
				
				bodyPanel.getChildren().clear();
				
				if(activeNavigationItem!=null) activeNavigationItem.setId("navigationItems");
				activeNavigationItem=null;
			}
		});
		
		Label runOnlines=new Label("Running Onlines");
		runOnlines.setId("navigationItems");
		runOnlines.setPrefSize(200,50);
		runOnlines.setLayoutY(15);
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
						runningOnlines=new Group();
						bodyPanel.getChildren().add(runningOnlines);
						return;
					}
					
					if(client!=null) logPanel.setText("Number of onlines running: "+availableOnlines.size());
					else logPanel.setText("Service disconnected Click refresh to reconnect.");
					
					logPanel.setOpacity(0.7);
					logPanel.requestFocus();
					
					runningOnlines=drawRunningOnline();
					bodyPanel.getChildren().add(runningOnlines);
				}
				catch(Exception ex)
				{
					logPanel.setText("Fatal error occured while loading onlines.");
				}
							
			}
		});
		
		Label runTest=new Label("Running Test");
		runTest.setId("navigationItems");
		runTest.setPrefSize(200,50);
		runTest.setLayoutY(65);
		runTest.setEffect(Effects.Light());
		
		runTest.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if(activeNavigationItem==runTest) return;
				if(activeNavigationItem!=null) activeNavigationItem.setId("navigationItems");
				activeNavigationItem=runTest;
				runTest.setId("navigationItems_Active");
				
				if(client!=null) logPanel.setText("Signed in as ID: "+client.getClientID());
				else logPanel.setText("Service disconnected. Click refresh to reconnect.");
				
				logPanel.setOpacity(0.7);
				logPanel.requestFocus();
				
				bodyPanel.getChildren().clear();
				
				try
				{
					runningExam=Database.readExamFileFromDatabase();
					int remTime=0;
					if(runningExam!=null) remTime=runningExam.getDuration()-Time.parseToMinute(Time.getCurrentTimeInMilisec()-runningExam.getStartTime());
					if(runningExam==null | remTime<=0)
					{
						logPanel.setText("Currently no exam running.");
						runningTest=new Group();
						bodyPanel.getChildren().add(runningTest);
						return;
					}
				}
				catch(Exception ex)
				{
					logPanel.setText("Error occured while loading exam from database.");
				}
				
				runningTest=drawRunningTest();
				bodyPanel.getChildren().add(runningTest);
			}
		});
		
		Label subOffline=new Label("Submit Offline");
		subOffline.setId("navigationItems");
		subOffline.setPrefSize(200,50);
		subOffline.setLayoutY(115);
		subOffline.setEffect(Effects.Light());
		
		subOffline.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if(activeNavigationItem==subOffline) return;
				if(activeNavigationItem!=null) activeNavigationItem.setId("navigationItems");
				activeNavigationItem=subOffline;
				subOffline.setId("navigationItems_Active");
				
				if(client!=null) logPanel.setText("Signed in as ID: "+client.getClientID());
				else logPanel.setText("Service disconnected. Click refresh to reconnect.");
				
				logPanel.setOpacity(0.7);
				logPanel.requestFocus();
				
				bodyPanel.getChildren().clear();
				bodyPanel.getChildren().add(submitOffline);
			}
		});
		
		Label subProject=new Label("Submit Projects");
		subProject.setId("navigationItems");
		subProject.setPrefSize(200,50);
		subProject.setLayoutY(165);
		subProject.setEffect(Effects.Light());
		
		subProject.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if(activeNavigationItem==subProject) return;
				if(activeNavigationItem!=null) activeNavigationItem.setId("navigationItems");
				activeNavigationItem=subProject;
				subProject.setId("navigationItems_Active");	
				
				if(client!=null) logPanel.setText("Signed in as ID: "+client.getClientID());
				else logPanel.setText("Service disconnected. Click refresh to reconnect.");
				
				logPanel.setOpacity(0.7);
				logPanel.requestFocus();			

				bodyPanel.getChildren().clear();
				bodyPanel.getChildren().add(submitProject);
			}
		});
		
		Label recFiles=new Label("Received Files");
		recFiles.setId("navigationItems");
		recFiles.setPrefSize(200,50);
		recFiles.setLayoutY(215);
		recFiles.setEffect(Effects.Light());
		
		recFiles.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if(activeNavigationItem==recFiles) return;
				if(activeNavigationItem!=null) activeNavigationItem.setId("navigationItems");
				activeNavigationItem=recFiles;
				recFiles.setId("navigationItems_Active");				
				
				if(client!=null) logPanel.setText("Signed in as ID: "+client.getClientID());
				else logPanel.setText("Service disconnected Click refresh to reconnect.");
				
				logPanel.setOpacity(0.7);
				logPanel.requestFocus();
				
				receivedFiles=drawReceivedFiles();
				bodyPanel.getChildren().clear();
				bodyPanel.getChildren().add(receivedFiles);
			}
		});
		
		
		out.getChildren().addAll(navigationBG,runOnlines,runTest,subOffline,subProject,recFiles);
		
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
	
	private int currentOnlineIndex=0;
	private Group drawRunningOnline()
	{
		Group out=initBodyPanel();
		Group currentOnlineComponent=DrawInterface.drawOnlineComponents(availableOnlines.get(currentOnlineIndex));
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
				Group currentOnlineComponent=DrawInterface.drawOnlineComponents(availableOnlines.get(currentOnlineIndex));
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
				Group currentOnlineComponent=DrawInterface.drawOnlineComponents(availableOnlines.get(currentOnlineIndex));
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
		
		
		out.getChildren().addAll(goRight,goLeft);
		out.getChildren().add(1,currentOnlineComponent);
		
		return out;
	}
	
	private Group drawRunningTest()
	{
		Group out=initBodyPanel();
		
		TextField title=new TextField(runningExam.getTitle().toUpperCase());
		title.setPrefSize(520,40);
		title.setLayoutX(10);
		title.setLayoutY(10);
		title.setEditable(false);
		title.setId("onlineTitle");
		
		Group questionComponents=new Group();
		ScrollPane questionPane=new ScrollPane();
		questionPane.setPrefSize(520,425);
		questionPane.setLayoutX(10);
		questionPane.setLayoutY(55);
		questionPane.setContent(questionComponents);
		questionPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		
		int totalQ=runningExam.getQuestions().size();
		
		for(int i=0;i<totalQ;i++)
		{
			Group temp=DrawInterface.drawMCQuestionComponents(runningExam.getQuestions().get(i),i,false);
			temp.setLayoutY(i*175);
			questionComponents.getChildren().add(temp);
		}
		
		String tm=DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(runningExam.getStartTime()));
		TextField startTime=new TextField("START TIME: "+tm);
		startTime.setPrefSize(210,25);
		startTime.setLayoutX(10);
		startTime.setLayoutY(485);
		startTime.setEditable(false);
		
		int remTime=runningExam.getDuration()-Time.parseToMinute(Time.getCurrentTimeInMilisec()-runningExam.getStartTime());
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
				if(runningExam==null)
				{
					t.stop();
					return;
				}
				int remTime=runningExam.getDuration()-Time.parseToMinute(Time.getCurrentTimeInMilisec()-runningExam.getStartTime());
				if(remTime<0) remTime=0;
				if(remTime==0)
				{
					int totalQ=runningExam.getQuestions().size(),valid=0;
					for(int i=0;i<totalQ;i++)
					{
						valid+=DrawInterface.checkValid(runningExam.getQuestions().get(i),i);
						DrawInterface.freezeMCQ((Group)questionComponents.getChildren().get(i));
					}
					
					logPanel.setText("Exam ended. Number of valid solutions by you: "+valid);
					logPanel.requestFocus();
					
					try
					{
						client.send(""+valid,runningExam.getTitle());
					}
					catch(Exception ex)
					{
						logPanel.setText("Failed to send your result. You gave "+valid+" valid answers.");
					}
					
					runningExam=null;
                                        for(int i=0;i<30;i++) examAns[i]=0;
					t.stop();
				}
				duration.setText("TIME REMAINING: "+remTime+" MIN");
			}
		}));
		
		t.setCycleCount(Timeline.INDEFINITE);
		t.play();
		
		out.getChildren().addAll(title,questionPane,duration,startTime);
		return out;
	}
	
	private Group drawSubmitOffline()
	{
		Group out=initBodyPanel();
		
		TextField submitAsID=new TextField("YOUR STUDENT ID: "+client.getClientID());
		submitAsID.setPrefSize(520,30);
		submitAsID.setLayoutX(10);
		submitAsID.setLayoutY(10);
		submitAsID.setStyle("-fx-font-weight: bold");
		submitAsID.setEditable(false);
		
		TextField addFile=new TextField();
		addFile.setPrefSize(520,30);
		addFile.setLayoutX(10);
		addFile.setLayoutY(45);
		addFile.setPromptText("CLICK HERE TO ASSIGN OFFLINE FILE (Must be in .zip format)");
		addFile.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				FileChooser fc=new FileChooser();
				fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP Format","*.zip"));
				File f=fc.showOpenDialog(new Stage());
				
				if(f==null) return;
				
				addFile.setText(f.getAbsolutePath());
			}
		});
		
		Button submit=new Button("SUBMIT OFFLINE");
		submit.setPrefSize(150,30);
		submit.setLayoutX(380);
		submit.setLayoutY(80);
		submit.setOnMouseClicked(new EventHandler<MouseEvent>()
		{

			@Override
			public void handle(MouseEvent event)
			{
				String file=addFile.getText().trim();
				
				if(file.isEmpty()) logPanel.setText("No file selected!");
				else
				{
					File f=new File(file);
					
					try
					{
						client.send(f,"offline"+client.getClientID()+".zip");
						logPanel.setText("Offline successfully submitted with ID: "+client.getClientID());
						addFile.setText("");
					}
					catch(Exception ex)
					{
						logPanel.setText("Submission failed. Refresh connection and try again.");
					}
				}
			}
		});
		
		out.getChildren().addAll(submitAsID,addFile,submit);
		return out;
	}

	private Group drawSubmitProject()
	{
		Group out=initBodyPanel();
		
		TextField submitAsID=new TextField("YOUR STUDENT ID: "+client.getClientID());
		submitAsID.setPrefSize(520,30);
		submitAsID.setLayoutX(10);
		submitAsID.setLayoutY(10);
		submitAsID.setStyle("-fx-font-weight: bold");
		submitAsID.setEditable(false);
		
		TextField projectName=new TextField();
		projectName.setPrefSize(520,30);
		projectName.setLayoutX(10);
		projectName.setLayoutY(45);
		projectName.setPromptText("ENTITLE YOUR PROJECT (Maximum 15 characters)");
		projectName.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable,String oldValue,String newValue)
			{
				if(newValue.length()>15) projectName.setText(oldValue);
			}
		});
		
		
		TextField addFile=new TextField();
		addFile.setPrefSize(520,30);
		addFile.setLayoutX(10);
		addFile.setLayoutY(80);
		addFile.setPromptText("CLICK HERE TO ASSIGN PROJECT FILE (Must be in .zip format)");
		addFile.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				FileChooser fc=new FileChooser();
				fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP Format","*.zip"));
				File f=fc.showOpenDialog(new Stage());
				
				if(f==null) return;
				
				addFile.setText(f.getAbsolutePath());
			}
		});
		
		Button submit=new Button("SUBMIT PROJECT");
		submit.setPrefSize(150,30);
		submit.setLayoutX(380);
		submit.setLayoutY(115);
		submit.setOnMouseClicked(new EventHandler<MouseEvent>()
		{

			@Override
			public void handle(MouseEvent event)
			{
				String title=projectName.getText().replaceAll("  "," ").trim().replace(" ","_");
				String file=addFile.getText().trim();
				
				if(title.isEmpty() | file.isEmpty()) logPanel.setText("All fields must be filled!");
				else
				{
					File f=new File(file);
					try
					{
						client.send(f,"project"+client.getClientID()+"_"+title+".zip");
						logPanel.setText("Project successfully submitted with ID: "+client.getClientID());
						projectName.setText("");
						addFile.setText("");
					}
					catch(Exception ex)
					{
						logPanel.setText("Submission failed. Refresh connection and try again.");
					}
				}
			}
		});
		
		out.getChildren().addAll(projectName,submitAsID,addFile,submit);
		return out;
	}
	
	private Group drawReceivedFiles()
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
				receivedFiles=drawReceivedFiles();
				bodyPanel.getChildren().clear();
				bodyPanel.getChildren().add(receivedFiles);
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
							Desktop.getDesktop().open(new File("ReceivedFiles/"));
						}
						catch(IOException ioe)
						{
							logPanel.setText("Unable to open directory: ReceivedFiles/");
						}
					}
				}).start();
			}
		});
		
		try
		{
			fileNames=Database.readAllFilesFromDirectory("ReceivedFiles");
			fileNames.sort(null);
			if(fileNames.isEmpty())
			{
				logPanel.setText("No files available.");
				fileListPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
				out.getChildren().addAll(refresh,openFolder);
				return out;
			}
		}
		catch(Exception ex)
		{
			logPanel.setText("Unable to read received files.");
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
		
		
		logPanel.setText(fileNames.size()+" file(s) found.");
		
		out.getChildren().addAll(fileListPane,refresh,openFolder);
		return out;
	}
	
	private Pane drawSettingsPanel()
	{
		Pane out=new Pane();
		out.setPrefSize(160,125);
		out.setLayoutX(20);
		out.setLayoutY(20);
		
		TextField label=new TextField("SERVER ADDRESS");
		label.setPrefSize(155,30);
		label.setLayoutY(20);
		label.setStyle("-fx-font-weight: bold");
		label.setAlignment(Pos.CENTER);
		label.setEditable(false);
			
		TextField ipAddr=new TextField(HOST);
		ipAddr.setPrefSize(155,30);
		ipAddr.setLayoutY(55);
		ipAddr.setStyle("-fx-alignment: CENTER");

		Button save=new Button("SAVE CHANGE");
		save.setPrefSize(155,30);
		save.setLayoutY(90);
		save.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				try
				{
					if(!Utility.validIP(ipAddr.getText()))
					{
						logPanel.setText("Invalid IP address!");
						return;
					}
				}
				catch(NumberFormatException ne)
				{
					logPanel.setText("Invalid IP address!");
					return;
				}
				
				HOST=ipAddr.getText();
				try
				{
					DataOutputStream d=new DataOutputStream(new FileOutputStream(CONFIG));
					d.write(HOST.getBytes());
					String temp=logPanel.getText();
					logPanel.setText("Server address updated.");
					new Timeline(new KeyFrame(Duration.seconds(2),new EventHandler<ActionEvent>()
					{
						@Override
						public void handle(ActionEvent event)
						{
							logPanel.setText(temp);
						}
					})).play();
					
				}
				catch(Exception e)
				{
					logPanel.setText("Error occured while saving IP address.");
				}
				
				root.getChildren().get(3).setVisible(false);
			}
		});
		
		out.setOnMouseExited(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				root.getChildren().remove(settingsPanel);
				settingsPanel=null;
			}
		});
		
		out.getChildren().addAll(label,ipAddr,save);
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
	public void stop() throws Exception
	{
		if(client!=null) client.close();
		client=null;
	}
	
	public static void main(String args[])
	{
		launch(args);
	}
}
