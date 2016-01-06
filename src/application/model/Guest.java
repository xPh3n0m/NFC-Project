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
	private String firstName;
	private String lastName;
	private String email;
	private boolean anonymous;
	
	/**
	 * Default constructor, used to create a non anonymous guest
	 * @param gid
	 * @param name
	 * @param balance
	 * @param card
	 * @param status
	 * @param connDB
	 */
	public Guest(int gid, String firstName, String lastName, String email) {
		this.setGid(gid);
		this.setFirstName(firstName);
		this.setLastName(lastName);
		this.setEmail(email);
		setAnonymous(false);
	}

	/**
	 * Constructor for an anonymous guest
	 * @param gid
	 */
	public Guest(int gid) {
		this.setGid(gid);
		this.setFirstName("");
		this.setLastName("");
		this.setEmail("");
		this.setAnonymous(true);
	}

	public int getGid() {
		return gid;
	}

	public void setGid(int gid) {
		this.gid = gid;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isAnonymous() {
		return anonymous;
	}

	public void setAnonymous(boolean anonymous) {
		this.anonymous = anonymous;
	}

}
