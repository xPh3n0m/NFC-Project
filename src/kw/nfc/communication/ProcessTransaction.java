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
	
	private ConnectDB connDB;
	private NFCWristband wristband;
	
	private List<Order> orderList;
	
	private int gpid;
	
	public ProcessTransaction(NFCCommunication nfcComm, NFCWristband wristband, ConnectDB connDB, List<Order> orderList, int gpid) {
		this.nfcComm = nfcComm;
		this.connDB = connDB;
		this.orderList = orderList;
		this.wristband = wristband;
		this.gpid = gpid;
	}
	
	public ProcessTransaction(NFCCommunication nfcComm, NFCWristband wristband, ConnectDB connDB, List<Order> orderList) {
		this(nfcComm, wristband, connDB, orderList, 0);
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
            	
            	double newBalance = wristband.getBalance() - amount;
            	if(newBalance < 0.0) {
            		throw new Exception("Unsufficient balance");
            	}
            	
            	Transaction t = Transaction.newTransaction(wristband, orderList, gpid, connDB);
            	
            	wristband.setBalance(newBalance);
            	connDB.updateBalance(wristband, newBalance);
            	nfcComm.writeDataToNFCCard(wristband.getJSONData(), wristband);
            	
            	return t;
            }
		};
		return task;
	}

}
