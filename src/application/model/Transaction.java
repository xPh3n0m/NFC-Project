package application.model;

import java.sql.SQLException;
import java.util.List;

import kw.nfc.communication.ConnectDB;

public class Transaction {
	
	private List<Order> orderList;
	private int transactionId;
	private NFCWristband wristband;
	private double amount;
	
	private Transaction(int tid, NFCWristband wristband, double amount, List<Order> orderList) {
		setTransactionId(tid);
		setWristband(wristband);
		this.setAmount(amount);
		this.setOrderList(orderList);
	}
	
	/**
	 * Creates a new transaction id, without the need for a guest
	 * @return
	 * @throws SQLException 
	 */
	public static Transaction newTransaction(NFCWristband wristband, List<Order> orderList, int gpid, ConnectDB connDB) throws SQLException {
		double amount = 0;
		for(Order o : orderList) {
			amount += o.getItemPrice() * o.getNumItem();
		}
		
		int tid = connDB.newTransaction(wristband.getWid(), wristband.getGid(), wristband.getBalance(), amount, gpid);
		
		for(Order o : orderList) {
			o.setTransactionId(tid);
			connDB.addOrder(o);
		}
		
		return new Transaction(tid, wristband, amount, orderList);
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

	public NFCWristband getWristband() {
		return wristband;
	}

	public void setWristband(NFCWristband w) {
		this.wristband = w;
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

