package application;

import java.sql.SQLException;
import java.util.Scanner;

import javax.smartcardio.CardException;

import org.json.simple.parser.ParseException;

import application.model.Guest;
import kw.nfc.communication.ConnectDB;
import kw.nfc.communication._NFCCommunication;
import kw.nfc.communication.Utility;

public class SimpleApp {
	
	public static ConnectDB connDB;

	public static void main(String[] args) {
		connDB = new ConnectDB();
		try {
			connDB.connect();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("What would you like to do?");
		
		System.out.println("1. Add/Remove a new guest");
		System.out.println("2. Add/Redeem cash for a guest");
		System.out.println("3. Bar application");
		
		int choice = (new Scanner(System.in)).nextInt();
		
		switch(choice) {
		case 1:
			guestApp();
			break;
		case 2:
			cashApp();
			break;
		case 3:
			barApp();
			break;
		default:
			System.out.println("Unvalid entry. Please try again");
		}
	}
	
	public static void guestApp() {
		System.out.println();
		System.out.println("1. Add a new guest");
		System.out.println("2. Get information on a wristband");
		
		int choice = (new Scanner(System.in)).nextInt();
		
		switch(choice) {
		case 1:
			System.out.println("Guest name: ");
			String name = (new Scanner(System.in)).nextLine();
			Guest g = null;
			try {
				g = Guest.newGuestInDatabase(name, connDB);
			} catch (SQLException e) {
				System.out.println("Database problem...");
				e.printStackTrace();
				System.exit(0);
			}
			
			System.out.println("Guest created in the database.");
		
			// Write info to wristband
			writeGuestToWristband(g);
			break;
		case 2:
			System.out.println("Please place a wristband on the terminal...");
			readDataFromWristband();
			
			break;
		default:
			System.out.println("Wrong entry");
			break;
		}
	}
	
	public static void cashApp() {
		System.out.println();
		System.out.println("1. Add cash for a guest");
		System.out.println("2. Refund a guest");
		System.out.println("3. View cash balance");
		
		int choice = (new Scanner(System.in)).nextInt();
		
		switch(choice) {
		case 1:
			System.out.println("How much would you like to charge?");
			double amount = (new Scanner(System.in)).nextDouble();
			Guest g = readDataFromWristband();
			
			double newBalance = g.recharge(amount, connDB);
			
			writeGuestToWristband(g);
			System.out.println("Succesfully charged " + amount + " on the wristband. New balance: " + newBalance);
			break;
		case 2:
			Guest g1 = readDataFromWristband();
			
			double currentBalance = g1.getBalance();
			System.out.println("Please refund " + currentBalance + " to the guest.");
			
			try {
				g1.setBalanceOnline(Utility.INITIAL_BALANCE, connDB);
				writeGuestToWristband(g1);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			break;
		case 3:
			readDataFromWristband();
			break;
		}
	}
	
	public static void barApp() {
		double price = 0.0;
		while(true) {
			System.out.println();
			System.out.println("Which drink would you like to buy?");
			System.out.println("1. Beer (5 CHF)");
			System.out.println("2. Martini (7 CHF)");
			System.out.println("3. Vodka (10 CHF)");
			System.out.println("4. Whiskey (10 CHF)");
			System.out.println("5. Nothing");
			
			int drink = (new Scanner(System.in)).nextInt();
			
			if(drink == 5) break;
	
			System.out.println("How many?");
			int quantity = (new Scanner(System.in)).nextInt();
			
			switch(drink) {
			case 1:
				price += 5.0 * quantity;
				break;
			case 2:
				price += 7.0 * quantity;
				break;
			case 3:
				price += 10.0 * quantity;
				break;
			case 4:
				price += 10.0 * quantity;
				break;
			}
			
			System.out.println("Something else? (y/n)");
			String cont = (new Scanner(System.in)).nextLine();
				
			if(cont.equals("n")) break;
		}
		
		System.out.println("Order price: " + price);
		
		Guest g = readDataFromWristband();
		
		try {
			if(g.pay(price, connDB)) {
				writeGuestToWristband(g);
				System.out.println("New balance: " +g.getBalance());
			} else {
				System.out.println("Unsufficient balance: " + g.getBalance());
			}
			
			;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private static void writeDataToWristband(String data) {
		_NFCCommunication reader = new _NFCCommunication();
		reader.connectToDefaultTerminal();
		try {
			reader.writeData(data);
		} catch (CardException e) {
			System.out.println("Failed to write on wristband");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Succesufully wrote on wristband");
	}
	
	private static Guest readDataFromWristband() {
		System.out.println("Please place a wristband on the terminal...");
		_NFCCommunication reader = new _NFCCommunication();
		reader.connectToDefaultTerminal();
		
		Guest g = null;
		
		String data = "";
		try {
			data = reader.readData();
			g = Guest.newGuestFromJSONString(data, connDB);
			System.out.println(g.toString());
		} catch (CardException e) {
			System.out.println("Failed to read from wristband");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Succesufully read from wristband");
		return g;
	}
	
	private static void writeGuestToWristband(Guest g) {
		System.out.println("Please replace wristband on reader...");
		String userData = g.getJSONString().toJSONString();
		writeDataToWristband(userData);
	}

}
