package application;

import java.io.IOException;
import java.sql.SQLException;

import application.view.WristbandRegistrationController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import kw.nfc.communication.ConnectDB;
import kw.nfc.communication.NFCCommunication;
import kw.nfc.communication.TerminalException;

public class RegistrationApp extends Application {
	
	private Stage primaryStage;
    private AnchorPane rootLayout;

	public static void main(String[] args) {
		launch(args);
		
	}

	@Override
	public void start(Stage stage) throws Exception {
		primaryStage = stage;
		primaryStage.setTitle("NFC Application - Wristband Registration");
		
		initRootLayout();
	}
	
	/**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("view/WristbandRegistration.fxml"));
            rootLayout = (AnchorPane) loader.load();
            
            WristbandRegistrationController controller = loader.getController();
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

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
            

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
