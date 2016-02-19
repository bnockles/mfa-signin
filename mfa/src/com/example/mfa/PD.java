package com.example.mfa;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PD implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5836136225145243870L;
	String title;
	ArrayList<Teacher> attendees;
	Date date;
	static SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy");
	
	public PD(String name, ArrayList<Teacher> attendees, Date date){
		title = name;
		this.attendees=attendees;
		this.date = date;
	}

	public String getTitle() {
		return title;
	}
	
	public String toString(){
		return title;
	}
	
	public ArrayList<Teacher> getAttendees(){
		return attendees;
	}
	
	public Date getDate() {
		return date;
	}
	
	public String getDateString() {
		return format.format(date);
	}
	
}
