package com.example.mfa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinService;


/**
 * 
 * @author bnockles
 *
 *For infomration about files, see: https://vaadin.com/docs/-/part/framework/application/application-resources.html
 */
public class AttendanceFile implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7908632964480728026L;
	BeanItemContainer<AttendanceRecord> allAttendanceRecords;
	ArrayList<PD> loadedPDs;
	
	/**
	 * Constructor for a default attendance file, used for testing/demo-ing
	 */
	public AttendanceFile(){
		allAttendanceRecords = new BeanItemContainer<AttendanceRecord>(AttendanceRecord.class);    
		loadedPDs=new ArrayList<PD>();
		loadCSVContent("demo-record.csv");
	}
	
	public AttendanceFile(String fileName){
	
		allAttendanceRecords = new BeanItemContainer<AttendanceRecord>(AttendanceRecord.class);
		loadedPDs=new ArrayList<PD>();
		loadCSVContent(fileName);
	
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
					String[] country = line.split(cvsSplitBy);
					
					//parse the date from this entry
					DateFormat format = new SimpleDateFormat("\"M/dd/yyyy\"");
					Date date ;
					try{
						date = format.parse(country[3]);
					}catch (ParseException e) {
						try{
							DateFormat format2 = new SimpleDateFormat("\"MM/dd/yyyy\"");
							date = format2.parse(country[3]);
						}catch (ParseException e2) {
							try{
								DateFormat format3 = new SimpleDateFormat("\"M/d/yyyy\"");
								date = format3.parse(country[3]);
							}catch (ParseException e3) {
								try{
									DateFormat format4 = new SimpleDateFormat("\"MM/d/yyyy\"");
									date = format4.parse(country[3]);
								}catch (ParseException e4) {
									date = new Date();
								}
							}
						}
					}
					
					//Start by checking whether or not the PD has already been loaded
					PD pd = new PD(country[4].replaceAll("\"", ""), Integer.parseInt(country[5].replace("\"Workshop ","").replace("\"", "")), new BeanItemContainer<AttendanceRecord>(AttendanceRecord.class), date);
					PD alreadyLoaded=pd;
					boolean wasLoaded = false;
					for(PD p: loadedPDs){
						if(pd.equals(p)){
							alreadyLoaded=p;
							wasLoaded=true;
						}
					}
					if(!wasLoaded){
						loadedPDs.add(pd);
					}
					
					AttendanceRecord thisRecord = new AttendanceRecord(new Teacher(country[1].replaceAll("\"", ""),country[0].replaceAll("\"", ""),0),alreadyLoaded,country[2].replaceAll("\"", ""),AttendanceRecord.ABSENT,date);
					allAttendanceRecords.addItem(thisRecord);
					alreadyLoaded.addRecord(thisRecord);				

				}catch(ArrayIndexOutOfBoundsException e){
					//this exception is thrown at the end of the document, which contains document information
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
	
	public BeanItemContainer<AttendanceRecord> getRecords(){
		return allAttendanceRecords;
	}
	
	public ArrayList<PD> getPDs(){
		return loadedPDs;
	}
	
}
