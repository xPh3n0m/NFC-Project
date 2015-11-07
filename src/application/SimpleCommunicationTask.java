package application;

import java.sql.SQLException;
import java.util.Scanner;

import application.model.Guest;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import kw.nfc.communication.ConnectDB;
import kw.nfc.communication.NFCCard;
import kw.nfc.communication.NFCCardException;
import kw.nfc.communication.NFCCommunication;

public class SimpleCommunicationTask {
	
	public static ConnectDB connDB;
	public static NFCCard currentNfcCard;
	
	public static void main(String[] args) {

		NFCCommunication nfcComm = new NFCCommunication();
    		
		nfcComm.setOnSucceeded(
    			new EventHandler<WorkerStateEvent>() {

    		    @Override
    		    public void handle(WorkerStateEvent t) {
    		    	NFCCard newNfcCard = (NFCCard) t.getSource().getValue();
    		    	
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
							} catch (NFCCardException e) {
								System.out.println(e.getMessage());
							}
						}
					} else {
						System.out.println(g);
					}
    		    }
    		});
    		
		nfcComm.setOnFailed(
    				new EventHandler<WorkerStateEvent>() {

    			    @Override
    			    public void handle(WorkerStateEvent t) {
    			    	System.out.println(t.getSource().getMessage());
    			    }
    		});
    		
		nfcComm.setOnCancelled(
    				new EventHandler<WorkerStateEvent>() {

    			    @Override
    			    public void handle(WorkerStateEvent t) {
    			    	System.out.println(t.getSource().getMessage());
    			    }
    		});
    		
		nfcComm.start();
    	}
}

