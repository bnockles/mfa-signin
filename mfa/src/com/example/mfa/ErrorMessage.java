package com.example.mfa;

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;

public class ErrorMessage extends Notification {

	/**
	 * 
	 */
	private static final long serialVersionUID = -589343688255644283L;

	public ErrorMessage(String caption) {
		super(caption);
		// TODO Auto-generated constructor stub
	}

	public ErrorMessage(String caption, Type type) {
		super(caption, type);
		setStyleName("error");
		setDelayMsec(3000);
		show(Page.getCurrent());
	}

	public ErrorMessage(String caption, String description) {
		super(caption, description);
		setStyleName("error");
		setDelayMsec(3000);
		show(Page.getCurrent());
	}

	public ErrorMessage(String caption, String description, Type type) {
		super(caption, description, type);
		// TODO Auto-generated constructor stub
	}

	public ErrorMessage(String caption, String description, Type type, boolean htmlContentAllowed) {
		super(caption, description, type, htmlContentAllowed);
		// TODO Auto-generated constructor stub
	}

}
