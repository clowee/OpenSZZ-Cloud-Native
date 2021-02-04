package com.rest.szz.entities;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.rest.szz.git.*;


public class Storage {
	
	private Path fileStoragePath = Paths.get(
			System.getProperty("user.dir") + File.separator + "home"
	);
	
	{
		// Create working directory if it does not exist.
		fileStoragePath.toFile().mkdirs();
	}
	
	private Git git = null;
	private final Pattern pGit = Pattern.compile(".+\\.git$");
	
	public Storage(String projectName) {
		fileStoragePath = Paths.get(
				System.getProperty("user.dir") + File.separator + "home"
				);
	}
	
	
	/**
	 * Gets a list of presumed bug-fixing-commits
	 * @param url
	 * @param projectName
	 * @return
	 */
	public List<Transaction> checkoutCvs(URL url, String projectName) {
		List<Transaction> list = new ArrayList<Transaction>();
		List<Transaction> result = new ArrayList<Transaction>();
		Matcher mGit = pGit.matcher(url.toString());
		if(mGit.find()) {
			this.git = new Git(fileStoragePath, url);
			try {
				File bugFixingCommitsFile = new File("home" + File.separator + projectName + ".txt");
				if(!bugFixingCommitsFile.exists()) {
					this.git.cloneRepository();
					this.git.pullUpdates();
					this.git.saveLog();
				}
				list = git.getCommits();
				for (Transaction t : list){
					if (isBugPresumedFixing(t.getComment(),projectName))
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
	private boolean isBugPresumedFixing(String comment, String projectName){
	    String pattern = projectName.toLowerCase()+"[ ]*-[ ]*[0-9]+";
	    Pattern r = Pattern.compile(pattern);
	    Matcher m = r.matcher(comment);
	    return m.find();
	}
	
	public Git getGit(){
		return this.git;
	}
}
