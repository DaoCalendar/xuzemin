package com.Application.Object;

import java.sql.Timestamp;

public class Person {
	private int userId;
	private String userName;
	private String userPassword;
	private int userType;
	private Timestamp lastlogin;
	private String macAddress;
	private String userUseName;
	public String getUserUseName() {
		return userUseName;
	}
	public void setUserUseName(String userUseName) {
		this.userUseName = userUseName;
	}
	public Timestamp getLastlogin() {
		return lastlogin;
	}
	public void setLastlogin(Timestamp lastlogin) {
		this.lastlogin = lastlogin;
	}
	public String getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	public int getUserType() {
		return userType;
	}
	public void setUserType(int userType) {
		this.userType = userType;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserPassword() {
		return userPassword;
	}
	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

}
