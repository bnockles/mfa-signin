package com.example.mfa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;


import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;


/**
 * 
 * @author bnockles
 *
 *For information about files, see: https://vaadin.com/docs/-/part/framework/application/application-resources.html
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
	String fileName;//TODO delete
	File attendanceCsv;
	private Date attendanceDate;
	/**
	 * SPECIAL NOTE:
	 * Everytime an attendance record is changed, save is called
	 * Save in turn calls update, but update potentially changes attendance records
	 * this causes update to be called more than once
	 * The following boolean is true only at the beginning of the update method.
	 * If this is true, then save will not be conducted
	 */
	private boolean processingUpdate;

	public final static int FIRST_INDEX = 0;
	public final static int LAST_INDEX = 1;
	public final static int ID_INDEX = 2;
	public final static int DATE_INDEX = 3;
	public final static int COURSE_INDEX = 4;
	public final static int WORKSHOP_INDEX = 5;
	public final static int LOCATION_INDEX = 6;
	public final static int ATTENDANCE_INDEX = 7;
	public final static int TIMESTAMP_INDEX = 8;
	public final static int ATTENDANCE_CONFIRMED_INDEX = 9;
	public final static int LATE_INDEX = 10;
	public final static String TIMESTAMP_FORMAT = "MM/dd/yyyy kk:mm:ss a";
	public final static int UTC_TIME_DIFFERENCE = 4;//UTC is 4 hours ahead
	//basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();  



	public AttendanceFile(String saveFileName, File file, Date attendanceDate){
		this.fileName = saveFileName;
		this.attendanceDate = attendanceDate;
		processingUpdate = false;
		attendanceCsv = file;
		allAttendanceRecords = new BeanItemContainer<AttendanceRecord>(AttendanceRecord.class);
		loadedPDsTonight=new ArrayList<PD>();
		loadedPDsPrevious= new ArrayList<PD>();
		locations=new ArrayList<String>();
		try {
			loadCSVFile(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}  
		String csvFile = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()+"/WEB-INF/attendance-records/"+fileName;
		try {
			writeFile(csvFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**a method to update potential changes to the saved file
	 * It does this by checking every attendance record's attendanceID and finding the line in save file with the same ID. If there is a variation in the content of the lines,
	 * the change is made to the one in memory
	 * this syncs changes across all instances
	 * @param fileReader
	 */
	private void update(FileReader fileReader, String earmarkedChange){
		//get a "summary" of the attendance file
		processingUpdate = true;
		ArrayList<AttendanceFileLine> content = new ArrayList<AttendanceFileLine>();
		String line = "";
		BufferedReader br = new BufferedReader(fileReader);
		try {
			while((line = br.readLine()) != null){
				String[] row = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);//split only a comma that has an even number of quotes ahead of it
				try{
					AttendanceFileLine afl = new AttendanceFileLine(row[ID_INDEX].replaceAll("\"", ""), row[ATTENDANCE_INDEX].replaceAll("\"", ""), row[TIMESTAMP_INDEX].replaceAll("\"", ""), row[ATTENDANCE_CONFIRMED_INDEX]);
					content.add(afl);
				}catch(ArrayIndexOutOfBoundsException e){
					new ErrorMessage("Update Error","An error ocurred while updating the attendance file. \nThis happened while attempting to parse the line: \n"+Arrays.toString(row));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		//sort the attendanceFile
		Collections.sort(content);

		Iterator<AttendanceRecord> iterator = allAttendanceRecords.getItemIds().iterator();
		while(iterator.hasNext()){
			AttendanceRecord current = iterator.next();
			String currentID = current.getID();
			//updates all attendance files except the one that is "earmarked" to be itself changed
			//For example, Joe signed in on a different IPad, Sue signed in on this one
			//When this iPad updates, it does not have a record of Joe

			//1)It finds Joes's record on Sue's iPad
			if(!currentID.equals(earmarkedChange)){
				//2)it recognizes that, according to this iPad, Joes is not checked in
				String currentStatus = current.getStatus();
				String confirmedAbsence = current.confirmedAbsenceAsString();
				//3) It finds Joe's record on the save file
				int matchIndex = binarySearch(content,currentID);
				//-1 will only be returned if the save file has been replaced by a new one (an admin has started a new session)
				if(matchIndex < 0){
					saveChanges();//window appears notifying user that the save file has been changed and asks if they would like to download current changes
					return;
				}
				//if status does not match
				//4) It identifies that the save file has a different status (present) than Sue has
				else if(!content.get(matchIndex).getAttendance().equals(currentStatus)){
					DateFormat df = new SimpleDateFormat(TIMESTAMP_FORMAT);
					Date d = null;
					try {
						//since the save file keeps dates as EST, we must convert to UTC
						//5) It convert's Joe's timestamp to UTC 
						d = convertESTToUTC(df.parse(content.get(matchIndex).getDate()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					//6) On Sue's iPad it updates with Joe's status
					current.setStatus(content.get(matchIndex).getAttendance(), d);
				}else if(!content.get(matchIndex).isConfirmed().equals(confirmedAbsence)){
					//7) checks if an attendance record has its absence confirmed
					current.setConfirmedAbsenceFromString(content.get(matchIndex).isConfirmed());
				}
			}
		}
		processingUpdate = false;
	}


	private int binarySearch(ArrayList<AttendanceFileLine> content, String ID){
		int lo = 0;
		int hi = content.size()- 1;
		while (lo <= hi) {
			// Key is in a[lo..hi] or not present.
			int mid = lo + (hi - lo) / 2;
			if      (ID.compareTo(content.get(mid).getID())< 0) hi = mid - 1;
			else if (ID.compareTo(content.get(mid).getID())> 0) lo = mid + 1;
			else return mid;
		}
		return -1;

	}


	private void saveChanges() {
		final TextWindow saveChanges = new TextWindow("Save Changes","60%","50%","The attendance file that this "
				+ "app instance had been saving data to has been replaced (most likely because an "
				+ "admin wanted to start a new session.) Therefore, this app instance has expired. "
				+ "If you have not already downloaded the attendance file associated with this app instance,"
				+ " you can do so now.");
		saveChanges.setWordwrap(true);
		HorizontalLayout hl = new HorizontalLayout();


		hl.addComponent(generateContent());
		Button close = new Button("close");
		close.addClickListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -1385584475447768719L;

			@Override
			public void buttonClick(ClickEvent event) {
				saveChanges.close();
			}
		});
		hl.addComponent(close);
		saveChanges.addFeature(hl);
		UI.getCurrent().addWindow(saveChanges);
	}

	private Component generateContent() {
		String content ="FirstName,Last Name,Record ID,Date,PD,Workshop,Location,Status,Timestamp,Confirmed Absence, Late,\n";
		Iterator<AttendanceRecord> iter= iterator();
		while(iter.hasNext()){		
			AttendanceRecord e = iter.next();
			content+="\""+e.getFirstName()+"\",\""+e.getLastName()+"\",\""+e.getID()+"\",\""+e.getPd().getDateString()+"\",\""+e.getPd().getTitle()+"\",\"Workshop "+e.getPd().getWorkshop()+"\",\""+e.getPd().getLocation()+"\",\""+e.getStatus()+","+e.getTime()+"\n";
		}

		TextArea area = new TextArea();
		area.setSizeFull();
		area.setValue(content);

		return area;
	}

	public void loadCSVFile(FileReader fileReader){
		BufferedReader br = null;
		String line = "";
		int foundContent = 0;

		try {

			br = new BufferedReader(fileReader);
			while ((line = br.readLine()) != null) {


				try{
					String[] row = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);//split only a comma that has an even number of quotes ahead of it
					//					System.out.print("Loaded a line: ");
					//					for(String s: row){
					//						System.out.print(s+" --- ");
					//					}
					//					System.out.println("");

					//parse the date from this entry
					DateFormat format = new SimpleDateFormat("MM/dd/yy");
					Date date ;
					try{
						date = format.parse(row[DATE_INDEX].replaceAll("\"", ""));
					}catch (ParseException e) {
						try{
							DateFormat format2 = new SimpleDateFormat("M/dd/yy");
							date = format2.parse(row[DATE_INDEX].replaceAll("\"", ""));
						}catch (ParseException e2) {
							try{
								DateFormat format3 = new SimpleDateFormat("M/d/yy");
								date = format3.parse(row[DATE_INDEX].replaceAll("\"", ""));
							}catch (ParseException e3) {
								try{
									DateFormat format4 = new SimpleDateFormat("MM/d/yy");
									date = format4.parse(row[DATE_INDEX].replaceAll("\"", ""));
								}catch (ParseException e4) {
									date = new Date();
								}
							}
						}
					}
					date = convertESTToUTC(date);


					//Start by checking whether or not the PD has already been loaded
					PD pd = new PD(row[COURSE_INDEX].replaceAll("\"", ""), Integer.parseInt(row[WORKSHOP_INDEX].replace("Workshop ","").replace("\"", "")), new BeanItemContainer<AttendanceRecord>(AttendanceRecord.class), date, row[LOCATION_INDEX].replaceAll("\"", ""));
					PD alreadyLoaded=pd;
					boolean wasLoaded = false;
					for(PD p: loadedPDsTonight){
						if(pd.equals(p)){
							alreadyLoaded=p;
							wasLoaded=true;
							break;
						}
					}
					if(!wasLoaded){
						SimpleDateFormat dayOnly = new SimpleDateFormat("MM/dd/yy");
						Date today = dayOnly.parse(dayOnly.format(attendanceDate));
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(attendanceDate);
						calendar.add(Calendar.DAY_OF_YEAR, 1);  
						Date tomorrow = calendar.getTime();

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

					String status = AttendanceRecord.ABSENT;
					DateFormat df = new SimpleDateFormat(TIMESTAMP_FORMAT);
					Date timeStamp = null;
					try{
						status=row[ATTENDANCE_INDEX].replaceAll("\"", "");
						String stamp = row[TIMESTAMP_INDEX].replaceAll("\"", "");
						timeStamp=convertESTToUTC(df.parse(stamp));						
					}catch(ArrayIndexOutOfBoundsException e){
						//will always throw error unless format of csv is changed to include status
					}catch(ParseException e){
						//will be thrown unless record contains a valid date
					}catch(NumberFormatException e){
						//thrown when int is parsed from the "Workshop" header
					}
					boolean confirmedAbsence = true;
					if(status.equals(AttendanceRecord.ABSENT) || status.equals("")){
						try{
							confirmedAbsence = parseOneOrZero(row[ATTENDANCE_CONFIRMED_INDEX]);
						}catch(Exception e){
							confirmedAbsence = false;
						}

					}
					boolean late = false;
					if(status.equals(AttendanceRecord.ATTENDED)){
						try{
							late = parseOneOrZero(row[LATE_INDEX]);
						}catch(Exception e){
						}
					}

					AttendanceRecord thisRecord = new AttendanceRecord(this, new Teacher(row[LAST_INDEX].replaceAll("\"", ""),row[FIRST_INDEX].replaceAll("\"", ""),0),alreadyLoaded,row[ID_INDEX].replaceAll("\"", ""),status,timeStamp, confirmedAbsence, late);
					allAttendanceRecords.addItem(thisRecord);
					//add the record to the PD as well
					alreadyLoaded.addRecord(thisRecord);				
					foundContent++;

				}catch(ArrayIndexOutOfBoundsException e){
					//this exception is thrown at the end of the document, which contains document information
					e.printStackTrace();
				}catch(Exception e){
					//this exception is thrown aduring the first line, since it is the header row
					e.printStackTrace();
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
					if(foundContent<=1)new ErrorMessage("Loading Error","There is no content in the selected file or no file was loaded. You should reload this page.");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}



	public static Date convertESTToUTC(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.HOUR_OF_DAY, UTC_TIME_DIFFERENCE);
		return cal.getTime();
	}

	public static Date converUTCToEST(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.HOUR_OF_DAY, -UTC_TIME_DIFFERENCE);
		return cal.getTime();
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

	/**
	 * write all current content into save file
	 * @param csvFile
	 * @throws IOException
	 */
	private void writeFile(String csvFile) throws IOException{
		FileWriter fileWriter = new FileWriter(csvFile);
		// Always wrap FileWriter in BufferedWriter.
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		// Note that write() does not automatically
		// append a newline character.
		bufferedWriter.write("\"First Name\",\"Last Name\",\"Attendance: ID\",\"Date\",\"Course\",\"Workshop\",\"Workshop Location\",Status,Timestamp,Confirmed Absence, Late,\n");
		for(Iterator<AttendanceRecord> i = allAttendanceRecords.getItemIds().iterator(); i.hasNext();){
			AttendanceRecord a = (AttendanceRecord)i.next();
			bufferedWriter.write("\""+a.getTeacher().getFirstName()+"\","
					+ "\""+a.getTeacher().getLastName()+"\","
					+ "\""+a.getID()+"\","
					+ "\""+a.getPd().getDateString()+"\","
					+ "\""+a.getPd().getTitle()+"\","
					+ "\"Workshop "+a.getPd().getWorkshop()+"\","
					+ "\""+a.getPd().getLocation()+"\","
					+ "\""+a.getStatus()+"\",");
			if(a.getStatus().equals(AttendanceRecord.ATTENDED)) bufferedWriter.write("\""+a.getFormattedTime()+"\"");
			else bufferedWriter.write("\"-\"");

			bufferedWriter.write(","+oneOrZero(a.confirmedAbsence())+","+oneOrZero(a.wasLate())+",\n");

		}

		// Always close files.
		bufferedWriter.close();
	}

	public static boolean parseOneOrZero(String s){
		if(s.equals("1"))return true;
		return false;
	}

	public static String oneOrZero(boolean b){
		if(b)return "1";
		return "0";
	}

	//Making this method synchronized means it can only be called by one thread at a time. This removes possibility of overriding data while 
	//it is being read on another iPad
	public synchronized boolean save(String earmarkedChange){
		String csvFile = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()+"/WEB-INF/attendance-records/"+fileName;

		try {
			if(!processingUpdate){
				update(new FileReader(csvFile), earmarkedChange);
				writeFile(csvFile);					

			}
			return true;
		}
		catch(IOException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public Iterator<AttendanceRecord> iterator() {
		return allAttendanceRecords.getItemIds().iterator();
	}

	public File getFile() {
		return attendanceCsv;
	}

}
