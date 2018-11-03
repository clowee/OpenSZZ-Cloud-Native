package com.SZZ.jiraAnalyser.entities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.SZZ.jiraAnalyser.entities.Transaction.FileInfo;
import com.SZZ.jiraAnalyser.git.*;

public class Storage {
	
	private Path FILE_STORAGE_PATH;
	
	private Git git = null;
	private final Pattern pGit = Pattern.compile(".+\\.git$");
	
	public Storage() {
		
	}
	
	
	/**
	 * Gets a list of presumed bug-fixing-commits
	 * @param url
	 * @param projectName
	 * @return
	 */
	public List<Transaction> checkoutCvs(String jiraKey) {
		List<Transaction> list = getCommits(new  File("gitlog.csv"));
		List<Transaction> result = new ArrayList<Transaction>();
		for (Transaction t : list){
		if (isBugPresumedFixing(t.getComment(),jiraKey)){
			result.add(t);}}
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
	
	public List<Transaction> getCommits(File logFile) {
		List<Transaction> transactions = new ArrayList<Transaction>();
		int count = 1;
		 String line="";
		 String line1="";
		 String hashId = "";
		try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
			while ((line = br.readLine()) != null) {
		       if (!line.isEmpty() && line.startsWith("\'")){
		    	   line = line.replaceAll("\'", "");
		    	   String[] array = line.split(";");
		    	   hashId = array[0];
				   String timestamp = array[1];
				   String author = array[2];
				   String comment = array[3];
		       List<FileInfo> filesAffected = new ArrayList<FileInfo>();
		       line1 = br.readLine();
		       if (line1 != null){
		    	 
	
		       while (!(line1).equals("")){
		    	  
		    	   int BUFFER_SIZE = 100000;
		    	   br.mark(BUFFER_SIZE);
		    	   if (!line1.startsWith("\'")){
		    		   String[] subarray = line1.split("	");
		    		   String status = subarray[0];
		    		   String file = subarray[1];
		    		   FileInfo fileInfo = new FileInfo(status, file);
		    		   filesAffected.add(fileInfo);}
		    	   else{
		    		 br.reset();
		    		 break;
		    	   }
		    	   line1 = br.readLine();
		    	   
		       }
		       }
		       Transaction transaction = new Transaction(
						hashId,
						timestamp,
						author,
						comment,
						filesAffected
				);
				transactions.add(transaction);
		    }
		}
		}
		catch(Exception e){
				e.printStackTrace();
			
		}
			
		return transactions;
	}
}
