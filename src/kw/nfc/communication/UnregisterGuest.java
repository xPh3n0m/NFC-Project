package kw.nfc.communication;

import java.sql.SQLException;

import application.model.Guest;
import application.model.NFCWristband;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class UnregisterGuest extends Service<NFCWristband> {

	private NFCCommunication nfcComm;
	private Task<NFCWristband> task;
	private Guest guest;
	private NFCWristband wristband;
	
	private ConnectDB connDB;
	
	public UnregisterGuest(NFCCommunication nfcComm, NFCWristband card, ConnectDB connDB) {
		this.nfcComm = nfcComm;
		this.connDB = connDB;
		this.wristband = card;
	}

	@Override
	protected Task<NFCWristband> createTask() {
		task = new Task<NFCWristband>() {
            @Override
            protected NFCWristband call() throws NFCCardException, SQLException {
            	// Update the status of the wristband in the database
            	connDB.deactivateWristband(wristband);
    			
    			// Write the new information on the NFC Wristband
            	// The wristband is set as recognized if the method returns succesfully
            	wristband.setStatus('I');
            	wristband.setGid(-1);
            	nfcComm.writeDBWristbandToNFCWristband(wristband);
            	
            	return wristband;
            }
		};
		return task;
	}
}
