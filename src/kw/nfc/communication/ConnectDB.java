package kw.nfc.communication;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;

import application.model.Guest;
import application.model.NFCWristband;
import application.model.Order;


public class ConnectDB {

	private Connection conn;
	
	public ConnectDB() {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void connect() throws SQLException {
		if(Utility.ONLINE_MODE) {
			this.conn = DriverManager.getConnection(Utility.DB_ONLINE_URL, Utility.DB_ONLINE_USER, Utility.DB_ONLINE_PASSWORD);
		} else {
			this.conn = DriverManager.getConnection(Utility.DB_URL, Utility.DB_USER, Utility.DB_PASSWORD);
		}
	}
	
	public void reconnect() throws SQLException {
		if(!conn.isValid(500)) {
			if(Utility.ONLINE_MODE) {
				this.conn = DriverManager.getConnection(Utility.DB_ONLINE_URL, Utility.DB_ONLINE_USER, Utility.DB_ONLINE_PASSWORD);
			} else {
				this.conn = DriverManager.getConnection(Utility.DB_URL, Utility.DB_USER, Utility.DB_PASSWORD);
			}
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
	/*
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
	}*/

	public int newGuest(Guest g) throws SQLException {
		String query;
		PreparedStatement psm;
		if(g.isAnonymous()) {
			query = "INSERT INTO guest (anonymous) VALUES (?)";
			psm = conn.prepareStatement(query);
			psm.setBoolean(1, g.isAnonymous());
		} else {
			query = "INSERT INTO guest (first_name, last_name, email, anonymous) VALUES (?, ?, ?, ?)";
			psm = conn.prepareStatement(query);
			psm.setString(1, g.getFirstName());
			psm.setString(2, g.getLastName());
			psm.setString(3, g.getEmail());
			psm.setBoolean(4, g.isAnonymous());

		}
		ResultSet res = psm.executeQuery();
		
		int gid = -1;
        if(res.next()) {
        	gid = res.getInt("gid");
        }

        return gid;
	}
	/*
	public Guest getGuestFromDB(int gid) throws SQLException {
		String query = "SELECT * FROM guest WHERE gid = ?;";
		PreparedStatement psm = conn.prepareStatement(query);
		psm.setInt(1, gid);
		ResultSet res = psm.executeQuery();
		
		String name = "";
		double balance = 0.0;
		byte[] atr = null;
		
        if(res.next()) {
        	name = res.getString("guest_name");
        	balance = res.getDouble("balance");
        	atr = res.getBytes("ATR");
        } else {
        	return null;
        }
        

        Guest g = Guest.newGuestForUpdate(gid, name, balance,
        		new NFCWristband(new ATR(atr)), this);

        return g;
	}*/
	
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

	public void updateGuest(int gid, String name, double balance, NFCWristband card, String status) throws SQLException {
		String query = "UPDATE guest SET guest_name = ?, balance = ?, ATR = ?, status = ? WHERE gid = ?;";
		
		PreparedStatement psm = conn.prepareStatement(query);
		psm.setString(1, name);
		psm.setDouble(2, balance);
		psm.setString(3, card.getATR().toString());
		psm.setString(4, status);
		psm.setInt(5, gid);
		psm.executeUpdate();
	}

	public int newTransaction(int gid, double balance, double amount) throws SQLException {
		String query = "INSERT INTO transaction (gid, amount, prev_balance, new_balance) VALUES (?, ?, ?, ?) RETURNING tid;";
		PreparedStatement psm = conn.prepareStatement(query);
		psm.setInt(1, gid);
		psm.setDouble(2, amount);
		psm.setDouble(3, balance);
		psm.setDouble(4, balance - amount);
		ResultSet res = psm.executeQuery();
		
		int tid = -1;
        while(res.next()) {
        	tid = res.getInt("tid");
        }

        return tid;
	}

	public int addOrder(Order o) throws SQLException {
		String query = "INSERT INTO orders (tid, iid, num_item, item_price) VALUES (?, ?, ?, ?) RETURNING oid;";
		PreparedStatement psm = conn.prepareStatement(query);
		psm.setInt(1, o.getTransactionId());
		psm.setDouble(2, o.getItemId());
		psm.setDouble(3, o.getNumItem());
		psm.setDouble(4, o.getItemPrice());
		ResultSet res = psm.executeQuery();
		
		int oid = -1;
        while(res.next()) {
        	oid = res.getInt("oid");
        }
        
        o.setOrderId(oid);
        return oid;
	}

	public NFCWristband newWristband(NFCWristband wristband) throws SQLException {
		String query = "INSERT INTO wristband (atr, balance, gid, status) VALUES (?, ?, ?, ?) RETURNING wid;";
		PreparedStatement psm = conn.prepareStatement(query);
		psm.setBytes(1, wristband.getATR().getBytes());
		psm.setDouble(2, Utility.INITIAL_BALANCE);
		psm.setInt(3, -1);
		psm.setString(4, String.valueOf('I'));
		ResultSet res = psm.executeQuery();
		
		int wid = -1;
        if(res.next()) {
        	wid = res.getInt("wid");
        	wristband.setWid(wid);
        	wristband.setGid(-1);
        	wristband.setStatus('I');
        	wristband.setBalance(Utility.INITIAL_BALANCE);
        	return wristband;
        }

        //TODO: Throw an Exception
        return null;
	}

	public void unregisterWristband(NFCWristband wristband) throws SQLException {
		// This will be used to de-activate the wristband
		//String query = "UPDATE wristband SET status = 'I' WHERE wid = ?;";
		
		String query = "DELETE FROM wristband WHERE wid = ?;";
		PreparedStatement psm = conn.prepareStatement(query);
		psm.setInt(1, wristband.getWid());
		psm.executeUpdate();
		
		//TODO: What to do if an SQL Exception is thrown?
	}

	public NFCWristband getNFCWristband(ATR atr) throws SQLException {
		// TODO Auto-generated method stub
			String query = "SELECT * FROM wristband WHERE atr = ?;";
			PreparedStatement psm = conn.prepareStatement(query);
			psm.setBytes(1, atr.getBytes());
			ResultSet res = psm.executeQuery();
			
			int wid = -1;
			int gid = -1;
			char status = 'I';
			double balance = Utility.INITIAL_BALANCE;
			
	        if(res.next()) {
	        	wid = res.getInt("wid");
	        	gid = res.getInt("gid");
	        	status = res.getString("status").charAt(0);
	        	balance = res.getDouble("balance");
	        	
		        NFCWristband wristband = NFCWristband.nfcWristbandFromDB(wid, gid, status, balance, atr);
		        return wristband;
	        }
	        
	        return null;
	}

	public void activateWristband(NFCWristband wristband) throws SQLException {
		String query = "UPDATE wristband SET status = ? WHERE wid = ?;";
		
		PreparedStatement psm = conn.prepareStatement(query);
		psm.setString(1, "A");
		psm.setInt(2, wristband.getWid());
		psm.executeUpdate();
	}

	public void deactivateWristband(NFCWristband wristband) throws SQLException {
		String query = "UPDATE wristband SET status = ? WHERE wid = ?;";
		
		PreparedStatement psm = conn.prepareStatement(query);
		psm.setString(1, "I");
		psm.setInt(2, wristband.getWid());
		psm.executeUpdate();
	}

}
