package org.openszzweb.samples.app.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

@Entity
public class Analysis extends BaseEntity {

	private String jiraUrl;
	private String gitUrl;
	private String projectName;
	private String email;
	private Boolean useJira = true;
	private String searchQuery;
	private String message;
	private String status;
	private String token;
	private String tokenURL;
	private long dateStart;
	private long dateEnd;

	public long getDateStart() {
		return dateStart;
	}
	public void setDateStart(long dateStart) {
		this.dateStart = dateStart;
	}
	public long getDateEnd() {
		return dateEnd;
	}
	public void setDateEnd(long dateEnd) {
		this.dateEnd = dateEnd;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}

	public String getTokenUrl() {
		return tokenURL;
	}
	public void setTokenUrl(String tokenURL) {
		this.tokenURL = tokenURL;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
    public String getJiraUrl() {
        return jiraUrl;
    }
    public void setJiraUrl(String jiraUrl) {
        this.jiraUrl = jiraUrl;
    }
    public Boolean getUseJira() {
        return useJira;
    }
    public void setUseJira(Boolean useJira) {
        this.useJira = useJira;
    }
    public String getSearchQuery() {
        return searchQuery;
    }
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
	public String getGitUrl() {
		return gitUrl;
	}
	public void setGitUrl(String gitUrl) {
		this.gitUrl = gitUrl;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
}
