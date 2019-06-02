package com.scheduler.szz.model;

public class DBEntry {
	
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
	private long epoch;
	
	public long getEpoch() {
		return epoch;
	}
	public void setEpoch(long epoch) {
		this.epoch = epoch;
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
