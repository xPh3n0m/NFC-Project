package kw.nfc.communication;

import java.sql.SQLException;

import org.json.simple.parser.ParseException;

import application.model.Guest;
import application.model.NFCWristband;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class RegisterWristband extends Service<NFCWristband> {

	private NFCCommunication nfcComm;
	private Task<NFCWristband> task;
	private NFCWristband wristband;
	
	private ConnectDB connDB;
	
	public RegisterWristband(NFCCommunication nfcComm, NFCWristband wristband, ConnectDB connDB) {
		this.nfcComm = nfcComm;
		this.connDB = connDB;
		this.wristband = wristband;
	}

	@Override
	protected Task<NFCWristband> createTask() {
		task = new Task<NFCWristband>() {
            @Override
            protected NFCWristband call() throws NFCCardException, SQLException {
            	// Insert the new wristband in the database            	
            	NFCWristband dbWristband = connDB.newWristband(wristband);
    			
    			// Write the new information on the NFC Wristband
            	nfcComm.writeDataToNFCCard(dbWristband.getJSONData(), dbWristband);
            	
            	return dbWristband;
            }
		};
		return task;
	}
}
