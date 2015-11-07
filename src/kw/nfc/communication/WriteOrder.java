package kw.nfc.communication;

import java.sql.SQLException;

import javax.smartcardio.CardException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class WriteOrder extends Service<String>{
	
	private int noBeers;
	private int noSpirits;
	private _NFCCommunication nfc;
	private ConnectDB connDB;
	private Task<String> task;
	
	public WriteOrder() {
		this.nfc = new _NFCCommunication();
		if(nfc.getStatus() != _NFCCommunication.CONNECTED) {
			nfc.connectToDefaultTerminal();
		}
		
	}

	@Override
	protected Task<String> createTask() {
		task = new Task<String>() {
            @Override
            protected String call() throws CardException, SQLException, ParseException {
            	setNewOrder();
            	return "Order confirmed";
            }
		};
		return task;
	}
	
	public void setNewOrder() throws SQLException, ParseException, CardException {
		String data;
		data = nfc.readData();

		JSONParser jparser = new JSONParser();
		JSONObject jsonData;

		System.out.println(data);
		jsonData = (JSONObject) jparser.parse(data);
		Long gid = (long) jsonData.get("gid");
		
		connDB.newOrder(gid.intValue(), noBeers, noSpirits);
	}


	public void setOrder(int nB, int nS) {
		noBeers = nB;
		noSpirits = nS;
	}
	
	public void stopTask() {
		nfc.cancel();
		task.cancel();
	}
	
	public void setConnDB(ConnectDB connDB) {
		this.connDB = connDB;
	}

}
