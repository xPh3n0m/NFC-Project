package kw.nfc.communication;

import java.sql.SQLException;

import application.model.Guest;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class UpdateGuest extends Service<Guest> {

	private NFCCommunication nfcComm;
	private Task<Guest> task;
	private Guest guest;
	private NFCCard card;
	
	private ConnectDB connDB;
	
	public UpdateGuest(NFCCommunication nfcComm, NFCCard card, ConnectDB connDB) {
		this.nfcComm = nfcComm;
		this.connDB = connDB;
		this.card = card;
	}
	
	public void setNewGuest(Guest g) {
		guest = g;
	}

	@Override
	protected Task<Guest> createTask() {
		task = new Task<Guest>() {
            @Override
            protected Guest call() throws NFCCardException, SQLException {
            	//Guest dbGuest = connDB.getGuestFromDB(guest.getGid());
            	// TODO: Check whether the guest exists already in the database. If not, throw exception
            	nfcComm.writeDataToNFCCard(guest.getJSONString().toJSONString(), card);
            	
            	guest.updateDatabase();
            	
            	return guest;
            }
		};
		return task;
	}


}
