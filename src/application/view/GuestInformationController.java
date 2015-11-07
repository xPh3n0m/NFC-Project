package application.view;

import application.Main;
import application.ReadWriteNFC;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import kw.nfc.communication.WriteOrder;

public class GuestInformationController {

	@FXML
	private AnchorPane background;
    @FXML
    private TextField guestId;
    @FXML
    private TextField guestName;
    @FXML
    private TextField guestBalance;


    // Reference to the main application.
    private ReadWriteNFC mainApp;
    
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
}
