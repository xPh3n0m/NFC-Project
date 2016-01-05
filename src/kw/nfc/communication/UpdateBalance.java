package kw.nfc.communication;

import java.sql.SQLException;

import application.model.Guest;
import application.model.NFCWristband;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class UpdateBalance extends Service<Guest> {

	private NFCCommunication nfcComm;
	private Task<Guest> task;
	private Guest guest;
	private NFCWristband card;
	
	private ConnectDB connDB;
	
	private double newBalance;
	
	public UpdateBalance(NFCCommunication nfcComm, NFCWristband card, ConnectDB connDB, Guest g, double newBalance) {
		this.nfcComm = nfcComm;
		this.connDB = connDB;
		this.card = card;
		this.guest = g;
		this.newBalance = newBalance;
	}

	@Override
	protected Task<Guest> createTask() {
		task = new Task<Guest>() {
            @Override
            protected Guest call() throws NFCCardException, SQLException {
            	guest.setBalanceOnline(newBalance, connDB);
            	nfcComm.writeDataToNFCCard(guest.getJSONString().toJSONString(), card);
            	
            	return guest;
            }
		};
		return task;
	}
}