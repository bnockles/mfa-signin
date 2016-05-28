package com.example.mfa;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

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
	String location;
	
	
	/**
	 * constructor for single multi-session PDs
	 * @param name
	 * @param number number of session (session 1, session 2, etc)
	 * @param records
	 * @param date
	 */
	public PD(String name, int number, BeanItemContainer<AttendanceRecord> records, Date date, String location){
		title = name;
		this.attendeeRecords=records;
		this.date = date;
		workshopNumber = number;
		this.location = location;
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

	public String getLocation() {
		return location;
	}

	public void cancel() {
		for (Iterator<AttendanceRecord> i = attendeeRecords.getItemIds().iterator(); i.hasNext();) {
		    // Get the current item identifier, which is an integer.
			AttendanceRecord iid = (AttendanceRecord) i.next();
		    
		    iid.setStatus(AttendanceRecord.CANCELLED);
	
		}
	}
}
