package application.model;

public class Order {
	
	private int transactionId;
	private int orderId;
	private int itemId;
	private int numItem;
	private double itemPrice;
	
	public Order(int tid, int oid, int iid, int nI, double iP) {
		setTransactionId(tid);
		setOrderId(oid);
		setItemId(iid);
		setNumItem(nI);
		setItemPrice(iP);
	}
	
	private Order(int itemId, int numItem, double itemPrice) {
		this.transactionId = -1;
		this.orderId = -1;
		this.itemId = itemId;
		this.numItem = numItem;
		this.itemPrice = itemPrice;
	}
	
	public static Order createOrder(int itemId, int numItem, double itemPrice) {
		return new Order(itemId, numItem, itemPrice);
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}


	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getNumItem() {
		return numItem;
	}

	public void setNumItem(int numItem) {
		this.numItem = numItem;
	}

	public double getItemPrice() {
		return itemPrice;
	}

	public void setItemPrice(double itemPrice) {
		this.itemPrice = itemPrice;
	}

	public int getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(int transactionId) {
		this.transactionId = transactionId;
	}
	
	

}
