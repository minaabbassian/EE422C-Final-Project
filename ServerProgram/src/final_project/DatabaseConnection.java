/* DatabaseConnection.java
 *  EE422C Final Project submission by
 *  Mina Abbassian
 *  mea2947
 *  16170
 *  Fall 2020
 */

package final_project;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;


//DatabaseConnection class
class DatabaseConnection {
	
	//Data Fields:
	private ArrayList<AuctionItem> auctionList = new ArrayList<AuctionItem>(); 
	
	//Methods: connect, collectAuctionItems
    
	/**
	 * connect
	 * Creates a connection to the SQLite database "databaseAuctionItems.db" in the package "final_project" 
	 * @author sqlitetutorial.net (ADD TO THE REPORT)
	 * @return an object representing the connection to the database containing the information of all auction items
	 */
    protected Connection connect() {
    	//declare a variable that holds a connecting string to the SQLite database
    	String urlName = "jdbc:sqlite::resource:final_project/databaseAuctionItems.db"; 
        Connection conn = null;
        try {
        	//use the DriveManager class to get a database connection based on the connection string
        	conn = DriverManager.getConnection(urlName);
        	conn.setAutoCommit(false);
        } catch (SQLException ex) { //trap any SQLException in the try catch block and display the error message
            System.out.println(ex.getMessage());
        }
        return conn; //return Connection object to the SQLite database 
    }
   
    
    /**
     * collectAuctionItems
     * Gets all the auction items by selecting the fields from the SQLite database table "Items_Table" made on DB Browser (SQLite)
     * 		and puts them in the ArrayList "auctionList" of all AuctionItems
     */
    protected ArrayList<AuctionItem> collectAuctionItems(){
    	
    	//fields from the database table "Items_Table" to select
        String fieldSelection = "SELECT ItemName, ItemDescription, MinimumPrice, BuyNow, TimeLimit FROM Items_Table";
        
        try (
        	 //connect to the SQLite database
        	 Connection conn = this.connect(); 
        	 //creates a Statement object for sending SQL statements to the database
        	 //holds the reference to a Statement object 
             Statement createStatement = conn.createStatement(); 
        	 //execute the SQL SELECT statement, returning a ResultSet object that has a set of accessor methods 
        	 //that allow you to get to the data returned from the database
             ResultSet rset = createStatement.executeQuery(fieldSelection)){
            
            //go through the ResultSet to get the data returned from the database
            while (rset.next()) {
            	double buyNow = rset.getDouble("BuyNow");
            	String itemName = rset.getString("ItemName");
            	BigDecimal timeLimit = new BigDecimal(rset.getDouble("TimeLimit"));
            	String itemDescription = rset.getString("ItemDescription");
            	double minimumPrice = rset.getDouble("MinimumPrice");
            	
            	//add each AuctionItem info into the "auctionList" ArrayList
            	AuctionItem oneItem = new AuctionItem(buyNow, itemName, timeLimit, itemDescription, minimumPrice);
            	auctionList.add(oneItem);
            }
            
        } catch (SQLException ex) { //trap any SQLException in the try catch block and display the error message
            System.out.println(ex.getMessage());
        }
        
        return auctionList; //return ArrayList holding all AuctionItems and their corresponding initial information
    }
    
} //End of DatabaseConnection class


