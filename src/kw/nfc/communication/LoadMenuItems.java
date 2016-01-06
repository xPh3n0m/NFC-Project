package kw.nfc.communication;

import java.sql.SQLException;
import java.util.ArrayList;

import application.model.MenuItem;
import application.model.NFCWristband;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class LoadMenuItems extends Service<ArrayList<MenuItem>> {

	private Task<ArrayList<MenuItem>> task;
	
	private ConnectDB connDB;
	private int groupNumber;
	
	public LoadMenuItems(ConnectDB connDB, int groupNumber) {
		this.connDB = connDB;
		this.groupNumber = groupNumber;
	}

	@Override
	protected Task<ArrayList<MenuItem>> createTask() {
		task = new Task<ArrayList<MenuItem>>() {
            @Override
            protected ArrayList<MenuItem> call() throws SQLException {
    			ArrayList<MenuItem> newMenuItems = connDB.getMenuItems(groupNumber);
    			
            	return newMenuItems;
            }
		};
		return task;
	}
}

