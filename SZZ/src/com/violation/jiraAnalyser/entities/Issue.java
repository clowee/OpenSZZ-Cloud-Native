package com.violation.jiraAnalyser.entities;


import java.util.List;

/*
 * This object is a representation of a Bug in Jira. 
 * It has as parameters a status/resolution enum, an id, a short description
 * open timestamp, last modification time stamp, assigner 
 * 
 */

public class Issue {

	public enum Status {
		UNCONFIRMED,
		NEW,
		ASSIGNED,
		REOPENED,
		RESOLVED,
		VERIFIED,
		CLOSED
	}
	
	public enum Resolution {
		NONE,
		FIXED,
		INVALID,
		WONTFIX,
		DUPLICATE,
		WORKSFORME,
		LATER,
		REMIND,
		INCOMPLETE,
		IMPLEMENTED
	}
		
	private  long id;
	private  Status status;
	private  Resolution resolution;
	private  String assignedTo;
	private  long open;
	private  long close;
	private  List<String> attachments;
	private  List<String> comments;
	private  String title;
	private String type;
	private String priority;
	
	public Issue(
			long id,String title,Status status,
			Resolution resolution, String assignedTo,
			long open,long close,List<String> attachments ,List<String> comments, String type) {
		this.id = id;
		this.status = status;
		this.resolution = resolution;
		this.assignedTo = assignedTo;
		this.open = open;
		this.title = title;
		this.close = close;
		this.comments = comments;
		this.attachments = attachments;
		this.type = type;
	}
	
	public long getId(){
		return this.id;
	}
	
	public String getType(){
		return type;
	}
	
	public Status getStatus(){
		return this.status;
	}
	
	public Resolution getResolution(){
		return this.resolution;
	}
	
	public String getAssigned(){
		return this.assignedTo;
	}
	
	public long getOpen(){
		return this.open;
	}
	
	public long getClose(){
		return this.close;
	}
	
	public List<String> getComments(){
		return this.comments;
	}
	public List<String> getAttachments(){
		return this.attachments;
	}
	
	public String getTitle(){
		return this.title;
	}
	
	@Override
	public String toString(){
		return 
		"CommitId:    " + this.id + 
		"Status:      " + this.status +
		"Resolution:  " + this.resolution +
		"AssignedTo:  " + this.assignedTo +
		"OpenTime:    " + this.open +
		"LastMod:     " + this.close;
	}
	
	

}
