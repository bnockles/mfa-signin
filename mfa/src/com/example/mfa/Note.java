package com.example.mfa;

import java.io.Serializable;
import java.util.Date;

public class Note implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2658002697649182852L;
	String text;
	Date date;
	
	public Note(String message, Date date){
		this.text = message;
		this.date = date;
	}
	
	public String getMessage(){
		return text;
	}
	
	public Date getDate(){
		return date;
	}
	
}
