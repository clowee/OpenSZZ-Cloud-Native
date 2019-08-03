package com.scheduler.szz.model;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Class for sending emails
 * @author LucaPellegrini
 *
 */
public class Email {
	
	final String username = System.getenv("EMAIL");
	final String password = System.getenv("PASS");
	private String projectName = "";
	private String emailTo  = "";
	private String token = "";
	private String urlWebService = "";
	
	private String htmlText = "";

    // Get system properties
    private Properties props = new Properties();
   
    // Get the default Session object.
    Session session = Session.getDefaultInstance(props);
	
	public Email(String emailTo, String token, String projectName, String urlWebService){
		this.emailTo = emailTo;
		this.projectName = projectName;
		this.token = token;
		this.urlWebService = urlWebService;
		
		props.put("mail.smtp.auth", "true");
	    props.put("mail.smtp.starttls.enable", "true");
	    props.put("mail.smtp.host", "smtp.gmail.com");
	    props.put("mail.smtp.port", "587");
	      
	    session = Session.getInstance(props,
	  		  new javax.mail.Authenticator() {
	  			protected PasswordAuthentication getPasswordAuthentication() {
	  				return new PasswordAuthentication(username, password);
	  			}
	  		  });
		
	}
	
	public boolean sentEmail(){
	      try {
	    	       htmlText = "<p>The project " +projectName+ " "
	    	       		+ "you submitted has been analysed.</p> <div>"
	    	       		+ "<p>Here you can download the csv file containing the BugInducingCommits of the project."
	    	       		+ "</p></div><div><p>You can download the csv using the token "+token+" or with this link "
	    	       		+ "<a href=\""+urlWebService+"\">Link</a></p></div>";
	    	 
	          // Create a default MimeMessage object.
	          MimeMessage message = new MimeMessage(session);

	          // Set From: header field of the header.
	          message.setFrom(new InternetAddress(username));

	          // Set To: header field of the header.
	          message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailTo));

	          // Set Subject: header field
	          message.setSubject("Analysis project " + projectName);
	          message.setContent(htmlText, "text/html; charset=utf-8");
	          

	          // Send message
	          Transport.send(message);
	          System.out.println("Sent message successfully....");
	       } catch (MessagingException mex) {
	          mex.printStackTrace();
	          return false;
	       }
		return true;
	}
	
	
	
}
