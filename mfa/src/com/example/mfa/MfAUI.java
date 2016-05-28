package com.example.mfa;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * 
 * @author bnockles
 * @notes For documentation on salesforce API, see https://developer.salesforce.com/page/Database
 */

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.annotation.WebServlet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
@Theme("mfa")
public class MfAUI extends UI {

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = MfAUI.class)

	public static class Servlet extends VaadinServlet {
	}

	//components
	VerticalLayout selectFileFields;
	Window fileSelectWindow;
	PasswordEntry fileSelectPasswordEntry;
	DateField dateField;
	Window locationWindow;
	Window nameWindow;
	Window explainAbsence;
	VerticalLayout m;//main content, but fits within a HorizontalLyout for spacing reasons 
	HorizontalLayout editForm;
	HorizontalLayout layout;
	HorizontalLayout otherFunctions;

	NativeSelect pdSelect;

	VerticalLayout nameLayout;
	NativeSelect name;
	Button addName;

	Button enter;
	NativeSelect changeEntry;
	Button export;
	Button cancelPD;
	Window confirmCancel;

	//admin functions
	static long startTime;
	AdminGroup group;
	Button admin;
	private String enteredPassword;
	boolean adminVisible;
	VerticalLayout adminLayout;
	ArrayList<Note> notes;
	String location;


	//values
	BeanItemContainer<PD> pds;
	AttendanceFile attendanceRecords;
	//TODO Currently loading the sample attendance file. Load from fil once parse function is done
	Table table;
	int entryIndex=1;
	public static final String ATTENDANCE_FILE_NAME = "MfA Attendance.csv";
	public static final String NOTES_FILE_NAME = "ServerNotes.txt";
	public String ATTENDANCE_FILE;
	public String NOTES_FILE;

	protected void init(VaadinRequest request) {
		ATTENDANCE_FILE = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()+"/WEB-INF/attendance-records/"+ATTENDANCE_FILE_NAME;
		NOTES_FILE = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()+"/WEB-INF/attendance-records/"+NOTES_FILE_NAME;
		notes = new ArrayList<Note>();
		pds = new BeanItemContainer<PD>(PD.class);
		Calendar startTimeCal = Calendar.getInstance();
		startTimeCal.set(Calendar.HOUR, 5+AttendanceFile.UTC_TIME_DIFFERENCE);
		startTimeCal.set(Calendar.MINUTE, 30);
		startTimeCal.set(Calendar.SECOND, 00);
		startTimeCal.set(Calendar.AM_PM, Calendar.PM);
		startTime = startTimeCal.getTimeInMillis();

		group = new AdminGroup();
		enteredPassword="";

		setLayout();
		setFileSelection();
		setNameEntry();

		// Open it in the UI
		addWindow(fileSelectWindow);
	}




	//Constructs a table that is not visible to attendees but can be viewed in administrator tools
	private void prepareTable(){
		table = new Table("MfA Sign In",attendanceRecords.getRecords());
		Object[] columns = new Object[]{"pd","lastName", "firstName","status","formattedTime","workshop","date"};
		table.setVisibleColumns(columns);
		table.setWidth("90%");
		table.setImmediate(true);
		table.setSelectable(true);
		table.setColumnReorderingAllowed(true);
		table.setSortEnabled(true);
		table.setSortContainerPropertyId("pd");
		table.setColumnCollapsingAllowed(true);
		//		table.setColumnHeader("id", "ID #");
		table.setColumnHeader("date", "Date");
		table.setColumnHeader("pd", "PD");
		table.setColumnHeader("firstName", "First Name");
		table.setColumnHeader("lastName", "Last Name");
		table.setColumnHeader("formattedTime", "Timestamp");
		table.setColumnHeader("workshop", "Workshop");
		table.setColumnHeader("status", "Status");

		
		try{
			table.setColumnCollapsed("date", true);
			table.setColumnCollapsed("workshop", true);
		}catch(IllegalStateException e){

		}
		table.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = -73954595086117200L;

			public void valueChange(ValueChangeEvent event) {
				// Close the form if the item is deselected
				if (event.getProperty().getValue() == null) {
					editForm.setVisible(false);
					cancelPD.setEnabled(false);
					return;
				}
				AttendanceRecord selected = (AttendanceRecord)table.getValue();
				String name = selected.getTeacher().getName();
				String selectedPD= selected.getPd().getTitle() + ", Workshop "+selected.getPd().getWorkshop();
				changeEntry.setCaption("Mark "+name+" as...");
				changeEntry.setValue(((AttendanceRecord)table.getValue()).getStatus());
				editForm.setVisible(true);
				cancelPD.setCaption("Cancel "+selectedPD);
				cancelPD.setEnabled(true);
			}


		});
	}

	private void setSelectLayout(){
		layout = new HorizontalLayout();
		layout.setWidth("70%");
		VerticalLayout pdLayout= new VerticalLayout();
		pdLayout.setSpacing(true);
		pds.sort(new String[]{"title"}, new boolean[]{true});//there must be a getTitle method
		pdSelect = new NativeSelect("Select PD",pds);
		pdSelect.setWidth("100%");
		pdSelect.setNullSelectionAllowed(false);
		pdLayout.addComponent(pdSelect);
		pdLayout.setComponentAlignment(pdSelect, Alignment.MIDDLE_CENTER);
		layout.addComponent(pdLayout);

		nameLayout = new VerticalLayout();
		nameLayout.setSpacing(true);
		layout.addComponent(nameLayout);

		enter=new Button("Sign In");
		enter.setWidth("60%");
		layout.addComponent(enter);
		layout.setComponentAlignment(nameLayout, Alignment.BOTTOM_CENTER);
		layout.setComponentAlignment(pdLayout, Alignment.BOTTOM_CENTER);
		layout.setComponentAlignment(enter, Alignment.BOTTOM_CENTER);
		layout.setSpacing(true);
	}

	private void setOtherFunctionsLayout(){
		otherFunctions = new HorizontalLayout();
		addName=new Button("Add a new name");
		addName.setStyleName(BaseTheme.BUTTON_LINK);
		admin = new Button("View Admin Data");
		admin.setStyleName(BaseTheme.BUTTON_LINK);
		addName.setEnabled(false);
		admin.setEnabled(false);

		otherFunctions.addComponent(addName);
		otherFunctions.addComponent(admin);

		otherFunctions.setComponentAlignment(addName, Alignment.MIDDLE_CENTER);
		otherFunctions.setComponentAlignment(admin, Alignment.MIDDLE_CENTER);
		otherFunctions.setSpacing(true);
	}

	private void prepAdminLayout(){
		adminLayout.removeAllComponents();

		final PasswordField passwordToAcessAdmin = new PasswordField("Enter an administrator password.");
		passwordToAcessAdmin.setImmediate(true);
		passwordToAcessAdmin.setWidth("100%");
		OnEnterKeyHandler onEnterHandler=new OnEnterKeyHandler(){
			@Override
			public void onEnterKeyPressed() {
				adminEntry(passwordToAcessAdmin.getValue());
			}
		};
		onEnterHandler.installOn(passwordToAcessAdmin);

		Button confirmPassword = new Button("Okay", new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				adminEntry(passwordToAcessAdmin.getValue());
			}
		});

		HorizontalLayout confirmLayout = new HorizontalLayout();
		confirmLayout.addComponent(passwordToAcessAdmin);
		confirmLayout.addComponent(confirmPassword);

		confirmLayout.setComponentAlignment(confirmPassword, Alignment.BOTTOM_RIGHT);
		confirmLayout.setSpacing(true);
		confirmLayout.setWidth("60%");
		confirmLayout.setMargin(true);
		adminLayout.addComponent(confirmLayout);
		adminLayout.setComponentAlignment(confirmLayout, Alignment.TOP_CENTER);


	}

	private void adminEntry(String password){
		if(group.isValidPassword(password)){
			finishAdminLayout();
			enteredPassword=password;
		}else{
			Notification change = new Notification("Incorrect Password","A correct password is required for administrative privilages.");
			enteredPassword="";
			change.setStyleName("error");
			change.setDelayMsec(3000);
			change.show(Page.getCurrent());
			adminLayout.setVisible(!adminVisible);
			adminVisible=!adminVisible;
		}
	}

	private void finishAdminLayout(){
		adminLayout.removeAllComponents();

		VerticalLayout reviewData = new VerticalLayout();
		reviewData.addComponent(table);
		reviewData.setSpacing(true);
		reviewData.setWidth("90%");
		editForm = new HorizontalLayout();
		editForm.setCaption("Edit Entry");
		editForm.setVisible(false);
		//deleteEntry and cancelEdit are initialized in setLayout so that clickListeners can be handled at the same time

		editForm.addComponent(changeEntry);
		editForm.setWidth("400px");
		reviewData.addComponent(editForm);

		reviewData.setComponentAlignment(table, Alignment.MIDDLE_CENTER);
		reviewData.setComponentAlignment(editForm, Alignment.MIDDLE_CENTER);

		Button updateTable = new Button("Update Table",new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				updateTable();
			}

		});


		File attendanceFile = attendanceRecords.getFile();
		FileResource csv = new FileResource(attendanceFile);
		FileDownloader fd = new FileDownloader(csv);
		Button genCsv = new Button("Download CSV");
		fd.extend(genCsv);


		Button viewNotes = new Button("View special notes", new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {


				final TextWindow notes = new TextWindow("Attendance Notes","70%","90%",noteContent());
				FileWriter notesFileWriter;
				Button updateNotes = new Button("Update Notes");
				updateNotes.setStyleName("reset");
				updateNotes.addClickListener(new ClickListener(){

					@Override
					public void buttonClick(ClickEvent event) {
						MfAUI.this.notes = new ArrayList<Note>();
						try {
							updateNotesFile(new FileReader(NOTES_FILE));
						}catch(IOException e){

						}
						notes.setText(noteContent());
					}

				});
				Button closeNotes = new Button("Close");
				closeNotes.addClickListener(new ClickListener() {

					@Override
					public void buttonClick(ClickEvent event) {
						notes.close();

					}
				});
				closeNotes.setStyleName("reset");
				Button downloadNotes = new Button("Download");
				downloadNotes.setStyleName("reset");
				try {
					String notesFileName = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()+"/WEB-INF/attendance-records/notes.txt";
					notesFileWriter = new FileWriter(notesFileName);
					BufferedWriter bufferedWriter = new BufferedWriter(notesFileWriter);
					bufferedWriter.write(noteContent());
					bufferedWriter.close();
					File notesFile = new File(notesFileName);
					FileResource zip = new FileResource(notesFile);
					FileDownloader fd = new FileDownloader(zip);
					fd.extend(downloadNotes);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// Always wrap FileWriter in BufferedWriter.



				HorizontalLayout hl = new HorizontalLayout();
				hl.addComponent(updateNotes);
				Label spacer = new Label("");
				hl.addComponent(spacer);
				hl.setExpandRatio(spacer, 1.0f);
				hl.addComponent(closeNotes);
				hl.setSpacing(true);
				hl.addComponent(downloadNotes);
				notes.addFeature(hl);
				notes.setFeatureAlignment(hl, Alignment.BOTTOM_RIGHT);
				notes.center();
				addWindow(notes);
			}

			private String noteContent() {
				String content ="Special Notes\r\n";
				boolean addedANote = false;
				for(Note s:notes){

					content+=s.getMessage()+"\r\n";
					addedANote = true;
				}
				if(!addedANote)content+="There are no special notes about attendance tonight.";

				content+=addAttendanceReport();
				return content;
			}
		});



		cancelPD.setEnabled(false);

		HorizontalLayout viewButtons = new HorizontalLayout();

		viewButtons.addComponent(updateTable);
		viewButtons.addComponent(genCsv);
		viewButtons.addComponent(viewNotes);
		viewButtons.addComponent(cancelPD);
		viewButtons.setSpacing(true);
		reviewData.addComponent(viewButtons);
		reviewData.setComponentAlignment(viewButtons, Alignment.MIDDLE_CENTER);

		adminLayout.addComponent(reviewData);
	}

	private String addAttendanceReport() {
		String content = "\n\n     ATTENDANCE REPORT FOR: "+location+"\n";

		int inAttendance=0;
		int numberOver30MinEarly=0;
		int numberOver15MinEarly=0;
		int numberWithin15MinEarly=0;
		int numberWithin15MinLate=0;
		int numberWithin30MinLate=0;
		int numberBeyond30MinLate=0;
		ArrayList<String> late15To30Arrivals = new ArrayList<String>();
		ArrayList<String> lateBeyond30Arrivals = new ArrayList<String>();
		Calendar checkDay = Calendar.getInstance();
		checkDay.setTime(dateField.getValue());
		checkDay.set(Calendar.HOUR, 0);
		checkDay.add(Calendar.HOUR, -1);
		checkDay.add(Calendar.DATE, -1);
		int total = 0;


		for(Iterator<AttendanceRecord> i = attendanceRecords.getRecords().getItemIds().iterator(); i.hasNext();){
			AttendanceRecord a = i.next();
			if(a.getStatus().equals(AttendanceRecord.ATTENDED) && a.getTime()!=null && a.getPd().getDate().after(checkDay.getTime())){
				total ++;
				inAttendance++;

				if(a.getTime().getTime()-startTime < -1800000) numberOver30MinEarly++;
				else if(a.getTime().getTime()-startTime < -900000) numberOver15MinEarly++;
				else if(a.getTime().getTime()-startTime<0) numberWithin15MinEarly++;
				else if(a.getTime().getTime()-startTime<900000) numberWithin15MinLate++;
				else if(a.getTime().getTime()-startTime <1800000) {
					numberWithin30MinLate++;
					late15To30Arrivals.add(a.getTeacher().getName()+" was over 15 minutes late to "+a.getPd().getTitle()+", (but not over 30 minutes late.)");
				}
				else if(a.getTime().getTime()-startTime >=1800000) {
					numberBeyond30MinLate++;
					lateBeyond30Arrivals.add(a.getTeacher().getName()+" was over 30 minutes late to "+a.getPd().getTitle() +"!");
				}
			}else if(a.getPd().getDate().after(checkDay.getTime())){
				total++;
			}

		}
		double percentAttendance;
		if(total!=0)percentAttendance= ((int)(((double)inAttendance/total)*100));
		else percentAttendance = 0.0;
		content+=inAttendance+" attending out of "+total+" ("+percentAttendance+"%) Of those,\r\n"
				+ "    "+numberOver30MinEarly+" were over 30 minutes early\r\n"
				+ "    "+numberOver15MinEarly+" were over between 30 and 15 minutes early\r\n"
				+ "    "+numberWithin15MinEarly+" were within 15 minutes early\r\n"
				+ "    "+numberWithin15MinLate+" were late by no more than 15 minutes\r\n"
				+ "    "+numberWithin30MinLate+" were between 15 and 30 minutes late (see names below)\r\n"
				+ "    "+numberBeyond30MinLate+" were at least 30 minutes late (see names below)\r\nLATE-COMERS:\r\n";
		for(String s: late15To30Arrivals){
			content+="  "+s+"\r\n";
		}
		content+="\n";
		for(String s: lateBeyond30Arrivals){
			content+="  "+s+"\r\n";
		}
		return content;

	}




	private void setLayout(){
		HorizontalLayout main = new HorizontalLayout();
		m = new VerticalLayout();
		setSelectLayout();
		setOtherFunctionsLayout();
		//		setAdminLayout();


		Resource res = new ThemeResource("images/MfA.jpeg");
		Image graphic = new Image(null, res);
		graphic.setHeight("200px");
		m.addComponent(graphic);
		m.setComponentAlignment(graphic, Alignment.MIDDLE_CENTER);
		m.addComponent(layout);
		m.addComponent(otherFunctions);
		// Create a form for editing a selected or new item.
		// It is invisible until actually used.

		//		reviewData.setWidth("80%");

		//        export = new Button("Export .xls");
		//        m.addComponent(export);
		//        m.setComponentAlignment(export, Alignment.TOP_CENTER);
		//        

		m.setSpacing(true);
		m.setWidth("100%");
		m.setHeight("100%");
		//		m.setExpandRatio(layout, 1.0f);
		m.setComponentAlignment(layout, Alignment.MIDDLE_CENTER);
		m.setComponentAlignment(otherFunctions, Alignment.MIDDLE_CENTER);

		//TODO insert into listener for admin button


		main.addComponent(m);
		main.setComponentAlignment(m, Alignment.MIDDLE_CENTER);
		main.setWidth("100%");
		setContent(main);

		ArrayList<String> statuses = new ArrayList<String>();
		statuses.add(AttendanceRecord.ATTENDED);
		statuses.add(AttendanceRecord.ABSENT);
		statuses.add(AttendanceRecord.EXCUSED);
		changeEntry = new NativeSelect("Mark person as",statuses);
		cancelPD = new Button("Cancel PD");
		changeEntry.setNullSelectionAllowed(false);

		addListeners();
	}

	private void setFileSelection(){
		fileSelectWindow = new Window("Create a new sign in sheet.");

		fileSelectPasswordEntry = new PasswordEntry();

		selectFileFields = new VerticalLayout();




		HorizontalLayout passContent = new HorizontalLayout();
		passContent.setMargin(true);
		passContent.setSpacing(true);
		//		passContent.addComponent(pass);
		passContent.setWidth("100%");
		selectFileFields.setMargin(true);
		selectFileFields.setSpacing(true);

		fileSelectWindow.setContent(selectFileFields);



		// Put the components in a panel
		dateField = new DateField("Select Date");
		dateField.setValue(new Date());

		selectFileFields.addComponent(dateField);


		//attempt to load attendance from save file
		try {
			File existingFile = new File(ATTENDANCE_FILE);
			FileReader test = new FileReader(existingFile);
			test.close();
			selectFileFields.addComponent(new OpenFromExisting(existingFile));
		} catch (IOException e) {
			selectFileFields.addComponent(new OpenFromExisting());
		}





		fileSelectWindow.center();

	}




	private void promptLocation(ArrayList<String> locations){
		locationWindow = new Window("Select the location.");
		VerticalLayout subContent = new VerticalLayout();
		subContent.setMargin(true);
		locationWindow.setContent(subContent);

		final NativeSelect locationSelect = new NativeSelect("Locations listed in file", locations);
		locationSelect.setNullSelectionAllowed(false);
		try{
			locationSelect.setValue(locations.get(0));
		}catch(IndexOutOfBoundsException e){

		}
		locationSelect.setWidth("100%");
		Button startSignIn = new Button("Start Collecting Attendance");
		startSignIn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = -77154685046537200L;

			@Override
			public void buttonClick(ClickEvent event) {
				location = (String)locationSelect.getValue();
				locationWindow.close();
				prepareTable();
				adminLayout=new VerticalLayout();

				//				prepAdminLayout();
				m.addComponent(adminLayout);
				adminVisible=false;
				adminLayout.setVisible(adminVisible);
				m.setExpandRatio(adminLayout, 0.0f);
				m.setComponentAlignment(adminLayout, Alignment.MIDDLE_CENTER);


				PD one = null;
				for(PD a: attendanceRecords.getPDs()){
					if(a.getLocation().equals(location)){
						pds.addItem(a);
						if(one==null)one = a;
					}
				}
				pdSelect.setValue(one);

				DateFormat format = new SimpleDateFormat("MM/dd/yy");		
				table.setCaption("MfA Professional Development, "+locationSelect.getValue()+", "+format.format(dateField.getValue()));

			}
		});
		subContent.addComponent(locationSelect);
		subContent.addComponent(startSignIn);
		subContent.setSpacing(true);
		subContent.setComponentAlignment(startSignIn, Alignment.MIDDLE_CENTER);
		addWindow(locationWindow);
		locationWindow.center();
	}

	private void enableUI(boolean b){
		  name.setEnabled(b);
          pdSelect.setEnabled(b);
          addName.setEnabled(b);
          admin.setEnabled(b);
	}
	
	private void setNameEntry(){
		nameWindow = new Window("All information is required.");

		FormLayout subContent = new FormLayout();
		subContent.setMargin(true);
		nameWindow.setWidth("40%");
        nameWindow.addCloseListener(new Window.CloseListener() {
            // inline close-listener
            public void windowClose(CloseEvent e) {
              enableUI(true);
            }
        });
		nameWindow.setContent(subContent);

		// Put some components in it
		final TextField lastName = new TextField("Last Name");
		lastName.addValidator(new RegexpValidator("[a-zA-Z \\-]+", "Please enter your last name."));
		final TextField firstName = new TextField("First Name");
		firstName.addValidator(new RegexpValidator("[a-zA-Z \\-]+", "Please enter your first name."));
		final TextField cohort= new TextField("Cohort");
		cohort.addValidator(new RegexpValidator("2[0-9][0-9][0-9]", "This is not a valid cohort year"));

		cohort.setImmediate(true);

		final Button add = new Button("Add");
		add.setWidth("50%");
		add.setDisableOnClick(true);
		add.addClickListener(new ClickListener(){
			private static final long serialVersionUID = -73154695086517200L;

			public void buttonClick(final ClickEvent event) {
				try{
					cohort.validate();
					lastName.validate();
					firstName.validate();
					int year = Integer.parseInt(cohort.getValue());
					boolean alreadyCreated = false;
					BeanItemContainer<AttendanceRecord> roster = ((PD)pdSelect.getValue()).getAttendanceRecords();
					for(AttendanceRecord a:roster.getItemIds()){
						if(a.getFirstName().equalsIgnoreCase(firstName.getValue()) && a.getLastName().equalsIgnoreCase(lastName.getValue())){
							alreadyCreated=true;
							break;
						}
					}
					if(!alreadyCreated){
						Teacher created = new Teacher(lastName.getValue(), firstName.getValue(),year);
						boolean late = isRightNowLate();
						AttendanceRecord tempRecord = new AttendanceRecord(attendanceRecords, created, (PD)pdSelect.getValue(), "UNREGISTERED", AttendanceRecord.ATTENDED, new Date(),true,late);
						roster.addBean(tempRecord);
						name.select(tempRecord);
						nameWindow.close();
						enableUI(true);
		
					}else{
						Notification alreadyEntered = new Notification("Teacher Already Listed");
						alreadyEntered.setStyleName("error");
						alreadyEntered.setDelayMsec(3000);
						alreadyEntered.show(Page.getCurrent());
					}
				}catch(Exception e){
					Notification alreadyEntered = new Notification("Please enter valid first name, last name, and cohort.");
					alreadyEntered.setStyleName("error");
					alreadyEntered.show(Page.getCurrent());
					add.setEnabled(true);
				}

			}

		});
		subContent.addComponent(firstName);
		subContent.addComponent(lastName);
		subContent.addComponent(cohort);
		subContent.addComponent(add);
		subContent.setComponentAlignment(add, Alignment.MIDDLE_CENTER);
		nameWindow.center();
		nameWindow.addCloseListener(new CloseListener() {

			@Override
			public void windowClose(CloseEvent e) {
				lastName.setValue("");
				firstName.setValue("");
				cohort.setValue("");
				add.setEnabled(true);
			}
		});
	}

	public static boolean isRightNowLate() {
		Calendar now = Calendar.getInstance();
		if(now.getTime().getTime()-startTime >900000) return true;
		else return false;
	}
	public void updateTable(){
		for(Object l:table.getItemIds()){//TODO since Items are coming from the Table, this may be why changes aren't being saved
			AttendanceRecord le = (AttendanceRecord)l;
			Iterator<AttendanceRecord> recordIterator = attendanceRecords.iterator();
			while (recordIterator.hasNext()) {
				AttendanceRecord ar = recordIterator.next();
				if(le.equals(ar)){
					le.setStatus(ar.getStatus(), ar.getTime());
					break;//break nested for loop once match is found
				}
			}
		}
		table.commit();
		table.refreshRowCache();
	} 

	public class PasswordEntry extends HorizontalLayout{

		PasswordField pass;
		Button enter;

		public PasswordEntry(){
			pass = new PasswordField("Enter an admin password (required)");
			pass.setWidth("400px");
			Label spacer = new Label();
			spacer.setWidth("30px");
			enter = new Button("Enter");
			enter.setStyleName("reset");
			addComponent(pass);
			addComponent(spacer);
			addComponent(enter);
			setComponentAlignment(enter, Alignment.BOTTOM_RIGHT);
		}

		/**
		 * Sets the action to take place once a correct password is entered
		 * @param fileToOpen
		 */
		public void setAction(final File fileToOpen) {
			OnEnterKeyHandler onEnterHandler=new OnEnterKeyHandler(){
				@Override
				public void onEnterKeyPressed() {
					act(fileToOpen);
				}
			};
			onEnterHandler.installOn(pass);
			enter.addClickListener(new ClickListener() {

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					act(fileToOpen);
				}
			});

		}

		private void act(final File fileToOpen){
			fileSelectWindow.close();
			if(group.isValidPassword(pass.getValue())){
				try{
					attendanceRecords = new AttendanceFile(ATTENDANCE_FILE_NAME,fileToOpen, dateField.getValue());
					promptLocation(attendanceRecords.getLocations());
					addName.setEnabled(true);
					admin.setEnabled(true);
				}catch(Exception e){
					e.printStackTrace();
					new ErrorMessage("Empty/Non-Existent file","Reload this page and try again.");
				}
			}else{
				new ErrorMessage("Invalid Password","Reload this page and try again.");
			}
		}

	}


	private void writeNotesFile() throws IOException{
		FileWriter fileWriter = new FileWriter(NOTES_FILE);
		// Always wrap FileWriter in BufferedWriter.
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);


		for(Note n: notes){
			bufferedWriter.write(n.getMessage()+"\r\n");
		}

		bufferedWriter.close();
	}

	private void updateNotesFile(FileReader fileReader) throws IOException {
		BufferedReader br = null;
		String line = "";


		br = new BufferedReader(fileReader);


		while ((line = br.readLine()) != null) {
			notes.add(new Note(line,new Date()));
		}


	}

	public void initializeNotesFile(boolean eraseExisting) {
		//tries to read existing notes file
		if(!eraseExisting){
			try {
				updateNotesFile(new FileReader(NOTES_FILE));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//tries to write existing notes file 
		//(if notes file does not already exist, jump straight to here)
		try {
			writeNotesFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class OpenFromExisting extends HorizontalLayout {

		public OpenFromExisting() {
			super();


			CsvUploader receiver = new CsvUploader();

			// Create the upload with a caption and set receiver later
			Upload upload = new Upload("Upload attendance.csv", receiver);
			upload.setButtonCaption("Upload new data");
			upload.addSucceededListener(receiver);
			upload.addStyleName("custom");
			addComponent(upload);


		}





		public OpenFromExisting(final File existingFile) {
			super();
			Button open = new Button ("Load existing data");
			open.setStyleName("confirm");
			open.addClickListener(new ClickListener() {


				/**
				 * 
				 */
				private static final long serialVersionUID = 8029651247160468035L;

				@Override
				public void buttonClick(ClickEvent event) {
					initializeNotesFile(false);
					fileSelectPasswordEntry.setAction(existingFile);
					selectFileFields.removeAllComponents();
					selectFileFields.addComponent(fileSelectPasswordEntry);
				}



			});

			CsvUploader receiver = new CsvUploader();

			// Create the upload with a caption and set receiver later
			Upload upload = new Upload("Upload attendance.csv", receiver);
			upload.setButtonCaption("Upload new data");
			upload.addSucceededListener(receiver);
			upload.addStyleName("custom");
			addComponent(upload);
			Label spacer = new Label("     ");
			spacer.setWidth("25px");
			addComponent(spacer);

			addComponent(open);
			setComponentAlignment(open, Alignment.BOTTOM_LEFT);

		}

	}

	class CsvUploader implements Receiver, SucceededListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2003285246567782020L;
		private File file;

		public OutputStream receiveUpload(String filename,
				String mimeType) {
			// Create upload stream
			FileOutputStream fos = null; // Stream to write to
			try {
				// Open a file for writing the contents of the upload
				file = new File(ATTENDANCE_FILE);
				fos = new FileOutputStream(file);
			} catch (final IOException e) {
				new Notification("Could not open file<br/>",
						e.getMessage(),
						Notification.Type.ERROR_MESSAGE)
				.show(Page.getCurrent());
				return null;
			}
			return fos; // Return the output stream to write to
		}

		public void uploadSucceeded(SucceededEvent event) {
			initializeNotesFile(true);
			fileSelectPasswordEntry.setAction(file);
			selectFileFields.removeAllComponents();
			selectFileFields.addComponent(fileSelectPasswordEntry);

		}
	};



	public void addListeners(){
		addName.addClickListener(new ClickListener(){
			private static final long serialVersionUID = -73954695086117200L;

			public void buttonClick(final ClickEvent event) {
				enableUI(false);
				addWindow(nameWindow);
			}
		});

		pdSelect.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				nameLayout.removeAllComponents();

				BeanItemContainer<AttendanceRecord> names = ((PD)pdSelect.getValue()).getAttendanceRecords();
				names.sort(new String[]{"lastName"}, new boolean[]{true});//there must be a getLastName method
				name = new NativeSelect("Select Name",names);
				name.setItemCaptionMode(ItemCaptionMode.PROPERTY);
				name.setItemCaptionPropertyId("teacher");
				name.setWidth("100%");
				name.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(ValueChangeEvent event) {
						if(pdSelect.getValue()!=null && name.getValue()!= null && !name.getValue().equals(""))
							enter.setEnabled(true);
					}
				});
				nameLayout.addComponent(name);
				nameLayout.setComponentAlignment(name, Alignment.MIDDLE_CENTER);
				enter.setEnabled(false);
			}
		});

		enter.addClickListener(new ClickListener(){
			private static final long serialVersionUID = -73954695086217200L;

			public void buttonClick(final ClickEvent event) {
				if(pdSelect.getValue()!=null){
					if(name.getValue()!=null){
						boolean alreadyListed = false;
						for(Object l:table.getItemIds()){
							AttendanceRecord le = (AttendanceRecord)l;
							if(le.equals((AttendanceRecord)(name.getValue()))){
								alreadyListed=true;
								if(le.getStatus().equals(AttendanceRecord.ATTENDED)){
									Notification duplicate = new Notification("Already Checked In","Either you are someone else has already logged in today.");
									duplicate.setStyleName("error");
									duplicate.setDelayMsec(3000);
									duplicate.show(Page.getCurrent());
									name.setValue(null);
									break;
								}else{
									le.setStatus(AttendanceRecord.ATTENDED);
									table.commit();
									table.refreshRowCache();
									//iterate through previous PDs to see if participant has attended each one
									boolean absentRecordFound = false;
									for(PD previous : attendanceRecords.getPreviousPDs()){
										if(previous.getTitle().equals(((PD)pdSelect.getValue()).getTitle())){
											//iterate through all attendance records within this PD to find the one matching this participant
											for (Iterator<AttendanceRecord> i = previous.getAttendanceRecords().getItemIds().iterator(); i.hasNext();) {
												AttendanceRecord a =  i.next();
												/**if a match is found AND
												 *  the match is an ABSENT record AND
												 *  the absence has not been confirmed
												 */
												if(a.getTeacher().equals(((AttendanceRecord)(name.getValue())).getTeacher()) &&
														a.getStatus().equals(AttendanceRecord.ABSENT) &&
														!a.confirmedAbsence()){
													absentRecordFound=true;
													notifyAbsence(previous.getDate());
													a.setConfirmedAbsence(true);
												}
											}

										}
									}
									if(!absentRecordFound){
										Notification success = new Notification("Success!",le.getFirstName()+", you are checked in for "+le.getPd().getTitle()+".");
										success.setDelayMsec(3000);
										success.show(Page.getCurrent());
										name.setValue(null);//if the teacher has a prior absences, the name is not reset until AFTER they write their reasoning
									}
								}
							}
						}
						if(!alreadyListed){
							Teacher newAddition = ((AttendanceRecord)name.getValue()).getTeacher();
							String pdTitle = ((PD)pdSelect.getValue()).getTitle();
							addNote(newAddition.getFirstName()+" "+newAddition.getLastName()+" attended the " + ((PD)pdSelect.getValue()).getTitle()+" PD without registering.");
							Notification success = new Notification("Success!",newAddition.getFirstName()+", you are checked in for "+pdTitle+".");
							success.setDelayMsec(3000);
							success.show(Page.getCurrent());
							name.setValue(null);
						}

						enter.setEnabled(false);

					}else{
						Notification selectPD = new Notification("Please select your name.","If your name is not listed, please add it by clicking the 'Add Name' button.");
						selectPD.setStyleName("error");
						selectPD.setDelayMsec(3000);
						selectPD.show(Page.getCurrent());
					}
				}else{

					Notification selectName = new Notification("Please select a PD.","If your PD is not listed, talk to someone at the front desk.");
					selectName.setStyleName("error");
					selectName.setDelayMsec(3000);
					selectName.show(Page.getCurrent());
				}
			}


		});
		// When the user selects an item, show it in the form
		changeEntry.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				//change occurs only if THIS row has changed, not if the change is due to a change in rows
				if(!changeEntry.getValue().equals(((AttendanceRecord)table.getValue()).getStatus())){
					((AttendanceRecord)table.getValue()).setStatus((String) changeEntry.getValue(), new Date());
					table.commit();
					table.refreshRowCache();
					Teacher marked = ((AttendanceRecord)table.getValue()).getTeacher();
					String value = (String) changeEntry.getValue();
					Notification change = new Notification(marked.getName()+"...","...has been marked "+value);
					change.setDelayMsec(3000);
					change.show(Page.getCurrent());
					editForm.setVisible(false);
					addNote(marked.getName()+" was marked "+value+ " by "+group.getAdmin(enteredPassword)+".");
				}
			}
		});

		admin.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {

				adminLayout.setVisible(!adminVisible);
				adminVisible=!adminVisible;
				if(adminVisible){
					prepAdminLayout();
					admin.setCaption("Hide Admin Data");
				}else{
					admin.setCaption("View Admin Data");
					enteredPassword="";
				}
			}

		});
		cancelPD.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				confirmCancel = new Window("Are you sure you want to cancel "+((AttendanceRecord)table.getValue()).getPd().getTitle()+"?");
				final PasswordField passwordToCancel = new PasswordField("Enter an administrator password to confirm.");
				OnEnterKeyHandler onEnterHandler=new OnEnterKeyHandler(){
					@Override
					public void onEnterKeyPressed() {
						cancelPD(passwordToCancel.getValue());
					}
				};
				onEnterHandler.installOn(passwordToCancel);
				Button noButton = new Button("Close", new ClickListener() {

					@Override
					public void buttonClick(ClickEvent event) {
						confirmCancel.close();
					}
				});
				Button cancelButton = new Button("Confirm Cancel", new ClickListener() {

					@Override
					public void buttonClick(ClickEvent event) {
						cancelPD(passwordToCancel.getValue());
					}
				});

				HorizontalLayout confirmLayout = new HorizontalLayout();
				HorizontalLayout buttonsLayout = new HorizontalLayout();
				confirmLayout.addComponent(passwordToCancel);
				passwordToCancel.setWidth("100%");
				buttonsLayout.addComponent(noButton);
				buttonsLayout.addComponent(cancelButton);
				buttonsLayout.setSpacing(true);
				confirmLayout.addComponent(buttonsLayout);
				confirmLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_RIGHT);
				confirmLayout.setSpacing(true);
				confirmLayout.setWidth("100%");
				confirmCancel.setContent(confirmLayout);
				confirmLayout.setMargin(true);
				confirmCancel.center();
				confirmCancel.setModal(true);
				addWindow(confirmCancel);
			}
		});


		//        export.addClickListener(new ClickListener() {
		//        	private static final long serialVersionUID = -73954695086117200L;
		//        	private ExcelExport excelExport;
		//
		//            public void buttonClick(final ClickEvent event) {
		//                excelExport = new ExcelExport(table);
		////                excelExport.excludeCollapsedColumns();
		////                SimpleDateFormat format = new SimpleDateFormat("yy.MM.dd");
		////                excelExport.setReportTitle("Tabletop");
		//                excelExport.export();
		//            }
		//		});
	}

	private void notifyAbsence(final Date date) {
		final SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy");
		explainAbsence = new Window();
		VerticalLayout l = new VerticalLayout();
		l.setMargin(true);
		final HorizontalLayout buttonRow = new HorizontalLayout();
		explainAbsence.setContent(l);
		final TextArea disclaimer = new TextArea("Looks like you missed a session!","You have been checked in for this evening's PD but our records show you have missed a previous PD in this series. The PD was on "+format.format(date)+".");
		disclaimer.setWidth("100%");
		disclaimer.setReadOnly(true);

		Button correct = new Button("Yes, that is correct.");
		Button incorrect = new Button("No, that is not correct");
		Button yesBut = new Button("Yes, but let me explain...");
		buttonRow.setSpacing(true);
		buttonRow.setMargin(true);
		buttonRow.addComponent(correct);
		buttonRow.addComponent(incorrect);
		buttonRow.addComponent(yesBut);

		correct.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				explainAbsence.close();
				addNote(((AttendanceRecord)name.getValue()).getTeacher().getName()+" confirmed his/her absence on "+format.format(date));
				Notification thanks = new Notification("Thank you!","We just wanted to make sure our records were correct.");
				thanks.setDelayMsec(3000);
				thanks.show(Page.getCurrent());
				name.setValue(null);
			}
		});

		incorrect.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				explainingButtonAction(buttonRow, disclaimer, "Is there any reason why you didn't sign in?"," claimed they were not absent on "+format.format(date)+". ");
			}
		});

		yesBut.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				explainingButtonAction(buttonRow, disclaimer, "Is there a reason why you were unable to make it?"," wrote a note explaining their "+format.format(date)+" absence.");
			}
		});


		l.addComponent(disclaimer);
		l.addComponent(buttonRow);
		addWindow(explainAbsence);
		explainAbsence.setModal(true);
		explainAbsence.setWidth("80%");
		explainAbsence.center();
	}

	private void cancelPD(String password){
		if(group.isValidPassword(password)){
			addNote(group.getAdmin(password)+" cancelled the workshop, "+((AttendanceRecord)table.getValue()).getPd().getTitle()+".");
			((AttendanceRecord)table.getValue()).getPd().cancel();
			table.refreshRowCache();
			confirmCancel.close();
		}else{
			Notification change = new Notification("Incorrect Password","You must enter a correct password to cancel any PD.");
			change.setStyleName("error");
			change.setDelayMsec(3000);
			change.show(Page.getCurrent());
		}
	}

	protected void addNote(String message) {
		notes = new ArrayList<Note>();
		try {
			updateNotesFile(new FileReader(NOTES_FILE));
			notes.add(new Note(message, new Date()));
			writeNotesFile();
		} catch (IOException e) {
		}
	}

	/**
	 * a little method for the two actions possible when the user chooses to explain an absence
	 * @param buttonRow
	 * @param disclaimer
	 * @param message
	 * @param noteMessage The text that is written in the note to the administrators
	 */
	private void explainingButtonAction(final HorizontalLayout buttonRow, final TextArea disclaimer, String message, final String noteMessage){
		buttonRow.removeAllComponents();
		disclaimer.setCaption(message);
		disclaimer.setReadOnly(false);
		disclaimer.setValue("");
		buttonRow.addComponent(new Button("Submit",new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				explainAbsence.close();
				addNote(((AttendanceRecord)name.getValue()).getTeacher().getName()+noteMessage+" The teacher explained, \""+disclaimer.getValue()+"\"");
				Notification thanks = new Notification("Thank you!","We'll take a look and email you if we have any questions.");
				thanks.setDelayMsec(3000);
				thanks.show(Page.getCurrent());
				name.setValue(null);
			}
		}));
	}



}
