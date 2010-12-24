package com.lamboratory.mail.actions;

import javax.mail.internet.MimeMessage;

public interface Action {

	/**
	 * Execute the action specific code
	 * This method should execute the specific logic for the Action
	 * The returned String will be sent back to the user who forwarded the mail to the application
	 */
	public String execute(MimeMessage message) throws Exception;
}
