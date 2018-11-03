package com.SZZ.jiraAnalyser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

import com.SZZ.jiraAnalyser.entities.Link;
import com.SZZ.jiraAnalyser.entities.LinkManager;
import com.SZZ.jiraAnalyser.entities.Suspect;
import com.SZZ.jiraAnalyser.entities.Transaction;
import com.SZZ.jiraAnalyser.entities.TransactionManager;
import com.SZZ.jiraAnalyser.git.JiraRetriever;

public class Application {

	static Logger logger;

	public static final String DEFAULT_BUG_TRACKER = "https://issues.apache.org/jira/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml";

	public final URL sourceCodeRepository;
	public final URL bugTracker;
	private String projectName;
	private final TransactionManager transactionManager = new TransactionManager();
	private final LinkManager linkManager = new LinkManager();
	private List<Transaction> transactionsWithFault;
	private List<Link> linksBugFixing;
	private String jiraKey;

	public Application(String projectName, String sourceCodeRepository, String jiraKey) throws MalformedURLException {
		this.projectName = projectName;
		System.out.println(sourceCodeRepository);
		this.sourceCodeRepository = new URL(sourceCodeRepository);
		this.bugTracker = new URL(DEFAULT_BUG_TRACKER);
		this.jiraKey   = jiraKey;
		System.setProperty("logfile.name", (projectName + "/" + projectName + "_JiraAnalyserLogger.log"));
		logger = Logger.getLogger(Application.class);
	}

	/*
	 * It downloads the logFile and import commits with fault keys
	 */
	public void mineData() {
		System.out.println("Downloading Git logs for project " + projectName);
		transactionsWithFault = transactionManager.getBugFixingCommits(sourceCodeRepository, projectName,jiraKey);
		System.out.println("Git logs downloaded for project " + projectName);
	}

	/*
	 * It calculates the bugFixing Commits showing syntatic/semantic scores.
	 * Commits accepted are saved on the projectName_BugFixingCommits.csv file.
	 */
	public void calculateBugFixingCommits() {
		System.out.println("Calculating bug fixing commits for project " + projectName);
		List<Link> links = linkManager.getLinks(transactionsWithFault, projectName, logger, jiraKey);
		printData(links);
		discartLinks(links);
		saveBugFixingCommits(links);
		linksBugFixing = links;
		System.out.println("Bug fixing commits for project " + projectName + "calculated");
		System.out.println(links.size() + " bug fixing commits for project " + projectName + "found");
	}

	/**
	 * It retrieves the bugs inducing commits applying SZZ algortihm and save
	 * the results on the file projectName+"_BugInducingCommits.csv
	 */
	public void calculateBugInducingCommits() {
		System.out.println("Calculating Bug Inducing Commits");
		int count = linksBugFixing.size();
		PrintWriter printWriter;
		try {
			printWriter = new PrintWriter("extraction/"+projectName + "/" + projectName + "_BugInducingCommits.csv");
			printWriter.println("bugFixingId,bugFixingTs,bugFixingfileChanged,bugInducingId,bugInducingTs,issueType,issueKey");
			for (Link l : linksBugFixing) {
				if (count % 100 == 0)
					System.out.println(count + " Commits left");
				l.calculateSuspects(transactionManager.getGit(), logger);
				String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
				SimpleDateFormat format1 = new SimpleDateFormat(pattern);
				for (Suspect s : l.getSuspects()) {
					printWriter.println();
					printWriter.println(l.transaction.getId() + "," + format1.format(l.transaction.getTimeStamp()) + ","
							+ s.getFileName() + "," + s.getCommitId() + "," + format1.format(s.getTs()) + ","
							+ l.issue.getType() + "," + jiraKey.toUpperCase() + "-"+l.issue.getId());
				}
				count--;
			}
			printWriter.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getStackTrace());
		}
	}
	
	/**
	 * It downloads all Jira Issues. The Issues are divided in files of 1000 issues each.
	 * projectName_0.csv => it is the first page with the first 1000 issues of the project
	 */
	public void downloadIssues(){
		JiraRetriever jr = new JiraRetriever(DEFAULT_BUG_TRACKER,logger,jiraKey);
		jr.printIssues();
		System.out.println("All Jira issues downloaded");
	}
	
	/**
	 * It prints a table summarying the results of the analysis
	 * 
	 * @param links
	 */
	private void printData(List<Link> links) {
		int[][] multi = new int[4][7];
		for (int row = 0; row < 4; row++)
			for (int col = 0; col < 7; col++)
				multi[row][col] = 0;
		multi[0][0] = 0;
		multi[1][0] = 1;
		multi[2][0] = 2;

		for (Link l : links) {
			int row = l.getSyntacticConfidence();
			int column = l.getSemanticConfidence();
			column++;
			multi[row][column]++;
			multi[row][6]++;
			multi[3][column]++;
			multi[3][6]++;
		}

		String print = "\n";
		print += String.format("%-16s%-16s%-16s%-16s%-16s%-16s%-16s", "syn / sem", "0", "1", "2", "3", "4", "total");
		print += "\n";
		print += String.format("%s",
				"--------------------------------------------------------------------------------------------------------------");
		print += "\n";
		print += String.format("%-16d%-16d%-16d%-16d%-16d%-16d%-16d", multi[0][0], multi[0][1], multi[0][2],
				multi[0][3], multi[0][4], multi[0][5], multi[0][6]);
		print += "\n";
		print += String.format("%-16d%-16d%-16d%-16d%-16d%-16d%-16d", multi[1][0], multi[1][1], multi[1][2],
				multi[1][3], multi[1][4], multi[1][5], multi[1][6]);
		print += "\n";
		print += String.format("%-16d%-16d%-16d%-16d%-16d%-16d%-16d", multi[2][0], multi[2][1], multi[2][2],
				multi[2][3], multi[2][4], multi[2][5], multi[2][6]);
		print += "\n";
		print += String.format("%s",
				"--------------------------------------------------------------------------------------------------------------");
		print += "\n";
		print += String.format("%-16d%-16d%-16d%-16d%-16d%-16d%-16d", multi[3][0], multi[3][1], multi[3][2],
				multi[3][3], multi[3][4], multi[3][5], multi[3][6]);
		System.out.println(print);
	}

	/*
	 * Only Links with sem > 1 OR ( sem = 1 AND syn > 0) must be considered
	 */
	private void discartLinks(List<Link> links){
		List<Link> linksToDelete = new LinkedList<Link>();
		for (Link l : links){
			if (l.getSemanticConfidence() < 1 && (l.getSemanticConfidence() != 1 ||  l.getSyntacticConfidence() < 0)) {
				linksToDelete.add(l);
				}
			else
				if (l.transaction.getTimeStamp().getTime() > l.issue.getClose()){
					linksToDelete.add(l);
				}
		}
		String print = "\n";
		print += "\n";
		print += String.format("%s", "--------------------------------------------------------------------------------------------------------------");
		print += "\n";
		print+=("Links removed too low score (sem > 1 v (sem = 1 and syn > 0)): "+ linksToDelete.size() +" ("+ ((double)linksToDelete.size()/(double)links.size())*100 + "%)");
		System.out.println(print);
		links.removeAll(linksToDelete);
	}
	
	/**
	 * It saves all bug fixing commits found on a file
	 * @param links
	 * @param projectName
	 */
	private void saveBugFixingCommits(List<Link> links){
		try {
			PrintWriter printWriter = new PrintWriter(new File("extraction/"+projectName + "/"+projectName+"_BugFixingCommits.csv"));
			printWriter.println("commitsSha,commitTs,commitComment,issueKey,issueOpen,issueClose,issueTitle");
			String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
		    SimpleDateFormat format = new SimpleDateFormat(pattern);
			for (Link l : links){
				String row = l.transaction.getId() + ","
						+    format.format(l.transaction.getTimeStamp()) + ","
						+    l.transaction.getComment() + ","
						+    projectName+"-"+l.issue.getId()	+","
						+    format.format(new Date(l.issue.getOpen())) + ","
					    +    format.format(new Date(l.issue.getClose())) + ","
					    +    l.issue.getTitle()
						;
				printWriter.println(row);				
			}
			printWriter.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}}
		
		public void calculateBugInducingCommits(List<Link> links){
			System.out.println("Calculating Bug Inducing Commits");
			int count = links.size();
			PrintWriter printWriter;
			try {
				printWriter = new PrintWriter("extraction/"+projectName+"/"+projectName+"_BugInducingCommits.csv");
				printWriter.println("bugFixingId,bugFixingTs,bugFixingfileChanged,bugInducingId,bugInducingTs,issueType,jiraKey");
				for (Link l : links){
					if (count % 100 == 0)
						logger.info(count + " Commits left");
					l.calculateSuspects(transactionManager.getGit(),logger);
					String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
			        SimpleDateFormat format1 = new SimpleDateFormat(pattern);
			        for (Suspect s : l.getSuspects()){
			        	printWriter.println();
			        	printWriter.println(
			        			l.transaction.getId() + "," + 
			        			format1.format(l.transaction.getTimeStamp()) +"," +
			        			s.getFileName()		+ "," +
			        			s.getCommitId()     + "," +
			        			format1.format(s.getTs()) +","+
			        			l.issue.getType() + "," + 
			        			l.issue.getId() 
			        			);
			        }
			        count--;
			}
				printWriter.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(e.getStackTrace());
			}	

		
	}
}
