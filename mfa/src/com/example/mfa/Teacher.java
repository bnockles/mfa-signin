package com.example.mfa;

/**
 * @author bnockles
 *
 */

import java.io.Serializable;

public class Teacher implements Serializable{

	
	static final long serialVersionUID = -236332262349791525L;
	String lastName;
    String firstName;
    int cohort;
    
    public Teacher(String ln,String fn, int cohort){
        lastName = ln;
        firstName = fn;
        this.cohort=cohort;
    }

	public String getLastName() {
		return lastName;
	}

	public String getFirstName() {
		return firstName;
	}

    
	public int getCohort(){
		return cohort;
	}
    
	
	public String toString(){
		return firstName + " " + lastName;
	}

	public String getName() {
		return firstName+ " "+lastName;
	}
	
	public boolean equals(Object t){
		if(t instanceof Teacher){
			if(((Teacher)t).getName().equals(getName()))return true;
		}
		return false;
		
	}
    
}
