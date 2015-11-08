package kw.nfc.communication;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import application.model.Guest;
import application.model.Order;


public class ConnectDB {

	private Connection conn;
	
	public ConnectDB() {
	}
	
	public void connect() throws SQLException {
			this.conn = DriverManager.getConnection(Utility.DB_URL, Utility.DB_USER, Utility.DB_PASSWORD);
	}
	
	public void reconnect() throws SQLException {
		if(!conn.isValid(500)) {
			conn = DriverManager.getConnection(Utility.DB_URL, Utility.DB_USER, Utility.DB_PASSWORD);
		}
	}
	
	public void updateCloackId(int gid, int cid) throws SQLException {
		Statement state;
		state = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		String query = ("SELECT * FROM guest WHERE gid=" + gid);
        ResultSet res = state.executeQuery(query);
        res.first();
        
        res.updateInt("cid", cid);
        res.updateRow();
        
        res.close();
        state.close();
	}
	/*
	public Guest getGuestInfo(int gid) throws SQLException {
        Statement state;
        
		state = conn.createStatement();
		String query = "SELECT guest_name FROM guest\n";
        query += ("WHERE gid=" + gid);
        ResultSet res = state.executeQuery(query);
        
        res.next();
        
        String name = res.getString("guest_name");

        Guest g = new Guest(gid, name);
        
        res.close();
        state.close();
        
        return g;
	}*/
	
	public void newOrder(int gid, int nbBeers, int nbSpirits) throws SQLException {
		String query = "INSERT INTO orders (gid, nbbeers, nbspirits) VALUES (?, ?, ?)";
		PreparedStatement psm = conn.prepareStatement(query);
		psm.setInt(1, gid);
		psm.setInt(2, nbBeers);
		psm.setInt(3, nbSpirits);
		
		psm.executeUpdate();
		
		
        System.out.println("Order added");
	}
	
	public Order guestOrderStatus(int gid) throws SQLException {
		Statement state;
		state = conn.createStatement();
		String query = "SELECT nbbeers, nbspirits FROM orders\n";
        query += ("WHERE gid=" + gid);
        ResultSet res = state.executeQuery(query);
        
        int nBbeers = 0;
        int nbSpirits = 0;
        
        while(res.next()) {
        	nBbeers += res.getInt("nbbeers");
        	nbSpirits += res.getInt("nbspirits");
        }
        res.close();
        state.close();
        
        return new Order(gid, nBbeers, nbSpirits);
	}

	public int newGuest(String name) throws SQLException {
		String query = "INSERT INTO guest (guest_name, balance) VALUES (?, ?) RETURNING gid;";
		PreparedStatement psm = conn.prepareStatement(query);
		psm.setString(1, name);
		psm.setDouble(2, Utility.INITIAL_BALANCE);
		ResultSet res = psm.executeQuery();
		
		int gid = -1;
        while(res.next()) {
        	gid = res.getInt("gid");
        }

        return gid;
	}
	
	public Guest getGuestFromDB(int gid) throws SQLException {
		String query = "SELECT * FROM guest WHERE gid = ?;";
		PreparedStatement psm = conn.prepareStatement(query);
		psm.setInt(1, gid);
		ResultSet res = psm.executeQuery();
		
		String name = "";
		double balance = 0.0;
        while(res.next()) {
        	name = res.getString("guest_name");
        	balance = res.getDouble("balance");
        }
        
        Guest g = new Guest(gid, name, balance, this);

        return g;
	}
	
	/*
	public void setGuestBalance(int gid, double newBalance) {
		String query = "INSERT INTO guest (gid, guest_name, balance) VALUES (?, ?, ?)";
		PreparedStatement psm = conn.prepareStatement(query);
		psm.setInt(1, gid);
		psm.setString(2, name);
		psm.setDouble(3, Utility.INITIAL_BALANCE);
		psm.executeUpdate();
		
	}*/

	public void setGuestBalance(int gid, double newBalance) throws SQLException {
		String query = "UPDATE guest SET balance = ? WHERE gid = ?;";
		
		PreparedStatement psm = conn.prepareStatement(query);
		psm.setDouble(1, newBalance);
		psm.setInt(2, gid);
		psm.executeUpdate();
		
	}

	public double getCurrentBalance(int gid) throws SQLException {
		// TODO Auto-generated method stub
		String query = "SELECT balance FROM guest WHERE gid = ?;";
		
		PreparedStatement psm = conn.prepareStatement(query);
		psm.setInt(1, gid);
		ResultSet res = psm.executeQuery();
		
		double currentBalance = -1.0;
        while(res.next()) {
        	currentBalance = res.getDouble("balance");
        }
		return currentBalance;
	}

	public void updateGuest(int gid, String name, double balance) throws SQLException {
		String query = "UPDATE guest SET guest_name = ?, balance = ? WHERE gid = ?;";
		
		PreparedStatement psm = conn.prepareStatement(query);
		psm.setString(1, name);
		psm.setDouble(2, gid);
		psm.setInt(3, gid);
		psm.executeUpdate();
	}

}
