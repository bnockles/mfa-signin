package com.example.mfa;

import java.text.SimpleDateFormat;

/**
 * 
 * @author bnockles
 * @notes For documentation on salesforce API, see https://developer.salesforce.com/page/Database
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.annotation.WebServlet;


import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
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
	Window fileSelectWindow;
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
	TextField fileNameEntry;
	Button export;
	Button cancelPD;
	Window confirmCancel;

	//admin functions
	AdminGroup group;
	Button admin;
	boolean adminVisible;
	VerticalLayout adminLayout;
	ArrayList<Note> notes;
	
	
	//values
	BeanItemContainer<PD> pds;
	AttendanceFile attendanceRecords;
	//TODO Currently loading the sample attendance file. Load from fil once parse function is done
	Table table;
	int entryIndex=1;

	protected void init(VaadinRequest request) {
		notes = new ArrayList<Note>();
		pds = new BeanItemContainer<PD>(PD.class);

		group = new AdminGroup();
		
		setLayout();
		setFileSelection();
		setNameEntry();

		// Open it in the UI
		addWindow(fileSelectWindow);
	}




	//Constructs a table that is not visible to attendees but can be viewed in administrator tools
	private void prepareTable(){
		table = new Table("Tabletop Club, "+(new Date()),attendanceRecords.getRecords());
		table.setWidth("90%");
		table.setImmediate(true);
		table.setSelectable(true);
		table.setColumnReorderingAllowed(true);
		table.setSortEnabled(true);
		table.setSortContainerPropertyId("cohort");
		table.setColumnCollapsingAllowed(true);
//		table.setColumnHeader("id", "ID #");
		table.setColumnHeader("date", "Date");
		table.setColumnHeader("cohort", "Cohort");
		table.setColumnHeader("pd", "PD");
		table.setColumnHeader("teacher", "Name");
		table.setColumnHeader("firstName", "First Name");
		table.setColumnHeader("lastName", "Last Name");
		
		try{
			table.setColumnCollapsed("date", true);
			table.setColumnCollapsed("cohort", true);
			table.setColumnCollapsed("firstName", true);
			table.setColumnCollapsed("lastName", true);
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
				String selectedPD= selected.getPD().getTitle() + ", Workshop "+selected.getPD().getWorkshop();
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
		pdSelect = new NativeSelect("Which PD are you participating in today?",pds);
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
		layout.setComponentAlignment(nameLayout, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(pdLayout, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(enter, Alignment.MIDDLE_CENTER);
		layout.setSpacing(true);
	}
	
	private void setOtherFunctionsLayout(){
		otherFunctions = new HorizontalLayout();
		addName=new Button("Add a new name");
		addName.setStyleName(BaseTheme.BUTTON_LINK);
		admin = new Button("View Admin Data");
		admin.setStyleName(BaseTheme.BUTTON_LINK);

		otherFunctions.addComponent(addName);
		otherFunctions.addComponent(admin);
		
		otherFunctions.setComponentAlignment(addName, Alignment.MIDDLE_CENTER);
		otherFunctions.setComponentAlignment(admin, Alignment.MIDDLE_CENTER);
		otherFunctions.setSpacing(true);
	}
	
	private void prepAdminLayout(){
		adminLayout.removeAllComponents();
		
		final PasswordField pass = new PasswordField("Enter an administrator password.");
		pass.setImmediate(true);
		pass.setWidth("100%");
		OnEnterKeyHandler onEnterHandler=new OnEnterKeyHandler(){
		            @Override
		            public void onEnterKeyPressed() {
		                adminEntry(pass.getValue());
		            }
		        };
		onEnterHandler.installOn(pass);
		
		Button confirmPassword = new Button("Okay", new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				adminEntry(pass.getValue());
			}
		});
		
		HorizontalLayout confirmLayout = new HorizontalLayout();
		confirmLayout.addComponent(pass);
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
		}else{
			Notification change = new Notification("Incorrect Password","A correct password is required for administrative privilages.");
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
//		editForm.setSpacing(true);
		reviewData.addComponent(editForm);
		
		reviewData.setComponentAlignment(table, Alignment.MIDDLE_CENTER);
		reviewData.setComponentAlignment(editForm, Alignment.MIDDLE_CENTER);
		Button genCsv = new Button("Generate CSV",new Button.ClickListener() {
			public void buttonClick(Button.ClickEvent event) {
				String content ="Date,Last Name,FirstName,ID,Cohort,PD,\n";
				for(Object l:table.getItemIds()){
					AttendanceRecord e = (AttendanceRecord)l;
					content+=e.getDate()+","+e.getLastName()+","+e.getFirstName()+","+e.getID()+","+e.getCohort()+","+e.getPD()+",\n";
				}
				Window csv = new Window(table.getCaption());
				csv.setWidth("90%");
				csv.setHeight("90%");
//				HorizontalLayout csvLayout = new HorizontalLayout();
				TextArea area = new TextArea();
				area.setSizeFull();
				area.setValue(content);
//				csvLayout.addComponent(area);
//				area.setSizeFull();
				csv.setContent(area);
				csv.center();
				addWindow(csv);
			}
		});
		Button viewNotes = new Button("View special notes", new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				String content ="Special Notes\n";
				boolean addedANote = false;
				for(Note s:notes){
					
					content+=s.getMessage()+"\n";
					addedANote = true;
				}
				if(!addedANote)content+="There are no special notes about attendance tonight.";
				Window notes = new Window(table.getCaption());
				notes.setWidth("70%");
				notes.setHeight("90%");
				TextArea area = new TextArea();
				area.setSizeFull();
				area.setValue(content);

				notes.setContent(area);
				notes.center();
				addWindow(notes);
			}
		});
		
		cancelPD.setEnabled(false);
		
		HorizontalLayout viewButtons = new HorizontalLayout();
		
		viewButtons.addComponent(genCsv);
		viewButtons.addComponent(viewNotes);
		viewButtons.addComponent(cancelPD);
		
		reviewData.addComponent(viewButtons);
		reviewData.setComponentAlignment(viewButtons, Alignment.MIDDLE_CENTER);
		
		adminLayout.addComponent(reviewData);
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
		
		addListeners();
	}

	private void setFileSelection(){
		fileSelectWindow = new Window("Create a new sign in sheet.");
		HorizontalLayout subContent = new HorizontalLayout();
		subContent.setMargin(true);
		subContent.setSpacing(true);
		fileSelectWindow.setContent(subContent);

		fileNameEntry = new TextField("Select a file from which to load attendance records.", "demo-record.csv");
		fileNameEntry.setWidth("100%");
		Button confirmFile = new Button("Next...");
		confirmFile.addClickListener(new ClickListener() {
			private static final long serialVersionUID = -73154695046517200L;

			@Override
			public void buttonClick(ClickEvent event) {
				fileSelectWindow.close();
				try{
					attendanceRecords = new AttendanceFile(fileNameEntry.getValue());
					promptLocation(attendanceRecords.getLocations());
				

				}catch(Exception e){
					Notification noFileError= new Notification("Empty/Non-Existent file","Reload this page and try again.");
					noFileError.setStyleName("error");
					noFileError.setDelayMsec(3000);
					noFileError.show(Page.getCurrent());
				}	
			}
		});
		subContent.addComponent(fileNameEntry);
		fileNameEntry.setWidth("340px");
		subContent.addComponent(confirmFile);
		subContent.setComponentAlignment(confirmFile, Alignment.BOTTOM_CENTER);
		
		fileSelectWindow.center();

	}

	private void promptLocation(ArrayList<String> locations){
		locationWindow = new Window("Select the location.");
		VerticalLayout subContent = new VerticalLayout();
		subContent.setMargin(true);
		locationWindow.setContent(subContent);

		final NativeSelect locationSelect = new NativeSelect("Locations listed in file", locations);
		locationSelect.setNullSelectionAllowed(false);
		locationSelect.setValue(locations.get(0));
		locationSelect.setWidth("100%");
		Button startSignIn = new Button("Start Collecting Attendance");
		startSignIn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = -77154685046537200L;

			@Override
			public void buttonClick(ClickEvent event) {
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
					if(a.getLocation().equals(((String)locationSelect.getValue()))){
						pds.addItem(a);
						if(one==null)one = a;
					}
				}
				pdSelect.setValue(one);

				table.setCaption("MfA Professional Development, "+locationSelect.getValue()+", "+new Date().toString());

			}
		});
		subContent.addComponent(locationSelect);
		subContent.addComponent(startSignIn);
		subContent.setSpacing(true);
		subContent.setComponentAlignment(startSignIn, Alignment.MIDDLE_CENTER);
		addWindow(locationWindow);
		locationWindow.center();
	}
	
	private void setNameEntry(){
		nameWindow = new Window("All information is required.");

		FormLayout subContent = new FormLayout();
		subContent.setMargin(true);
		nameWindow.setWidth("40%");
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
						AttendanceRecord tempRecord = new AttendanceRecord(created, (PD)pdSelect.getValue(), "UNREGISTERED", AttendanceRecord.ATTENDED, new Date());
						roster.addBean(tempRecord);
						name.select(tempRecord);
						nameWindow.close();
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


	public void addListeners(){
		addName.addClickListener(new ClickListener(){
			private static final long serialVersionUID = -73954695086117200L;

			public void buttonClick(final ClickEvent event) {
				addWindow(nameWindow);
			}
		});

		pdSelect.addValueChangeListener(new ValueChangeListener() {
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				nameLayout.removeAllComponents();
				name = new NativeSelect("Select Name",((PD)pdSelect.getValue()).getAttendanceRecords());
				name.setItemCaptionMode(ItemCaptionMode.PROPERTY);
				name.setItemCaptionPropertyId("teacher");
				name.setWidth("100%");
				nameLayout.addComponent(name);
				nameLayout.setComponentAlignment(name, Alignment.MIDDLE_CENTER);
//				nameLayout.setComponentAlignment(addName, Alignment.MIDDLE_LEFT);
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
												if(a.getTeacher().equals(((AttendanceRecord)(name.getValue())).getTeacher()) && a.getStatus().equals(AttendanceRecord.ABSENT)){
													absentRecordFound=true;
													notifyAbsence(previous.getDate());
												}
											}
										
										}
									}
									if(!absentRecordFound){
										Notification success = new Notification("Success!","You are checked in for "+le.getPD().getTitle()+".");
										success.setDelayMsec(3000);
										success.show(Page.getCurrent());
									}
								}
							}
						}
						if(!alreadyListed){
							Teacher newAddition = ((AttendanceRecord)name.getValue()).getTeacher();
							addNote(newAddition.getFirstName()+" "+newAddition.getLastName()+" attended the " + ((PD)pdSelect.getValue()).getTitle()+" PD without registering.");
						}
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
					((AttendanceRecord)table.getValue()).setStatus((String) changeEntry.getValue());
					table.commit();
					table.refreshRowCache();
					Teacher marked = ((AttendanceRecord)table.getValue()).getTeacher();
					String value = (String) changeEntry.getValue();
					Notification change = new Notification(marked.getName()+"...","...has been marked "+value);
					change.setDelayMsec(3000);
					change.show(Page.getCurrent());
					editForm.setVisible(false);
					notes.add(new Note(marked.getName()+" was marked "+value+ " by an administrator.",new Date()));
				}
			}
		});

		admin.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				
				adminLayout.setVisible(!adminVisible);
				adminVisible=!adminVisible;
				if(adminVisible)prepAdminLayout();
				if(adminVisible)admin.setCaption("Hide Admin Data");
				else admin.setCaption("View Admin Data");
			}
		});
		cancelPD.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				confirmCancel = new Window("Are you sure you want to cancel "+((AttendanceRecord)table.getValue()).getPD().getTitle()+"?");
				final PasswordField pass = new PasswordField("Enter an administrator password to confirm.");
				OnEnterKeyHandler onEnterHandler=new OnEnterKeyHandler(){
		            @Override
		            public void onEnterKeyPressed() {
		                cancelPD(pass.getValue());
		            }
		        };
		        onEnterHandler.installOn(pass);
				Button noButton = new Button("Close", new ClickListener() {
					
					@Override
					public void buttonClick(ClickEvent event) {
						confirmCancel.close();
					}
				});
				Button cancelButton = new Button("Confirm Cancel", new ClickListener() {
					
					@Override
					public void buttonClick(ClickEvent event) {
						cancelPD(pass.getValue());
					}
				});
				
				HorizontalLayout confirmLayout = new HorizontalLayout();
				HorizontalLayout buttonsLayout = new HorizontalLayout();
				confirmLayout.addComponent(pass);
				pass.setWidth("100%");
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
		final TextArea disclaimer = new TextArea("Looks like you missed a session!","You have been checked in for this evening's PD but our records show you have missed a previous PD in this series.");
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
				Notification thanks = new Notification("Thanks you!","We just wanted to make sure our records were correct.");
				thanks.setDelayMsec(3000);
				thanks.show(Page.getCurrent());
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
			((AttendanceRecord)table.getValue()).getPD().cancel();
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
		notes.add(new Note(message, new Date()));
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
				Notification thanks = new Notification("Thanks you!","We'll take a look and email you if we have any questions.");
				thanks.setDelayMsec(3000);
				thanks.show(Page.getCurrent());
			}
		}));
	}

}