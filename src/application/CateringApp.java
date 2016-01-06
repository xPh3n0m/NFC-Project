package application;

import java.io.IOException;
import java.sql.SQLException;

import application.view.CateringController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import kw.nfc.communication.ConnectDB;
import kw.nfc.communication.NFCCommunication;
import kw.nfc.communication.TerminalException;

public class CateringApp extends Application {
			
    private Stage primaryStage;
    private BorderPane rootLayout;
    
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Catering App");
		
		initRootLayout();
		
        // Load person overview.
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("view/Catering.fxml"));
        AnchorPane cateringApp;
		try {
			cateringApp = (AnchorPane) loader.load();
			// Set person overview into the center of root layout.
            rootLayout.setCenter(cateringApp);
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        // Give the controller access to the main app.
        CateringController controller = loader.getController();
        controller.setMainApp(this);

        ConnectDB connDB = new ConnectDB();
        try {
			connDB.connect();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
        controller.setConnDB(connDB);
        
        NFCCommunication nfcComm = new NFCCommunication();
        try {
			nfcComm.connectToDefaultTerminal();
		} catch (TerminalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
        controller.setNFCCommunication(nfcComm);
        
        controller.startReadingNFCCards();
	}
	
	 /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();
            
            ConnectDB connDB = new ConnectDB();
            try {
				connDB.connect();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(0);
			}

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public BorderPane getRootLayout() {
    	return rootLayout;
    }
	
	public static void main(String[] args) {
		launch(args);
	}
}
