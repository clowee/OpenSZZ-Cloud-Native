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

	private final Path fileStoragePath;

	private Git git = null;
	private final Pattern pGit = Pattern.compile(".+\\.git$");

	public Storage() {
	    this.fileStoragePath = Paths.get(Application.getWorkingDirectory());
    }


	/**
	 * Gets a list of presumed bug-fixing-commits
	 * @param url
	 * @param projectName
     * @param searchQuery may be null
	 * @return
	 */
	public List<Transaction> checkoutCvs(URL url, String projectName, String searchQuery) {
		List<Transaction> list = new ArrayList<Transaction>();
		List<Transaction> result = new ArrayList<Transaction>();
		Matcher mGit = pGit.matcher(url.toString());
		if(mGit.find()) {
			this.git = new Git(fileStoragePath, url);
			try {
				File bugFixingCommitsFile = new File(Application.getWorkingDirectory() + File.separator + projectName + ".txt");
				if(!bugFixingCommitsFile.exists()) {
					this.git.cloneRepository();
					this.git.pullUpdates();
					this.git.saveLog();
				}
				list = git.getCommits();
				for (Transaction t : list) {
				    Boolean commitPresumedFixingCommit = searchQuery == null
                        ? isCommitPresumedFixingByIssueReference(t.getComment(),projectName)
                        : isCommitPresumedFixingBySearchQuery(t.getComment(), searchQuery);
				    if (commitPresumedFixingCommit) {
                        result.add(t);
                    }
				}
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
	private boolean isCommitPresumedFixingByIssueReference(String comment, String projectName){
	    String pattern = projectName.toLowerCase()+"[ ]*-[ ]*[0-9]+";
	    Pattern r = Pattern.compile(pattern);
	    Matcher m = r.matcher(comment);
	    return m.find();
	}

    /**
     * It controls whether it contains at least a Jira issue
     * @param comment
     * @param searchQuery
     * @return
     */
    private boolean isCommitPresumedFixingBySearchQuery(String comment, String searchQuery){
        Pattern r = Pattern.compile(searchQuery, Pattern.CASE_INSENSITIVE);
        Matcher m = r.matcher(comment);
        return m.find();
    }

	public Git getGit(){
		return this.git;
	}
}
