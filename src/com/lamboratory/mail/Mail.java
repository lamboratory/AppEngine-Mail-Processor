package com.lamboratory.mail;

import java.io.IOException;

import javax.mail.Address;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import javax.servlet.http.*;

import java.lang.Character;
import java.lang.StringBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import java.util.logging.Logger;

import com.lamboratory.mail.actions.Action;

public class Mail extends HttpServlet {

	public static final Logger _log = Logger.getLogger("Mail");

	// Default package and suffix for Action classes
	public static final String DEFAULT_ACTION_PACKAGE = "com.lamboratory.mail.actions.";
	public static final String DEFAULT_ACTION_SUFFIX = "Action";

	// Address to send reply messages from
	// Note: This must be an administrator of your App Engine application
	// 	Set this to one of your administrator emails in your App Engine application or mails will not be delivered
	public static final String EMAIL = "appenginemailprocessor@gmail.com"; // TODO: Set a valid administrator

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
	}

	/**
	 * Process a mail and send the response back
	 * Load an Action based on the mail received, execute it and send the response back to the user
	 * Actions should be developed using com.lamboratory.mail.actions.Action interface
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException { 
		try {
			// Parse received mail
			Properties props = new Properties(); 
			Session session = Session.getDefaultInstance(props, null); 
			MimeMessage message = new MimeMessage(session, req.getInputStream());

			// Log mail information
			_log.info("Got an email. Subject = " + message.getSubject());
			List<Header> headers = Collections.list(message.getAllHeaders());
			for(Header h : headers) {
				_log.info("	Header: "+h.getName()+" - "+h.getValue());
			}

			Address[] from = message.getFrom();
			for(int i=0; i<from.length; i++) {
				if(from[i].toString().indexOf(EMAIL)>=0) {
					// Do not loop over already processed emails
					throw new Exception("Mail loop - Unable to process mails delivered by the same application");
				}
			}

			// Load and execute the action
			Action action = getAction(message);
			String responseText = action.execute(message);

			// Prepare response mail
			Message msg = message.reply(false);
			msg.setFrom(new InternetAddress(EMAIL)); // This must be an administrator of the App Engine application
			Address[] to = new Address[1];
			to[0] = new InternetAddress(message.getHeader("Delivered-To", null));
			msg.setRecipients(Message.RecipientType.TO, to);

			// These headers should make the response mail part of the same thread in conversation view
			// They are currently removed by App Engine:
			// http://code.google.com/p/googleappengine/issues/detail?id=2802
			// http://groups.google.com/group/google-appengine/browse_thread/thread/b80737b41f53541f
			msg.setHeader("In-Reply-To", message.getHeader("Message-Id", null));
			msg.setHeader("References", message.getHeader("Message-Id", null));

			msg.setText(responseText);

			// Log response mail information
			List<Header> responseHeaders = Collections.list(msg.getAllHeaders());
			for(Header h : responseHeaders) {
				_log.info("	Reply Header: "+h.getName()+" - "+h.getValue());
			}

			// Send response message
			Transport.send(msg);
			_log.info("Message sent");

		} catch(Exception e) {
			_log.info("Error processing mail: "+e.getMessage());
		}
	}

	/**
	 * Get the Action class that should process the message
	 * Action is loaded based on the address to where the email was forwarded (based on "X-Forwarded-To" header)
	 * Forward address can be set as:
	 *	my_example to execute com.lamboratory.mail.actions.MyExampleAction
	 *	Fully Qualified Name of the Action class to load
	 */
	protected Action getAction(MimeMessage message) throws Exception {
		String sEmail = message.getHeader("X-Forwarded-To", null);
		if(sEmail==null) {
			throw new Exception("Unknown action - no email found");
		}
		String sActionId = sEmail.substring(0, sEmail.indexOf('@'));

		Class c = null;
		if(sActionId.indexOf('.')>0) { // Fully Qualified Name of Action class
			_log.info("Using class: "+sActionId);
			c = Class.forName(sActionId);
		} else { // Default package for Action classes
			String className = DEFAULT_ACTION_PACKAGE+underscoreToCamelCase(sActionId)+DEFAULT_ACTION_SUFFIX;
			_log.info("Using class: "+className);
			c = Class.forName(className);
		}
		return (Action)c.newInstance();
	}

	/**
	 * Transform the input String in underscore format (example_string) to camel case format (ExampleString)
	 * Used to correctly find the Action class to execute using naming convention
	 */
	protected String underscoreToCamelCase(String s) {
		StringBuilder sb = new StringBuilder(s.length());
		String[] split = s.split("_");
		for(int i=0; i<split.length; i++) {
			sb.append(Character.toUpperCase(split[i].charAt(0)));
			sb.append(split[i].substring(1).toLowerCase());
		}
		return sb.toString();
	}
}
