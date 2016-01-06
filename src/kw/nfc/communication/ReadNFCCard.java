package kw.nfc.communication;

import java.sql.SQLException;

import application.model.NFCWristband;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ReadNFCCard extends Service<NFCWristband> {
	
	private NFCCommunication nfcComm;
	private ConnectDB connDB;
	private Task<NFCWristband> task;
	
	public ReadNFCCard(NFCCommunication nfcComm, ConnectDB connDB) {
		this.nfcComm = nfcComm;
		this.connDB = connDB;
	}

	@Override
	protected Task<NFCWristband> createTask() {
		task = new Task<NFCWristband>() {
            @Override
            protected NFCWristband call() throws NFCCardException {
            	
            	// Step 1: Read ATR, read NFC data and create NFCWristband object
            	NFCWristband wristband = nfcComm.getCurrentNFCCard();
            	
            	try {
					NFCWristband dbWristband = connDB.getNFCWristband(wristband.getUid().getBytes());
					if(dbWristband != null) { // A match has been found in the database
						if(!dbWristband.equals(wristband)) {
							nfcComm.writeDBWristbandToNFCWristband(dbWristband);
						}
						dbWristband.setValid(true);
						return dbWristband;
					}
				} catch (SQLException e) {
					// TODO Handle database connection problems
					e.printStackTrace();
				}
            	return wristband;
            }
		};
		return task;
	}

}
