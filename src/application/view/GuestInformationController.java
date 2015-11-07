package application.view;

import java.sql.SQLException;
import java.util.Scanner;

import application.Main;
import application.ReadWriteNFC;
import application.model.Guest;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import kw.nfc.communication.ConnectDB;
import kw.nfc.communication.NFCCard;
import kw.nfc.communication.NFCCardException;
import kw.nfc.communication.NFCCommunication;
import kw.nfc.communication.WriteOrder;

public class GuestInformationController {

	@FXML
	private AnchorPane background;
    @FXML
    private TextField guestIdTextField;
    @FXML
    private TextField guestNameTextField;
    @FXML
    private TextField guestBalanceTextField;


    // Reference to the main application.
    private ReadWriteNFC mainApp;
    
    private ConnectDB connDB;
    
    public GuestInformationController() {
    	
    }
    
    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
    	
    }
    
    /**
     * Is called by the main application to give a reference back to itself.
     * 
     * @param mainApp
     */
    public void setMainApp(ReadWriteNFC mainApp) {
        this.mainApp = mainApp;

        // Add observable list data to the table
    }
    
    public void setNewGuest() {
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
						guestNameTextField.setText(g.getName());
						guestBalanceTextField.setText(g.getBalance() + "");
						guestIdTextField.setText(g.getGid() + "");
					}
    		    }
    		});
    		
		nfcComm.setOnFailed(
    				new EventHandler<WorkerStateEvent>() {
    					
    			    @Override
    			    public void handle(WorkerStateEvent t) {
    			    	guestNameTextField.setText(t.getSource().exceptionProperty().toString());
    			    }
    		});
    		
		nfcComm.setOnCancelled(
    				new EventHandler<WorkerStateEvent>() {

    			    @Override
    			    public void handle(WorkerStateEvent t) {
    			    	guestNameTextField.setText(t.getSource().getException().getMessage());
    			    }
    		});
    		
		nfcComm.start();
    }
}
