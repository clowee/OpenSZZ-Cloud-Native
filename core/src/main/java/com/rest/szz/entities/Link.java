package com.rest.szz.entities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import gr.uom.java.xmi.diff.CodeRange;
import org.eclipse.jgit.revwalk.RevCommit;

import com.rest.szz.entities.Issue.Resolution;
import com.rest.szz.entities.Transaction.FileInfo;
import com.rest.szz.git.*;


public class Link {

	public final long number;
	public final Transaction transaction;
	public Issue issue = null;
	private String projectName = "";

	private int syntacticConfidence = 0;

	private int semanticConfidence = 0;

	private List<Suspect> suspects = new LinkedList<Suspect>();



	/**
	 * Object Representation of a Link Transaction, commit found from the log
	 * files Corresponding bug in Bugzilla The number, who links the transition
	 * with the bug
	 *
	 * @param t
	 * @param number
	 * @param projectName
	 */
	public Link(Transaction t, long number, String projectName) {
		this.transaction = t;
		this.number = number;
		this.projectName = projectName;
		this.setBug();
		this.setSyntacticConfidence();
		this.setSemanticConfidence();
	}

    public Link(Transaction t, String projectName) {
        this.transaction = t;
        this.number = 0;
        this.projectName = projectName;
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
		if (LinkUtils.containsKeywords(transaction.getComment()))
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
			String e = LinkUtils.longestCommonSubstrings(transaction.getComment().toLowerCase(), issue.getTitle().toLowerCase());
			if (e.length() > 20)
				this.semanticConfidence++;
		}
	}

	/**
	 * It checks whether one or more of the files affected by the transaction t
	 * have been attached to the bug b
	 *
	 * @return
	 */
	private boolean checkAttachments() {
		List<FileInfo> tFiles = transaction.getFiles();
		Set<String> bugFiles = issue.getAttachments();

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
		if (issue == null) return false;
		return true;
	}

	/**
	 * It gets from Jira Log File
	 *
	 * @return true false
	 */
	private void setBug() {
		int page = (int)Math.floor(number / 1000);
		while(!new File(Application.getWorkingDirectory() + File.separator + projectName+"_"+page+".csv").exists()){
			page--;
		}
		try (BufferedReader br = new BufferedReader(new FileReader((Application.getWorkingDirectory() + File.separator + projectName+"_"+page+".csv")))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				sCurrentLine = sCurrentLine.replaceAll("\"", "");
				if (sCurrentLine.startsWith(projectName + "-" + number)) {
					String[] s = sCurrentLine.split(";");
					String title = s[1];
					Resolution resolution = LinkUtils.getResolutionFromString(s[2]);
					Issue.Status status = LinkUtils.getStatusFromString(s[3]);
					String assignee = s[4];
					Long createdDate = Long.parseLong(s[5]);
					Long resolvedDate = Long.parseLong(s[6]);
					String type = s[7];
					Set<String> attachments = LinkUtils.stringToSet(s[8]);
					Set<String> brokenBy = LinkUtils.stringToSet(s[9]);
                    String description = s[10].substring(1,s[10].length()-1);
					String comments = s[11].substring(1,s[11].length()-1);

					this.issue = new Issue(number, title, status, resolution, assignee, createdDate, resolvedDate, attachments, comments, type, brokenBy, description);
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	public void calculateSuspects(Git git, PrintWriter l, Boolean addAllBFCToResult, Boolean useIssueInfo, boolean ignoreCommentChanges,RefactoringMiner refactoringMiner) {
	    if (this.issue != null && useIssueInfo) {
            suspects.addAll(LinkUtils.getSuspectsByAddressedIssues(this.issue.getBrokenBy(), this.projectName + this.issue.getId(), git,"brokenBy"));
            if (this.suspects.size() > 0) return;

            suspects.addAll(LinkUtils.getSuspectsByIssueDescriptionAndComments(git, this.transaction.getId(), this.projectName, this.issue));
            if (this.suspects.size() > 0) return;
        }

        ArrayList<CodeRange> refactoringCodeRanges = new ArrayList<>();
        if (transaction.getFiles().stream().anyMatch(file -> LinkUtils.isJavaFile(file))) {
            refactoringCodeRanges = refactoringMiner.getRefactoringCodeRangesForTransaction(transaction);
        }

		for (FileInfo fi : transaction.getFiles()) {
            if (LinkUtils.isCodeFile(fi)) {
                List<Integer> linesMinus = LinkUtils.isJavaFile(fi)
                    ? LinkUtils.getLinesMinusJava(git, transaction.getId(), fi.filename, ignoreCommentChanges, l, refactoringCodeRanges)
                    : LinkUtils.getLinesMinus(git, transaction.getId(), fi.filename, ignoreCommentChanges, l);
                if (linesMinus == null || linesMinus.isEmpty()) {
                    this.suspects.add(new Suspect(null, null, fi.filename, "No changed lines, only additions"));
                    continue;
                }
                String previousCommit = git.getPreviousCommit(transaction.getId(), l);
                Suspect suspect = null;
                if (previousCommit != null) {
                    suspect = getSuspect(previousCommit, git, fi.filename, linesMinus, l);
                }
                if (suspect != null && !suspect.getCommitId().equals(transaction.getId())) {
                    this.suspects.add(suspect);
                } else if (addAllBFCToResult) {
                    this.suspects.add(new Suspect(null, null, fi.filename, null));
                }
            } else if (addAllBFCToResult) {
                this.suspects.add(new Suspect(null, null, fi.filename, "Ignored file type"));
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
	private Suspect getSuspect(String previous, Git git, String fileName, List<Integer> linesMinus, PrintWriter l) {
    	RevCommit closestCommit = null;
    	long tempDifference = Long.MAX_VALUE;
    	for (int i : linesMinus){
    		try{
    			String sha = git.getBlameAt(previous,fileName,i);
    			if (sha == null)
    				continue;
    			RevCommit commit = git.getCommit(sha);
    			long difference;
    			if (issue == null) {
                    RevCommit bugFixingCommit = git.getCommit(transaction.getId());
                    difference = bugFixingCommit.getCommitTime() - commit.getCommitTime();
                } else {
                    difference = (issue.getOpen()/1000) - (commit.getCommitTime());
                }
    			if (difference > 0){
    				if (difference < tempDifference ){
    					closestCommit = commit;
    					tempDifference = difference; }
    				}
    			} catch (Exception e){
    				e.printStackTrace();
    				l.println(e);
    			}
    	}
        if (closestCommit != null){
            return LinkUtils.generateSuspect(closestCommit,fileName);
        }

		return null;
	}

}
