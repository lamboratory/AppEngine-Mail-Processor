package com.lamboratory.mail;

import javax.mail.internet.MimeMessage;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;

import java.io.InputStream;
import java.io.BufferedInputStream;

public class MailUtils {

	/**
	 * Get the contents of the mail as a String
	 */
	public static String getContentsAsString(MimeMessage message) throws Exception {
		return getObjectAsString(message.getContent());
	}

	/**
	 * Get the contents of an object as a String, recursively for some special types of objects
	 */
	public static String getObjectAsString(Object o) throws Exception {
		if(o instanceof String) {
			return (String)o;
		} else if(o instanceof InputStream) {
			InputStream is = (InputStream)o;
			BufferedInputStream bis = new BufferedInputStream((InputStream)o);
			int c;
			StringBuilder sb = new StringBuilder();
			while((c = bis.read()) != -1) {
				sb.append(c);
			}
			return sb.toString();
		} else if (o instanceof Multipart) {
			Multipart mp = (Multipart)o;

			int count = mp.getCount();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < count; i++) {
				sb.append(getObjectAsString(mp.getBodyPart(i)));
			}
			return sb.toString();
		} else if (o instanceof MimeBodyPart) {
			return getObjectAsString(((MimeBodyPart)o).getContent());
		}
		return o.getClass().getName();
	}
}
