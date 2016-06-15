package com.example.mfa;

/**
 * A class for storing read information off of an attendance file, customized to save space by only storing the data that can be changed
 * @author bnockles
 *
 */
public class AttendanceFileLine implements Comparable<AttendanceFileLine> {

	String ID;
	String attendance;
	String date;
	String confirmedAbsence;
	
	public AttendanceFileLine(String ID, String attendance, String date, String confirmedAbsence) {
		this.ID = ID;
		this.attendance = attendance;
		this.date = date;
		this.confirmedAbsence = confirmedAbsence;
	}
	

	public String getAttendance() {
		return attendance;
	}


	public String getDate() {
		return date;
	}


	public String isConfirmed(){
		return confirmedAbsence;
	}
	
	@Override
	public int compareTo(AttendanceFileLine o) {
		return this.ID.compareTo(o.getID());
	}

	public String getID() {

		return ID;
	}
	
	public boolean equals(Object afl){
		return afl instanceof AttendanceFileLine && this.ID.equals(((AttendanceFileLine)afl).getID());
	}

}
