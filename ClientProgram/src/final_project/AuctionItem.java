/* AuctionItem.java
 *  EE422C Final Project submission by
 *  Mina Abbassian
 *  mea2947
 *  16170
 *  Fall 2020
 */

package final_project;

import java.math.BigDecimal;

//AuctionItem class
class AuctionItem {
	
	//Data Fields: 
	protected double buyNow; //Buy it Now Price 
	protected String itemName; //Name of item 
	protected BigDecimal timeLimit; //Duration in minutes
	protected double currBidding; //Current Bid Price of item
	protected String itemDescription; //Description of item
	protected String itemPrintMessage; //Message displayed when an item is sold or bid on
	protected double minimumPrice; //Minimum Start Price of the item 
	protected String nameHighestBidder; //Name of the Highest Bidder of the item 
	
	//Methods: AuctionItem + Setters and Getters
	
	/**
	 * AuctionItem Constructor
	 * Called by the Client class
	 * Initializes all auction items with their corresponding information 
	 */
	protected AuctionItem (double buyNow, String itemName, BigDecimal timeLimit, double currBidding, String itemDescription, 
			               String itemPrintMessage, double minimumPrice, String nameHighestBidder) {
		this.buyNow = buyNow;
		this.itemName = itemName;
		this.timeLimit = timeLimit; 
		this.currBidding = currBidding;
		this.itemDescription = itemDescription;
		this.itemPrintMessage = itemPrintMessage;
		this.minimumPrice = minimumPrice;
		this.nameHighestBidder = new String();
		this.nameHighestBidder += nameHighestBidder;
	}
	
	//Getters
	
	/**
	 * getBuyNow
	 * @return the buy now price of the auction item 
	*/
	protected double getBuyNow() {
		return buyNow;
	}
		
	/**
	 * getItemName
	 * @return name of the Auction Item
	 */
	protected String getItemName() {
		return itemName;
	}
		
	/**
	 * getTimeLimit
	 * @return the time limit that the auction item is available for 
	 */
	protected BigDecimal getTimeLimit() {
		return timeLimit;
	}

	/**
	 * getCurrBidding
	 * @return the current bidding price of the auction item 
	 */
	protected double getCurrBidding() {
		return currBidding;
	}
		
	/**
	 * getItemDescription
	 * @return description of the auction item
	 */
	protected String getItemDescription() {
		return itemDescription;
	}
		
	/**
	 * getMinimumPrice
	 * @return the minimum price of the auction item
	 */
	protected double getMinimumPrice() {
		return minimumPrice;
	}

	/**
	 * getItemPrintMessage
	 * @return the print message of the auction item 
	 */
	protected String getItemPrintMessage() {
		return itemPrintMessage;
	}
		
	/**
	 * getNameHighestBidder
	 * @return the name of the highest bidder of the auction item 
	 */
	protected String getNameHighestBidder() {
		return nameHighestBidder;
	}
		
	//Setters 
		
	/**
	 * setBuyNow
	 * @param buyNow price to set the auction item's buy now price to
	 */
	protected void setBuyNow(double buyNow) {
		this.buyNow = buyNow;
	}
		
	/**
	 * setItemName
	 * @param itemName to set the name of the auction item
	 */
	protected void setItemName(String itemName) {
		this.itemName = itemName;
	}

	/**
	 * setTimeLimit
	 * @param timeLimit to set the auction item 
	 */
	protected void setTimeLimit(BigDecimal timeLimit) {
		this.timeLimit = timeLimit;
	}
		
	/**
	 * setCurrBidding
	 * @param currBidding the current bidding price of the auction item 
	 */
	protected void setCurrBidding(double currBidding) {
		this.currBidding = currBidding;
	}
		
	/**
	 * setItemDescription
	 * @param ItemDescription to set the description of the auction item
	 */
	protected void setItemDescription(String itemDescription) {
		this.itemDescription = itemDescription;
	}

	/**
	 * setMinimumPrice
	 * @param minimumPrice to set the auction item 
	 */
	protected void setMinPrice(double minimumPrice) {
		this.minimumPrice = minimumPrice;
	}

	/**
	 * setItemPrintMessage
	 * @param itemPrintMessage to set the print message of the auction item 
	 */
	protected void setItemPrintMessage(String itemPrintMessage) {
		this.itemPrintMessage = itemPrintMessage;
	}

	/**
	 * setNameHighestBidder
	 * @param nameHighestBidder the name of the highest bidder of the auction item 
	 */
	protected void setNameHighestBidder(String nameHighestBidder) {
		this.nameHighestBidder = nameHighestBidder;
	}
		
} //End of AuctionItem class (for Client Program)
