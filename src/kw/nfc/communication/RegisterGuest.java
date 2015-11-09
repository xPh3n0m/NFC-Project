package kw.nfc.communication;

import java.sql.SQLException;
import java.util.Scanner;

import application.model.Guest;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class RegisterGuest extends Service<Guest> {

	private NFCCommunication nfcComm;
	private Task<Guest> task;
	private String guestName;
	private NFCCard card;
	
	private ConnectDB connDB;
	
	public RegisterGuest(NFCCommunication nfcComm, NFCCard card, ConnectDB connDB, String name) {
		this.nfcComm = nfcComm;
		this.connDB = connDB;
		this.card = card;
		this.guestName = name;
	}

	@Override
	protected Task<Guest> createTask() {
		task = new Task<Guest>() {
            @Override
            protected Guest call() throws NFCCardException, SQLException {
            	// Write the guest in the database
    			Guest g = Guest.newGuestInDatabase(guestName, connDB);
    			
    			// Write the guest on the wristband
    			nfcComm.writeDataToNFCCard(g.getJSONString().toJSONString(), card);
            	
            	return g;
            }
		};
		return task;
	}
}