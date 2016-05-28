package com.example.mfa;


import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;

public class AttendanceRecord implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4690838271263352606L;

	AttendanceFile file;
	Teacher teacher;
	PD pd;
	String teacherName;
	String identifier;
	String status;
	Date time;
	boolean confirmedAbsence;
	boolean late;

	public static final String ATTENDED= "Attended";
	public static final String ABSENT= "Absent";
	public static final String EXCUSED= "Excused";
	public static final String CANCELLED= "Cancelled";


	public AttendanceRecord(AttendanceFile file, Teacher t, PD p, String id, String status, Date time, boolean confirmed, boolean late){		
		this.file = file;
		this.teacher = t;
		teacherName=t.getFirstName()+" "+t.getLastName();
		pd = p;
		identifier = id;
		this.status=status;
		this.time = time;
		this.confirmedAbsence = confirmed;
		this.late = late;
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
		if(status.equals(""))status = ABSENT;
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

	public PD getPd() {
		return pd;
	}
	
	public int getWorkshop() {
		return pd.getWorkshop();
	}

	public Date getTime() {
		return time;
	}

	public boolean confirmedAbsence(){
		return confirmedAbsence;
	}
	
	public void setConfirmedAbsence(boolean b){
		confirmedAbsence = b;
		file.save(this.identifier);

	}
	
	public boolean wasLate(){
		return late;
	}
	
	public void setLate(boolean b){
		late = b;
	}
	
	/**
	 * 
	 * @return the time the teacher checked in, formatted to EST time zone, even though actual data is UTC
	 */
	public String getFormattedTime() {
		if(time != null){
			DateFormat df = new SimpleDateFormat(AttendanceFile.TIMESTAMP_FORMAT);
			return df.format(AttendanceFile.converUTCToEST(time));
		}
		else return "";
	}

	@Override
	public boolean equals(Object record2) {
		if(record2 instanceof AttendanceRecord && ((AttendanceRecord) record2).getID().equals(identifier))return true;
		else return false;
	}

	/**
	 * this method sets the attendance status when a person is being checked in, so a Date is not required, since in calls the current time
	 * @param status the status to be marked
	 */
	public void setStatus(String status) {
		this.status = status;
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, 0);
		if(this.status.equals(ATTENDED)){
			this.time=cal.getTime();
			late = MfAUI.isRightNowLate();
		}
		else {
			this.time = null;
			late = false;
		}
		
		setStatus(status, time);
	}

	/**
	 *  this method sets the attendance status when a file is being loaded, so a Date is required so that it pulls from the record
	 * @param status
	 * @param date
	 */
	public void setStatus(String status, Date date) {
		this.status = status;
		//		cal.add(Calendar.HOUR, -5);
		if(this.status.equals(ATTENDED))this.time=date;
		else this.time = null;
		
		if(!this.status.equals(ABSENT)){
			confirmedAbsence=true;
		}
		
		if(!file.save(this.identifier)){
			Notification saveError = new Notification("Saving Error","The attendance records have not been saved");
			saveError.setStyleName("error");
			saveError.setDelayMsec(3000);
			saveError.show(Page.getCurrent());
		}
	}


}
