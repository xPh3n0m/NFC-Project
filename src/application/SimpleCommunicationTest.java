package application;

import java.sql.SQLException;
import java.util.Scanner;

import org.json.simple.parser.ParseException;

import application.model.Guest;
import kw.nfc.communication.ConnectDB;
import kw.nfc.communication.NFCCard;
import kw.nfc.communication.NFCCardException;
import kw.nfc.communication.NFCCommunication;
import kw.nfc.communication.TerminalException;

public class SimpleCommunicationTest {
	
	public static ConnectDB connDB;

	public static void main(String[] args) {
		connDB = new ConnectDB();
		try {
			connDB.connect();
		} catch (SQLException e) {
			System.out.println("Cannot connect to the database");
			System.exit(0);
		}
		
		NFCCommunication nfcComm;
		while(true) {
			nfcComm = new NFCCommunication();
			try {
				nfcComm.connectToDefaultTerminal();
				break;
			} catch (TerminalException e) {
				System.out.println(e.getMessage());
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		
		NFCCard currentNfcCard = null;
		while(true) {
			NFCCard newNfcCard = null;
			try {
				newNfcCard = nfcComm.getCurrentNFCCard();
				
				if(newNfcCard.equals(currentNfcCard)) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else { 
					String data = newNfcCard.getData();
					
					Guest g = Guest.newGuestFromJSONString(data);
					if(g == null) {
						System.out.println("Unregistered NFC Card. Register a new guest? (y/n)");
						String choice = (new Scanner(System.in)).nextLine();
						if(choice.equals("y")) {
							System.out.println("Guest name: ");
							String guestName = (new Scanner(System.in)).nextLine();
							
							Guest newGuest;
							try {
								newGuest = Guest.newGuest(guestName, connDB);
								nfcComm.writeDataToNFCCard(newGuest.getJSONString().toJSONString(), newNfcCard);
								System.out.println("Succesfully created new user");
								System.out.println(newGuest);
							} catch (SQLException e) {
								System.out.println("Unable to write the new guest to the DB. Try again.");
							}
						}
					} else {
						System.out.println(g);
					}
				}
			} catch (NFCCardException e) {
				System.out.println(e.getMessage());
			}
			
			currentNfcCard = newNfcCard;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
