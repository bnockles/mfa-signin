package com.example.mfa;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class TextWindow extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8991464406677409903L;
	VerticalLayout layout;
	TextArea area;
	
	/**
	 * 
	 */
	public TextWindow() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param caption
	 */
	public TextWindow(String caption, String percentWidth, String percentHeight, String content) {
		super(caption);
		setWidth(percentWidth);
		setHeight(percentHeight);
		layout = new VerticalLayout();
		area = new TextArea();
		area.setValue(content);
		layout.addComponent(area);
		layout.setExpandRatio(area, 1.0f);
		area.setSizeFull();
		area.setWordwrap(false);
		layout.setSizeFull();
		setContent(layout);
	}
	
	public void addFeature(Component c){
		layout.addComponent(c);
	}
	
	public void setFeatureAlignment(Component c, Alignment a){
		layout.setComponentAlignment(c, a);
		
	}
	
	public void setText(String text){
		area.setValue(text);
	}

	public void setWordwrap(boolean b){
		area.setWordwrap(b);
	}
	
	/**
	 * @param caption
	 * @param content
	 */
	public TextWindow(String caption, Component content) {
		super(caption, content);
		
	}
}
