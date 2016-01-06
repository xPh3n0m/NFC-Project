package application.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MenuItem {
	
	private int iid;
	private final StringProperty itemName;
	private final StringProperty itemDescription;
	private final DoubleProperty itemPrice;
	private final IntegerProperty itemQuantity;
	private final DoubleProperty totalPrice;
	
	public MenuItem(int iid, String itemName, String itemDescription, double itemPrice) {
		this.iid = iid;
		this.itemName = new SimpleStringProperty(itemName);
		this.itemDescription = new SimpleStringProperty(itemDescription);
		this.itemPrice = new SimpleDoubleProperty(itemPrice);
		this.itemQuantity = new SimpleIntegerProperty(0);
		this.totalPrice = new SimpleDoubleProperty(0.0);
	}
	
	public StringProperty getItemNameProperty() {
		return itemName;
	}
	
	public StringProperty getItemDescriptionProperty() {
		return itemDescription;
	}
	
	public DoubleProperty getItemPriceProperty() {
		return itemPrice;
	}
	
	public IntegerProperty getItemQuantityProperty() {
		return itemQuantity;
	}
	
	public DoubleProperty getTotalPriceProperty() {
		return totalPrice;
	}
	
	public int getIid() {
		return iid;
	}

	public String getItemName() {
		return itemName.get();
	}

	public String getItemDescription() {
		return itemDescription.get();
	}

	public double getItemPrice() {
		return itemPrice.get();
	}
	
	public int getItemQuantity() {
		return itemQuantity.get();
	}
	
	public double getTotalPrice() {
		return totalPrice.get();
	}
	
	public void setItemName(String name) {
		itemName.set(name);
	}

	public void setItemDescription(String description) {
		itemDescription.set(description);
	}

	public void setItemPrice(double price) {
		itemPrice.set(price);
	}
	
	public void setItemQuantity(int quantity) {
		itemQuantity.set(quantity);
	}
	
	public void setTotalPrice(double price) {
		totalPrice.set(price);
	}

}
