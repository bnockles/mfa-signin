package com.example.mfa;


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AttendanceRecord implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4690838271263352606L;
	Date date;
	Teacher teacher;
	PD pd;
	String teacherName;
	String identifier;
	String status;
	
	Date time;
	
	public static final String ATTENDED= "Attended";
	public static final String ABSENT= "Absent";
	public static final String EXCUSED= "Excused";
	public static final String CANCELLED= "Cancelled";
	
	
	public AttendanceRecord(Teacher t, PD p, String id, String status, Date time){
		this.teacher = t;
		teacherName=status;
				//t.getFirstName()+" "+t.getLastName();
		pd = p;
		identifier = id;
		this.status=status;
		this.time = time;
	}

	public String getDate() {
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy");
		return format.format(pd.getDate());
	}

	public Teacher getTeacher() {
		return teacher;
	}
	
	public String getID() {
		return identifier;
	}
	
	public String getStatus() {
		return status;
	}
	
	public String getLastName() {
		return teacher.getLastName();
	}
	
	public String getFirstName() {
		return teacher.getFirstName();
	}

	public String getCohort() {
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy");
		return format.format(teacher.getCohort());
	}
	
	public PD getPD() {
		return pd;
	}
	
	public Date getTime() {
		return time;
	}

	@Override
	public boolean equals(Object record2) {
		if(record2 instanceof AttendanceRecord && ((AttendanceRecord) record2).getID().equals(identifier))return true;
		else return false;
	}

	public void setStatus(String status) {
		this.status = status;
		
	}
	
	
}
