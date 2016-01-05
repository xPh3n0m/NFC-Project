package kw.nfc.communication;

import java.sql.SQLException;
import java.util.List;

import application.model.Guest;
import application.model.NFCWristband;
import application.model.Order;
import application.model.Transaction;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ProcessTransaction extends Service<Transaction>{
	
	private NFCCommunication nfcComm;
	private Task<Transaction> task;
	private Guest guest;
	private NFCWristband card;
	
	private ConnectDB connDB;
	
	private List<Order> orderList;
	
	public ProcessTransaction(NFCCommunication nfcComm, NFCWristband card, ConnectDB connDB, Guest g, List<Order> orderList) {
		this.nfcComm = nfcComm;
		this.connDB = connDB;
		this.card = card;
		this.guest = g;
		this.orderList = orderList;
	}

	@Override
	protected Task<Transaction> createTask() {
		task = new Task<Transaction>() {
            @Override
            protected Transaction call() throws Exception {
            	double amount = 0;
            	for(Order o : orderList) {
            		amount += o.getItemPrice() * o.getNumItem();
            	}
            	if(guest.getBalance() - amount < 0) {
            		throw new Exception("Unsufficient balance");
            	}
            	Transaction t = Transaction.newTransaction(guest, orderList, connDB);
            	
            	t.getGuest().setBalanceOnline(t.getGuest().getBalance() - t.getAmount(), connDB);
            	nfcComm.writeDataToNFCCard(t.getGuest().getJSONString().toJSONString(), card);
            	
            	return t;
            }
		};
		return task;
	}

}
