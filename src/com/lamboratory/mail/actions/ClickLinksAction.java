package com.lamboratory.mail.actions;

import com.lamboratory.mail.actions.Action;

import com.lamboratory.mail.MailUtils;

import java.util.logging.Logger;
import javax.mail.internet.MimeMessage;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ClickLinksAction implements Action {

	public static final Logger _log = Logger.getLogger("ClickLinksAction");

	// Default Pattern, extend this class to use a different one
	protected static final Pattern pattern = Pattern.compile("(https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])", Pattern.DOTALL);

	/**
	 * Execute the Action specific logic
	 * This Action looks for all links in a mail and request them and return the contents retrieved
	 * Note: This Action will click all links found by the Pattern in the mail, including possible unwanted links like unsubscription
	 */
	public String execute(MimeMessage message) throws Exception {

		String contents = MailUtils.getContentsAsString(message);

		boolean urlFound = false;
		StringBuilder response = new StringBuilder();
		Matcher matcher = getPattern().matcher(contents);

		while(matcher.find()) {
			urlFound = true;

			String sUrl = matcher.group();
			_log.info("Matching URL found: \""+sUrl+"\"");

			response.append("Matching URL found: \""+sUrl+"\"\n");
			response.append("Response from URL: \n\n");

			try {
				URL url = new URL(sUrl);
				BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					response.append(line);
				}
				reader.close();
			} catch(Exception e) {
				response.append("Error during request: "+e.getMessage());
			} finally {
				response.append("\n\n");
			}
		}
		if(urlFound) {
			return response.toString();
		} else {
			return "ClickLinksAction action didn't match RegExp\ncontents: "+contents;
		}
	}

	/**
	 * Pattern to match URLs
	 * Extend this class and return a different Pattern to request only some of the URLs in the mail
	 */
	protected Pattern getPattern() {
		return pattern;
	}
}

