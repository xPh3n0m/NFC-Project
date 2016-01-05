package kw.nfc.communication;

import java.sql.SQLException;

import application.model.NFCWristband;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class UnregisterWristband extends Service<NFCWristband> {

	private NFCCommunication nfcComm;
	private Task<NFCWristband> task;
	private NFCWristband wristband;
	
	private ConnectDB connDB;
	
	public UnregisterWristband(NFCCommunication nfcComm, NFCWristband wristband, ConnectDB connDB) {
		this.nfcComm = nfcComm;
		this.connDB = connDB;
		this.wristband = wristband;
	}

	@Override
	protected Task<NFCWristband> createTask() {
		task = new Task<NFCWristband>() {
            @Override
            protected NFCWristband call() throws NFCCardException {
            	// Remove the wristband from the database  
            	//TODO: Handle SQL Exception
            	try {
					connDB.unregisterWristband(wristband);
				} catch (SQLException e) {
					// TODO This means that the wristband was not registered in the database. It should not be possible to unregister it
					e.printStackTrace();
				}
    			
    			// Write the new information on the NFC Wristband
            	// The wristband is set as recognized if the method returns succesfully
            	wristband.setStatus('I');
            	nfcComm.writeDataToNFCCard(wristband.getJSONData(), wristband);
            	
            	return wristband;
            }
		};
		return task;
	}
}
