package com.SZZ.jiraAnalyser.entities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import org.eclipse.jgit.revwalk.RevCommit;

import com.SZZ.jiraAnalyser.git.Git;
import com.SZZ.jiraAnalyser.entities.*;
import com.SZZ.jiraAnalyser.entities.Issue.Resolution;
import com.SZZ.jiraAnalyser.entities.Issue.Status;
import com.SZZ.jiraAnalyser.entities.Transaction.FileInfo;

public class Link {

	public final long number;
	public final Transaction transaction;
	
	public Issue issue = null;

	private int syntacticConfidence = 0;

	private int semanticConfidence = 0;

	private List<Suspect> suspects = new LinkedList<Suspect>();
	
	private String jiraKey; 
	


	/**
	 * Object Representation of a Link Transaction, commit found from the log
	 * files Corresponding bug in Bugzilla The number, who links the transition
	 * with the bug
	 * 
	 * @param t
	 * @param b
	 * @param number
	 */
	public Link(Transaction t, long number, String jiraKey) {
		this.transaction = t;
		this.jiraKey = jiraKey;
		this.number = number;
		this.setBug();
		this.setSyntacticConfidence();
		this.setSemanticConfidence();
		
	}

	public List<Suspect> getSuspects() {
		return suspects;
	}

	/**
	 * It sets the syntacticConfidence a number between 0 - 2. 1. The number is
	 * a bug number 2. The log message contains a keyword,or the log message
	 * contains only plain or bug numbers
	 */
	private void setSyntacticConfidence() {
		if (isBugInJira())
			this.syntacticConfidence++;
		if (containsKeywords())
			this.syntacticConfidence++;
	}

	/**
	 * It sets the semanticConfidence
	 */
	private void setSemanticConfidence() {
		if (this.issue != null) {
			// The bug b has been resolved as FIXED at least once.
			if (this.issue.getResolution().equals(Resolution.FIXED))
				this.semanticConfidence++;
			// The author of the transaction t has been assigned to the bug b
			if (this.issue.getAssigned().equals(transaction.getAuthor()))
				this.semanticConfidence++;
			// One or more of the files affected by the transaction t have been
			// attached to the bug b.
			if (this.checkAttachments())
				this.semanticConfidence++;
			// The short description of the bug report b is contained in the log
			// message of the transaction t
			String e = longestCommonSubstrings(transaction.getComment().toLowerCase(), issue.getTitle().toLowerCase());
			if (e.length() > 20)
				this.semanticConfidence++;
		}
	}

	public static String longestCommonSubstrings(String s, String t) {
		int[][] table = new int[s.length()][t.length()];
		int longest = 0;
		Set<String> result = new HashSet<>();

		for (int i = 0; i < s.length(); i++) {
			for (int j = 0; j < t.length(); j++) {
				if (s.charAt(i) != t.charAt(j)) {
					continue;
				}

				table[i][j] = (i == 0 || j == 0) ? 1 : 1 + table[i - 1][j - 1];
				if (table[i][j] > longest) {
					longest = table[i][j];
					result.clear();
				}
				if (table[i][j] == longest) {
					result.add(s.substring(i - longest + 1, i + 1));
				}
			}
		}
		return result.toString();
	}

	/**
	 * It checks whether one or more of the files affected by the transaction t
	 * have been attached to the bug b
	 * 
	 * @return
	 */
	private boolean checkAttachments() {
		List<FileInfo> tFiles = transaction.getFiles();
		List<String> bugFiles = issue.getAttachments();

		for (FileInfo f : tFiles) {
			File p = new File(f.filename);
			String name = p.getName();
			for (String bugFile : bugFiles) {
				if (bugFile.equals(name))
					return true;
			}

		}
		return false;
	}

	/**
	 * It checks whether a bug has been fixed at least once
	 * 
	 * @return
	 */
	/*
	 * private boolean checkResolution(){ try (BufferedReader br = new
	 * BufferedReader(new FileReader(("stati.txt")))) { String sCurrentLine;
	 * while ((sCurrentLine = br.readLine()) != null) { if
	 * (sCurrentLine.startsWith(number+"")){ if (sCurrentLine.contains("FIXED"))
	 * return true; else return false; } } } catch (IOException e) {
	 * e.printStackTrace();
	 * 
	 * }
	 * 
	 * return false; }
	 */

	/**
	 * It checks whether the number is really a bug.
	 * 
	 * @return true false
	 */
	private boolean isBugInJira() {
		boolean result = false;
		if (issue == null)
			return false;
		else
			result = true;
		return result;
	}

	/**
	 * It gets from Jira Log File
	 * 
	 * @return true false
	 */
	private void setBug() {
		try (BufferedReader br = new BufferedReader(new FileReader(("faults.csv")))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				sCurrentLine = sCurrentLine.replaceAll("\"", "");
				if (sCurrentLine.startsWith(jiraKey + "-" + number)) {
					String[] s = sCurrentLine.split(",");
					List<String> comments = new LinkedList<String>();
					List<String> attachments = null;
					try{
						attachments = 		Arrays.asList(s[7].replace("[", "").replace("]", ""));
					}
					catch(Exception e){
						e.printStackTrace();
					}
					int i = 8;
					while (i < s.length) {
						comments.add(s[i]);
						i++;
					}
					Status status = Status.UNCONFIRMED;
					Resolution resolution = Resolution.NONE;
					
					try{
						Status.valueOf(s[3].toUpperCase());
					}
					catch(Exception e){
						status = Status.UNCONFIRMED;
					}
					
					try{
						Resolution.valueOf(s[2].toUpperCase().replace(" ", "").replace("'", ""));
					}
					catch(Exception e){
						 resolution = Resolution.NONE;
					}
					
					if  (s.length>4){
					issue = new Issue(number, s[1], status,resolution, s[4],
							Long.parseLong(s[5]), Long.parseLong(s[6]),attachments, comments,s[7]);
					}
					
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean containsKeywords() {
		String comment = transaction.getComment().toLowerCase();
		Pattern patter1 = Pattern.compile("fix(e[ds])?|bugs?|defects?|patch", Pattern.CASE_INSENSITIVE);
		Pattern patter2 = Pattern.compile("^[0-9]*$", Pattern.CASE_INSENSITIVE);
		Matcher p1 = patter1.matcher(comment.toLowerCase());
		Matcher p2 = patter2.matcher(comment);
		boolean b1 = p1.find();
		boolean b2 = p2.find();
		if (b1 || b2)
			return true;
		else
			return false;
	}

	public int getSyntacticConfidence() {
		return this.syntacticConfidence;
	}

	public int getSemanticConfidence() {
		return this.semanticConfidence;
	}

	/**
	 * For each modified file it calculates the suspect
	 * 
	 * @param git
	 */
	public void calculateSuspects(Git git, Logger l) {
		for (FileInfo fi : transaction.getFiles()) {
			if (fi.filename.endsWith(".java")) {
					String diff = git.getDiff(transaction.getId(), fi.filename, l);
					if (diff == null)
						break;
					List<Integer> linesMinus = git.getLinesMinus(diff);
					if (linesMinus == null)
						return;
					if (linesMinus.size() == 0)
						return;
					String previousCommit = git.getPreviousCommit(transaction.getId(), fi.filename,l);
					if (previousCommit != null) {
						Suspect s = getSuspect(previousCommit, git, fi.filename, linesMinus,l);
						if (s != null)
							this.suspects.add(s);
					}
			}
		}
	}

	/**
	 * It gets the commit closest to the Bug Open reposrt date
	 * 
	 * @param previous
	 * @param git
	 * @param fileName
	 * @param linesMinus
	 * @return
	 */
	private Suspect getSuspect(String previous, Git git, String fileName, List<Integer> linesMinus, Logger l) {
    	RevCommit closestCommit = null; 
    	long tempDifference = Long.MAX_VALUE; 
    	for (int i : linesMinus){ 
    		try{ 
    			String sha = git.getBlameAt(previous,fileName,l,i); 
    			if (sha == null)
    				break;
    			RevCommit commit = git.getCommit(sha,l); 
    			long difference =(issue.getOpen()/1000) - (commit.getCommitTime()); 
    			if (difference > 0){ 
    				if (difference < tempDifference ){
    					closestCommit = commit; 
    					tempDifference = difference; } 
    				}
    			} catch (Exception e){ 
    				e.printStackTrace();
    				l.error(e);
    			}
    	} 
    	if (closestCommit != null){ 
    		Long temp = Long.parseLong(closestCommit.getCommitTime()+"") * 1000; 
    		Suspect s = new Suspect(closestCommit.getName(), new Date(temp), fileName);
    	return s; 
    	}
  
		return null;
	}

}
