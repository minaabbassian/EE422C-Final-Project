/* Client.java
 *  EE422C Final Project submission by
 *  Mina Abbassian
 *  mea2947
 *  16170
 *  Fall 2020
 */

package final_project;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.text.DecimalFormat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Queue;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.media.Media;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.FontPosture;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;


//Client class
public class Client extends Application {
	
	//===========================================================================================================
	//DATA FIELDS: 
	
	//Networking
	private static PrintWriter toServer;
	private static BufferedReader fromServer;
	private static Socket sock;
	
	//Constant
	private static final BigDecimal decimal_ten = new BigDecimal(0.175); //ten seconds
	private static final BigDecimal dec_zero = BigDecimal.ZERO;
	private static String host = "127.0.0.1";
	private static final DecimalFormat decimal_form = new DecimalFormat("#.00"); //for prices 
	
	//Boolean Flags
	private static boolean updatedAuctionItemsArrayList = false; //is it updated?
	private static boolean processComplete = false; //session over
	private static boolean evenWatchlistItem = false; //for colors of watchlist 
	
	//Data Structure
	private static Queue<String> purchaseHistoryQueue = new LinkedList<String>(); //displayed in the purchase history tab
	private static Queue<String> bidHistoryQueue = new LinkedList<String>(); //displayed in the bid history tab
	private static String usernameClient = null; //username of Client 
	private static ArrayList<AuctionItem> auctionItemsArrayList = new ArrayList<AuctionItem> (); //list of all auction items 
	private static ArrayList<AuctionItem> watchItemsArrayList = new ArrayList<AuctionItem>(); //list of watchlist items 
	private static ArrayList<Pair<String, VBox>> watchPairArrayList = new ArrayList<Pair<String, VBox>>(); //each node contains the name of the item in watchlist and all info about that item 
	private static Object wLock = new Object(); //watchlist lock
	private static ArrayList<Thread> threadsArrayList = new ArrayList<Thread>(); //active threads
	private static ArrayList<String> auctionNamesArrayList = new ArrayList<String> ();
	private static HashSet<String> watchNamesHashSet = new HashSet<String>();
	
	//Media
	private static MediaPlayer logonMedia = null; //when user logs in
	private static MediaPlayer exitMedia = null; //quit auction
	private static MediaPlayer errMedia = null; //when an error is encountered
	private static MediaPlayer mouseMedia = null; //mouse click
	private static MediaPlayer addWatchlistMedia = null; //when user adds an item to their watchlist
	private static MediaPlayer remWatchlistMedia = null; //when user removes an item from their watchlist
	private static MediaPlayer purchaseMedia = null; //when user purchases an item
	private static MediaPlayer bidMedia = null; //when user bids on an item
	
	
	//===========================================================================================================
	//METHODS:
	
	/**
	 * Client class default constructor 
	 * Because just one client for every project, this constructor is not called ever in the program 
	 */
	public Client() {
	}
	
	//===========================================================================================================
	
	/**
	 * initializeDataFields
	 * Initializes all data fields (boolean flags and data structure) of the Client class to their defaults when a client clicks logout button
	*/
	private static void initializeDataFields() {
		//boolean flags
		updatedAuctionItemsArrayList = false;
		processComplete = false;
		evenWatchlistItem = false;
		//data structure 
		purchaseHistoryQueue.clear();
		bidHistoryQueue.clear();
		usernameClient = null;
		auctionItemsArrayList = new ArrayList<AuctionItem> ();
		watchItemsArrayList = new ArrayList<AuctionItem>();
		watchPairArrayList  = new ArrayList<Pair<String, VBox>>();
		wLock = new Object();
		threadsArrayList  = new ArrayList<Thread>();
		auctionNamesArrayList = new ArrayList<String> ();
		watchNamesHashSet = new HashSet<String>();
	}
	
	//===========================================================================================================
	
	/**
	 * main
	 * Launches Java FX Application thread 
	 */
	public static void main(String[] args) {	
		launch(args); 
	}
	
	//===========================================================================================================
	
	/**
	 * start
	 * The main entry point for all JavaFX applications
	 */
	@Override
	public void start(Stage primaryStage) {
		setUpSounds(); //initializes all media players for the sound effects
		primaryStage.setTitle("Logon to eHills"); //stage title for the eHills login scene 
		primaryStage.setScene(createSceneForLogin(primaryStage)); //place the scene on the primary stage
		primaryStage.show(); // Display the stage to the user 
	}
	
	//===========================================================================================================
	
	/**
	 * playSoundEffect (Helper method)
	 * Plays sound effect of media
	 * @param media, MediaPlayer sound file to play 
	 */
	private static void playSoundEffect(MediaPlayer media) {
	    //play sound for successful login 
	    media.seek(Duration.ZERO);  
	    media.play();
		
	}
	
    //===========================================================================================================
	
	/**
	 * setUpSounds (Helper Method)
	 * Loads all sound files and initializes media players using ClassLoader for the program GUI 
	 */
	public void setUpSounds() {
		//logon sound
		Media logonWav = new Media(getClass().getClassLoader().getResource("final_project/logon.wav").toString());
		logonMedia = new MediaPlayer(logonWav);
		
		//exit sound
		Media exitWav = new Media(getClass().getClassLoader().getResource("final_project/exit.wav").toString());
		exitMedia = new MediaPlayer(exitWav);
		
		//error sound
		Media errWav = new Media(getClass().getClassLoader().getResource("final_project/err.wav").toString());
		errMedia = new MediaPlayer(errWav);
		
		//mouse click sound
		Media mouseWav = new Media(getClass().getClassLoader().getResource("final_project/mouse.wav").toString());
		mouseMedia = new MediaPlayer(mouseWav);
		
		//add to watchlist sound 
		Media addWatchlistWav = new Media(getClass().getClassLoader().getResource("final_project/addWatchlist.wav").toString());
		addWatchlistMedia = new MediaPlayer(addWatchlistWav);
		
		//remove from watchlist sound 
		Media remWatchlistWav = new Media(getClass().getClassLoader().getResource("final_project/remWatchlist.wav").toString());
		remWatchlistMedia = new MediaPlayer(remWatchlistWav);
		
		//purchase item sound 
		Media purchaseWav = new Media(getClass().getClassLoader().getResource("final_project/purchase.wav").toString());
		purchaseMedia = new MediaPlayer(purchaseWav);
		
		//bid on item sound 
		Media bidWav = new Media(getClass().getClassLoader().getResource("final_project/bid.wav").toString());
		bidMedia = new MediaPlayer(bidWav);
	}
	
	//===========================================================================================================
	
	/**
	 * sendToServer (copied from starter file)
	 * Sends to the Server a String command from the Client to the Server 
	 * @param commandString, the command to sent from the Client to the Server
	 * 		  The three commands are of the form: "setUpAuctionItems" , "disconnectClient" , "changeItemBid|itemName|updatedBidding|nameHighestBidder"
	 */
    private static void sendToServer(String commandString) {
    	//command sent from Client to Server through the toServer PrintWriter output stream  
    	System.out.println("Sending to server: " + commandString); 
    	toServer.println(commandString);
    	toServer.flush();
    }
    
    //===========================================================================================================
    
    /**
	 * disconnectClientFromServer (called by logoffButt handler when the client clicks LOGOUT)
	 * Sends to the Server the "disconnectClient" command to delete ClientHandler from the Server's list of observers
	 * @throws IOException
	 */
	private static void disconnectClientFromServer() throws IOException {
		//the Server closes the socket connection from their side 
		Client.sendToServer("disconnectClient");
	}
	
	//===========================================================================================================
	
	/**
	 * timeFormatter
	 * @param timeRemaining, BigDecimal time left for auction item 
	 * @return String of time remaining for the auction item 
	 * 			Format: Time Remaining: hours:minutes:seconds
	 */
	private static String timeFormatter(BigDecimal timeRemaining) {
		String out = "";
		int entireTime = timeRemaining.intValue();
		int hrs = entireTime / 60; //hours
		int mins = entireTime % 60; //minutes
		BigDecimal decPortion = timeRemaining.subtract(new BigDecimal(entireTime)); //decimal part of time 
		int secs = (decPortion.multiply((new BigDecimal(60)))).intValue(); //seconds
		String hrsString =String.format("%02d", hrs);
		String minsString = String.format("%02d", mins);
		String secsString = String.format("%02d", secs);
		out = "  Time Remaining: " + hrsString + ":" + minsString + ":" + secsString;
		return out;
	}
	
	//===========================================================================================================
	
	/**
	 * setUpNetworking (called in logiButt handler after a user presses the Login button)
	 * Creates socket connection of port 4242 to "127.0.0.1" (IP address specified by "host" data field that the client is connecting to)
	 * Creates and starts a thread to manage communication from the Server to the Client 
	 * @throws Exception
	 */
	private static void setUpNetworking() throws Exception {
		// set up socket connection and data streams
	    try {
	    	sock = new Socket(host, 4242); //socket bound to port 4242
	    } catch (IOException ex) { //error while trying to create socket connection to the server
	    	System.out.println("Error in setting up socket connection!");
	    }
	    System.out.println("Connecting to the server through connection to " + sock); //print to console 
	    //Gets the socket's input stream and opens a BufferedReader on it
		//To get the Client's response, Server reads from the BufferedReader
	    fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
	    //Gets the socket's output stream and opens a PrintWriter on it
		//To send data through the socket to the Client, Server simply needs to write to the the PrintWriter
	    toServer = new PrintWriter(sock.getOutputStream()); 
	   
	    // serverCommandThread handles all commands received by the Client from the Server
	    Thread serverCommandThread = new Thread(new Runnable() {
	    	@Override
	    	public void run() {
	    		String serverCommand;
	    		try {
	    			while (!sock.isClosed() && !processComplete) { 
	    				if (fromServer.ready()) { 
	    					//read server command
	    					serverCommand = fromServer.readLine();  
	    					//process request received by Client from Server
	    					processRequest(serverCommand); 
	    				}
	    			}
	    		} catch (Exception ex) {
	    			ex.printStackTrace();
	    		}
	    	}
	    });
	    serverCommandThread.setName("serverCommandThread"); 
	    //start the thread
	    serverCommandThread.start();
	    //add the thread to the ArrayList of threads
	    threadsArrayList.add(serverCommandThread); 
	}
	
	//===========================================================================================================
    
    //////////////////// eHills Login Page Logic ////////////////////
    /**
     *  createSceneForLogin
     *  Initializes all buttons, TextFields, and other nodes in the eHills page titled "Logon to eHills"
     *  Returns a new eHills Login scene  
     *  @param primaryStage, a start() parameter
     *  @return Scene loginScene, a newly created Scene titled "Logon to eHills"
     */
	private static Scene createSceneForLogin(Stage primaryStage) {
		
		//This BorderPane is where all nodes in this scene will be placed 
		BorderPane welcomePane = new BorderPane();
		//background Sky Image of the BorderPane 
		welcomePane.setStyle("-fx-background-image: url(\"/final_project/lightSky.png\");");
		
		//////////////////// Labels ////////////////////
		//Welcome Label - Top and in the Center of the BorderPane 
		Label welcomeLabel = new Label();
		welcomeLabel.setText("Welcome to eHills");
		welcomeLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 30));
		welcomeLabel.setTextFill(Color.BLUE);
		//Continue Label - Top and in the Center of the BorderPane under welcomeLabel
		Label continueLabel = new Label();
		continueLabel.setText("Please enter a username and password or continue as guest:");
		continueLabel.setFont(Font.font("Verdana", FontPosture.ITALIC, 12));
		continueLabel.setTextFill(Color.DIMGRAY);
		//welcomeLabel and continueLabel one on top of the other in a vertical column 
		VBox greetingVBox = new VBox(welcomeLabel, continueLabel);
		greetingVBox.setAlignment(Pos.TOP_CENTER); 
		VBox.setMargin(welcomeLabel,  new Insets(15, 0, 0, 0)); //Insets - top, right, bottom, left offsets
		//Error Label - Right of Sign In Button under the password TextField 
		Label errorLabel = new Label(); //error message displayed when there is a problem signing in
		errorLabel.setFont(Font.font("Verdana", FontPosture.ITALIC, 12));
		errorLabel.setTextFill(Color.RED);

		
		//////////////////// TextFields ////////////////////
		//Username TextField - Center node of the BorderPane under the Labels 
		TextField userTextField = new TextField(); 
		userTextField.setPromptText("Enter Username");
		userTextField.setFont(Font.font("Verdana", FontPosture.ITALIC, 11));
		userTextField.setStyle("-fx-text-fill: dimgray;");
		//Password TextField - Center node of the BorderPane under the username TextField
		TextField passTextField = new PasswordField(); 
		passTextField.setPromptText("Enter Password");
		passTextField.setFont(Font.font("Verdana", FontPosture.ITALIC, 11));
		passTextField.setStyle("-fx-text-fill: dimgray;");

		
		//////////////////// Buttons ////////////////////
		//Quit Button - Bottom Right Corner of BorderPane 
		Button quitButt = new Button("Quit"); // node for quit button 
		quitButt.setPrefSize(80, 20);
		quitButt.setStyle("-fx-text-fill: red; -fx-border-color: blue; font-weight: bold;");
		quitButt.setFont(Font.font("Verdana", 20));
		HBox rightBox = new HBox(quitButt);
		rightBox.setAlignment(Pos.BOTTOM_RIGHT);
		rightBox.setPadding(new Insets(10));
		welcomePane.setBottom(rightBox);
		//handler for the Quit Button - exits the program and JavaFX Application
		quitButt.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				//play sound for quitting the program 
				playSoundEffect(exitMedia);
			    
			    //signals the JavaFX Toolkit to shut down
			    //resulting in the application instance's stop() method called on the FXApplication thread, 
			    //and the FX Application Thread being allowed to terminate 
				Platform.exit(); 
				
				//the JVM basically exits immediately 
				System.exit(0);
			}
		});
		
		//Login Button - Center node below the password TextField 
		Button loginButt = new Button("Login"); 
		loginButt.setPrefSize(70, 20);
		loginButt.setStyle("-fx-text-fill: blue; -fx-border-color: blue; font-weight: bold;");
		loginButt.setFont(Font.font("Verdana", 15));
		loginButt.setAlignment(Pos.BASELINE_CENTER); 
		//HBox to place errorLabel to the right of LoginButt 
		HBox errorHBox = new HBox (15, loginButt, errorLabel); // place error message to the right of sign-in button
		//handler for the Login Button 
		loginButt.setOnAction(new EventHandler<ActionEvent>() { 
			@Override
			public synchronized void handle(ActionEvent event) {
				//get password
				String enteredPass = passTextField.getText();
				//get username 
				String enteredUsername = userTextField.getText();
				
				//user enters a username and a password entered - SUCCESSFUl Login
				if (!(enteredUsername.equals("")) && !(enteredPass.equals(""))) { 	
					try {
						//no error message displayed 
						errorLabel.setText(""); 
						//set up socket connection
						setUpNetworking(); 
						//initialize auction items first 
					    sendToServer("setUpAuctionItems"); 
					    while (!updatedAuctionItemsArrayList) { //changes to true in processRequest
					    	//wait until auctionItemsArrayList is updated before continuing on
					    	Thread.yield(); 
					    } 
					    //play sound for successful login 
					    playSoundEffect(logonMedia);
					    
					    //set username to entered username 
					    usernameClient = enteredUsername; 
					    
					    //AUCTION PAGE 
					    primaryStage.setTitle("eHills Auction"); 
						primaryStage.setScene(createSceneForAuction(primaryStage)); 
						primaryStage.show();
					} catch (Exception ex) { //catch all errors 
						ex.printStackTrace();
						errorLabel.setText("An error occured while trying to connect to the server.");
						//play error sound effect
						playSoundEffect(errMedia);

						//reset fields for eHills login page
						userTextField.clear();
						passTextField.clear();
					}
				}
				
				//no username and no password entered 
				else if (enteredUsername.equals("") && enteredPass.equals("")) {
					//error label to be displayed 
					errorLabel.setText("Error - Enter a username and password.");
					//play error sound effect
					playSoundEffect(errMedia);

					//reset fields for eHills login page
					userTextField.clear();
					passTextField.clear();
				}
						
				//no username is entered, but a password was entered
				else if (enteredUsername.equals("")) {
					//error label to be displayed
					errorLabel.setText("Error - A username was not entered.");
					//play error sound effect
					playSoundEffect(errMedia);

					//reset fields for eHills login page
					userTextField.clear();
					passTextField.clear();
				}
				
				//no password is entered, but a username is entered
				else if (enteredPass.equals("")) {
					//error label to be displayed 
					errorLabel.setText("Error - A password was not entered.");
					//play error sound effect
					playSoundEffect(errMedia);
					
					//reset fields for eHills login page
					userTextField.clear();
					passTextField.clear();
				}
			}
		});
		
		//Continue as Guest Button - Center node below the Login Button 
		Button guestButt = new Button("Continue as guest"); // sign-in as guest button
		guestButt.setStyle("-fx-text-fill: blue; -fx-border-color: blue; font-weight: bold;");
		guestButt.setFont(Font.font("Verdana", 11));
		guestButt.setAlignment(Pos.BASELINE_CENTER); 
		//handler for the Continue as Guest Button - when both a username and password is not entered 
		guestButt.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public synchronized void handle(ActionEvent event) {
				try {
					//no error message displayed 
					errorLabel.setText("");
					//set up socket connection 
					setUpNetworking(); 
					//initialize auction items first 
			    	sendToServer("setUpAuctionItems"); 
			    	while (!updatedAuctionItemsArrayList) { //changes to true in processRequest 
			    		//wait until auctionItemsArrayList is updated before continuing on
			    		Thread.yield(); 
			    	}
			    	//play logon sound effect for successful logon
			    	playSoundEffect(logonMedia);
			    	
			    	//set username to "Guest"
			    	usernameClient = "Guest"; 
			    	primaryStage.setTitle("eHills Auction"); 
					primaryStage.setScene(createSceneForAuction(primaryStage));
					primaryStage.show();
				} catch (Exception ex) { //catch all errors 
					ex.printStackTrace();
					errorLabel.setText("An error occured while trying to connect to the server.");
					//play error sound effect
					playSoundEffect(errMedia);

					//reset fields for eHills login page
					userTextField.clear();
					passTextField.clear();
				}
			}
		});
		
		
		//VBox for the vertical alignment of the center nodes of the BorderPane
		VBox mainVBox = new VBox(6, greetingVBox, userTextField, passTextField, errorHBox, guestButt);
		mainVBox.setMaxSize(410, 910); // this controls the max size of the text fields
		VBox.setMargin(greetingVBox, new Insets(0, 12, 17, 12)); //offsets 
		VBox.setMargin(userTextField, new Insets(0, 12, 0, 12));
		VBox.setMargin(passTextField, new Insets(0, 12, 0, 12));
		VBox.setMargin(errorHBox, new Insets(0, 12, 0, 12));
		VBox.setMargin(guestButt, new Insets(17, 12, 0, 12));
		welcomePane.setCenter(mainVBox);
		
		//create new welcomeScene for the eHills Login Page
		Scene welcomeScene = new Scene(welcomePane, 500, 400);
		return welcomeScene; //return scene
	}
	
	//===========================================================================================================

	/**
	 * processRequest (called in setUpNetworking() method)
	 * Processes all commands received by the Client from the Server
	 * Commands from Server must be one of the following:
	 * 		"purchasedItemNotification|itemName|itemPrintMessage"
	 * 		"changeItemBidNotification|itemName|currBidding|nameHighestBidder"
	 * 		"changedTimeNotification|itemName|timeLimit"
	 * 		"setUpAuctionItemsNotification|auctionString"
	 * @param serverCommand, input command String received by the Client from the Server
	 */
    private static void processRequest(String serverCommand) {

    	//String is split around pipe character 
    	String[] commandString = serverCommand.split("\\|"); 
    	
    	//switch statement for the first argument 
    	switch (commandString[0]) {
    		//CASE 1 - format: purchasedItemNotification|itemName|itemPrintMessage
			case "purchasedItemNotification": 
				//get the name of the auction item that is sold 
				String nameSold = commandString[1];
				//get the message to display of the item that is sold 
				String itemSoldDisplay = commandString[2];
				//do for all items in the ArrayList
				for (AuctionItem i : auctionItemsArrayList) {
					//find the item that is sold 
					if (i.itemName.contentEquals(nameSold)) { 
						//update the message of the sold item 
						i.itemPrintMessage = itemSoldDisplay; 
						if (itemSoldDisplay.contains("Auction for this item has ended. The item has been marked unsold.")) { 
							purchaseHistoryQueue.add(i.itemName.toUpperCase() + ": " + itemSoldDisplay);
						}
						else {
							//just add regularly 
							purchaseHistoryQueue.add(itemSoldDisplay);
						}
	    				break;
					}
				}
				break;
			
	    	//CASE 2 - format: changeItemBidNotification|itemName|currBidding|nameHighestBidder
	    	case "changeItemBidNotification": 
	    		//get the name of the item to update
	    		String updateItem = commandString[1];
	    		//get the new bidding price of the item 
	    		Double updatedCurrBidding = Double.parseDouble(commandString[2]);
	    		//get the name of the new highest bidder on the item 
	    		String updatedNameHighestBidder = commandString[3];
	    		//do for all items in the ArrayList
	    		for (AuctionItem i : auctionItemsArrayList) {
	    			//find the item to update 
	    			if (i.itemName.contentEquals(updateItem)) { 
	    				//set the new current bid price
	    				i.currBidding = updatedCurrBidding; 
	    				//set the new name of the highest bidder
	    				i.nameHighestBidder = updatedNameHighestBidder; 
	    				//message to be printed in bid alerts tab
	    				bidHistoryQueue.add(updatedNameHighestBidder + " placed a bid of $" + decimal_form.format(updatedCurrBidding) + " on item: " + i.itemName);
	    				break;
	    			}
	    		}
	    		break;	
	    	
	    	//CASE 3 - format: changedTimeNotification|itemName|timeLimit
	    	case "changedTimeNotification": 
	    		//get the name of the auction item to update duration
	    		String nameAuctionItem = commandString[1];
	    		//get the updated time left on the auction item 
	    		BigDecimal updatedTimeLeft = new BigDecimal(commandString[2]);
	    		//do for all items in the ArrayList
	    		for (AuctionItem i : auctionItemsArrayList) {
	    			//find the item to update
	    			if (i.itemName.contentEquals(nameAuctionItem)) { 
	    				//set the new time left 
	    				i.timeLimit = updatedTimeLeft; 
	    				break;
	    			}
	    		}
	    		break;
    	
	    	//CASE 4 - format: setUpAuctionItemsNotification|auctionString
	    	case "setUpAuctionItemsNotification": 
	    		//clear ArrayList of items 
    			auctionItemsArrayList.clear(); 
 				
    			//initialize auction item data fields 
    			String itemPrintMessage = "";
    			Double buyNow = 0.00;
    			String itemName = "";
    			BigDecimal timeLeft = null;
				String itemDescription = "";
				Double minimumPrice = 0.00;
				String nameHighestBidder = "";
				Double currBidding = 0.00;
    			
				//parse the string of auction items 
				for (int i = 1; i < commandString.length; i++) {
    				if (!commandString[i].contentEquals("")) {
	    				if (i % 8 == 1) { 
	    					//update the item's name 
	    					itemName += commandString[i]; 
	    					//add the item's name to the ArrayList
	    					auctionNamesArrayList.add(itemName);  
	    				}
	    				else if (i % 8 == 3) {
	    					//update the item's minimum price
	    					minimumPrice = Double.parseDouble(commandString[i]); 
	    				}
	    				else if (i % 8 == 5) {
	    					//update the item's buy now price 
	    					buyNow = Double.parseDouble(commandString[i]); 
	    				}
	    				else if (i % 8 == 7) {
	    					//update the time remaining of the item
	    					timeLeft = new BigDecimal(commandString[i]); 
	    				}
	    				else if (i % 8 == 2) {
	    					//update the item's description
	    					itemDescription += commandString[i];
	    				}
	    				else if (i % 8 == 4) {
	    					//update the item's current bid price
	    					currBidding = Double.parseDouble(commandString[i]); 
	    				}
	    				else if (i % 8 == 6) {
	    					//update the item's highest bidder username
	    					nameHighestBidder = commandString[i]; 
	    				}
	    				else {
	    					//update the item's print message
	    					itemPrintMessage = commandString[i]; 
	    					//add the item to the ArrayList of auction items 
	    					auctionItemsArrayList.add(new AuctionItem(buyNow, itemName, timeLeft, currBidding, itemDescription, itemPrintMessage, minimumPrice, nameHighestBidder));
	    	 				//reset variables for next item 
	    	 				itemName = "";
	    	 				nameHighestBidder = "";
	        				itemDescription = "";
	        				itemPrintMessage = "";
	    				}
    				}
    			}
				updatedAuctionItemsArrayList = true; // set changed flag to true 
    			break;
    	}
    	
    	return;
    }	
	
	//===========================================================================================================
	
	//////////////////// eHills Auction Page Logic ////////////////////
	/**
	 * createSceneForAuction 
	 * Called after a successful login (either through login button handler or guest button handler)
	 * Creates all JavaFX nodes and returns the new auction scene  
	 * @param primaryStage
	 * @return eHillsScene, a newly generated eHillsScene with freshly initialized nodes
	 */
	private static Scene createSceneForAuction(Stage primaryStage) {
		////////////////////Top Row - Greeting Label and LOGOUT Button ////////////////////
		//Greeting Label - Top Center
		Label greetingLabel = new Label(Client.usernameClient + ", welcome to the eHills auction!"); 
		greetingLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 30));
		greetingLabel.setTextFill(Color.BLUE);

		//Log Out button - Top Right
		Button logoffButt = new Button("LOGOUT"); // log-out button
		logoffButt.setStyle("-fx-text-fill: red; -fx-border-color: blue; font-weight: bold;");
		logoffButt.setFont(Font.font("Verdana", 15));
		logoffButt.setAlignment(Pos.CENTER_RIGHT);

		//////////////////// Row 2 - Drop-down menu, addItem button, removeItem button + addItemErrorMessage ////////////////////
		//Drop-down menu Label
		Label itemsMenuLabel = new Label("Items:");
		itemsMenuLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 11));
		itemsMenuLabel.setTextFill(Color.BLUE);
				
		//Drop-down menu node of the items in the auction 
		ChoiceBox<String> itemsChoiceBox = new ChoiceBox<String>(); 
		itemsChoiceBox.setPrefHeight(50);
		itemsChoiceBox.setStyle("-fx-text-fill: blue; -fx-border-color: blue; font-weight: bold;");
		itemsChoiceBox.getItems().addAll(auctionNamesArrayList); //data field 
		//handler for itemsChoiceBox - plays mouse click sound when user clicks drop down menu
		itemsChoiceBox.setOnMouseClicked(e -> {
			//play mouse click sound 
			playSoundEffect(mouseMedia);
		});
		itemsChoiceBox.getStylesheets().add("final_project/choicebox.css");
				
		//Add Item to Watchlist Button 
		Button addButt = new Button("Add to your watchlist"); 
		addButt.setPrefHeight(50);
		addButt.setStyle("-fx-text-fill: blue; -fx-border-color: blue; font-weight: bold;");
		addButt.setFont(Font.font("Verdana", 10));
				
		//Remove Item from Watchlist Button 
		Button remButt = new Button("Remove from your watchlist"); 
		remButt.setPrefHeight(50);
		remButt.setStyle("-fx-text-fill: blue; -fx-border-color: blue; font-weight: bold;");
		remButt.setFont(Font.font("Verdana", 10));
				
		//Add Item to Watchlist Error Label - displayed when the item is already in the user's watchlist
		Label addErrorLabel = new Label();
		addErrorLabel.setFont(Font.font("Verdana", FontPosture.ITALIC, 13));
		addErrorLabel.setTextFill(Color.RED);
				
		////////////////////Row 3 - enter bid Label + bid TextField + bid Button ////////////////////
		//Enter your bid Label
		Label enterBidLabel = new Label("Enter your bid: $");
		enterBidLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 17));
		enterBidLabel.setTextFill(Color.BLUE);
		enterBidLabel.setAlignment(Pos.BASELINE_CENTER);
		
		//Bid TextField 
		TextField bidTextField = new TextField();
		bidTextField.setPrefHeight(50);

		bidTextField.setStyle("-fx-text-inner-color: red;");
		bidTextField.setFont(Font.font("Verdana", FontWeight.BOLD, 11));
		
		//Bid Button
		Button bidButt = new Button("BID");
		bidButt.setPrefHeight(50);
		bidButt.setStyle("-fx-text-fill: blue; -fx-border-color: blue; font-weight: bold;");
		bidButt.setFont(Font.font("Verdana", 10));
		
		//Bidding Error Label - displayed when the user encounters a problem when trying to place a bid on an item 
		Label biddingErrorLabel = new Label();
		biddingErrorLabel.setFont(Font.font("Verdana", FontPosture.ITALIC, 13));
		biddingErrorLabel.setTextFill(Color.RED);
		
		//////////////////// END OF CONTROLLER ////////////////////					
		
		//Watchlist Label
		Label watchlistLabel = new Label("Your Watchlist:");
		watchlistLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
		watchlistLabel.setTextFill(Color.BLUE);
		
		//Watchlist ListView window
		ListView<VBox> watchlistListView = new ListView<VBox>();
		watchlistListView.setPrefWidth(1900);
		watchlistListView.setPrefHeight(680);

		//////////////////// Notifications Window - shows purchase history in one tab and bid history in another ////////////////////
		//Multi-Tab Notification Window - shows all sell history and bid history
		//Notifications Label
		Label tabsLabel = new Label("Notifications:");
		tabsLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
		tabsLabel.setTextFill(Color.BLUE);
		
		//updates TabPane
		TabPane updatesTabPane = new TabPane();
		//Purchase History Tab
        Tab soldHistoryTab = new Tab("Purchase History", new Label("Purchase History of the eHills Auction"));
        soldHistoryTab.setStyle("-tab-text-color: blue;");
        //Bid History Tab
        Tab biddingHistoryTab = new Tab("Bid History", new Label("Bidding History of the eHills Auction"));
        biddingHistoryTab.setStyle("-tab-text-color: blue;");

        //add purchase history and bid history tabs
        updatesTabPane.getTabs().add(soldHistoryTab);
        updatesTabPane.getTabs().add(biddingHistoryTab);
       
        //add windows to each of the tabs
        ListView<String> purchaseTab = new ListView<String>();
        purchaseTab.getStylesheets().add("final_project/list.css");
        ListView<String> bidTab = new ListView<String>();
        bidTab.getStylesheets().add("final_project/list.css");
       
        //set the content of the tabs
        updatesTabPane.getTabs().get(0).setContent(purchaseTab);
        updatesTabPane.getTabs().get(1).setContent(bidTab);
        
		////////////////////HBoxes and VBoxes ////////////////////
		//Row 1
		//HBox to hold greetingLabel and logoffButt in the top row 
		HBox topRowHBox = new HBox(300, greetingLabel, logoffButt);
		topRowHBox.setAlignment(Pos.CENTER_RIGHT);
		HBox.setMargin(logoffButt, new Insets(12, 12, 0, 0));
		HBox.setMargin(greetingLabel, new Insets(9, 0, 0, 0));
		
		//Row 2
		//HBox controller node for all the items in row 2
		HBox rowTwoHBox = new HBox(6, itemsMenuLabel, itemsChoiceBox, addButt, remButt, addErrorLabel);  
		rowTwoHBox.setAlignment(Pos.BASELINE_CENTER);
		
		//Row 3
		//HBox holding all the nodes in row 3 
		HBox rowThreeHBox = new HBox(6, enterBidLabel, bidTextField, bidButt, biddingErrorLabel);
		rowThreeHBox.setAlignment(Pos.BASELINE_CENTER);
		//VBOX node for Controller Row Two and Controller Row Three
		VBox rowsTwoAndThreeVBox = new VBox(6, rowTwoHBox, rowThreeHBox); 
		
        
        //////////////////// Finalize the content of the Auction Page Layout ////////////////////
		//A separator line (horizontal by default) that occupies the full horizontal space allocated to it 
		Separator controllerSeparator = new Separator();
		//Divider line between itemView and alerts window
		//A separator line (horizontal) between the Watchlist and Alerts window 
		Separator watchlistSeparator = new Separator();
		
        //Complete VBox 
		VBox finalVBox = new VBox(5, topRowHBox, rowsTwoAndThreeVBox, controllerSeparator, watchlistLabel, watchlistListView, watchlistSeparator, tabsLabel, updatesTabPane);
		finalVBox.setStyle("-fx-background-image: url(\"/final_project/lightSky.png\");"); //sky image background
		VBox.setMargin(watchlistLabel, new Insets(0, 0, 0, 30));
		VBox.setMargin(watchlistListView, new Insets(15, 30, 15, 30));
		VBox.setMargin(tabsLabel, new Insets(0, 0, 0, 30));
		VBox.setMargin(updatesTabPane, new Insets(15, 30, 15, 30));
		
		//////////////////// Threads in the Background ////////////////////	
	
		//Thread that updates the Notifications window 
		Thread threadUpdatingNotifications = new Thread (new Runnable() {
			@Override
			public void run() {
				while (!processComplete) {
					
					//BID ALERTS
					while (!bidHistoryQueue.isEmpty()) { //while bids have been placed 
						//get a bidding notification
						String biddingDisplay = bidHistoryQueue.remove();
						
						//run the specified Runnable on the JavaFX Application Thread at some unspecified time in the future
						Platform.runLater(() -> {
							bidTab.getItems().add(biddingDisplay.replace(usernameClient, "you"));
						});
					}
					
					//SELL ALERTS
					while (!purchaseHistoryQueue.isEmpty()) { //while items have been sold 
						//get a purchase notification to display 
						String sellDisplay = purchaseHistoryQueue.remove();  
						
						//run the specified Runnable on the JavaFX Application Thread at some unspecified time in the future
						Platform.runLater(() -> {
							//item sold to another user other than the client of this program
							if (!(sellDisplay.contains(usernameClient))) { 
								purchaseTab.getItems().add(sellDisplay);
								//play purchase sound effect
								playSoundEffect(purchaseMedia);
							}
							else { //the user is the person that the item was sold to 
								purchaseTab.getItems().add(sellDisplay.replace(usernameClient, "you"));
								//play purchase sound effect
								playSoundEffect(purchaseMedia);
							}
						});
					}
	
					try {
						//all other threads run first before updating GUI for watchlist window 
						Thread.sleep(200); //sleep for 200 ms
					} catch (InterruptedException e) {
						System.out.println("Thread Updating Notifications INTERRUPTED.");
					}
				}
			}
		});
		//set the name
		threadUpdatingNotifications.setName("threadUpdatingNotifications");
		
		
		//Thread that Enables/Disables Buttons and TextFields - addButt, remButt, bidTextFied, bidButt
		Thread threadEnablingButtons = new Thread(new Runnable () {
			@Override
			public void run() {
				// while the watchlistListView has added nodes
				while (!processComplete) { 
					//user has selected an auction item from the drop down menu
					if (itemsChoiceBox.getValue() != null) {
						addButt.setDisable(false); //can add 
						bidTextField.setDisable(false); //can enter a bid text
						//if no bid text is entered, cannot press bid button
						if (bidTextField.getText().isEmpty()) { 
							bidButt.setDisable(true);
						}
						else {
							//if the user enters a bid price, can press bid button
							bidButt.setDisable(false); 
						}	
					}
					else { //no item is selected from the drop down menu
						addButt.setDisable(true); //cannot add 
						bidButt.setDisable(true); //cannot press bid 
						remButt.setDisable(true); //cannot remove
						bidTextField.setDisable(true); //cannot enter bid price
					}
					
					//cannot remove auction item if not in the user's watchlist 
					if (watchNamesHashSet.isEmpty() || !watchNamesHashSet.contains(itemsChoiceBox.getValue()) || watchItemsArrayList.isEmpty()) {
						remButt.setDisable(true); 
					}
					
					else { //can remove
						remButt.setDisable(false);  
					}
				}
			}
		});
		//set the name
		threadEnablingButtons.setName("threadEnablingButtons");
		
		
		//Thread that continuously updates the user's watchlist
		Thread threadUpdatingWatch = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!processComplete) {
					synchronized(wLock) { //synchronization
						//do for all watchlistNodes containing the name of the item in watchlist and all info about that item 
						for (Pair<String, VBox> watchlistNode : watchPairArrayList) {
							//key is the auction item's name 
							String nameKey = watchlistNode.getKey();
							//the value is the auction item's info
							VBox infoVBox = watchlistNode.getValue(); //node of auction item
							
							//get the info of the auction item in the watchlist -- addButt handler 
							VBox itemDetailsVBox = (VBox) infoVBox.lookup("#itemDetailsVBox"); //biddingHBox, sellLabel
							HBox biddingHBox = (HBox) itemDetailsVBox.getChildren().get(0); //biddingLabel, timeLeftLabel
							Label biddingLabel = (Label) biddingHBox.lookup("#biddingLabel");
							Label timeLeftLabel = (Label) biddingHBox.lookup("#timeLeftLabel");
							Label sellLabel = (Label) itemDetailsVBox.lookup("#sellLabel");
							
							//do for all auction items in the watchlist ArrayList
							for (AuctionItem i : watchItemsArrayList) {
								if (nameKey.contentEquals(i.itemName)) { //find the corresponding item 
									//get the time limit of the auction item 
									BigDecimal timeRemaining = i.timeLimit;
									String timeLeftString = timeFormatter(timeRemaining);
									//run the specified Runnable on the JavaFX Application Thread at some unspecified time in the future
									Platform.runLater(() -> { 
										String currBiddingString = "$" + decimal_form.format(i.currBidding); //get the current bid price 
										
										//if nobody has bid on the auction item yet
										if (i.currBidding == 0.00) {
											currBiddingString = "none";
										}
										
										//coordinate this with the previous bidding label 
										biddingLabel.setText("Minimum Bid Price: $" + decimal_form.format(i.minimumPrice) + "  Buy Now Price: $" + 
															decimal_form.format(i.buyNow) + "  Highest Bid: " + currBiddingString + 
															"  Highest Bidder Username: " + i.nameHighestBidder);
										
										//NO TIME REMAINING ON AUCTION ITEM - change to red
										if (timeRemaining.compareTo(decimal_ten) == -1) {
											timeLeftLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 11));
											timeLeftLabel.setTextFill(Color.RED);
										}
										timeLeftLabel.setText(timeLeftString); //there is time left
										
										//item is available for purchase, so set the label of the watchlist item 
										if (!i.itemPrintMessage.contentEquals("Item is available for purchase!")) { 
											sellLabel.setText(i.itemPrintMessage);
										}
										
										//no time remaining, disable the watchlist box for the item 
										if (timeRemaining.compareTo(dec_zero) == 0) {
											infoVBox.setDisable(true); 
										}
									});
								}
							}
						}
					}
					try { 
						//all other threads run first before updating GUI for watchlist window 
						Thread.sleep(100); //sleep for 100 ms
					} 
					catch (InterruptedException ex) {
						//catch all InterruptedExceptions
						ex.printStackTrace();
					}
				}
			}
        });
		//set the name
		threadUpdatingWatch.setName("threadUpdatingWatch");
		
		//start all three threads
		threadUpdatingNotifications.start();
		threadsArrayList.add(threadUpdatingNotifications);
		threadEnablingButtons.start();
		threadsArrayList.add(threadEnablingButtons);
		threadUpdatingWatch.start();
		threadsArrayList.add(threadUpdatingWatch);
		
		//////////////////// Button Handlers ////////////////////
		//handler for the Bid Button
		bidButt.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				//item chosen by user to bid on
				String biddedItem = itemsChoiceBox.getValue();
				
				//get the bid amount
				Double bidAmount = Double.parseDouble(bidTextField.getText());
				
				//do for all items in auction ArrayList
				for (AuctionItem i : auctionItemsArrayList) {
					if (i.itemName.contentEquals(biddedItem)) { //find the item the user is bidding on
						
						//if the time has expired for the auction item 
						if (i.timeLimit.doubleValue() <= 0.00) { 
							biddingErrorLabel.setText("INVALID - Bidding for the item - " + biddedItem + " - has closed.");
							//play error sound 
							playSoundEffect(errMedia);

						}
						
						//if the user's bid is below the minimum price 
						else if (bidAmount <= i.minimumPrice) { 
							biddingErrorLabel.setText("INVALID - You must bid more than the item's minimum bid price.");
							//play error sound 
							playSoundEffect(errMedia);
						}
						
						//if the user's bid is below the current bid 
						else if (bidAmount <= i.currBidding) { 
							biddingErrorLabel.setText("INVALID - You must bid more than the highest bidder.");
							//play error sound
							playSoundEffect(errMedia);
						}
						
						//valid bid 
						else { 
							//reset all fields for auction page
							addErrorLabel.setText(""); //no error labels - valid bid
							biddingErrorLabel.setText("");
							//play click sound
							playSoundEffect(mouseMedia);
							
							//the user is the highest bidder
							if (!(bidAmount >= i.buyNow)) {
								//send to server Client command 
								sendToServer("changeItemBid|" + biddedItem + "|" + String.valueOf(bidAmount) + "|" + usernameClient);
								//play bid sound effect 
								playSoundEffect(bidMedia);

								//user is highest bidder - display message
								biddingErrorLabel.setText("Your are now the highest bidder on the item: " + biddedItem);
								//reset text field 
								bidTextField.clear();
							} else { //if amount is greater than the buy now price of the auction item 
								//send to server Client command 
								sendToServer("changeItemBid|" + biddedItem + "|" + String.valueOf(bidAmount) + "|" + usernameClient);
								//user purcahsed item - display message
								biddingErrorLabel.setText("You have successfully purchased the item: " + biddedItem);
								bidTextField.clear();
							}
						}
						break;
					}
				}
			}
		});
		
		
		// handler for the Add Item Button
		addButt.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				//get the string name of the item selected to add to watchlist
				String itemToAdd = itemsChoiceBox.getValue();
				
				//if item is not already in watchlist, add it to the watchlist
				if (!watchNamesHashSet.contains(itemToAdd)) { 
					//play add sound 
					playSoundEffect(addWatchlistMedia);

					//reset fields for auction page
					addErrorLabel.setText("");
					biddingErrorLabel.setText("");
					
					//the auction item to add
					AuctionItem itemAdded = null; 
					
					//do for all auction items 
					for (AuctionItem i: auctionItemsArrayList) {
						//find the corresponding auction item in the ArrayList
						if (i.itemName.contentEquals(itemToAdd)) { 
							//set the auction item to add
							itemAdded = i; 
						}
					}
					
					//auction item name Label
					Label nameLabel = new Label(itemAdded.itemName);
					nameLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 22));
					nameLabel.setTextFill(Color.DARKBLUE);
					
					//auction item description Label
					Label descriptionLabel = new Label (itemAdded.itemDescription);
					descriptionLabel.setFont(Font.font("Verdana", 11));
					descriptionLabel.setTextFill(Color.DARKBLUE);
					
					//get current bid price on auction item 
					String currBidding = "none"; //no current bids yet
					//there is a bid
					if (itemAdded.currBidding != 0.00) {
						currBidding = new String(); 
						//decimal format of bid
						currBidding += "$" + decimal_form.format(itemAdded.currBidding);
					}
					
					//get the time remaining on the auction item
					BigDecimal timeLeft = itemAdded.timeLimit;
					String timeLeftString = timeFormatter(timeLeft);
					
					//bidding information Label
					Label biddingLabel = new Label ("Minimum Bid Price: $" + decimal_form.format(itemAdded.minimumPrice) + 
													"  Buy Now Price: $" + decimal_form.format(itemAdded.buyNow) + "  Highest Bid: " + 
													currBidding + "  Highest Bidder Username: " + itemAdded.nameHighestBidder);

					biddingLabel.setFont(Font.font("Verdana", 11));
					biddingLabel.setTextFill(Color.DARKBLUE);
					biddingLabel.setId("biddingLabel"); //DO NOT CHANGE
					
					//time left Label
					Label timeLeftLabel = new Label(timeLeftString);
					timeLeftLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 11));
					timeLeftLabel.setTextFill(Color.DARKBLUE);
					timeLeftLabel.setId("timeLeftLabel"); //DO NOT CHANGE
					
					//HBox for biddingLabel and timeLeftLabel
					HBox biddingHBox = new HBox(biddingLabel, timeLeftLabel);
					
					//sold Label 
					Label sellLabel = new Label(itemAdded.itemPrintMessage); //info about whether the item is sold or available
					sellLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
					sellLabel.setTextFill(Color.DARKBLUE);
					sellLabel.setId("sellLabel"); //DO NOT CHANGE		
					
					//VBox holding biddingHBOX and sellLabel
					VBox itemDetailsVBox = new VBox(0, biddingHBox, sellLabel);
					itemDetailsVBox.setId("itemDetailsVBox"); //DO NOT CHANGE
					VBox.setMargin(sellLabel, new Insets(3, 0, 3, 0));
					
					//horizontal separator between items in the watchlist 
					Separator itemsSeparator = new Separator();
					
					//Final VBox of all info of an auction item in the watchlist
					VBox entireItemVBox = new VBox(2, nameLabel, descriptionLabel, itemDetailsVBox, itemsSeparator);
					if(evenWatchlistItem == false) { //its false
						entireItemVBox.setStyle("-fx-background-color: #F0FFFF;"); //AZURE
						evenWatchlistItem = true;
					} else { //evenWatchlistItem is true
						entireItemVBox.setStyle("-fx-background-color: #F0F8FF;"); //ALICEBLUE
						evenWatchlistItem = false;
					}
					entireItemVBox.setId(itemToAdd); //DO NOT CHANGE
					watchlistListView.getItems().add(entireItemVBox);
					
					//update data fields
					watchNamesHashSet.add(itemToAdd);
					watchItemsArrayList.add(itemAdded);
					watchPairArrayList.add(new Pair<String, VBox>(itemToAdd, entireItemVBox));
				}
				else { //auction item is already in user's watchlist
					addErrorLabel.setText("You have already added the item - " + itemToAdd + " - to your watchlist.");
					//play error sound 
					playSoundEffect(errMedia);
				}
			}
		});
		
		
		//handler for LOGOUT Button
		logoffButt.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				//play quit sound
				playSoundEffect(exitMedia);
				//set flag
	    		processComplete = true; //helper threads to finish execution
				for (Thread t : threadsArrayList) {
				    try {
						t.join();
					} catch (InterruptedException e) {
						//waiting for the joining of the background threads after logout
						System.out.println("Interruption occurred after user logged out."); 
					}
				}
				
				//initialize data fields upon LOGOUT button press 
				initializeDataFields();
				try {
					//try to disconnect the client form the server
					disconnectClientFromServer();
				} catch (IOException ex) {
					//error when attempting to close i/o streams and the socket connection.
					ex.printStackTrace();
				}
				
				//Stage title for the eHills login scene 
				primaryStage.setTitle("Logon to eHills"); //go back to logon page
				//Place the scene in the primary stage
				primaryStage.setScene(createSceneForLogin(primaryStage)); 
				//Display stage
				primaryStage.show(); 
			}
		});
		
		
		//handler for the Remove Item from Watchlist Button 
		remButt.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				//get the string name of the item selected to remove from watchlist
				String itemToRemove = itemsChoiceBox.getValue();
				synchronized (wLock) {
					//item is in watchlist currently
					if (watchNamesHashSet.remove(itemToRemove) && watchItemsArrayList.removeIf(n -> (n.itemName.contentEquals(itemToRemove))) && watchPairArrayList.removeIf(n -> (n.getKey().equals(itemToRemove)))) { 
						//play remove sound 
						playSoundEffect(remWatchlistMedia);

						watchlistListView.getItems().removeIf(n -> (n.getId().equals(itemToRemove)));
						//reset fields for auction page
						addErrorLabel.setText("");
						biddingErrorLabel.setText("");
					}
					else { //the remove button is already disabled, but just in case
						//play error sound 
						playSoundEffect(errMedia);
					}
				}
			}
		});
				
		//////////////////// Create eHills auction Scene ////////////////////
		Scene eHillsScene = new Scene(finalVBox, Screen.getPrimary().getBounds().getMaxX(), Screen.getPrimary().getBounds().getMaxY());
		eHillsScene.getStylesheets().add("final_project/myStyle.css");
		return eHillsScene;
	}
    
} //End of Client class