package application;

import kw.nfc.communication.NFCCommunicationThread;

public class ReadWriteNFC {

	public static void main(String[] args) {
		(new Thread(new NFCCommunicationThread())).start();

	}

}
