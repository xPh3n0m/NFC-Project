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
import kw.nfc.communication.RegisterGuest;
import kw.nfc.communication.TerminalException;
import kw.nfc.communication.UpdateBalance;
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
    private Button getCashBackButton;
    @FXML 
    private Label messageLabel;
    
    @FXML
    private TextField beersTextField;
    @FXML
    private TextField totalTextField;
    @FXML
    private Button orderButton;

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
    	} else {
    		String guest_name = guestNameTextField.getText();
    		
    		if(guest_name.equals("")) {
    			messageLabel.setText("Please enter a valid name");
    			return;
    		}
    		
    		RegisterGuest regGuest = new RegisterGuest(nfcComm, currentCard, connDB, guest_name);
    		
    		regGuest.setOnSucceeded(
	    			new EventHandler<WorkerStateEvent>() {

	    		    @Override
	    		    public void handle(WorkerStateEvent t) {
	    		    	Guest newGuest = (Guest) t.getSource().getValue();
	    		    	
	    		    	messageLabel.setText("Succesfully created guest:\n" + newGuest.toString());

						}
	    		    }
	    		);
	    		
    		regGuest.setOnFailed(
	    				new EventHandler<WorkerStateEvent>() {
	    					
	    			    @Override
	    			    public void handle(WorkerStateEvent t) {
	    			    	
	    			    	if(t.getSource().getException() != null) {
	    			    		messageLabel.setText(t.getSource().getException().getMessage());
	    			    	}
	    			    }
	    		});
	    		
    		regGuest.setOnCancelled(
	    				new EventHandler<WorkerStateEvent>() {

	    					@Override
    	    			    public void handle(WorkerStateEvent t) {
    	    			    	if(t.getSource().getException() != null) {
    	    			    		messageLabel.setText(t.getSource().getException().getMessage());
    	    			    	}
    	    			    }
	    		});
    		
    		regGuest.start();
    	}
    }
    
    public void getCashBack() {
    	updateBalance(Utility.INITIAL_BALANCE);
    }
    
    public void updateBalance(double balance) {
    	UpdateBalance updateBalance = 
    			new UpdateBalance(nfcComm, currentCard, connDB, currentGuest, balance);
    	
    	updateBalance.setOnSucceeded(
    			new EventHandler<WorkerStateEvent>() {

    		    @Override
    		    public void handle(WorkerStateEvent t) {
    		    	Guest newGuest = (Guest) t.getSource().getValue();
    		    	
    		    	guestBalanceTextField.setText(newGuest.getBalance() + "");
    		    	messageLabel.setText("Succesfully updated cash balance");

					}
    		    }
    		);
    		
    	updateBalance.setOnFailed(
    				new EventHandler<WorkerStateEvent>() {
    					
    			    @Override
    			    public void handle(WorkerStateEvent t) {
    			    	
    			    	if(t.getSource().getException() != null) {
    			    		messageLabel.setText(t.getSource().getException().getMessage());
    			    	}
    			    }
    		});
    		
    	updateBalance.setOnCancelled(
    				new EventHandler<WorkerStateEvent>() {

    					@Override
	    			    public void handle(WorkerStateEvent t) {
	    			    	if(t.getSource().getException() != null) {
	    			    		messageLabel.setText(t.getSource().getException().getMessage());
	    			    	}
	    			    }
    		});
		
    	updateBalance.start();
    }
    
    public void placeOrder() {
    	double total = new Double(totalTextField.getText());
    	
    	if(currentGuest.getBalance() >= total) {
    		updateBalance(total);
    	} else {
    		messageLabel.setText("Unsufficient cash balance");
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
					
					Guest g;
					g = Guest.newGuestFromJSONString(data, connDB);

					if(g == null) {
						if(status != Utility.NEW_NFC_CARD) {
							status = Utility.NEW_NFC_CARD;
							currentGuest = g;
							
							errorLabel.setText("Unregistered NFC Card. Register a new guest");
							registerGuestButton.setText("Register new guest");
							registerGuestButton.setVisible(true);
							guestNameTextField.setEditable(true);

							getCashBackButton.setVisible(false);
							orderButton.setDisable(true);
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
							getCashBackButton.setVisible(true);
							orderButton.setDisable(false);
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
						getCashBackButton.setVisible(false);
						orderButton.setDisable(true);
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
						getCashBackButton.setVisible(false);
						orderButton.setDisable(true);
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
    
    public void updateTotal() {
    	int nbBeers = new Integer(beersTextField.getText());
    	totalTextField.setText((nbBeers * 5.0) + "");
    }

	public void setConnDB(ConnectDB connDB2) {
		this.connDB = connDB2;
	}

	public void setNFCCommunication(NFCCommunication nfcComm2) {
		this.nfcComm = nfcComm2;
		
	}


}
