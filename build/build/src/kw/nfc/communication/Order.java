package kw.nfc.communication;

public class Order {
	
	private int orderId;
	private int guestId;
	private int nbBeers;
	private int nbSpirits;
	
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
