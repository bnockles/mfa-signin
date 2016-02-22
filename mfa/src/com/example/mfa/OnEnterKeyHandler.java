package com.example.mfa;

import java.io.Serializable;

import com.vaadin.event.FieldEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.PasswordField;


public abstract class OnEnterKeyHandler implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = 4396251227346433467L;
	final ShortcutListener enterShortCut = new ShortcutListener(
			"EnterOnTextAreaShorcut", ShortcutAction.KeyCode.ENTER, null) {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2571502949794336589L;

		@Override
		public void handleAction(Object sender, Object target) {
			onEnterKeyPressed();
		}
	};

	public void installOn(final PasswordField component)
	{
		component.addFocusListener(
				new FieldEvents.FocusListener() {

					/**
					 * 
					 */
					private static final long serialVersionUID = -3029430358435679256L;

					@Override
					public void focus(FieldEvents.FocusEvent event
							) {
						component.addShortcutListener(enterShortCut);
					}

				}
				);

		component.addBlurListener(
				new FieldEvents.BlurListener() {

					/**
					 * 
					 */
					private static final long serialVersionUID = -536959356750068439L;

					@Override
					public void blur(FieldEvents.BlurEvent event
							) {
						component.removeShortcutListener(enterShortCut);
					}

				}
				);
	}

	public abstract void onEnterKeyPressed();

}
