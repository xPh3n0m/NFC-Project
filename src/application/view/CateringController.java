package application.view;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.javafx.iio.gif.GIFDescriptor;
import com.sun.prism.impl.Disposer.Record;

import application.CashHandlerApp;
import application.CateringApp;
import application.model.NFCWristband;
import application.model.Order;
import application.model.Transaction;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import application.model.MenuItem;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import kw.nfc.communication.ConnectDB;
import kw.nfc.communication.LoadMenuItems;
import kw.nfc.communication.NFCCommunication;
import kw.nfc.communication.ProcessTransaction;
import kw.nfc.communication.ReadNFCCard;

public class CateringController implements Initializable {
	
    @FXML
    private TableView<MenuItem> menuItemsTable;
    @FXML
    private TableColumn<MenuItem, String> itemNameColumn;
    @FXML
    private TableColumn<MenuItem, String> itemDescriptionColumn;
    @FXML
    private TableColumn<MenuItem, Double> totalPriceColumn;
    @FXML
    private TableColumn<MenuItem, Integer> itemQuantityColumn;
    @FXML
    private TableColumn<MenuItem, Double> itemPriceColumn;
    
    @FXML
    private ObservableList<MenuItem> menuItemsData = FXCollections.observableArrayList();
    
    
    @FXML
    private TextField cateringGroupNumberTextField;
    @FXML
    private Button decreaseGroupNumberButton;
    @FXML
    private Button increaseGroupNumberButton;
    @FXML
    private Button loadMenuButton;
    
    @FXML
    private TextField totalTextField;
    @FXML
    private Button orderButton;
    @FXML
    private Button cancelButton;
    
    @FXML
    private Label errorLabel;
    @FXML
    private Label informationLabel;
    
    @FXML
    private Label gidLabel;
    @FXML
    private Label widLabel;
    @FXML
    private Label balanceLabel;
    @FXML
    private Label wristbandInfoLabel;
    @FXML
    private Label wristbandErrorLabel;
    
    @FXML
    private Label informationOrderLabel;
    @FXML
    private Label errorOrderLabel;

    private int currentCateringId;
    
    private ConnectDB connDB;
    private NFCCommunication nfcComm;
    
    private NFCWristband currentWristband;
	
    // Reference to the main application.
    private CateringApp mainApp;
    
    /**
     * Constructor
     */
    public CateringController() {
    	
    }
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
        // Initialize the event table with the columns.
		itemNameColumn.setCellValueFactory(cellData -> cellData.getValue().getItemNameProperty());
		itemDescriptionColumn.setCellValueFactory(cellData -> cellData.getValue().getItemDescriptionProperty());
		itemPriceColumn.setCellValueFactory(cellData -> cellData.getValue().getItemPriceProperty().asObject());
		itemQuantityColumn.setCellValueFactory(cellData -> cellData.getValue().getItemQuantityProperty().asObject());
		totalPriceColumn.setCellValueFactory(cellData -> cellData.getValue().getTotalPriceProperty().asObject());		
		
		menuItemsTable.setColumnResizePolicy((param) -> true );

		createQuantityButtons();
		cateringGroupNumberTextField.setText("1");
		resetTotal();
		resetAllErrorInfomationFields();
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
    		    			gidLabel.setText(wristband.getGid()+"");
    		    			widLabel.setText(wristband.getWid()+"");
    		    			balanceLabel.setText(wristband.getBalance()+"");
    		    			resetAllErrorInfomationFields();
    		    			orderButton.setDisable(false);
    		    		} else {
    		    			wristbandInfoLabel.setText("This wristband is not registered. Please register before");  
    		    			wristbandErrorLabel.setText("");
    		    			orderButton.setDisable(true);
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
    		    			wristbandInfoLabel.setText("");  
    		    			wristbandErrorLabel.setText(t.getSource().getException().getMessage());
    		    			orderButton.setDisable(true);
    			    	} else {
    			    		_displayErrorMessage("Unrecognized error");
    			    	}

    			    }
    		});
    		
    	readNFC.setOnCancelled(
    				new EventHandler<WorkerStateEvent>() {

    			    @Override
    			    public void handle(WorkerStateEvent t) {
    			    	currentWristband = null;
    			    	
    			    	if(t.getSource().getException() != null) {
    		    			wristbandInfoLabel.setText("");  
    		    			wristbandErrorLabel.setText(t.getSource().getException().getMessage());
    		    			orderButton.setDisable(true);    			    	
    		    		} else {
    			    		_displayErrorMessage("Unrecognized error");
    			    	}
    			    }
    		});
    	
    		readNFC.start();
    }
    
    public void placeOrder() {    	
    	List<Order> orderList = new ArrayList<Order>();
    	for(MenuItem mi : menuItemsData) {
    		if(mi.getItemQuantity()>0) {
    			Order o = Order.createOrder(mi.getIid(), mi.getItemQuantity(), mi.getItemPrice());
    			orderList.add(o);
    		}
    	}
    	newTransaction(orderList);
    	//updateBalance(Utility.INITIAL_BALANCE);
    }
    
    public void newTransaction(List<Order> orderList) {
    	
    	ProcessTransaction processTransaction = new ProcessTransaction(nfcComm, currentWristband, connDB, orderList, currentCateringId);
    	
    	processTransaction.setOnSucceeded(
    			new EventHandler<WorkerStateEvent>() {

        		    @Override
        		    public void handle(WorkerStateEvent t) {
        		    	Transaction transaction = (Transaction) t.getSource().getValue();
        		    	resetAllErrorInfomationFields();
        		    	balanceLabel.setText(currentWristband.getBalance() + "");
        		    	informationOrderLabel.setText("Succesfully processed transaction " + transaction.getTransactionId());
        		    	resetTotal();
        		    	cancel();
    					}
        		    }
    			);
    	
    	processTransaction.setOnFailed(
				new EventHandler<WorkerStateEvent>() {
					
			    @Override
			    public void handle(WorkerStateEvent t) {
			    	
			    	if(t.getSource().getException() != null) {
			    		errorOrderLabel.setText(t.getSource().getException().getMessage());
			    	}
			    	
			    }
		});
		
    	processTransaction.setOnCancelled(
				new EventHandler<WorkerStateEvent>() {

					@Override
    			    public void handle(WorkerStateEvent t) {
    			    	if(t.getSource().getException() != null) {
    			    		errorOrderLabel.setText(t.getSource().getException().getMessage());
    			    	}
    			    	
					}
		});
	
    	processTransaction.start();
    }
    
    public void decreaseGroupNumber() {
    	int k = new Integer(cateringGroupNumberTextField.getText());
    	if(k > 1) {
    		cateringGroupNumberTextField.setText((k-1)+"");
    	}
    }
    
    public void increseGroupNumber() {
    	int k = new Integer(cateringGroupNumberTextField.getText());
    	if(k < 9) {
    		cateringGroupNumberTextField.setText((k+1)+"");
    	}
    }
    
    public void loadMenuItems() {
    	int groupNumber = new Integer(cateringGroupNumberTextField.getText());
    	LoadMenuItems loadMenu = new LoadMenuItems(connDB, groupNumber);
 
    	
    	loadMenu.setOnSucceeded(
    			new EventHandler<WorkerStateEvent>() {

    		    @Override
    		    public void handle(WorkerStateEvent t) {
    		    	ArrayList<MenuItem> newMenuItems = (ArrayList<MenuItem>) t.getSource().getValue();
    		       	resetTable();
    		    	resetTotal();
    				for (MenuItem mi : newMenuItems){
    					menuItemsData.add(mi);
    				}
    				menuItemsTable.setItems(menuItemsData);
    				currentCateringId = groupNumber;
    				_displayErrorMessage("");
    				_displayInformationMessage("Succesfully loaded menu");
    		    }
    		});
    		
    	loadMenu.setOnFailed(
    				new EventHandler<WorkerStateEvent>() {
    					
    			    @Override
    			    public void handle(WorkerStateEvent t) {
    			    	currentWristband = null;
    			    	
    			    	if(t.getSource().getException() != null) {
    			    		_displayErrorMessage(t.getSource().getException().getMessage());
    			    	} else {
    			    		_displayErrorMessage("Unrecognized error");
    			    	}
    			    }
    		});
    		
    	loadMenu.setOnCancelled(
    				new EventHandler<WorkerStateEvent>() {

    			    @Override
    			    public void handle(WorkerStateEvent t) {
    			    	currentWristband = null;
    			    	
    			    	if(t.getSource().getException() != null) {
    			    		_displayErrorMessage(t.getSource().getException().getMessage());
    			    	} else {
    			    		_displayErrorMessage("Unrecognized error");
    			    	}
    			    }
    		});
    	
    	loadMenu.start();
    }
    
	private void _displayErrorMessage(String string) {
		errorLabel.setText(string);
	}
	
	private void _displayInformationMessage(String string) {
		informationLabel.setText(string);
	}
	
	private void resetTable() {
		menuItemsData.removeAll(menuItemsData);
	}
	
	private void resetAllErrorInfomationFields() {
		errorLabel.setText("");
		informationLabel.setText("");
		informationOrderLabel.setText("");
		errorOrderLabel.setText("");
		wristbandErrorLabel.setText("");
		wristbandInfoLabel.setText("");
	}
	
	public void updateTotal() {
		double totalPrice = 0.0;
		for(MenuItem mi : menuItemsData) {
			totalPrice += mi.getTotalPrice();
		}
		
		totalTextField.setText(totalPrice+"");
	}
	
	public void resetTotal() {
		totalTextField.setText("0");
	}
	
	public void cancel() {
		for(MenuItem mi : menuItemsData) {
			mi.setTotalPrice(0.0);
			mi.setItemQuantity(0);
		}
		resetTotal();
	}
    
    /**
     * Is called by the main application to give a reference back to itself.
     * 
     * @param mainApp
     */
    public void setMainApp(CateringApp mainApp) {
    	this.mainApp = mainApp;
    }
    
	public void setConnDB(ConnectDB connDB2) {
		this.connDB = connDB2;
	}

	public void setNFCCommunication(NFCCommunication nfcComm2) {
		this.nfcComm = nfcComm2;
	}
	
	private void createQuantityButtons() {
        TableColumn decreaseQuantityColumn = new TableColumn<>("");
        menuItemsTable.getColumns().add(3, decreaseQuantityColumn);
        
        decreaseQuantityColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Record, Boolean>, 
                ObservableValue<Boolean>>() {

            @Override
            public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<Record, Boolean> p) {
                return new SimpleBooleanProperty(p.getValue() != null);
            }
        });

        //Adding the Button to the cell
        decreaseQuantityColumn.setCellFactory(
                new Callback<TableColumn<Record, Boolean>, TableCell<Record, Boolean>>() {

            @Override
            public TableCell<Record, Boolean> call(TableColumn<Record, Boolean> p) {
                return new ReduceQuantityCell();
            }
        
        });
        
        TableColumn increaseQuantityColumn = new TableColumn<>("");
        menuItemsTable.getColumns().add(5, increaseQuantityColumn);
        
        increaseQuantityColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Record, Boolean>, 
                ObservableValue<Boolean>>() {

            @Override
            public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<Record, Boolean> p) {
                return new SimpleBooleanProperty(p.getValue() != null);
            }
        });

        //Adding the Button to the cell
        increaseQuantityColumn.setCellFactory(
                new Callback<TableColumn<Record, Boolean>, TableCell<Record, Boolean>>() {

            @Override
            public TableCell<Record, Boolean> call(TableColumn<Record, Boolean> p) {
                return new IncreaseQuantityCell();
            }
        
        });
	}
	
	
	private class ReduceQuantityCell extends TableCell<Record, Boolean>{
		final Button button = new Button("-");
	    
		public ReduceQuantityCell(){
	    	//Action when the button is pressed
	        button.setOnAction(new EventHandler<ActionEvent>(){

	            @Override
	            public void handle(ActionEvent t) {
	                // get Selected Item
	            	MenuItem currentPerson = (MenuItem) ReduceQuantityCell.this.getTableView().getItems().get(ReduceQuantityCell.this.getIndex());
	            	//remove selected item from the table list
	            	if(currentPerson.getItemQuantity()>0)
	            		currentPerson.setItemQuantity(currentPerson.getItemQuantity()-1);
	            	
	            	currentPerson.setTotalPrice(currentPerson.getItemQuantity()*currentPerson.getItemPrice());
	            	
	            	updateTotal();
	            }
	        });
	    }

	    //Display button if the row is not empty
	    @Override
	    protected void updateItem(Boolean t, boolean empty) {
	        super.updateItem(t, empty);
	        if(!empty){
	            setGraphic(button);
	        }
	    }

	}
	
	private class IncreaseQuantityCell extends TableCell<Record, Boolean>{
		final Button button = new Button("+");
	    
		public IncreaseQuantityCell(){
	    	//Action when the button is pressed
	        button.setOnAction(new EventHandler<ActionEvent>(){

	            @Override
	            public void handle(ActionEvent t) {
	                // get Selected Item
	            	MenuItem currentPerson = (MenuItem) IncreaseQuantityCell.this.getTableView().getItems().get(IncreaseQuantityCell.this.getIndex());
	            	//remove selected item from the table list
	            	currentPerson.setItemQuantity(currentPerson.getItemQuantity()+1);
	            	
	            	currentPerson.setTotalPrice(currentPerson.getItemQuantity()*currentPerson.getItemPrice());
	            	
	            	updateTotal();
	            }
	        });
	    }

	    //Display button if the row is not empty
	    @Override
	    protected void updateItem(Boolean t, boolean empty) {
	        super.updateItem(t, empty);
	        if(!empty){
	            setGraphic(button);
	        }
	    }

	}

}
