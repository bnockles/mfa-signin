package com.example.mfa;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.vaadin.data.util.BeanItemContainer;
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
	ArrayList<PD> loadedPDsTonight;
	ArrayList<PD> loadedPDsPrevious;
	ArrayList<String> locations;


	public AttendanceFile(String fileName){

		allAttendanceRecords = new BeanItemContainer<AttendanceRecord>(AttendanceRecord.class);
		loadedPDsTonight=new ArrayList<PD>();
		loadedPDsPrevious= new ArrayList<PD>();
		locations=new ArrayList<String>();
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
					String[] row = line.split(cvsSplitBy);

					//parse the date from this entry
					DateFormat format = new SimpleDateFormat("\"M/dd/yyyy\"");
					Date date ;
					try{
						date = format.parse(row[3]);
					}catch (ParseException e) {
						try{
							DateFormat format2 = new SimpleDateFormat("\"MM/dd/yyyy\"");
							date = format2.parse(row[3]);
						}catch (ParseException e2) {
							try{
								DateFormat format3 = new SimpleDateFormat("\"M/d/yyyy\"");
								date = format3.parse(row[3]);
							}catch (ParseException e3) {
								try{
									DateFormat format4 = new SimpleDateFormat("\"MM/d/yyyy\"");
									date = format4.parse(row[3]);
								}catch (ParseException e4) {
									date = new Date();
								}
							}
						}
					}
					
					//Start by checking whether or not the PD has already been loaded
					PD pd = new PD(row[4].replaceAll("\"", ""), Integer.parseInt(row[5].replace("\"Workshop ","").replace("\"", "")), new BeanItemContainer<AttendanceRecord>(AttendanceRecord.class), date, row[6].replaceAll("\"", ""));
					PD alreadyLoaded=pd;
					boolean wasLoaded = false;
					for(PD p: loadedPDsTonight){
						if(pd.equals(p)){
							alreadyLoaded=p;
							wasLoaded=true;
						}
					}
					if(!wasLoaded){
						Calendar calendar = Calendar.getInstance();
						Date today = calendar.getTime();
						calendar.add(Calendar.DAY_OF_YEAR, 1);  
						Date tomorrow = calendar.getTime();
						
						//TODO: When finishing the Google demo, this part must be cut out, or the demo file will not work
						if(pd.getDate().before(today)){
							loadedPDsPrevious.add(pd);
							
						}
						else if(pd.getDate().before(tomorrow))loadedPDsTonight.add(pd);
					}
					//Start by checking whether or not the location has already been loaded
					String location = row[6].replaceAll("\"", "");
					boolean locationWasLoaded = false;
					for(String l: locations){
						if(location.equals(l)){
							locationWasLoaded=true;
						}
					}
					if(!locationWasLoaded){
						locations.add(location);
					}

					//TODO: tell Miriam to include attendance status in report
					String status = AttendanceRecord.ABSENT;
					try{
						status=row[8].replaceAll("\"", "");
					}catch(ArrayIndexOutOfBoundsException e){
						//will always throw error unless format of csv is changed to include status
					}
					
					AttendanceRecord thisRecord = new AttendanceRecord(new Teacher(row[1].replaceAll("\"", ""),row[0].replaceAll("\"", ""),0),alreadyLoaded,row[2].replaceAll("\"", ""),status,date);
					allAttendanceRecords.addItem(thisRecord);
					//add the record to the PD as well
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
		return loadedPDsTonight;
	}
	
	public ArrayList<PD> getPreviousPDs(){
		return loadedPDsPrevious;
	}

	public ArrayList<String> getLocations(){
		return locations;
	}

}
