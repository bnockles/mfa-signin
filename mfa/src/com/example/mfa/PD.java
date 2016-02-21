package com.example.mfa;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.vaadin.data.util.BeanItemContainer;

public class PD implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5836136225145243870L;
	String title;
	int workshopNumber;
	BeanItemContainer<AttendanceRecord> attendeeRecords = new BeanItemContainer<AttendanceRecord>(AttendanceRecord.class);
	Date date;
	
	/**
	 * constructor for single session PDs
	 * @param name Name of the PD
	 * @param records list of records
	 * @param date date and time of PD
	 */
	public PD(String name, BeanItemContainer<AttendanceRecord> records, Date date){
		title = name;
		this.attendeeRecords=records;
		this.date = date;
		workshopNumber = 1;
	}
	
	/**
	 * constructor for single multi-session PDs
	 * @param name
	 * @param number number of session (session 1, session 2, etc)
	 * @param records
	 * @param date
	 */
	public PD(String name, int number, BeanItemContainer<AttendanceRecord> records, Date date){
		title = name;
		this.attendeeRecords=records;
		this.date = date;
		workshopNumber = number;
	}

	public String getTitle() {
		return title;
	}
	
	public String toString(){
		return title;
	}
	
	public BeanItemContainer<AttendanceRecord> getAttendanceRecords(){
		return attendeeRecords;
	}
	
	public void addRecord(AttendanceRecord ar){
		attendeeRecords.addItem(ar);
	}
	
	public Date getDate() {
		return date;
	}
	
	public String getDateString() {
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy");
		return format.format(date);
	}
	
	public int getWorkshop(){
		return workshopNumber;
	}
	
	@Override
	public boolean equals(Object record2) {
		if(record2 instanceof PD && ((PD) record2).getTitle().equals(title) && ((PD) record2).getWorkshop() == workshopNumber)return true;
		else return false;
	}
}
