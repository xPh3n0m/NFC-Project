package kw.nfc.communication;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import application.model.Guest;
import application.model.MenuItem;
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

	public Guest newGuest(Guest g) throws SQLException {
		String query;
		PreparedStatement psm;
		if(g.isAnonymous()) {
			query = "INSERT INTO guest (anonymous) VALUES (?) RETURNING gid ";
			psm = conn.prepareStatement(query);
			psm.setBoolean(1, g.isAnonymous());
		} else {
			query = "INSERT INTO guest (first_name, last_name, email, anonymous) VALUES (?, ?, ?, ?) RETURNING gid";
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

        g.setGid(gid);
        return g;
	}
	
	public Guest getGuest(int gid) {
		String query = "SELECT * FROM guest WHERE gid = ?;";
		PreparedStatement psm;
		try {
			psm = conn.prepareStatement(query);
			psm.setInt(1, gid);
			ResultSet res = psm.executeQuery();
			
			String firstName = "";
			String lastName = "";
			String email = "";
			boolean anonymous = false;
			
	        if(res.next()) {
	        	anonymous = res.getBoolean("anonymous");
	        	if(!anonymous) {
		        	firstName = res.getString("first_name");
		        	lastName = res.getString("last_name");
		        	email = res.getString("email");
		        	return new Guest(gid, firstName, lastName, email);
	        	} else {
	        		return new Guest(gid);
	        	}
	        } else {
	        	return null;
	        }
		} catch (SQLException e) {
			return null;
		}
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

	public void updateGuest(Guest g) throws SQLException {
		String query = "UPDATE guest SET first_name = ?, last_name = ?, email = ?, anonymous = ? WHERE gid = ?;";
		PreparedStatement psm = conn.prepareStatement(query);

		if(g.isAnonymous()) {
			psm.setString(1, "");
			psm.setString(2, "");
			psm.setString(3, "");
			psm.setBoolean(4, g.isAnonymous());
			psm.setInt(5, g.getGid());
		} else {
			psm.setString(1, g.getFirstName());
			psm.setString(2, g.getLastName());
			psm.setString(3, g.getEmail());
			psm.setBoolean(4, g.isAnonymous());
			psm.setInt(5, g.getGid());
		}
		
		psm.executeUpdate();		
	}

	public int newTransaction(int wid, int gid, double balance, double amount, int gpid) throws SQLException {
		String query = "INSERT INTO transaction (wid, gid, amount, prev_balance, new_balance, gpid) VALUES (?, ?, ?, ?, ?, ?) RETURNING tid;";
		PreparedStatement psm = conn.prepareStatement(query);
		psm.setInt(1, wid);
		psm.setInt(2, gid);
		psm.setDouble(3, amount);
		psm.setDouble(4, balance);
		psm.setDouble(5, balance - amount);
		psm.setInt(6,  gpid);
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
        if(res.next()) {
        	oid = res.getInt("oid");
        }
        
        o.setOrderId(oid);
        return oid;
	}

	public NFCWristband newWristband(NFCWristband wristband) throws SQLException {
		String query = "INSERT INTO wristband (uid, balance, gid, status) VALUES (?, ?, ?, ?) RETURNING wid;";
		PreparedStatement psm = conn.prepareStatement(query);
		psm.setBytes(1, wristband.getUid().getBytes());
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

	public NFCWristband getNFCWristband(byte[] uid) throws SQLException {
		// TODO Auto-generated method stub
			String query = "SELECT * FROM wristband WHERE uid = ?;";
			PreparedStatement psm = conn.prepareStatement(query);
			psm.setBytes(1, uid);
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
	        	
		        NFCWristband wristband = NFCWristband.nfcWristbandFromDB(wid, gid, status, balance, uid);
		        return wristband;
	        }
	        
	        return null;
	}

	public Guest activateWristband(Guest g, NFCWristband wristband) throws SQLException {
		Guest guest = newGuest(g);
		
		String query = "UPDATE wristband SET status = ?, gid = ? WHERE wid = ?;";
		
		PreparedStatement psm = conn.prepareStatement(query);
		psm.setString(1, "A");
		psm.setInt(2, guest.getGid());
		psm.setInt(3, wristband.getWid());
		psm.executeUpdate();
		
		return guest;
	}

	public void deactivateWristband(NFCWristband wristband) throws SQLException {
		String query = "UPDATE wristband SET status = ?, gid = ? WHERE wid = ?;";
		
		PreparedStatement psm = conn.prepareStatement(query);
		psm.setString(1, "I");
		psm.setInt(2, -1);
		psm.setInt(3, wristband.getWid());
		psm.executeUpdate();
	}

	public void updateBalance(NFCWristband wristband, double newBalance) throws SQLException {
		String query = "UPDATE wristband SET balance = ? WHERE wid = ?;";
		
		PreparedStatement psm = conn.prepareStatement(query);
		psm.setDouble(1, newBalance);
		psm.setInt(2, wristband.getWid());
		psm.executeUpdate();
	}

	public ArrayList<MenuItem> getMenuItems(int groupNumber) throws SQLException {
		String query = "SELECT mu.iid, mu.name, mu.price, mu.description "
				+ "FROM menu_items mu, group_items gi "
				+ "WHERE ? = gi.gpid AND mu.iid = gi.iid AND mu.iid > 1";
		PreparedStatement psm;
		ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
	
		psm = conn.prepareStatement(query);
		psm.setInt(1, groupNumber);
		ResultSet res = psm.executeQuery();

		
        while(res.next()) {
        	int iid = res.getInt("iid");
        	String name = res.getString("name");
        	String description = res.getString("description");
        	double price = res.getDouble("price");
        	
        	MenuItem mi = new MenuItem(iid, name, description, price);
        	menuItems.add(mi);
        }

        return menuItems;
	}
}
