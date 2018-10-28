package com.SZZ.jiraAnalyser.entities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Transaction {
	
	public enum FileStatus {
		A("Added"),
		C("Copied"),
		D("Deleted"),
		M("Modified"),
		R("Renamed"),
		T("TypeChanged"),
		U("Unmerged"),
		X("Unknown"),
		B("Broken");
		
		private String status;
		
		private FileStatus(String status) {
			this.status = status;
		}
		
		@Override
		public String toString() {
			return status;
		}
		
		public static FileStatus from(String status) {
			for (FileStatus fs : FileStatus.values()) {
				if (fs.name().equalsIgnoreCase(status)) {
					return fs;
				}
			}
			return FileStatus.B;
		}
	}
	
	public static class FileInfo {
		public final FileStatus fileStatus;
		public final String filename;
		
		public FileInfo(FileStatus fileStatus, String filename) {
			this.fileStatus = fileStatus;
			this.filename = filename;
		}
		
		public FileInfo(String fileStatus, String filename) {
			this.fileStatus = FileStatus.from(fileStatus);
			this.filename = filename;
		}
		
		@Override
		public String toString() {
			return "'" + filename + "': '" + fileStatus + "'";
		}
	}
	
	private final String hashId;
	private final Date timestamp; 
	private final String author;
	private final String comment;
	private final List<FileInfo> filesAffected;
	
	public Transaction(
			String hashId, String timestamp, String author, String comment,
			List<FileInfo> filesAffected) {
		this.hashId = hashId;
		Date dateTemp = null;
		timestamp = timestamp.replace("T", "");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm:ssXXX");
		try {
			dateTemp = sdf.parse(timestamp);
		} catch (ParseException e) {
			dateTemp = null;
			e.printStackTrace();
		}
		this.timestamp = dateTemp;
		this.author = author;
		this.comment = comment.toLowerCase();
		this.filesAffected = filesAffected;
	}
	
	
	/*
	 * Bug Number Patterns
	 */
	private static final Pattern patter1 = Pattern.compile("bug[# \t]*[0-9]+", Pattern.CASE_INSENSITIVE);
	private static final Pattern patter2 = Pattern.compile("pr[# \t]*[0-9]+", Pattern.CASE_INSENSITIVE);
	private static final Pattern patter3 = Pattern.compile("show\\_bug\\.cgi\\?id=[0-9]+", Pattern.CASE_INSENSITIVE);
	private static final Pattern patter4 = Pattern.compile("\\[[0-9]+\\]", Pattern.CASE_INSENSITIVE);

	/*
	 * Plain Number Pattern
	 */
	private static final Pattern patter5 = Pattern.compile("[0-9]+", Pattern.CASE_INSENSITIVE);

	/*
	 * Keyword Pattern 
	 */
 	private static final Pattern patter6 = Pattern.compile("(fix(e[ds])?|bugs?|defects?|patch)+", Pattern.CASE_INSENSITIVE);
	
	/*
	 * Word Pattern 
	 */
	private static final Pattern patter7 = Pattern.compile("^[a-zA-Z0-9]*$", Pattern.CASE_INSENSITIVE);
	
		
	private List<Long> bugIds = new LinkedList<Long>();
	
	public boolean hasBugId() {
		//System.out.println(comment);
        Matcher p1 = Transaction.patter1.matcher(comment);
		Matcher p2 = Transaction.patter2.matcher(comment);
		Matcher p3 = Transaction.patter3.matcher(comment);
		Matcher p4 = Transaction.patter4.matcher(comment);
		Matcher p5 = Transaction.patter5.matcher(comment);
		Matcher p6 = Transaction.patter6.matcher(comment);
 		Matcher p7 = Transaction.patter7.matcher(comment);
		
		boolean b1 = p1.find();
		boolean b2 = p2.find();
		boolean b3 = p3.find();
		boolean b4 = p4.find();
		boolean b5 = p5.find();
 		boolean b6 = p6.find();
		boolean b7 = p7.find();
		
		Set<String> results = new HashSet<String>();

			if (b1) results.add(p1.group().replaceAll("[^\\d]", ""));
			if (b2) results.add(p2.group().replaceAll("[^\\d]", ""));
			if (b3) results.add(p3.group().replaceAll("[^\\d]", ""));
			if (b4) results.add(p4.group().replaceAll("[^\\d]", ""));
			if (b5) results.add(p5.group().replaceAll("[^\\d]", ""));
			if (b6) results.add(p6.group().replaceAll("[^\\d.]", ""));
			if (b7) results.add(p7.group().replaceAll("[^\\d.]", ""));
			
			for(String s : results) {
				if (!s.isEmpty())
					bugIds.add(Long.parseLong(s));
			}
		
			return (bugIds.size() > 0);
	}
	
	public List<Long> getBugIds() {
		return this.bugIds;
	}
	
	@Override
	public String toString() {
		String str = "'" + hashId + "': {\n" +
				"  ts: '" + timestamp + "',\n" +
				"  author: '" + author + "',\n" +
				"  comment: '" + comment + "',\n" +
				"  files: {\n";
		for(FileInfo fi : filesAffected) {
			str += "    " + fi.toString() + ",\n";
		}
		str += "  }\n";
		str += "}";
		return str;
	}
	
	public Date getTimeStamp(){
		return this.timestamp;
	}
	
	public String getComment(){
		return this.comment;
	}
	
	public String getAuthor(){
		return this.author;
	}
	
	public String getId(){
		return this.hashId;
	}
	
	public List<FileInfo> getFiles(){
		return this.filesAffected;
	}
	
	

	}
	
	
	

