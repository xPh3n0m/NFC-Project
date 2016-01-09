package application.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import application.CashHandlerApp;
import application.RegistrationApp;
import application.model.Guest;
import application.model.NFCWristband;
import application.model.Order;
import application.model.Transaction;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import kw.nfc.communication.ConnectDB;
import kw.nfc.communication.NFCCommunication;
import kw.nfc.communication.ProcessTransaction;
import kw.nfc.communication.ReadNFCCard;
import kw.nfc.communication.Utility;

public class CashHandlerController {
	
	@FXML
	private AnchorPane background;
    @FXML
    private Button button9;
    @FXML
    private Button button8;
    @FXML
    private Button button7;
    @FXML
    private Button button6;
    @FXML
    private Button button5;
    @FXML
    private Button button4;
    @FXML
    private Button button3;
    @FXML
    private Button button2;
    @FXML
    private Button button1;
    @FXML
    private Button button0;
    @FXML
    private Button buttonDot;
    @FXML
    private Button buttonClear;
    
    
    @FXML
    private TextField sumTextField;
    
    @FXML
    private Button creditButton;
    @FXML
    private Button refundButton;
    
    
    @FXML
    private Label widLabel;
    @FXML 
    private Label gidLabel;
    @FXML
    private Label balanceLabel;
    
    @FXML
    private Label errorLabel;
    @FXML
    private Label informationLabel;
    
    
    private ConnectDB connDB;
    private NFCCommunication nfcComm;
    
    private NFCWristband currentWristband;
    
    // Reference to the main application.
    private CashHandlerApp mainApp;
    
    /**
     * Constructor
     */
    public CashHandlerController() {
    	
    }
    
    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {	
    	_fillWristbandInformationLabels("", "", "");
    	_displayErrorMessage("");
    	_displayInformationMessage("");
    	_activateButtons(false);
    	clear();
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
    public void setMainApp(CashHandlerApp mainApp) {
        this.mainApp = mainApp;
    }
    
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
    		    	if(!wristband.uidEquals(currentWristband)) { // A new wristband has been placed, we must read again
    		    		currentWristband = wristband;
    		    		if(wristband.isValid() && wristband.getStatus() == 'A') { // The wristband is not valid, it must be registered before continuing
        		    		_fillWristbandInformationLabels(wristband.getWid()+"", wristband.getGid()+"", wristband.getBalance()+"");
        		    		_displayErrorMessage("");
        		    		_displayInformationMessage("The wristband has been recognized. You can either credit or refund it.");
        		    		_activateButtons(true);
    		    		} else {
    		    			_fillWristbandInformationLabels("", "", "");
    		    			_displayErrorMessage("");
    		    			_displayInformationMessage("The wristband is not activated. Please register it before.");
    		    			_activateButtons(false);
    		    		}
    		    	}
    		    }
    		});
    		
    	readNFC.setOnFailed(
    				new EventHandler<WorkerStateEvent>() {
    					
    			    @Override
    			    public void handle(WorkerStateEvent t) {
    			    	currentWristband = null;
    			    	
    			    	if(t.getSource().getException() != null) {
    			    		_displayErrorMessage(t.getSource().getException().getMessage());
    			    	} else {
    			    		_displayErrorMessage("Unrecognized error");
    			    	}
    			    	_fillWristbandInformationLabels("", "", "");
    			    	_displayInformationMessage("");
    			    	_activateButtons(false);
    			    }
    		});
    		
    	readNFC.setOnCancelled(
    				new EventHandler<WorkerStateEvent>() {

    			    @Override
    			    public void handle(WorkerStateEvent t) {
    			    	currentWristband = null;
    			    	
    			    	if(t.getSource().getException() != null) {
    			    		_displayErrorMessage(t.getSource().getException().getMessage());
    			    	} else {
    			    		_displayErrorMessage("Unrecognized error");
    			    	}
    			    	_fillWristbandInformationLabels("", "", "");
    			    	_displayInformationMessage("");
    			    	_activateButtons(false);
    			    }
    		});
    	
    		readNFC.start();
    }
    
    public void creditWristband() {
    	double amount = new Double (sumTextField.getText());
    	Order o = Order.createOrder(Utility.CASH_ORDER_ID, 1, -amount);
    	List<Order> orderList = new ArrayList<Order>();
    	orderList.add(o);
    	newTransaction(orderList);
    	//updateBalance(Utility.INITIAL_BALANCE);
    }
    
    public void refundWristband() {
    	double amount = new Double (balanceLabel.getText());
    	Order o = Order.createOrder(Utility.CASH_ORDER_ID, 1, amount);
    	List<Order> orderList = new ArrayList<Order>();
    	orderList.add(o);
    	newTransaction(orderList);
    	//updateBalance(Utility.INITIAL_BALANCE);
    }
    
    public void newTransaction(List<Order> orderList) {
    	
    	ProcessTransaction processTransaction = new ProcessTransaction(nfcComm, currentWristband, connDB, orderList);
    	
    	processTransaction.setOnSucceeded(
    			new EventHandler<WorkerStateEvent>() {

        		    @Override
        		    public void handle(WorkerStateEvent t) {
        		    	Transaction transaction = (Transaction) t.getSource().getValue();
        		    	
        		    	balanceLabel.setText(currentWristband.getBalance() + "");
        		    	informationLabel.setText("Succesfully processed transaction " + transaction.getTransactionId());
        		    	sumTextField.setText("0");
    					}
        		    }
    			);
    	
    	processTransaction.setOnFailed(
				new EventHandler<WorkerStateEvent>() {
					
			    @Override
			    public void handle(WorkerStateEvent t) {
			    	
			    	if(t.getSource().getException() != null) {
			    		errorLabel.setText(t.getSource().getException().getMessage());
			    	}
			    	
			    	informationLabel.setText("ERROR in transaction handling");
    		    	sumTextField.setText("0");
			    }
		});
		
    	processTransaction.setOnCancelled(
				new EventHandler<WorkerStateEvent>() {

					@Override
    			    public void handle(WorkerStateEvent t) {
    			    	if(t.getSource().getException() != null) {
    			    		errorLabel.setText(t.getSource().getException().getMessage());
    			    	}
    			    	
    			    	informationLabel.setText("CANCEL in transaction handling");
        		    	sumTextField.setText("0");
    			    }
		});
	
    	processTransaction.start();
    }
    
    public void appendText(ActionEvent event) {
    	Button btn = (Button) event.getSource();
    	String sumText = sumTextField.getText();
    	if(sumText.length() > 9) {
    		return;
    	}
    	
    	if(sumText.contains(".") && btn.getText().equals(".")) {
    		return;
    	}
    	
    	if(sumText.charAt(0) == '0') {
    		sumTextField.setText(btn.getText());
    	} else {
    		sumTextField.appendText(btn.getText());
    	}
    }
    
    public void clear() {
    	sumTextField.setText("0");
    }
    
	private void _fillWristbandInformationLabels(String wid, String gid, String balance) {
		// TODO Auto-generated method stub
		widLabel.setText(wid);
		gidLabel.setText(gid);
		balanceLabel.setText(balance);
	}

	private void _displayErrorMessage(String errorMessage) {
		errorLabel.setText(errorMessage);
	}
	
	private void _displayInformationMessage(String infoMessage) {
		informationLabel.setText(infoMessage);
	}
	
	private void _activateButtons(boolean b) {
		creditButton.setDisable(!b);
		refundButton.setDisable(!b);
	}
	
	public void setConnDB(ConnectDB connDB2) {
		this.connDB = connDB2;
	}

	public void setNFCCommunication(NFCCommunication nfcComm2) {
		this.nfcComm = nfcComm2;
	}
}
