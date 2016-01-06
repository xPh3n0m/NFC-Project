package kw.nfc.communication;

import java.sql.SQLException;

import application.model.Guest;
import application.model.NFCWristband;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class UpdateGuest extends Service<Guest> {

	private NFCCommunication nfcComm;
	private Task<Guest> task;
	private Guest guest;	
	private ConnectDB connDB;
	
	public UpdateGuest(Guest guest, ConnectDB connDB) {
		this.guest = guest;
		this.connDB = connDB;
	}

	@Override
	protected Task<Guest> createTask() {
		task = new Task<Guest>() {
            @Override
            protected Guest call() throws NFCCardException, SQLException {
            	connDB.updateGuest(guest);
            	
            	return guest;
            }
		};
		return task;
	}
}
