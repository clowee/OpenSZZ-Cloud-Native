package com.rest.szz.entities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jgit.revwalk.RevCommit;

import com.rest.szz.entities.*;
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
					Resolution resolution = getResolutionFromString(s[2]);
					Issue.Status status = getStatusFromString(s[3]);
					String assignee = s[4];
					Long createdDate = Long.parseLong(s[5]);
					Long resolvedDate = Long.parseLong(s[6]);
					String type = s[7];
					Set<String> attachments = stringToSet(s[8]);
					Set<String> brokenBy = stringToSet(s[9]);
                    String description = s[10].substring(1,s[10].length()-1);
					String comments = s[10].substring(1,s[10].length()-1);

					this.issue = new Issue(number, title, status, resolution, assignee, createdDate, resolvedDate, attachments, comments, type, brokenBy, description);
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Resolution getResolutionFromString(String str) {
		Resolution resolution;
		try{
			resolution = Resolution.valueOf(str.toUpperCase().replace(" ", "").replace("'", ""));
		}
		catch(Exception e){
			resolution = Resolution.NONE;
		}
		return resolution;
	}

	private Issue.Status getStatusFromString(String str) {
		Issue.Status status;
		try{
			status = Issue.Status.valueOf(str.toUpperCase());
		}
		catch(Exception e){
			status = Issue.Status.UNCONFIRMED;
		}
		return status;
	}

	private Set<String> stringToSet(String str) {
		String resultString = str.replace("[", "").replace("]", "");
		Set<String> result = new HashSet<>();
		if (resultString.length() > 0) {
			List<String> resultsList = Arrays.asList(resultString.split("\\s*,\\s*"));
			result = new HashSet<>(resultsList);
		}
		return result;
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

    private boolean isCodeFile(FileInfo file) {
        if (!file.filename.contains(".")) return false;
        List<String> extensionsToIgnore = Arrays.asList("txt","md");
        return extensionsToIgnore.stream().noneMatch(extension -> file.filename.endsWith("." + extension));
    }

	private Set<Suspect> getSuspectsByAddressedIssues(Set<String> issueIds,Git git,String source) {
		Set<Suspect> suspects = new LinkedHashSet<>();
		issueIds.stream()
				.filter(issueId -> !issueId.equals(this.projectName + "-" + this.issue.getId()))
				.forEach(issueId -> {
					List<Transaction> transactions = git.getCommits(issueId);
                    List<Transaction> filteredTransactions = transactions.stream().filter(t -> {
                        List<FileInfo> changedCodeFiles = t.getFiles().stream().filter(file -> isCodeFile(file)).collect(Collectors.toList());
                        return changedCodeFiles.size() > 0;
                    }).collect(Collectors.toList());
                    if (filteredTransactions.size() > 0) {
						List<Suspect> foundSuspects = transactions.stream()
                                .filter(t -> !t.getId().equals(this.transaction.getId()))
								.map(t -> new Suspect(t.getId(),t.getTimeStamp(),null,source))
								.collect(Collectors.toList());
						suspects.addAll(foundSuspects);
					}
				});
		return suspects.stream().filter(distinctByKey(s -> s.getCommitId())).collect(Collectors.toSet());
	}

    private void setSuspectsByIssueDescriptionAndComments(Git git) {
        String lookBehind = "(?<=(((introduc(ed|ing)|started|broken) ((this|the) (bug|issue|error) )?(in|by|with)|caused by|due to|after|before|because( of)?|since) ))";
        String lookAhead = "(?=( (introduced|caused)|[^.,:]* cause))";
        String issueIdPattern = projectName+"[ ]*-[ ]*[0-9]+";
        String commitShaPattern = "(\\b|(?<=(\\br)))[0-9a-f]{5,41}\\b";
        String issuePattern = (lookBehind + issueIdPattern) + "|" + (issueIdPattern + lookAhead);
        String commitPattern = (lookBehind + commitShaPattern) + "|" + (commitShaPattern + lookAhead);
        String text = issue.getDescription()+issue.getComments();
        Set<String> issueMatches = getMatches(text,issuePattern);
        Set<String> commitMatches = getMatches(text,commitPattern);
        Set<Suspect> newSuspects = getSuspectsByAddressedIssues(issueMatches, git,"description/comments");
        commitMatches.forEach(sha -> {
            RevCommit commit = git.getCommit(sha);
            if (commit != null && !commit.getName().equals(transaction.getId())) {
                newSuspects.add(generateSuspect(commit,"description/comments"));
            }
        });
        suspects.addAll(newSuspects.stream().filter(distinctByKey(s -> s.getCommitId())).collect(Collectors.toSet()));
    }

	public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> map = new ConcurrentHashMap<>();
		return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

    private Set<String> getMatches(String source, String regex) {
        Pattern pattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
        Set<String> result = new LinkedHashSet<>();
        Matcher resultMatcher = pattern.matcher(source);
        while (resultMatcher.find()) {
            result.add(resultMatcher.group());
        }
        return result;
    }

	/**
	 * For each modified file it calculates the suspect
	 *
	 * @param git
	 */
	public void calculateSuspects(Git git, PrintWriter l, Boolean addAllBFCToResult, Boolean useIssueInfo) {
	    if (this.issue != null && useIssueInfo) {
            suspects.addAll(getSuspectsByAddressedIssues(this.issue.getBrokenBy(), git, "brokenBy"));
            if (this.suspects.size() > 0) return;

            setSuspectsByIssueDescriptionAndComments(git);
            if (this.suspects.size() > 0) return;
        }

		for (FileInfo fi : transaction.getFiles()) {
            if (isCodeFile(fi)) {
                String diff = git.getDiff(transaction.getId(), fi.filename, l);
                if (diff == null) {
                    if (addAllBFCToResult) this.suspects.add(new Suspect(null,null, null, "No changes in commit"));
                    continue;
                }
                List<Integer> linesMinus = git.getLinesMinus(diff,fi.filename);
                if (linesMinus == null || linesMinus.size() == 0) {
                    if (addAllBFCToResult) this.suspects.add(new Suspect(null,null, fi.filename, "No changed lines, only additions"));
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
    				break;
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
            return generateSuspect(closestCommit,fileName);
        }

		return null;
	}

	private Suspect generateSuspect(RevCommit commit, String fileName) {
		Long temp = Long.parseLong(commit.getCommitTime()+"") * 1000;
		return new Suspect(commit.getName(), new Date(temp), fileName, null);
	}

}
