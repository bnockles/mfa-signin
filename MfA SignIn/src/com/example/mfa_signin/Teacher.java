package com.example.mfa_signin;

/**
 * @author bnockles
 *
 */

import java.io.Serializable;

public class Teacher implements Serializable{

	
	static final long serialVersionUID = -236332262349791525L;
	String lastName;
    String firstName;
    long id;
    int cohort;
    
    public Teacher(String ln,String fn, long id, int cohort){
        lastName = ln;
        firstName = fn;
        this.id=id;
        this.cohort=cohort;
    }

	public String getLastName() {
		return lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public long getIdentifier() {
		return id;
	}
    
	public int getCohort(){
		return cohort;
	}
    
	
	public String toString(){
		return firstName + " " + lastName;
	}
    
}
