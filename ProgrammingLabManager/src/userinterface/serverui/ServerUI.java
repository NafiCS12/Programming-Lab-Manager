package userinterface.serverui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import miscelleneous.Constants;
import miscelleneous.Effects;
import miscelleneous.Logger;
import networking.Server;
import userinterface.adminpanelui.AdminPanelUI;
import utility.Database;
import utility.MCQExam;
import utility.Time;

public class ServerUI extends Application
{
	private int SERVER_STATUS=Constants.OFFLINE;
	private final int PORT=10004;
	
	private Server server=null;
	private final AdminPanelUI adminPanel=new AdminPanelUI();
	private final Stage adminPanelStage=new Stage();
	private final List<String> connectedIDs=new ArrayList();
	
	private Group logPanel;
	private Group statusPanel;
	private String statusBarText="";
	private final Label statusBar=new Label();
	private final Timeline logStreamer=new Timeline();
	private final File logFile=new File("Server.log");

	@Override
	public void start(Stage stage)
	{
		Pane root=new Pane();
		
		statusBar.setPrefSize(410,25);
		statusBar.setLayoutX(180);
		statusBar.setLayoutY(5);
		statusBar.setId("statusBar");
		statusBar.setText("Welcome to Programming Lab Manager!");
		
		logPanel=drawLogPanel();
		logPanel.setLayoutX(10);
		logPanel.setLayoutY(35);
		
		statusPanel=drawStatusPanel();
		statusPanel.setLayoutX(340);
		statusPanel.setLayoutY(35);
		
		try
		{
			PrintStream logWriter=new PrintStream(new FileOutputStream(logFile));
			BufferedReader logReader=Files.newBufferedReader(logFile.toPath());
			Logger.prepareLogger(logWriter);
			
			logStreamer.getKeyFrames().add(new KeyFrame(Duration.seconds(2),new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent event)
				{
					try
					{
						if(!logReader.ready()) return;
						String log=logReader.readLine();
						setLog(log);
						
						if(log.startsWith("Connected to ")) addPeople(log.replaceFirst("Connected to ",""));
						else if(log.endsWith(" disconnected.")) removeID(log.replaceAll(" disconnected.",""));
					}
					catch(Exception ex)
					{
						setLog("Auto-synchronization failed.");
					}
					
				}
			}));
			
			logStreamer.setCycleCount(Timeline.INDEFINITE);
			setLog("Auto-synchronization enabled.");
		}
		catch(Exception e)
		{
			setLog("Auto-synchronization disabled.");
		}
			
		root.getChildren().addAll(statusBar,logPanel,statusPanel);
		
		Scene scene=new Scene(root,600,420);
		stage.setScene(scene);
		scene.getStylesheets().add(ServerUI.class.getResource("serverui.css").toExternalForm());
		stage.setResizable(false);
		stage.setTitle("Programming Lab Manager: Server");
		stage.setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent event)
			{
				if(server!=null) try
				{
					server.close();
				}
				catch(Exception e){}
				
				MCQExam runningExam=adminPanel.getRunningExam();
				if(runningExam!=null)
				{
					int remTime=runningExam.getDuration()-Time.parseToMinute(Time.getCurrentTimeInMilisec()-runningExam.getStartTime());
					if(remTime<=0)
					{
						runningExam=null;
						try
						{
							Database.writeExamfileToDatabase(runningExam);
						}
						catch(Exception ex){}
					}
				}
				
				adminPanelStage.close();
				adminPanel.stop();
			}
		});
                logPanel.requestFocus();
		stage.show();
	}
	
	private Group drawLogPanel()
	{
		Group out=new Group();
		
		Rectangle bodyBG=new Rectangle(0,0,320,375);
		bodyBG.setId("navigationBG");
		bodyBG.setEffect(Effects.Blur(null));
		
		TextField label=new TextField("SERVER LOG");
		label.setStyle("-fx-font-weight: bold;\n-fx-font-size: 15;\n-fx-alignment: CENTER");
		label.setPrefSize(300,30);
		label.setLayoutX(10);
		label.setLayoutY(10);
		label.setEditable(false);
		label.setId("listItem");
		
		label.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				((TextArea)logPanel.getChildren().get(1)).setText(">> Log cleared.");
			}
		});
		
		label.setOnMouseEntered(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				statusBarText=statusBar.getText();
				statusBar.setText("Click to clear log.");
			}
		});
		
		label.setOnMouseExited(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				statusBar.setText(statusBarText);
				statusBarText="";
			}
		});
		
		TextArea logField=new TextArea(">> Server started.");
		logField.setPrefSize(300,320);
		logField.setLayoutX(10);
		logField.setLayoutY(45);
		logField.setEditable(false);
		
		out.getChildren().addAll(bodyBG,label);
		out.getChildren().add(1,logField);
		
		return out;
	}

	private Group drawStatusPanel()
	{
		Group out=new Group();
		
		Rectangle bodyBG=new Rectangle(0,0,250,375);
		bodyBG.setId("navigationBG");
		bodyBG.setEffect(Effects.Blur(null));
		
		TextField label=new TextField("SERVER IS OFFLINE");
		label.setStyle("-fx-font-weight: bold;\n-fx-font-size: 15;\n-fx-alignment: CENTER");
		label.setPrefSize(230,30);
		label.setLayoutX(10);
		label.setLayoutY(10);
		label.setEditable(false);
		
		ScrollPane onlinePeople=new ScrollPane();
		onlinePeople.setPrefSize(230,250);
		onlinePeople.setLayoutX(10);
		onlinePeople.setLayoutY(45);
		onlinePeople.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		onlinePeople.setDisable(true);
		
		Group peopleList=new Group();
		onlinePeople.setContent(peopleList);
		
		Button startAdmin=new Button("OPEN ADMIN-PANEL");
		startAdmin.setPrefSize(230,30);
		startAdmin.setLayoutX(10);
		startAdmin.setLayoutY(335);
		startAdmin.setDisable(true);
		startAdmin.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				if(adminPanel.isOpened())
				{
					setLog("Admin-panel is already running.");
					return;
				}
				
				try
				{
					adminPanel.start(adminPanelStage);
					adminPanel.setOpened(true);
					setLog("Admin-panel started.");
				}
				catch(Exception ex)
				{
					setLog("Error occured while openning admin-panel.");
				}
			}
		});
		
		Button startServer=new Button("STARTUP SERVER");
		startServer.setPrefSize(230,30);
		startServer.setLayoutX(10);
		startServer.setLayoutY(300);
		startServer.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				if(SERVER_STATUS==Constants.OFFLINE)
				{
					removeAll();
					try
					{
						server=new Server(PORT);
						adminPanel.setServer(server);
						
						SERVER_STATUS=Constants.ONLINE;
						startServer.setText("SHUTDOWN SERVER");
						setLog("Server started using port "+PORT);
						
						startAdmin.setDisable(false);
						label.setText("PEOPLE CONNECTED: "+connectedIDs.size());
						onlinePeople.setDisable(false);
						
						statusBar.setText("Broadcasting IP address: "+server.getHost());
						logStreamer.play();
					}
					catch(Exception ex)
					{
						setLog("Port "+PORT+" is occupied.");
					}
				}
				else
				{
					try
					{
						server.close();
						server=null;
						adminPanelStage.close();
						adminPanel.setOpened(false);
	
						SERVER_STATUS=Constants.OFFLINE;
						startServer.setText("STARTUP SERVER");
						setLog("Server is shut-down.");
						
						startAdmin.setDisable(true);
						label.setText("SERVER IS OFFLINE");
						onlinePeople.setDisable(true);
						
						statusBar.setText("Welcome to Programming Lab Manger!");
						logStreamer.stop();
					}
					catch(Exception ex)
					{
						setLog("Error occured shutting down server.");
					}
				}
			}
		});
		
		
		out.getChildren().addAll(bodyBG,startServer,startAdmin);
		out.getChildren().add(1,label);
		out.getChildren().add(2,onlinePeople);
		
		return out;
	}

	@Override
	public void stop() throws Exception
	{
		if(server!=null) server.close();
		super.stop();
	}
	
	public void setLog(String log)
	{
		TextArea logField=(TextArea)logPanel.getChildren().get(1);
		logField.appendText("\n>> "+log);
	}
	
	public void addPeople(String ID)
	{
		TextField label=(TextField)statusPanel.getChildren().get(1);
		
		Group peopleList=(Group)((ScrollPane)statusPanel.getChildren().get(2)).getContent();
		
		TextField ppl=new TextField(ID);
		ppl.setPrefSize(208,25);
		ppl.setLayoutY(connectedIDs.size()*30);
		ppl.setEditable(false);
		ppl.setId("listItem");
		ppl.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				try
				{
					removeID(ppl.getText());
					setLog("Successfully removed ID: "+ppl.getText());
				}
				catch(Exception ex)
				{
					setLog("Failed to remove ID: "+ppl.getText());
				}
			}
		});
		
		ppl.setOnMouseEntered(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				statusBarText=statusBar.getText();
				statusBar.setText("Click to disconnect ID: "+ID);
			}
		});
		
		ppl.setOnMouseExited(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				statusBar.setText(statusBarText);
				statusBarText="";
			}
		});
		
		peopleList.getChildren().add(ppl);
		
		connectedIDs.add(ID);
		label.setText("PEOPLE ONLINE: "+connectedIDs.size());
	}
	
	public void removeID(String sID) throws Exception
	{
		server.deleteClient(sID);
		
		TextField label=(TextField)statusPanel.getChildren().get(1);
		Group peopleList=(Group)((ScrollPane)statusPanel.getChildren().get(2)).getContent();
		
		int i=connectedIDs.indexOf(sID);
		connectedIDs.remove(i);
		
		for(;i<peopleList.getChildren().size()-1;i++)
			peopleList.getChildren().set(i,peopleList.getChildren().get(i+1));
		
		peopleList.getChildren().remove(i);
		label.setText("PEOPLE ONLINE: "+connectedIDs.size());
	}
	
	public void removeAll()
	{
		Group peopleList=(Group)((ScrollPane)statusPanel.getChildren().get(2)).getContent();
		peopleList.getChildren().clear();
		connectedIDs.clear();
	}
	
	public static void main(String args[])
	{
		launch(args);
	}

}