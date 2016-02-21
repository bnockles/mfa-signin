package com.example.mfa;

/**
 * 
 * @author bnockles
 * @notes For documentation on salesforce API, see https://developer.salesforce.com/page/Database
 */

import java.util.ArrayList;
import java.util.Date;

import javax.servlet.annotation.WebServlet;


import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
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
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
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
	Window dateWindow;
	Window nameWindow;
	FormLayout editForm;
	HorizontalLayout layout;
	
	NativeSelect pdSelect;
	
	VerticalLayout nameLayout;
	NativeSelect name;
	Button addName;

	Button enter;
	Label selectedEntry;
	Button deleteEntry;
	Button cancelEdit;
	DateField date;
	Button export;

	//values
	BeanItemContainer<PD> pds = new BeanItemContainer<PD>(PD.class);
	AttendanceFile attendanceRecords;
	//TODO Currently loading the sample attendance file. Load from fil once parse function is done
	Table table;
	int entryIndex=1;

	protected void init(VaadinRequest request) {
		setRosterAndPDDefaults();
		prepareTable();
		setLayout();
		setDateSelection();
		setNameEntry();

		// Open it in the UI
		addWindow(dateWindow);
	}

	private void setRosterAndPDDefaults(){

		attendanceRecords= new AttendanceFile();

		
		for(PD a: attendanceRecords.getPDs()){
			pds.addItem(a);
		}
		

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
//			table.setColumnCollapsed("id", true);
			table.setColumnCollapsed("firstName", true);
			table.setColumnCollapsed("lastName", true);
		}catch(IllegalStateException e){
			
		}
	}

	
	
	private void setLayout(){
		HorizontalLayout main = new HorizontalLayout();
		VerticalLayout m = new VerticalLayout();
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
		addName=new Button("Add a new name");

		layout.addComponent(nameLayout);


		enter=new Button("Sign In");
		enter.setWidth("60%");
		layout.addComponent(enter);
		layout.setComponentAlignment(nameLayout, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(pdLayout, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(enter, Alignment.MIDDLE_CENTER);
		layout.setSpacing(true);
		Resource res = new ThemeResource("images/MfA.jpeg");
		Image graphic = new Image(null, res);
		graphic.setHeight("200px");
		m.addComponent(graphic);
		m.setComponentAlignment(graphic, Alignment.MIDDLE_CENTER);
		m.addComponent(layout);
		// Create a form for editing a selected or new item.
		// It is invisible until actually used.
		VerticalLayout reviewData = new VerticalLayout();
		reviewData.addComponent(table);
		reviewData.setWidth("90%");
		editForm = new FormLayout();
		editForm.setCaption("Edit Entry");
		editForm.setVisible(false);
		selectedEntry=new Label("");
		deleteEntry = new Button("Mark person absent");
		cancelEdit = new Button("Cancel");
		editForm.addComponent(selectedEntry);
		editForm.addComponent(deleteEntry);
		editForm.addComponent(cancelEdit);
		reviewData.addComponent(editForm);
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
		reviewData.addComponent(genCsv);
//		reviewData.setWidth("80%");
		m.addComponent(reviewData);

		//        export = new Button("Export .xls");
		//        m.addComponent(export);
		//        m.setComponentAlignment(export, Alignment.TOP_CENTER);
		//        

		m.setSpacing(true);
		m.setWidth("100%");
		m.setHeight("100%");
//		m.setExpandRatio(layout, 1.0f);
		m.setExpandRatio(reviewData, 0.0f);
		m.setComponentAlignment(layout, Alignment.MIDDLE_CENTER);
		m.setComponentAlignment(reviewData, Alignment.MIDDLE_CENTER);
		main.addComponent(m);
		main.setComponentAlignment(m, Alignment.MIDDLE_CENTER);
		main.setWidth("100%");
		setContent(main);
		addListeners();
	}

	private void setDateSelection(){
		dateWindow = new Window("Create a new sign in sheet.");
		VerticalLayout subContent = new VerticalLayout();
		subContent.setMargin(true);
		dateWindow.setContent(subContent);

		Label dateSelect = new Label("Select a date for this form.");
		date = new DateField();
		date.setWidth("100%");
		date.setValue(new Date());
		Button startSignIn = new Button("Start Collecting Attendance");
		startSignIn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = -73154695046517200L;

			@Override
			public void buttonClick(ClickEvent event) {
				dateWindow.close();
				table.setCaption("MfA Professional Development, "+date.getValue());
			}
		});
		subContent.addComponent(dateSelect);
		subContent.addComponent(date);
		subContent.addComponent(startSignIn);
		subContent.setComponentAlignment(startSignIn, Alignment.MIDDLE_CENTER);
		dateWindow.center();

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
						name.select(created);
						nameWindow.close();
					}else{
						Notification alreadyEntered = new Notification("Teacher Already Listed");
						alreadyEntered.setStyleName("error");
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
				nameLayout.addComponent(addName);
				nameLayout.setComponentAlignment(name, Alignment.MIDDLE_CENTER);
				nameLayout.setComponentAlignment(addName, Alignment.MIDDLE_LEFT);
			}
		});
		
		enter.addClickListener(new ClickListener(){
			private static final long serialVersionUID = -73954695086217200L;

			public void buttonClick(final ClickEvent event) {
				if(pdSelect.getValue()!=null){
					if(name.getValue()!=null){
						for(Object l:table.getItemIds()){
							AttendanceRecord le = (AttendanceRecord)l;
							if(le.equals((AttendanceRecord)(name.getValue()))){
								if(le.getStatus().equals(AttendanceRecord.ATTENDED)){
									Notification duplicate = new Notification("Already Checked In","Either you are someone else has already logged in today.");
									duplicate.setStyleName("error");
									duplicate.setDelayMsec(3000);
									duplicate.show(Page.getCurrent());
									break;
								}else{
									le.setStatus(AttendanceRecord.ATTENDED);
									table.commit();
									Notification success = new Notification("Success!","You are checked in for "+le.getPD().getTitle()+".");
//									success.setStyleName("error");
									success.setDelayMsec(3000);
									success.show(Page.getCurrent());
									table.refreshRowCache();
								}
							}
						}
					}else{
						Notification selectPD = new Notification("Please select your name.","If your name is not listed, please add it by clicking the 'Add Name' button.");
						selectPD.setStyleName("error");
						selectPD.show(Page.getCurrent());
					}
				}else{

					Notification selectName = new Notification("Please select a PD.","If your PD is not listed, talk to someone at the front desk.");
					selectName.setStyleName("error");
					selectName.show(Page.getCurrent());
				}
			}
		});
		// When the user selects an item, show it in the form
		deleteEntry.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				((AttendanceRecord)table.getValue()).setStatus(AttendanceRecord.ABSENT);
				table.commit();
				table.refreshRowCache();
				Teacher marked = ((AttendanceRecord)table.getValue()).getTeacher();
				Notification absent = new Notification(marked.getFirstName()+" "+marked.getLastName(),"...has been marked absent.");
				absent.setDelayMsec(3000);
				absent.show(Page.getCurrent());
				editForm.setVisible(false);
			}
		});
		cancelEdit.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				editForm.setVisible(false);
			}
		});
		table.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = -73954595086117200L;

			public void valueChange(ValueChangeEvent event) {
				// Close the form if the item is deselected
				if (event.getProperty().getValue() == null) {
					editForm.setVisible(false);
					return;
				}
				AttendanceRecord selected = (AttendanceRecord)table.getValue();
				selectedEntry.setValue(selected.getTeacher()+" participating in "+selected.getPD());
				if(((AttendanceRecord)table.getValue()).getStatus().equals(AttendanceRecord.ABSENT))deleteEntry.setEnabled(false);
				else deleteEntry.setEnabled(true);
				editForm.setVisible(true);

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

}