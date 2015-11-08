package kw.nfc.communication;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ReadNFCCard extends Service<NFCCard> {
	
	private NFCCommunication nfcComm;
	private Task<NFCCard> task;
	
	public ReadNFCCard(NFCCommunication nfcComm) {
		this.nfcComm = nfcComm;
	}

	@Override
	protected Task<NFCCard> createTask() {
		task = new Task<NFCCard>() {
            @Override
            protected NFCCard call() throws NFCCardException {
            	return nfcComm.getCurrentNFCCard();
            }
		};
		return task;
	}

}
