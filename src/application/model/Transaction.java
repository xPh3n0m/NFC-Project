package application.model;

import java.sql.SQLException;
import java.util.List;

import kw.nfc.communication.ConnectDB;

public class Transaction {
	
	private List<Order> orderList;
	private int transactionId;
	private Guest guest;
	private double amount;
	
	private Transaction(int tid, Guest g, double amount, List<Order> orderList) {
		setTransactionId(tid);
		setGuest(g);
		this.setAmount(amount);
		this.setOrderList(orderList);
	}
	
	/**
	 * Creates a new transaction id, without the need for a guest
	 * @return
	 * @throws SQLException 
	 */
	public static Transaction newTransaction(Guest g, List<Order> orderList, ConnectDB connDB) throws SQLException {
		double amount = 0;
		for(Order o : orderList) {
			amount += o.getItemPrice() * o.getNumItem();
		}
		
		int tid = connDB.newTransaction(g.getGid(), g.getBalance(), amount);
		
		for(Order o : orderList) {
			o.setTransactionId(tid);
			connDB.addOrder(o);
		}
		
		return new Transaction(tid, g, amount, orderList);
	}
	
	public void processTransaction(int gid, List<Order> orders) {
		
		//Get the price of each order, and compute the price of the transaction
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public Guest getGuest() {
		return guest;
	}

	public void setGuest(Guest guest) {
		this.guest = guest;
	}

	public int getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(int transactionId) {
		this.transactionId = transactionId;
	}

	public List<Order> getOrderList() {
		return orderList;
	}

	public void setOrderList(List<Order> orderList) {
		this.orderList = orderList;
	}

}

