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
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
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
	Window pdWindow;
	FormLayout editForm;
	HorizontalLayout layout;
	VerticalLayout nameLayout;
	NativeSelect name;
	Button addName;
	Button addPD;
	NativeSelect game;
	Button enter;
	Label selectedEntry;
	Button deleteEntry;
	Button cancelEdit;
	DateField date;
	Button export;

	//values
	BeanItemContainer<Teacher> roster = new BeanItemContainer<Teacher>(Teacher.class);
	BeanItemContainer<PD> pds = new BeanItemContainer<PD>(PD.class);
	BeanItemContainer<LogEntry> entries = new BeanItemContainer<LogEntry>(LogEntry.class);
	Table table;
	int entryIndex=1;

	protected void init(VaadinRequest request) {
		setRosterAndPDDefaults();
		prepareTable();
		setLayout();
		setDateSelection();
		setNameEntry();
		setPDEntry();

		// Open it in the UI
		addWindow(dateWindow);
	}

	private void setRosterAndPDDefaults(){

		Teacher t0 = new Teacher("Nockles","Benjamin",0000000000000000,2012);
		Teacher t1 = new Teacher("Lang","Jason",0000000000000001, 2012);
		Teacher t2 = new Teacher("Honner","Patring",0000000000000002, 2013);
		Teacher t3 = new Teacher("Shuman","Doug",0000000000000003, 2013);
		Teacher t4 = new Teacher("Nisani","Daniel",0000000000000004, 2014);
		
		roster.addItem(t0);
		roster.addItem(t1);
		roster.addItem(t2);
		roster.addItem(t3);
		roster.addItem(t4);

		//TODO: USe salesforce IDE to retrieve scheduled PD for date of this attendance form
		ArrayList<Teacher> geoParticipants = new ArrayList<Teacher>();
		geoParticipants.add(t0);
		geoParticipants.add(t2);
		geoParticipants.add(t4);
		
		Date today = new Date();
		
		ArrayList<Teacher> a2Participants = new ArrayList<Teacher>();
		geoParticipants.add(t1);
		geoParticipants.add(t3);
		
		pds.addItem(new PD("The Story of Geometry", geoParticipants, today));
		pds.addItem(new PD("I teach algebra too!", a2Participants, today));


	}


	private void prepareTable(){
		table = new Table("Tabletop Club, "+(new Date()),entries);
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
		nameLayout = new VerticalLayout();
		nameLayout.setSpacing(true);
		name = new NativeSelect("Select Name",roster);
		name.setWidth("100%");
		addName=new Button("Add a new name");
		nameLayout.addComponent(name);
		nameLayout.addComponent(addName);
		nameLayout.setComponentAlignment(name, Alignment.MIDDLE_CENTER);
		nameLayout.setComponentAlignment(addName, Alignment.MIDDLE_LEFT);
		layout.addComponent(nameLayout);

		VerticalLayout gameLayout= new VerticalLayout();
		gameLayout.setSpacing(true);
		addPD = new Button("Add a new PD");
		game = new NativeSelect("Which PD are you participating in today?",pds);
		game.setWidth("100%");
		game.setNullSelectionAllowed(false);
		gameLayout.addComponent(game);
		gameLayout.addComponent(addPD);
		gameLayout.setComponentAlignment(game, Alignment.MIDDLE_CENTER);
		gameLayout.setComponentAlignment(addPD, Alignment.MIDDLE_LEFT);

		layout.addComponent(gameLayout);
		enter=new Button("Sign In");
		enter.setWidth("60%");
		layout.addComponent(enter);
		layout.setComponentAlignment(nameLayout, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(gameLayout, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(enter, Alignment.MIDDLE_CENTER);
		layout.setSpacing(true);
		Resource res = new ThemeResource("images/shadowsLogo.jpg");
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
		deleteEntry = new Button("Delete this entry");
		cancelEdit = new Button("Cancel");
		editForm.addComponent(selectedEntry);
		editForm.addComponent(deleteEntry);
		editForm.addComponent(cancelEdit);
		reviewData.addComponent(editForm);
		Button genCsv = new Button("Generate CSV",new Button.ClickListener() {
			public void buttonClick(Button.ClickEvent event) {
				String content ="Date,Last Name,FirstName,ID,Cohort,PD,\n";
				for(Object l:table.getItemIds()){
					LogEntry e = (LogEntry)l;
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
				table.setCaption("Tabletop Club,"+date.getValue());
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
		final TextField id= new TextField("ID #");
		id.addValidator(new RegexpValidator("[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]", "This is not a valid ID # (must be nine digits)"));
		final TextField cohort= new TextField("Cohort");
		cohort.addValidator(new RegexpValidator("2[0-9][0-9][0-9]", "This is not a valid cohort year"));
		
		id.setImmediate(true);
		cohort.setImmediate(true);
		
		final Button add = new Button("Add");
		add.setWidth("50%");
		add.setDisableOnClick(true);
		add.addClickListener(new ClickListener(){
			private static final long serialVersionUID = -73154695086517200L;

			public void buttonClick(final ClickEvent event) {
				try{
					cohort.validate();
					id.validate();
					lastName.validate();
					firstName.validate();
					long newOsis = Long.parseLong(id.getValue());
					int year = Integer.parseInt(cohort.getValue());
					boolean alreadyCreated = false;
					for(Teacher s:roster.getItemIds()){
						if(s.getIdentifier()==newOsis){
							alreadyCreated=true;
							break;
						}
					}
					if(!alreadyCreated){
						Teacher created = new Teacher(lastName.getValue(), firstName.getValue(), newOsis,year);
						roster.addBean(created);
						name.select(created);
						nameWindow.close();
					}else{
						Notification alreadyEntered = new Notification("Teacher Already Listed");
						alreadyEntered.setStyleName("error");
						alreadyEntered.show(Page.getCurrent());
					}
				}catch(Exception e){
					Notification alreadyEntered = new Notification("Please enter valid first name, last name, and osis number.");
					alreadyEntered.setStyleName("error");
					alreadyEntered.show(Page.getCurrent());
					add.setEnabled(true);
				}

			}
		});
		subContent.addComponent(firstName);
		subContent.addComponent(lastName);
		subContent.addComponent(cohort);
		subContent.addComponent(id);
		subContent.addComponent(add);
		subContent.setComponentAlignment(add, Alignment.MIDDLE_CENTER);
		nameWindow.center();
		nameWindow.addCloseListener(new CloseListener() {
			
			@Override
			public void windowClose(CloseEvent e) {
				lastName.setValue("");
				firstName.setValue("");
				id.setValue("");
				cohort.setValue("");
				add.setEnabled(true);
			}
		});
	}

	private void setPDEntry(){
		pdWindow = new Window("Add a new PD for today. (with no registered attendees)");
		FormLayout subContent = new FormLayout();
		subContent.setMargin(true);
		pdWindow.setContent(subContent);
		pdWindow.setWidth("40%");

		// Put some components in it
		final TextField gameTitle = new TextField("PD Title");
		gameTitle.setWidth("90%");
		Button add = new Button("Add");
		add.setWidth("50%");
		add.addClickListener(new ClickListener(){
			private static final long serialVersionUID = -73154694083517200L;

			public void buttonClick(final ClickEvent event) {
				String newTitle = gameTitle.getValue();
				boolean distinct = true;
				for(PD g:pds.getItemIds()){
					if(g.getTitle().equals(newTitle)){
						distinct=false;
						break;
					}
				}
				if(distinct){
					PD created = new PD(newTitle, new ArrayList<Teacher>(), new Date());
					pds.addBean(created);
					game.select(created);
					pdWindow.close();
				}else{
					Notification alreadyEntered = new Notification("PD Already Listed");
					alreadyEntered.setStyleName("error");
					alreadyEntered.show(Page.getCurrent());
				}
			}
		});

		subContent.addComponent(gameTitle);
		subContent.addComponent(add);
		subContent.setComponentAlignment(add, Alignment.MIDDLE_CENTER);
		
		pdWindow.center();
	}

	public void addListeners(){
		addName.addClickListener(new ClickListener(){
			private static final long serialVersionUID = -73954695086117200L;

			public void buttonClick(final ClickEvent event) {
				addWindow(nameWindow);
			}
		});
		addPD.addClickListener(new ClickListener(){
			private static final long serialVersionUID = -73954695186177200L;

			public void buttonClick(final ClickEvent event) {
				addWindow(pdWindow);
			}
		});
		enter.addClickListener(new ClickListener(){
			private static final long serialVersionUID = -73954695086217200L;

			public void buttonClick(final ClickEvent event) {
				if(name.getValue()!=null){
					if(game.getValue()!=null){
						boolean distinct=true;
						for(Object l:table.getItemIds()){
							LogEntry le = (LogEntry)l;
							if(le.matches((Teacher)(name.getValue()))){
								distinct = false;
								break;
							}
						}
						if(!distinct){
							Notification duplicate = new Notification("Duplicate Entry.","A teacher with this ID # has already logged in today.");
							duplicate.setStyleName("error");
							duplicate.show(Page.getCurrent());
						}else{
							entries.addItem(new LogEntry(date.getValue(), (Teacher)name.getValue(), (PD)game.getValue()));
							table.commit();
							entryIndex++;
						}
					}else{
						Notification selectPD = new Notification("Please select a game.","If your game is not listed, please add it by clicking the 'Add new game' button.");
						selectPD.setStyleName("error");
						selectPD.show(Page.getCurrent());
					}
				}else{

					Notification selectName = new Notification("Please select your name.","If your name is not listed, please add it by clicking the 'Add new name' button.");
					selectName.setStyleName("error");
					selectName.show(Page.getCurrent());
				}
			}
		});
		// When the user selects an item, show it in the form
		deleteEntry.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				entries.removeItem(table.getValue());
				table.commit();
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
				LogEntry selected = (LogEntry)table.getValue();
				selectedEntry.setValue(selected.getTeacher()+" participating in "+selected.getPD());
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