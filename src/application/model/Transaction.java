package application.model;

import java.util.List;

public class Transaction {
	
	public List<Order> orderList;
	public int transactionId;
	public int guestId;
	public double price;
	
	private Transaction(int tid) {
		transactionId = tid;
	}
	
	/**
	 * Creates a new transaction id, without the need for a guest
	 * @return
	 */
	public static Transaction newTransaction() {
		// Need to create a new transaction in the db and get the transaction id
		int tid = 0;
		
		return new Transaction(tid);
	}
	
	public void processTransaction(int gid, List<Order> orders) {
		guestId = gid;
		
		//Get the price of each order, and compute the price of the transaction
	}

}

