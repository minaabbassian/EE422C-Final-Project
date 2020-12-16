/* Server.java
 *  EE422C Final Project submission by
 *  Mina Abbassian
 *  mea2947
 *  16170
 *  Fall 2020
 */

package final_project;

import java.util.Queue;
import java.io.IOException;
import java.net.ServerSocket;
import java.math.BigDecimal;
import java.util.TimerTask;
import java.net.Socket;
import java.util.LinkedList;
import java.util.ArrayList;
import java.math.RoundingMode;
import java.util.Timer;
//An observable object can have one or more observers. An observer may be any object that implements interface Observer. 
//After an observable instance changes, an application calling the Observable's notifyObservers method causes all of 
//its observers to be notified of the change by a call to their update method.
import java.util.Observable; 


//Server class
public class Server extends Observable {
	
	//Data Fields:
	private ArrayList<ClientHandler> clientList = new ArrayList<ClientHandler>(); //list of observers 
	private Object availableLock = new Object(); //for synchronization
	private ArrayList<AuctionItem> availableAuctionList = new ArrayList<AuctionItem>(); //available auction items list 
	private Integer clientNum = 0; //number of Clients 
	private Queue<AuctionItem> unavailableAuctionQueue = new LinkedList<AuctionItem>(); //expired auction items list 
	final static BigDecimal sec = new BigDecimal(1.0).divide(new BigDecimal(60.0), 100, RoundingMode.HALF_UP); //one second
	final static BigDecimal decimalZero = BigDecimal.ZERO; 
	private ArrayList<AuctionItem> auctionList = new ArrayList<AuctionItem>(); //list of AuctionItems 
	
	//Methods: main, runServer, setUpNetworking, processRequest, runAvailableTimer, runUnavailableTimer
	
	/**
	 * main
	 * Creates and runs a new Server
	 * @param args
	 */
	public static void main(String[] args) {
		new Server().runServer();
	}
	
	
	/**
	 * runServer
	 * Initializes two timers
	 * Handles networking constantly for server 
	 */
	private void runServer() {
	    try {
			//creates a DatabaseConnection object 
	    	DatabaseConnection reader = new DatabaseConnection();
	    	//connect to SQLite database "databaseAuctionItems.db"
	    	reader.connect();
	    	//initialize the two ArrayLists based on the auction items' info in the database
	    	auctionList.addAll(reader.collectAuctionItems()); //holds the info of all AuctionItems in an ArrayList
	    	availableAuctionList.addAll(auctionList); //holds all of the available auction items for sale
	    	
	    	//set up the two timers 
	    	runAvailableTimer();
	    	runUnavailableTimer();
	    	
	    	//establish socket connections 
	    	setUpNetworking();
	    	
	    } catch (Exception ex) { //catch all exceptions
	    	ex.printStackTrace();
	    	return;
	    }
	}
	
	
	/**
	 * setUpNetworking (copied from starter files)
	 * Looks for and then accepts Clients attempting to connect to the ServerSocket 
	 * For every Client connected to the ServerSocket, a new ClientHandler and thread is created and added a Server Observer
	 * @throws Exception
	 */
	private void setUpNetworking() throws Exception {
	    @SuppressWarnings("resource") //should remove the warning for a potential resource leak
		ServerSocket serverSocket = new ServerSocket(4242); //serverSocket bound to the port 4242
	    while (true) {
	    	Socket clientSocket = serverSocket.accept(); //accept Client requests to connect to the Server
	    	System.out.println("Client #" + clientNum + " is connecting to the server by " + clientSocket); //print to console
	    	//ClientHandler created for each Client connected to the Server 
	    	ClientHandler hand = new ClientHandler(this, clientSocket, clientNum);
	    	//add ClientHandler as an observer of the Server
	    	this.addObserver(hand);
	    	//increment number of Clients connected to the Server 
	    	clientNum = clientNum + 1;
	    	//add ClientHandler to the ArrayList of observers  
	    	clientList.add(hand); 
	    	//create and start a new thread 
	    	Thread clientThread = new Thread(hand);
	    	clientThread.start();
	    }
	}
	
	
	/**
	 * processRequest (called by run() in ClientHandler class)
	 * Processes the commands received by the Server from the Client 
	 * The 3 possible input commands are:
	 * 		"changedItemBidchangeItemBid|itemName|updatedBidding|nameHighestBidder" , "disconnectClient clientNum" , "setUpAuctionItems clientNum"
	 * @param inputCommand String holding command received by the Server from the Client 
	 */
	protected synchronized void processRequest(String inputCommand) {
		String output = ""; 
	   
	   //input command contains "|" characters
	   if (inputCommand.contains("|")) {
		   String[] commandString = inputCommand.trim().split("\\|");
		   
			  //switch statement for first argument of input command 
			  switch (commandString[0]) {
			    //the command is "changeItemBid|itemName|updatedBidding|nameHighestBidder"
			  	case "changeItemBid": 
			  		//do for all available auction items 
			  		for (AuctionItem i : availableAuctionList) {
			  			//find the auction item whose bid price needs to be updated 
			  			if (i.itemName.contentEquals(commandString[1])) {
			  				i.nameHighestBidder = new String(); 
			  				//set the name of the highest bidder of that auction item 
			  				i.nameHighestBidder = i.nameHighestBidder + commandString[3]; 
			  				
			  				//set the current highest bid to the new bid value in the second input command 
			  				i.currBidding = Double.parseDouble(commandString[2]);
			  			
			  			    //marks this Observable object (Server) as having been changed; the hasChanged method will now return true
					  		this.setChanged(); 
					  		
					  		//If this object has changed, as indicated by the hasChanged method, then notify all of its observers and then
					  		//	call the clearChanged method to indicate that this object has no longer changed.
					  		//observers notified: changeItemBidNotification|itemName|currBidding|nameHighestBidder
					  		this.notifyObservers("changeItemBidNotification|" + i.itemName + "|" + i.currBidding + "|" + i.nameHighestBidder);
					  		break;
			  			}
			  		}
					break;

				default:
			 }
	   } else { //no "|" characters in the inputCommand String
		   		// command is one of the following: "disconnectClient clientNum" , "setUpAuctionItems clientNum"
		   //parse around spaces in the command 
		   String[] commandString = inputCommand.trim().split(" ");
		   
		   //switch statement for first argument of input command 
		   switch (commandString[0]) {
		   
		 //command is of the form "disconnectClient clientNum"
		   case "disconnectClient": //removes Client as an observer of the Server
			     ClientHandler rem = null; //the one to remove 
		  		 int num = Integer.parseInt(commandString[1]); //clientNum 
		  		 
		  		 //do for all observers in the clientList
		  		 for (ClientHandler o : clientList) {
		  			 //find the corresponding ClientHandler using the clientNum 
		  		 	 if (o.clientNum == num) {
		  				 rem = o; //set the ClientHandler to be removed to corresponding observer
		  				 break;
		  			 }
		  		 }
				 try {
					 rem.toClient.flush();
					 rem.toClient.close();
					 rem.fromClient.close();
					 rem.clientSocket.close();
					 
				 } catch (IOException ex) {
					 System.out.println("ERROR disconnecting Client from the Server."); 
				 }
		  		 this.deleteObserver(rem); //remove ClientHandler as an observer 
		  		 clientNum = clientNum - 1; //decrement number of Client observers 
		  		 clientList.remove(rem); //remove ClientHandler from observer list 
		  		 break;
		  		 
		   //command is of the form "setUpAuctionItems clientNum"
		   case "setUpAuctionItems" :
			     ClientHandler h = null; 
		  		 int clientNumber = Integer.parseInt(commandString[1]); //clientNum to setUpAuctionItems for 
		  		 
		  		 //do for all observers in the clientList 
		  		 for (ClientHandler o : clientList) {
		  			 //found the customer 
		  			 if (o.clientNum == clientNumber) {
		  				 h = o; //set the ClientHandler to corresponding observer
		  			 }
		  		 }
		  		 
		 		String auctionString = ""; //String holding all AuctionItem info separated by "|" characters in the auctionList 
				//do for all AuctionItems in the auctionList
				for (AuctionItem i : auctionList) {
				    //string is of the form: itemName|itemDescription|minimumPrice|currBidding|buyNow|nameHighestBidder|timeLimit|itemPrintMessage|
				    auctionString += i.itemName + "|" + i.itemDescription + "|" + String.valueOf(i.minimumPrice) + "|" + 
				    					String.valueOf(i.currBidding) + "|" + String.valueOf(i.buyNow) + "|" + 
				    					i.nameHighestBidder + "|" + String.valueOf(i.timeLimit) + "|" + i.itemPrintMessage + "|";
				}
				
		  		 output += "setUpAuctionItemsNotification|" + auctionString; 
		  		 h.sendToClient(output); //sent to client: setUpAuctionItemsNotification|auctionString
				 break;

		  	 default:
		  		
		   }
	  }
			  
	}
	   
	
	/**
	 * runAvailableTimer (count down clock for each auction item)
	 * Sets up and runs a timer that decreases the time remaining by one every second for all available auction items 
	 * Synchronization with the runUnavailableTimer() code to remove unavailable auction items 
	 * 		will avoid concurrent modification exceptions errors
	 */
	private void runAvailableTimer() {
		//Timer
		Timer timer = new Timer();
		
		//Server object 
		Server obj = this;

		//used to schedule the specified task for repeated fixed-rate execution, beginning after the specified delay 
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				//synchronization to prevent concurrent modification exceptions 
				synchronized(availableLock) {
					//do for all available auction items 
					for (AuctionItem i : availableAuctionList) {
						
						//time remaining for auction item is less than 1 second -- item expired
						if (i.timeLimit.compareTo(sec) != 1) { 
							i.timeLimit = decimalZero; //set time remaining of auction item to 0
							unavailableAuctionQueue.add(i); //add the auction item to the unavailable queue
							
							//marks this Observable object (Server) as having been changed; the hasChanged method will now return true
					  		obj.setChanged();
					  		//observers notified of the time change: changedTimeNotification|itemName|timeLimit
					  		obj.notifyObservers("changedTimeNotification|" + i.itemName + "|" + i.timeLimit);
						} else { //decrement time limit by 1 if time remaining is greater than 1 second 
							i.timeLimit = i.timeLimit.subtract(sec); //decrease by 1 second 
							
							//marks this Observable object (Server) as having been changed; the hasChanged method will now return true
					  		obj.setChanged();
					  		//observers notified of the time change: changedTimeNotification|itemName|timeLimit
					  		obj.notifyObservers("changedTimeNotification|" + i.itemName + "|" + i.timeLimit);

						}
					}
				}
			}
		}, 0, 1000); //delay in milliseconds before task is to be executed, period in milliseconds between successive task executions 
	}
	
	
	/**
	 * runUnavailableTimer (clock for sold or otherwise unavailable auction items)
	 * Runs a timer that notifies Client of all sold or otherwise unavailable auction items 
	 * Removes these sold or otherwise unavailable auction items from the availableAuctionList
	 * Synchronization of remove() with the runAvailableTimer code will avoid concurrent modification exceptions errors  
	 */
	private void runUnavailableTimer() {
		//Timer
		Timer timer = new Timer();
		
		//Server object
		Server obj = this;
		
		//used to schedule the specified task for repeated fixed-rate execution, beginning after the specified delay 
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				//synchronization to prevent concurrent modification exceptions 
				synchronized (availableLock) {
					//do for all available auction items
					for (AuctionItem i : availableAuctionList) {
						
						//if the time limit for the auction item has ended, i.e. timeLimit == 0
						if (i.timeLimit.compareTo(decimalZero) == 0) { 
							//bidding for the auction item ended with a highest bidder
							if (!(i.nameHighestBidder.contentEquals("none"))) { 
								i.itemPrintMessage = (i.nameHighestBidder + " purchased the item - " + i.itemName + " - for the highest bidded price of $" + i.currBidding + "."); 
								
							} else { //nobody bid on the auction item 
								i.itemPrintMessage = "Auction for this item has ended. The item has been marked unsold."; 
							}
							obj.setChanged();
							//observers notified: purchasedItemNotification|itemName|itemPrintMessage
							obj.notifyObservers("purchasedItemNotification|" + i.itemName + "|" + i.itemPrintMessage);
							
						} else if (i.currBidding >= i.buyNow) { //customer bid higher than the buyNow price
							//purchase notification
							i.itemPrintMessage = (i.itemName + " was sold to " + i.nameHighestBidder + " for the price of $" + i.currBidding + ".");
							obj.setChanged();
							//observers notified: purchasedItemNotification|itemName|itemPrintMessage
							obj.notifyObservers("purchasedItemNotification|" + i.itemName + "|" + i.itemPrintMessage);
							
							//time notification
							i.timeLimit = decimalZero; //set timeLimit to 0 since the item has been sold immediately 
					  		unavailableAuctionQueue.add(i); //this auction item is now unavailable 
					  		obj.setChanged();
					  		//observers notified: changedTimeNotification|itemName|timeLimit
					  		obj.notifyObservers("changedTimeNotification|" + i.itemName + "|" + i.timeLimit);
						}
					} while (!unavailableAuctionQueue.isEmpty()) {
						//remove all sold/expired auction items from the availableAuctionList 
						availableAuctionList.remove(unavailableAuctionQueue.remove()); //synchronization
					}
				}
			}
		}, 0, 50); //delay in milliseconds before task is to be executed, period in milliseconds between successive task executions
	}
	  
} //End of Server class
