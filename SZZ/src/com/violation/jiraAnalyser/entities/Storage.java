package com.violation.jiraAnalyser.entities;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.violation.jiraAnalyser.git.*;

public class Storage {
	
	private Path FILE_STORAGE_PATH;
	
	private Git git = null;
	private final Pattern pGit = Pattern.compile(".+\\.git$");
	
	public Storage(String projectName) {
		FILE_STORAGE_PATH =  Paths.get("./extraction/"+projectName);
	}
	
	
	/**
	 * Gets a list of presumed bug-fixing-commits
	 * @param url
	 * @param projectName
	 * @return
	 */
	public List<Transaction> checkoutCvs(URL url, String projectName, String jiraKey) {
		List<Transaction> list = new ArrayList<Transaction>();
		List<Transaction> result = new ArrayList<Transaction>();
		Matcher mGit = pGit.matcher(url.toString());
		if(mGit.find()) {
			this.git = new Git(FILE_STORAGE_PATH, url, projectName);
			try {
				this.git.cloneRepository();
				//this.git.pullUpdates();

				
				this.git.saveLog();
				
				list = git.getCommits();
				for (Transaction t : list){
					if (isBugPresumedFixing(t.getComment(),jiraKey))
						result.add(t);}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	/**
	 * It controls whether it contains at least a Jira issue
	 * @param comment
	 * @param projectName
	 * @return
	 */
	private boolean isBugPresumedFixing(String comment, String jiraKey){
	    String pattern = jiraKey.toLowerCase()+"[ ]*-[ ]*[0-9]+";
	    Pattern r = Pattern.compile(pattern);
	    Matcher m = r.matcher(comment);
	    return m.find();
	}
	
	public Git getGit(){
		return this.git;
	}
}
