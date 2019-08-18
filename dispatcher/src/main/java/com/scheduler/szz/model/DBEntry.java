package com.scheduler.szz.model;

public class DBEntry extends BaseEntity {
	
	public enum Status{
		ANALYSED,
		PROCESSING,
		ERROR;
	}
	
	private String email;
	private String jiraUrl;
	private String token;
	private String projectName;
	private String ipAddress;
	private String gitUrl;
	private Status status;
	private long startEpoch;
	private long endEpoch;
	
	public long getStartEpoch() {
		return startEpoch;
	}
	public void setStartEpoch(long epoch) {
		this.startEpoch = epoch;
	}
	
	public long getEndEpoch() {
		return endEpoch;
	}
	public void setEndEpoch(long epoch) {
		this.endEpoch = epoch;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getJiraUrl() {
		return jiraUrl;
	}
	public void setJiraUrl(String jiraUrl) {
		this.jiraUrl = jiraUrl;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAdress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getGitUrl() {
		return gitUrl;
	}
	public void setGitUrl(String gitUrl) {
		this.gitUrl = gitUrl;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
}
