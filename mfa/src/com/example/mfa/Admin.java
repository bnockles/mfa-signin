package com.example.mfa;

import java.io.Serializable;

public class Admin implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3194714696221411193L;
	private String name;
	private String password;
	
	public Admin(String name, String password){
		this.name = name;
		this.password = password;
	}
	
	public String getName(){
		return name;
	}
	
	public boolean isPassword(String check){
		if(check.equals(password))return true;
		return false;
	}
	
}
