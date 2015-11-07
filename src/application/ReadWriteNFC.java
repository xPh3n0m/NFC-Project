package application;

import java.io.IOException;

import application.view.GuestInformationController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import kw.nfc.communication.NFCCommunicationThread;

public class ReadWriteNFC extends Application {
	
	private Stage primaryStage;
    private AnchorPane rootLayout;

	public static void main(String[] args) {
		launch(args);
		
	}

	@Override
	public void start(Stage stage) throws Exception {
		primaryStage = stage;
		primaryStage.setTitle("NFC Application - Read Guest Information");
		
		initRootLayout();
	}
	
	/**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("view/GuestInformation.fxml"));
            rootLayout = (AnchorPane) loader.load();
            
            GuestInformationController controller = loader.getController();
            controller.setMainApp(this);

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
            

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
