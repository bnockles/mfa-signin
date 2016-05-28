package com.example.mfa;

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;

public class DebugNote extends Notification {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3423545013257403372L;

	public DebugNote(String caption) {
		super(caption, Type.TRAY_NOTIFICATION);
		setDelayMsec(2000);
		show(Page.getCurrent());
	}

	public DebugNote(String caption, Type type) {
		super(caption, type);
		// TODO Auto-generated constructor stub
	}

	public DebugNote(String caption, String description) {
		super(caption, description);
		// TODO Auto-generated constructor stub
	}

	public DebugNote(String caption, String description, Type type) {
		super(caption, description, type);
		// TODO Auto-generated constructor stub
	}

	public DebugNote(String caption, String description, Type type, boolean htmlContentAllowed) {
		super(caption, description, type, htmlContentAllowed);
		// TODO Auto-generated constructor stub
	}

}
