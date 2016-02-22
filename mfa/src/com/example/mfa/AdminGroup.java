package com.example.mfa;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import com.vaadin.server.VaadinService;

public class AdminGroup implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8488852180152276153L;

	ArrayList<Admin> appAdmins;
	
	
	public AdminGroup(){
	
		appAdmins=new ArrayList<Admin>();
		loadCSVContent("passwords.csv");
	
	}
	
	public void loadCSVContent(String fileName){
		String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();  
		String csvFile = basepath+"/WEB-INF/attendance-records/"+fileName;
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {

				
				try{
					String[] row = line.split(cvsSplitBy);
					
					
					
					
					//Start by checking whether or not the PD has already been loaded
					Admin admin = new Admin(row[0],row[1]);
					
					appAdmins.add(admin);				

				}catch(Exception e){
					//this exception is thrown aduring the first line, since it is the header row
				}
				

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	public boolean isValidPassword(String password){
		for(Admin ad: appAdmins){
			if(ad.isPassword(password))return true;
		}
		return false;
	}
	
	
	public String getAdmin(String password){
		for(Admin ad: appAdmins){
			if(ad.isPassword(password))return ad.getName();
		}
		return "Not a registered admin";
	}
}
