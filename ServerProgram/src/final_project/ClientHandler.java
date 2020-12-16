/* ClientHandler.java
 *  EE422C Final Project submission by
 *  Mina Abbassian
 *  mea2947
 *  16170
 *  Fall 2020
 */

package final_project;

import java.util.Observable;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.net.Socket; //A socket is an endpoint for communication between two machines
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Observer; //A class can implement the Observer interface when it wants to be informed of changes in 
						   // observable objects.


//ClientHandler class
class ClientHandler implements Runnable, Observer {
	
	//Data Fields:
	protected int clientNum = -1; //added
	private Server ser;
	protected Socket clientSocket;
	protected BufferedReader fromClient; //reads text from a character-input stream 
	protected PrintWriter toClient; //prints formatted representations of objects to a text-output stream 

	//Methods: ClientHandler, sendToClient, run, update
	
	/**
	 * Constructor for the ClientHandler class
	 */
	protected ClientHandler(Server server, Socket clientSocket, int clientNum) {
		this.ser = server;
		this.clientSocket = clientSocket; //creates a socket, thereby getting a connection to the Server
		this.clientNum = clientNum;
		try {
			//Gets the socket's output stream and opens a PrintWriter on it
			//To send data through the socket to the Server, Client simply needs to write to the the PrintWriter
			toClient = new PrintWriter(this.clientSocket.getOutputStream());
			
			//Gets the socket's input stream and opens a BufferedReader on it
			//To get the server's response, Client reads from the BufferedReader
			fromClient = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	
	/**
	 * sendToClient (Called once in the Server class)
	 * Sends String commands from Server to Client and flushes all buffers in a chain of Writers and OutputStreams
	 * @param commandString command sent from Server to Client 
	 * 		  in the form : "setUpAuctionItemsNotification|" + auctionListToString();  
	 */
	protected void sendToClient(String commandString) {
    	toClient.println(commandString);
    	
    	//flushes the stream
    	//if the stream has saved any characters from the various write() methods in a buffer, write them immediately 
    	//	to their intended destination
    	//Then, if that destination is another character or byte stream, flush it
    	//One flush() invocation will flush all the buffers in a chain of Writers and OutputStreams
    	toClient.flush(); 
	}
	

	/**
	 * run (Copied from starter files)
	 * Reads a String command line from the Client at a time from the standard input stream and immediately sends it to the Server
	 * Commands from the Client are of the form: "setUpAuctionItems clientNum" , "disconnectClient clientNum" , "changeItemBid|itemName|updatedBidding|nameHighestBidder"
	 * The String command is then processed by the Server using processRequest method in the Server class
	 */
	@Override
	public void run() {
		String inCommand;
		try {
			//while loop continues until the user types an end-of-input character
			while ((inCommand = fromClient.readLine()) != null) {
				//displays on the console the inCommand received by the Server from the Client
				System.out.println("Command from client: " + inCommand); 
				//add clientNum to the input command for setUpAuctionItems or disconnectClient commands
				if (inCommand.contentEquals("setUpAuctionItems") || inCommand.contentEquals("disconnectClient")) { 
					inCommand = inCommand + " " + clientNum;
				}
				ser.processRequest(inCommand); //server processes command 
			}
		} catch (IOException ex) {
		
		} finally { 
			//no socket connection exists for the Client anymore - when this Client leaves the server (Quits the auction)
			System.out.println("Socket connection no longer exists for Client #" + clientNum + "."); 
		}
	}

	
	/**
	 * update (Observer Interface -- must include)
	 * Copied from starter files
	 * This method is called whenever the observed object is changed. 
	 * An application calls an Observable object's notifyObservers method to have all the object's observers notified of the change.
	 */
	@Override
	public void update(Observable o, Object arg) {
		this.sendToClient((String) arg);
	} //End of ClientHandler class
}