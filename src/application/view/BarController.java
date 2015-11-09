package application.view;

import java.sql.SQLException;

import javax.smartcardio.CardException;

import org.controlsfx.dialog.Dialogs;

import application.Main;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Paint;
import kw.nfc.communication.ConnectDB;
import kw.nfc.communication.ConnectDB;
import kw.nfc.communication.Utility;
import kw.nfc.communication.WriteOrder;

public class BarController {
	@FXML
	private AnchorPane background;
    @FXML
    private Button confirmOrder;
    @FXML
    private Button cancelOrder;
    @FXML
    private TextArea noBeers;
    @FXML
    private TextArea noSpirits;
    @FXML
    private TextField actionsTextField;

    
    private WriteOrder writeOrder;

    // Reference to the main application.
    private Main mainApp;
    
    private ConnectDB connDB;

    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public BarController() {
    	writeOrder = new WriteOrder();
    	connDB = new ConnectDB();
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
    	noBeers.setText("0");
    	noSpirits.setText("0");
    	
    	try {
			connDB.connect();
		} catch (SQLException e) {
			actionsTextField.setText("Error connecting to the Database. CONTACT ADMIN");
			e.printStackTrace();
		}
    }

    /**
     * Is called by the main application to give a reference back to itself.
     * 
     * @param mainApp
     */
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }
    
    public void confirmOrderHandler() {
    	int currentOrder_noBeers = new Integer(noBeers.getText());
    	int currentOrder_noSpirits = new Integer(noSpirits.getText());
    	
    	if(currentOrder_noBeers == 0 && currentOrder_noSpirits == 0) {
    		actionsTextField.setText("Order must contain at least one drink");
    		reset();
    	} else {
        	Utility.CANCELED = false;
    		confirmOrder.setDisable(true);
        	cancelOrder.setDisable(false);
        	
        	
        	actionsTextField.setText("Place wristband on reader");
        	
        	writeOrder = new WriteOrder();
        	
        	writeOrder.setConnDB(connDB);
        	writeOrder.setOrder(currentOrder_noBeers, currentOrder_noSpirits);
	        	writeOrder.setOnSucceeded(
	    			new EventHandler<WorkerStateEvent>() {
	
	    		    @Override
	    		    public void handle(WorkerStateEvent t) {
	    		    	actionsTextField.setText("" + t.getSource().getValue());
	    		    	background.setStyle("-fx-background-color: green;");
	    		    	reset();
	    		    }
	    		});
	    		
	        	writeOrder.setOnFailed(
	    				new EventHandler<WorkerStateEvent>() {
	
	    			    @Override
	    			    public void handle(WorkerStateEvent t) {
	    			    	actionsTextField.setText("Failed, please try again");
	    			    	background.setStyle("-fx-background-color: red;");
	    			    	reset();
	    			    }
	    		});
	    		
	        	writeOrder.setOnCancelled(
	    				new EventHandler<WorkerStateEvent>() {
	
	    			    @Override
	    			    public void handle(WorkerStateEvent t) {
	    			    	actionsTextField.setText("Order cancelled");
	    			    	reset();
	    			    }
	    		});
	    		
	    		actionsTextField.setText("Place wristband on reader");
	    		writeOrder.start();
        	}
    	}
    	
    
    public void cancelOrderHandler() {
    	writeOrder.stopTask();
    	writeOrder.cancel();
    }
    
    public void incrementBeerNumberHandler() {
    	int n = new Integer(noBeers.getText());
    	if(n < 10) {
    		n++;
    		noBeers.setText(n + "");
    	}
    }
    
    public void decrementBeerNumberHandler() {	
    	int n = new Integer(noBeers.getText());
    	if(n > 0) {
    		n--;
    		noBeers.setText(n + "");
    	}
    }
    
    public void incrementSpiritsNumberHandler() {
    	int n = new Integer(noSpirits.getText());
    	if(n < 10) {
    		n++;
    		noSpirits.setText(n + "");
    	}
    }
    
    public void decrementSpiritsNumberHandler() {
    	int n = new Integer(noSpirits.getText());
    	if(n > 0) {
    		n--;
    		noSpirits.setText(n + "");
    	}
    }
    
    public void reset() {
    	noBeers.setText("0");
    	noSpirits.setText("0");
    	confirmOrder.setDisable(false);
    	cancelOrder.setDisable(true);
    	
    	try {
			connDB.reconnect();
		} catch (SQLException e) {
			actionsTextField.setText("ERROR: CONTACT ADMIN");
			background.setStyle("-fx-background-color: red;");
			e.printStackTrace();
		}
    }
}
