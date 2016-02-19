package com.example.mfa;


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogEntry implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4690838271263352606L;
	Date date;
	Teacher teacher;
	PD pd;
	SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy");
	
	public LogEntry(Date d, Teacher t, PD p){
		date = d;
		this.teacher = t;
		pd = p;
	}

	public String getDate() {
		return format.format(date);
	}

	public Teacher getTeacher() {
		return teacher;
	}
	
	public String getLastName() {
		return teacher.getLastName();
	}
	
	public String getFirstName() {
		return teacher.getFirstName();
	}

	public String getID() {
		return ""+teacher.getIdentifier();
	}

	public String getCohort() {
		return format.format(teacher.getCohort());
	}
	
	public PD getPD() {
		return pd;
	}

	public boolean matches(Teacher teacher2) {
		if(teacher2.getIdentifier()==teacher.getIdentifier())return true;
		else return false;
	}
	
	
}
