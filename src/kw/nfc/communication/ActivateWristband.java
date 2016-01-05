package kw.nfc.communication;

import java.sql.SQLException;

import application.model.NFCWristband;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ActivateWristband extends Service<NFCWristband> {

	private NFCCommunication nfcComm;
	private Task<NFCWristband> task;
	private NFCWristband wristband;
	
	private ConnectDB connDB;
	
	public ActivateWristband(NFCCommunication nfcComm, NFCWristband wristband, ConnectDB connDB) {
		this.nfcComm = nfcComm;
		this.connDB = connDB;
		this.wristband = wristband;
	}

	@Override
	protected Task<NFCWristband> createTask() {
		task = new Task<NFCWristband>() {
            @Override
            protected NFCWristband call() throws NFCCardException, SQLException {
            	// Update the status of the wristband in the database
            	connDB.activateWristband(wristband);
    			
    			// Write the new information on the NFC Wristband
            	// The wristband is set as recognized if the method returns succesfully
            	wristband.setStatus('A');
            	nfcComm.writeDBWristbandToNFCWristband(wristband);
            	
            	return wristband;
            }
		};
		return task;
	}
}
