package application.model;

import kw.nfc.communication.Utility;

public class Order {
	
	private int orderId;
	private int guestId;
	private int nbBeers;
	private int nbSpirits;
	private double totalPrice;
	
	public Order(int oid, int gid, int nB, int nS) {
		orderId = oid;
		guestId = gid;
		nbBeers = nB;
		nbSpirits = nS;
	}
	
	public Order(int gid, int nB, int nS) {
		orderId = Utility.currentOrder++;
		guestId = gid;
		nbBeers = nB;
		nbSpirits = nS;	
	}

	public String toString() {
		return new String("Order " + orderId +" for guest " + guestId + ": " + nbBeers + " beers & " + nbSpirits + " spirits");
	}

}
