package kw.nfc.communication;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class ConnectDB {

	private Connection conn;
	
	public ConnectDB() {
		
		
	}
	
	public void connect() {
		try {
			this.conn = DriverManager.getConnection(Utility.DB_ONLINE_URL, Utility.DB_USER, Utility.DB_PASSWORD);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateCloackId(int gid, int cid) {
		Statement state;
		try {
			state = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			String query = ("SELECT * FROM guest WHERE gid=" + gid);
	        ResultSet res = state.executeQuery(query);
	        res.first();
	        
	        res.updateInt("cid", cid);
	        res.updateRow();
	        
	        res.close();
	        state.close();
	        
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public Guest getGuestInfo(int gid) {
        Statement state;
		try {
			state = conn.createStatement();
			String query = "SELECT guest_name, tid, cid FROM guest\n";
	        query += ("WHERE gid=" + gid);
	        ResultSet res = state.executeQuery(query);
	        
	        res.next();
	        
	        String name = res.getString("guest_name");
	        int tid = res.getInt("tid");
	        int cid = res.getInt("cid");

	        Guest g = new Guest(gid, name, tid, cid);
	        
	        res.close();
	        state.close();
	        
	        return g;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
	}
	
	public void newOrder(int gid, int nbBeers, int nbSpirits) {
		try {
			String query = "INSERT INTO orders (gid, nbbeers, nbspirits) VALUES (?, ?, ?)";
			PreparedStatement psm = conn.prepareStatement(query);
			psm.setInt(1, gid);
			psm.setInt(2, nbBeers);
			psm.setInt(3, nbSpirits);
			
			psm.executeUpdate();
			
			
	        System.out.println("Order added");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Order guestOrderStatus(int gid) {
		Statement state;
		try {
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
	        
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
	}
}
