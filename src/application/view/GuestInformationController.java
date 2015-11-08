package application.view;

import java.sql.SQLException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import application.Main;
import application.ReadWriteNFC;
import application.model.Guest;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import kw.nfc.communication.ConnectDB;
import kw.nfc.communication.NFCCard;
import kw.nfc.communication.NFCCardException;
import kw.nfc.communication.NFCCommunication;
import kw.nfc.communication.ReadNFCCard;
import kw.nfc.communication.TerminalException;
import kw.nfc.communication.UpdateGuest;
import kw.nfc.communication.Utility;
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
    @FXML
    private Label errorLabel;
    @FXML
    private Button registerGuestButton;
    
    @FXML 
    private Label messageLabel;

    // Reference to the main application.
    private ReadWriteNFC mainApp;
    
    private ConnectDB connDB;
    
    private NFCCard currentCard;
    
    private NFCCommunication nfcComm;
    
    private Guest currentGuest;
    
    private int status;
    
    public GuestInformationController() {
    	
    }
    
    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {	
    	
    }
    
	public void startReadingNFCCards() {
		// TODO Auto-generated method stub
		
		
		new Timer().schedule(
			    new TimerTask() {

			        @Override
			        public void run() {
			        	setNewGuest();
			        }
			    }, 0, 1000);
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
    
    public void registerGuest() {
    	boolean update = true;
    	int gid;
    	try {
    		gid = new Integer(guestIdTextField.getText());
    	} catch (NumberFormatException e) {
    		update = false;
    	}
    	
    	if(update) {
    		
    		UpdateGuest updateGuest = new UpdateGuest(nfcComm, currentCard, connDB);
    		Guest g = new Guest(new Integer(guestIdTextField.getText()), guestNameTextField.getText(), new Double(guestBalanceTextField.getText()), connDB);

    		updateGuest.setNewGuest(g);
    	    	
    		updateGuest.setOnSucceeded(
    	    			new EventHandler<WorkerStateEvent>() {

    	    		    @Override
    	    		    public void handle(WorkerStateEvent t) {
    	    		    	Guest newGuest = (Guest) t.getSource().getValue();
    	    		    	
    	    		    	messageLabel.setText("Succesfully updated guest " + newGuest.toString());

    						}
    	    		    }
    	    		);
    	    		
    		updateGuest.setOnFailed(
    	    				new EventHandler<WorkerStateEvent>() {
    	    					
    	    			    @Override
    	    			    public void handle(WorkerStateEvent t) {
    	    			    	
    	    			    	if(t.getSource().getException() != null) {
    	    			    		messageLabel.setText(t.getSource().getException().getMessage());
    	    			    	}
    	    			    }
    	    		});
    	    		
    		updateGuest.setOnCancelled(
    	    				new EventHandler<WorkerStateEvent>() {

    	    					@Override
        	    			    public void handle(WorkerStateEvent t) {
        	    			    	if(t.getSource().getException() != null) {
        	    			    		messageLabel.setText(t.getSource().getException().getMessage());
        	    			    	}
        	    			    }
    	    		});
    		
    		updateGuest.start();
    	}
    }
    
    public void setNewGuest() {
    	ReadNFCCard readNFC = new ReadNFCCard(nfcComm);
    	
    	readNFC.setOnSucceeded(
    			new EventHandler<WorkerStateEvent>() {

    		    @Override
    		    public void handle(WorkerStateEvent t) {
    		    	NFCCard newNfcCard = (NFCCard) t.getSource().getValue();
    		    	currentCard = newNfcCard;
    		    	String data = newNfcCard.getData();
					
					Guest g = Guest.newGuestFromJSONString(data);

					if(g == null) {
						if(status != Utility.NEW_NFC_CARD) {
							status = Utility.NEW_NFC_CARD;
							currentGuest = g;
							
							errorLabel.setText("Unregistered NFC Card. Register a new guest");
							registerGuestButton.setText("Register new guest");
							registerGuestButton.setVisible(true);
							guestNameTextField.setEditable(true);
						}
						/*
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
						}*/
					} else {
						if(status != Utility.CARD_PRESENT) {
							status = Utility.CARD_PRESENT;
						}
						if(!g.equals(currentGuest)) {
							currentGuest = g;
							errorLabel.setText("");
							guestNameTextField.setText(g.getName());
							guestBalanceTextField.setText(g.getBalance() + "");
							guestIdTextField.setText(g.getGid() + "");
							registerGuestButton.setText("Update guest");
							registerGuestButton.setVisible(true);
							guestNameTextField.setEditable(true);
						}
						/*
						try {
							Thread.sleep(1000);
							setNewGuest();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
						}*/
					}
    		    }
    		});
    		
    	readNFC.setOnFailed(
    				new EventHandler<WorkerStateEvent>() {
    					
    			    @Override
    			    public void handle(WorkerStateEvent t) {
    			    	status = Utility.CARD_ABSCENT;
    			    	currentGuest = null;
    			    	currentCard = null;
    			    	
    			    	if(t.getSource().getException() != null) {
    			    		errorLabel.setText(t.getSource().getException().getMessage());
    			    	}
    			    	
    			    	/*
    			    	if(t.getSource().getException() != null) {
    			    		StackTraceElement[] stackTrace = t.getSource().getException().getStackTrace();
    			    		for(StackTraceElement i : stackTrace) {
    			    			errorLabel.setText(errorLabel.getText() + "\n" + i.toString());
    			    		}
    			    	}*/
						guestNameTextField.setText("");
						guestBalanceTextField.setText("");
						guestIdTextField.setText("");
						registerGuestButton.setVisible(false);
						guestNameTextField.setEditable(false);
						/*
						try {
							Thread.sleep(1000);
							setNewGuest();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
						}*/
    			    }
    		});
    		
    	readNFC.setOnCancelled(
    				new EventHandler<WorkerStateEvent>() {

    			    @Override
    			    public void handle(WorkerStateEvent t) {
    			    	status = Utility.CARD_ABSCENT;
    			    	currentGuest = null;
    			    	currentCard = null;
    			    	
    			    	if(t.getSource().getException() != null) {
    			    		errorLabel.setText(t.getSource().getException().getMessage());
    			    	}
						guestNameTextField.setText("");
						guestBalanceTextField.setText("");
						guestIdTextField.setText("");
						registerGuestButton.setVisible(false);
						guestNameTextField.setEditable(false);
						/*
						try {
							Thread.sleep(1000);
							setNewGuest();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
						}*/
						
    			    }
    		});
    	
    		readNFC.start();
    	
    }

	public void setConnDB(ConnectDB connDB2) {
		this.connDB = connDB2;
	}

	public void setNFCCommunication(NFCCommunication nfcComm2) {
		this.nfcComm = nfcComm2;
		
	}


}
