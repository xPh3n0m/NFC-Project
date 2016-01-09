package kw.nfc.communication;

import java.sql.SQLException;
import java.util.Scanner;

import org.json.simple.parser.ParseException;

import application.model.Guest;
import application.model.NFCWristband;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class RegisterGuest extends Service<Guest> {

	private Task<Guest> task;
	
	private ConnectDB connDB;
	private Guest guest;
	private NFCCommunication nfcComm;
	private NFCWristband wristband;
	
	public RegisterGuest(ConnectDB connDB, NFCCommunication nfcComm, Guest guest, NFCWristband wristband) {
		this.connDB = connDB;
		this.guest = guest;
		this.nfcComm = nfcComm;
		this.wristband = wristband;
	}

	@Override
	protected Task<Guest> createTask() {
		task = new Task<Guest>() {
            @Override
            protected Guest call() throws NFCCardException, SQLException {
            	// Update the status of the wristband in the database
            	Guest g = connDB.activateWristband(guest, wristband);
    			
            	// Write the guest in the database
    			//Guest g = Guest.newGuestInDatabase(guest, connDB);
    			
    			wristband.setGid(g.getGid());
            	wristband.setStatus('A');
    			// Write the guest on the wristband
    			nfcComm.writeDataToNFCCard(wristband.getJSONData(), wristband);
            	
            	return g;
            }
		};
		return task;
	}
}