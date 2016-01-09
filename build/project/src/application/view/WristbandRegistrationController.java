package application.view;

import java.util.Timer;
import java.util.TimerTask;

import application.RegistrationApp;
import application.model.Guest;
import application.model.NFCWristband;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import kw.nfc.communication.ConnectDB;
import kw.nfc.communication.NFCCommunication;
import kw.nfc.communication.ReadNFCCard;
import kw.nfc.communication.RegisterGuest;
import kw.nfc.communication.RegisterWristband;
import kw.nfc.communication.UnregisterGuest;
import kw.nfc.communication.UnregisterWristband;
import kw.nfc.communication.UpdateGuest;
import kw.nfc.communication.Utility;

public class WristbandRegistrationController {

	@FXML
	private AnchorPane background;
    @FXML
    private Label widInfoLabel;
    @FXML
    private Label atrInfoLabel;
    @FXML
    private Label statusInfoLabel;
    @FXML 
    private Label balanceInfoLabel;
    @FXML
    private Label errorLabel;
    @FXML
    private Label infoMessageLabel;
    @FXML
    private Button registerWristbandButton;
    @FXML
    private Button unregisterWristbandButton;
    @FXML
    private Button activateWristbandButton;
    @FXML
    private Button deactivateWristbandButton;
    
    @FXML
    private AnchorPane guestInformationAnchorPane;
    @FXML
    private Label gidLabel;
    @FXML
    private CheckBox anonymousCheckbox;
    @FXML
    private TextField firstNameTextField;
    @FXML
    private TextField lastNameTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private Button updateGuestButton;
    
    
    private ConnectDB connDB;
    private NFCCommunication nfcComm;
    
    private NFCWristband currentWristband;
    private Guest currentGuest;
    
    private int status;
    
    // Reference to the main application.
    private RegistrationApp mainApp;
    
    
    /**
     * Constructor
     */
    public WristbandRegistrationController() {
    	
    }
    
    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {	
    	_resetAllFields();
    	_hideRegistrationButtons();
    	activateWristbandButton.setVisible(false);
    	deactivateWristbandButton.setVisible(false);
    	_hideGuestPanel();
    	deactivateWristbandButton.setVisible(false);
    	activateWristbandButton.setVisible(false);
    	updateGuestButton.setVisible(false);
    }
    
	public void startReadingNFCCards() {
		// TODO Auto-generated method stub
		
		new Timer().schedule(
			    new TimerTask() {

			        @Override
			        public void run() {
			        	readWristband();
			        }
			    }, 0, 1000);
	}
    
    /**
     * Is called by the main application to give a reference back to itself.
     * 
     * @param mainApp
     */
    public void setMainApp(RegistrationApp mainApp) {
        this.mainApp = mainApp;
    }
    
    /**
     * This method reads NFC wristbands from the terminal and displays the information on the UI
     */
    public void readWristband() {
    	ReadNFCCard readNFC = new ReadNFCCard(nfcComm, connDB);
    	
    	readNFC.setOnSucceeded(
    			new EventHandler<WorkerStateEvent>() {

    		    @Override
    		    public void handle(WorkerStateEvent t) {
    		    	NFCWristband wristband = (NFCWristband) t.getSource().getValue();
    		    	if(wristband == null) {
    		    		// TODO: Do something here, no value has been returned
    		    		System.exit(0);
    		    	}
    		    	_displayErrorMessage("");
    		    	if(!wristband.uidEquals(currentWristband)) { // A new wristband has been placed, we must read again
    		    		currentWristband = wristband;
    		    		if(!wristband.isValid()) { // The wristband is not valid, it must be registered before continuing
        		    		_fillWristbandInformationLabels("", wristband.getUid().toString(), "","");
        		    		_displayInformationMessage("This wristband been recognized. "
        		    				+ "But is not part of the database. Please register it");
        		    		
        		    		registerWristbandButton.setVisible(true);
        		    		unregisterWristbandButton.setVisible(false);
            		    	activateWristbandButton.setVisible(false);
            		    	deactivateWristbandButton.setVisible(false);
            		    	_hideGuestPanel();
    		    		} else { // The wristband is in the database. If we arrived here, the NFC wristband has been updated with corect info
        		    		_fillWristbandInformationLabels(wristband.getWid() + "", wristband.getUid().toString(),
    	    		    			wristband.getStatus() + "",wristband.getBalance() + "");

        		    		// Check the status of the wristband
        		    		switch(wristband.getStatus()) {
	        		    		case 'A':
	        		    			_displayInformationMessage("Active wristband.");
	            		    		registerWristbandButton.setVisible(false);
	            		    		unregisterWristbandButton.setVisible(true);
	                		    	activateWristbandButton.setVisible(false);
	                		    	deactivateWristbandButton.setVisible(true);
	                		    	updateGuestButton.setVisible(true);
	                		    	_showGuestPanel();
	                		    	Guest g = connDB.getGuest(wristband.getGid());
	                		    	if(g != null) {
	                		    		_fillGuestInformationFields(g);
	                		    	} else {
	                		    		_fillGuestInformationFields(-1, false, "", "", "");
	                		    	}
	            		    		break;
	        		    		case 'I':
	        		    			_displayInformationMessage("The wristband has been recognized, but it is inactive."
	        		    					+ "Please activate it before using it.");
	            		    		registerWristbandButton.setVisible(false);
	            		    		unregisterWristbandButton.setVisible(true);
	                		    	activateWristbandButton.setVisible(true);
	                		    	deactivateWristbandButton.setVisible(false);
	                		    	_showGuestPanel();
	                		    	_resetGuestFields();
	            		    		break;
	            		    	default:
	        		    			_displayInformationMessage("Unkown status. Please unregister the wristband. And register it again.");
	            		    		registerWristbandButton.setVisible(false);
	            		    		unregisterWristbandButton.setVisible(true);
	                		    	activateWristbandButton.setVisible(false);
	                		    	deactivateWristbandButton.setVisible(false);
	                		    	_hideGuestPanel();
	            		    		break;
        		    		}
    		    		}
    		    	}
    		    }
    		});
    		
    	readNFC.setOnFailed(
    				new EventHandler<WorkerStateEvent>() {
    					
    			    @Override
    			    public void handle(WorkerStateEvent t) {
    			    	status = Utility.CARD_ABSCENT;
    			    	currentGuest = null;
    			    	currentWristband = null;
    			    	
    			    	if(t.getSource().getException() != null) {
    			    		_displayErrorMessage(t.getSource().getException().getMessage());
    			    	} else {
    			    		_displayErrorMessage("Unrecognized error");
    			    	}
    			    	_resetWristbandInfoFields();
    			    	_hideRegistrationButtons();
        		    	_hideGuestPanel();
    			    }
    		});
    		
    	readNFC.setOnCancelled(
    				new EventHandler<WorkerStateEvent>() {

    			    @Override
    			    public void handle(WorkerStateEvent t) {
    			    	status = Utility.CARD_ABSCENT;
    			    	currentGuest = null;
    			    	currentWristband = null;
    			    	
    			    	if(t.getSource().getException() != null) {
    			    		_displayErrorMessage(t.getSource().getException().getMessage());
    			    	} else {
    			    		_displayErrorMessage("Unrecognized error");
    			    	}
    			    	
    			    	_resetWristbandInfoFields();
    			    	_hideRegistrationButtons();
        		    	_hideGuestPanel();
    			    }
    		});
    	
    		readNFC.start();
    }
    
    public void registerWristband() {
    	RegisterWristband registerWristband = new RegisterWristband(nfcComm, currentWristband, connDB);
    	
    	registerWristband.setOnSucceeded(
    			new EventHandler<WorkerStateEvent>() {

    		    @Override
    		    public void handle(WorkerStateEvent t) {
    		    	NFCWristband wristband = (NFCWristband) t.getSource().getValue();
    		    	currentWristband = wristband;
    		    	_displayInformationMessage("Succesfully registered wristband");
		    		_fillWristbandInformationLabels(wristband.getWid() + "", wristband.getUid().toString(),
    		    			wristband.getStatus() + "",wristband.getBalance() + "");
		    		unregisterWristbandButton.setVisible(true);
		    		registerWristbandButton.setVisible(false);
    		    	activateWristbandButton.setVisible(true);
    		    	deactivateWristbandButton.setVisible(false);
    		    	updateGuestButton.setVisible(true);
		    		_resetErrorFields();
    		    	_showGuestPanel();
    		    	_resetGuestFields();
    		    }
    		});
    	
    	registerWristband.setOnFailed(
				new EventHandler<WorkerStateEvent>() {
					
			    @Override
			    public void handle(WorkerStateEvent t) {
			    	if(t.getSource().getException() != null) {
			    		_displayErrorMessage(t.getSource().getException().getMessage());
			    	} else {
			    		_displayErrorMessage("Unrecognized error");
			    	}
			    }
		});
		
    	registerWristband.setOnCancelled(
				new EventHandler<WorkerStateEvent>() {

			    @Override
			    public void handle(WorkerStateEvent t) {
			    	if(t.getSource().getException() != null) {
			    		_displayErrorMessage(t.getSource().getException().getMessage());
			    	} else {
			    		_displayErrorMessage("Unrecognized error");
			    	}
			    }
		});
	
    	registerWristband.start();
    }
    
    public void unregisterWristband() {
    	UnregisterWristband unregisterWristband = new UnregisterWristband(nfcComm, currentWristband, connDB);
    	
    	unregisterWristband.setOnSucceeded(
    			new EventHandler<WorkerStateEvent>() {

    		    @Override
    		    public void handle(WorkerStateEvent t) {
    		    	NFCWristband wristband = (NFCWristband) t.getSource().getValue();
    		    	currentWristband = wristband;
        		    _displayInformationMessage("Succesfully unregistered wristband");
    		    	_fillWristbandInformationLabels("", wristband.getUid().toString(),"","");
    		    	unregisterWristbandButton.setVisible(false);
    		    	registerWristbandButton.setVisible(true);
    		    	activateWristbandButton.setVisible(false);
    		    	deactivateWristbandButton.setVisible(false);
    		    	updateGuestButton.setVisible(false);
    		    	_resetErrorFields();
    		    	_hideGuestPanel();
    		    }
    		});
    	
    	unregisterWristband.setOnFailed(
				new EventHandler<WorkerStateEvent>() {
					
			    @Override
			    public void handle(WorkerStateEvent t) {
			    	if(t.getSource().getException() != null) {
			    		_displayErrorMessage(t.getSource().getException().getMessage());
			    	} else {
			    		_displayErrorMessage("Unrecognized error");
			    	}
			    }
		});
		
    	unregisterWristband.setOnCancelled(
				new EventHandler<WorkerStateEvent>() {

			    @Override
			    public void handle(WorkerStateEvent t) {
			    	if(t.getSource().getException() != null) {
			    		_displayErrorMessage(t.getSource().getException().getMessage());
			    	} else {
			    		_displayErrorMessage("Unrecognized error");
			    	}
			    }
		});
	
    	unregisterWristband.start();
    }
    
    
    public void unregisterGuest() {
    	UnregisterGuest unregisterGuest = new UnregisterGuest(nfcComm, currentWristband, connDB);
    	
    	unregisterGuest.setOnSucceeded(
    			new EventHandler<WorkerStateEvent>() {

    		    @Override
    		    public void handle(WorkerStateEvent t) {
    		    	NFCWristband wristband = (NFCWristband) t.getSource().getValue();
    		    	currentWristband = wristband;
        		    _displayInformationMessage("Succesfully deactivated wristband");
    		    	_fillWristbandInformationLabels(wristband.getWid()+"", wristband.getUid().toString()
    		    			,wristband.getStatus()+"",wristband.getBalance()+"");
    		    	
    		    	unregisterWristbandButton.setVisible(true);
    		    	registerWristbandButton.setVisible(false);
    		    	activateWristbandButton.setVisible(true);
    		    	deactivateWristbandButton.setVisible(false);
    		    	updateGuestButton.setVisible(false);

    		    	_resetErrorFields();
    		    	_resetGuestFields();
    		    	_showGuestPanel();
    		    	gidLabel.setText("");
    		    }
    		});
    	
    	unregisterGuest.setOnFailed(
				new EventHandler<WorkerStateEvent>() {
					
			    @Override
			    public void handle(WorkerStateEvent t) {
			    	if(t.getSource().getException() != null) {
			    		_displayErrorMessage(t.getSource().getException().getMessage());
			    	} else {
			    		_displayErrorMessage("Unrecognized error");
			    	}
			    }
		});
		
    	unregisterGuest.setOnCancelled(
				new EventHandler<WorkerStateEvent>() {

			    @Override
			    public void handle(WorkerStateEvent t) {
			    	if(t.getSource().getException() != null) {
			    		_displayErrorMessage(t.getSource().getException().getMessage());
			    	} else {
			    		_displayErrorMessage("Unrecognized error");
			    	}
			    }
		});
	
    	unregisterGuest.start();
    }
    
    public void registerGuest() {
    	Guest guest = _getCurrentGuest();
    	if(guest == null) {
    		_displayInformationMessage("Please fill in all the fields, or check anonymous");
    		return;
    	}
    	RegisterGuest registerGuest = new RegisterGuest(connDB, nfcComm, guest, currentWristband);
    	
    	registerGuest.setOnSucceeded(
    			new EventHandler<WorkerStateEvent>() {

    		    @Override
    		    public void handle(WorkerStateEvent t) {
    		    	Guest g = (Guest) t.getSource().getValue();
    		    	_fillGuestInformationFields(g);
        		    _displayInformationMessage("Succesfully registered guest");
    		    	
    		    	unregisterWristbandButton.setVisible(true);
    		    	registerWristbandButton.setVisible(false);
    		    	_resetErrorFields();
    		    	_showGuestPanel();
    		    	
    		    	activateWristbandButton.setVisible(false);
    		    	deactivateWristbandButton.setVisible(true);
    		    	updateGuestButton.setVisible(true);

    		    }
    		});
    	
    	registerGuest.setOnFailed(
				new EventHandler<WorkerStateEvent>() {
					
			    @Override
			    public void handle(WorkerStateEvent t) {
			    	if(t.getSource().getException() != null) {
			    		_displayErrorMessage(t.getSource().getException().getMessage());
			    	} else {
			    		_displayErrorMessage("Unrecognized error");
			    	}
			    }
		});
		
    	registerGuest.setOnCancelled(
				new EventHandler<WorkerStateEvent>() {

			    @Override
			    public void handle(WorkerStateEvent t) {
			    	if(t.getSource().getException() != null) {
			    		_displayErrorMessage(t.getSource().getException().getMessage());
			    	} else {
			    		_displayErrorMessage("Unrecognized error");
			    	}
			    }
		});
	
    	registerGuest.start();
    }
    
    public void updateGuest() {
    	Guest guest = _getCurrentGuest();
    	if(guest == null) {
    		_displayInformationMessage("Please fill in all the fields, or check anonymous");
    		return;
    	}
    	guest.setGid(currentWristband.getGid());
    	UpdateGuest updateGuest = new UpdateGuest(guest, connDB);
    	
    	updateGuest.setOnSucceeded(
    			new EventHandler<WorkerStateEvent>() {

    		    @Override
    		    public void handle(WorkerStateEvent t) {
    		    	Guest g = (Guest) t.getSource().getValue();
    		    	_fillGuestInformationFields(g);
        		    _displayInformationMessage("Succesfully updated guest");
    		    	
    		    	unregisterWristbandButton.setVisible(true);
    		    	registerWristbandButton.setVisible(false);
    		    	_resetErrorFields();
    		    	_showGuestPanel();

    		    	activateWristbandButton.setVisible(false);
    		    	deactivateWristbandButton.setVisible(true);
    		    }
    		});
    	
    	updateGuest.setOnFailed(
				new EventHandler<WorkerStateEvent>() {
					
			    @Override
			    public void handle(WorkerStateEvent t) {
			    	if(t.getSource().getException() != null) {
			    		_displayErrorMessage(t.getSource().getException().getMessage());
			    	} else {
			    		_displayErrorMessage("Unrecognized error");
			    	}
			    }
		});
		
    	updateGuest.setOnCancelled(
				new EventHandler<WorkerStateEvent>() {

			    @Override
			    public void handle(WorkerStateEvent t) {
			    	if(t.getSource().getException() != null) {
			    		_displayErrorMessage(t.getSource().getException().getMessage());
			    	} else {
			    		_displayErrorMessage("Unrecognized error");
			    	}
			    }
		});
	
    	updateGuest.start();
    }
    
    public void anonymize() {
    	if(anonymousCheckbox.isSelected()) {
    		_resetGuestFields();
    		firstNameTextField.setDisable(true);
    		lastNameTextField.setDisable(true);
    		emailTextField.setDisable(true);
    	} else {
    		firstNameTextField.setDisable(false);
    		lastNameTextField.setDisable(false);
    		emailTextField.setDisable(false);
    	}
    }
    
    private Guest _getCurrentGuest() {
    	Guest guest;
    	if(anonymousCheckbox.isSelected()) {
    		guest = new Guest(-1);
    	} else {
    		if(_guestIsValid()) {
    			guest = new Guest(-1, firstNameTextField.getText(), lastNameTextField.getText(), emailTextField.getText());
    		} else {
    			return null;
    		}
    	}
    	
    	return guest;
    }
    
    private boolean _guestIsValid() {
    	if(firstNameTextField.getText().equals("") 
    			|| lastNameTextField.getText().equals("")
    			|| emailTextField.getText().equals(""))
    		return false;
    	return true;
    }
    
    private void _resetGuestFields() {
		firstNameTextField.setText("");
		lastNameTextField.setText("");
		emailTextField.setText("");
		gidLabel.setText("");
    }
    
    private void _fillWristbandInformationLabels(String wid, String atr, String status, String balance) {
    	widInfoLabel.setText(wid);
    	atrInfoLabel.setText(atr);
    	statusInfoLabel.setText(status);
    	balanceInfoLabel.setText(balance);
    }
    
    private void _fillGuestInformationFields(Guest g) {
    	_fillGuestInformationFields(g.getGid(), g.isAnonymous(), g.getFirstName(), g.getLastName(), g.getEmail());
    }
    
    private void _fillGuestInformationFields(int gid, boolean anonymous, String firstName, String lastName, String email) {
    	if(gid > 0) {
    		gidLabel.setText(gid + "");
    	} else {
    		gidLabel.setText("");
    	}
		anonymousCheckbox.setSelected(anonymous);
    	if(anonymous) {
    		firstNameTextField.setText("");
    		lastNameTextField.setText("");
    		emailTextField.setText("");
    	} else {
    		firstNameTextField.setText(firstName);
    		lastNameTextField.setText(lastName);
    		emailTextField.setText(email);
    	}
    }
    
    private void _resetAllFields() {
    	widInfoLabel.setText("");
    	atrInfoLabel.setText("");
    	statusInfoLabel.setText("");
    	balanceInfoLabel.setText("");
    	errorLabel.setText("");
    	infoMessageLabel.setText("");
    }
    
    private void _resetWristbandInfoFields() {
    	widInfoLabel.setText("");
    	atrInfoLabel.setText("");
    	statusInfoLabel.setText("");
    	balanceInfoLabel.setText("");
    }
    
	private void _resetErrorFields() {
    	errorLabel.setText("");
    	//infoMessageLabel.setText("");
	}
    
    private void _hideRegistrationButtons() {
    	registerWristbandButton.setVisible(false);
    	unregisterWristbandButton.setVisible(false);
    }
    
    private void _displayInformationMessage(String message) {
    	//_resetAllFields();
    	infoMessageLabel.setText(message);
    }
    
    private void _displayErrorMessage(String errorMessage) {
    	errorLabel.setText(errorMessage);
    }
    
    private void _showGuestPanel() {
    	guestInformationAnchorPane.setVisible(true);
    }
    
    private void _hideGuestPanel() {
    	guestInformationAnchorPane.setVisible(false);
    }
    
	public void setConnDB(ConnectDB connDB2) {
		this.connDB = connDB2;
	}

	public void setNFCCommunication(NFCCommunication nfcComm2) {
		this.nfcComm = nfcComm2;
	}
}
