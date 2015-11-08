package application.model;
import java.sql.SQLException;
import java.util.Map;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import kw.nfc.communication.ConnectDB;
import kw.nfc.communication.Utility;

public class Guest {
	
	private int gid;
	private String name;
	private double balance;
	private ConnectDB connDB;
	
	private Guest(String name) {
		this.setName(name);
		this.setBalance(Utility.INITIAL_BALANCE);
	}
	
	private Guest(int gid, String name) {
		this.setGid(gid);
		this.setName(name);
		this.setBalance(Utility.INITIAL_BALANCE);
	}

	public Guest(int gid, String name, double balance, ConnectDB connDB) {
		this.setGid(gid);
		this.setName(name);
		this.setBalance(balance);
		this.connDB = connDB;
	}
	
	private Guest(int gid, String name, double balance) throws SQLException {
		this.setGid(gid);
		this.setName(name);
		this.setBalance(balance);
		this.connDB = new ConnectDB();
		connDB.connect();
	}
	
	public boolean equals(Guest g) {
		if(g == null) {
			return false;
		}
		if((!g.getName().equals(name)) || (g.getBalance() != balance)) {
			return false;
		}
		return true;
	}
	
	private void setBalance(double b) {
		this.balance = b;
		
	}

	public JSONObject getJSONString() {
		JSONObject j = new JSONObject();
		j.put("gid", gid);
		j.put("guest_name", name);
		j.put("balance", balance);

		return j;
	}

	public int getGid() {
		return gid;
	}

	public void setGid(int gid) {
		this.gid = gid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public double getBalance() {
		return balance;
	}

	public void setBalanceOnline(double b, ConnectDB connDB) throws SQLException {
		this.balance = b;
		connDB.setGuestBalance(gid, balance);
	}
	
	/**
	 * Process a payment
	 * Returns true and updates the balance if the balance is sufficient
	 * Returns false otherwise
	 * @param amount
	 * @return
	 * @throws SQLException 
	 */
	public boolean pay(double amount, ConnectDB connDB) throws SQLException {
		if(balance - amount < 0) {
			return false;
		} else {
			setBalanceOnline(balance - amount, connDB);
			return true;
		}
	}
	
	public double recharge(double amount, ConnectDB connDB) {
		double newBalance = amount + balance;
		try {
			double dbBalance = connDB.getCurrentBalance(gid);
			assert dbBalance == balance;
			
			setBalanceOnline(balance + amount, connDB);
			return newBalance;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return -1.0;
	}

	/**
	 * Creates a new guest in the database
	 * @param name
	 * @return
	 * @throws SQLException 
	 */
	public static Guest newGuest(String name, ConnectDB connDB) throws SQLException {
		int gid = connDB.newGuest(name);
		
		return new Guest(gid, name);
	}

	public static Guest newGuestFromJSONString(String jsonString) {
		JSONParser parser=new JSONParser();
		JSONObject guestJSON;
		try {
			System.out.println(jsonString);
			guestJSON = (JSONObject) parser.parse(jsonString);
		} catch (ParseException e) {
			return null;
		}
		
		if(guestJSON.containsKey("gid") && guestJSON.containsKey("guest_name") && guestJSON.containsKey("balance")) {
			long gid = (long) guestJSON.get("gid");		
			String name = (String) guestJSON.get("guest_name");
			double balance = (double) guestJSON.get("balance");
			try {
				return new Guest((int) gid, name, balance);
			} catch (SQLException e) {
				return null;
			}
		}
		return null;
	}
	
	public String toString() {
		String guestInfo = "";
		guestInfo += "Guest id: " + gid;
		guestInfo += "\nGuest name: " + name;
		guestInfo += "\nCurrent balance: " + balance;
		return guestInfo;
	}

	public void updateDatabase() throws SQLException {
		connDB.updateGuest(gid, name, balance);
	}

}
