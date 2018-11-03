package com.SZZ.jiraAnalyser.git;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import com.SZZ.jiraAnalyser.entities.*;
import com.SZZ.jiraAnalyser.entities.Transaction.FileInfo;


public class Git {
	
	public String cloneCommand;
	public  File storagePath;
	public  String pullCommand;
	public  File workingDirectory;
	public  String logCommand;
	public  File logFile;
	public  File csvFile;
	private BlameResult blame;
	private String projectName = "";
	
	private static final char DELIMITER = ';';
	
	
	public Git(Path storagePath){
		this.storagePath = storagePath.toFile();
		System.out.println(storagePath.toString());
		this.pullCommand = "git pull";
		this.workingDirectory = new File("./extraction/"+projectName+"/"+projectName+"/.git");
		this.logCommand ="git log " +
				"--pretty=format:\'" +
				"%H" + DELIMITER +
				"%aI" + DELIMITER +
				"%aN" + DELIMITER +
				"%s" +  DELIMITER +
				"\' " +
				"--name-status -M100% " ;
		workingDirectory.delete();
	}
	
	
	
	public Git(Path storagePath, URL url, String projectName) {
		this.projectName = projectName;
		this.cloneCommand = "git clone " + url.toString() + " " +projectName;
		this.storagePath = storagePath.toFile();
		System.out.println(storagePath.toString());
		this.pullCommand = "git pull";
		this.workingDirectory = new File("./extraction/"+projectName+"/"+projectName+"/.git");
		this.logCommand ="git log " +
				"--pretty=format:\'" +
				"%H" + DELIMITER +
				"%aI" + DELIMITER +
				"%aN" + DELIMITER +
				"%s" +  DELIMITER +
				"\' " +
				"--name-status -M100% " ;
		this.logFile = this.gitLogFile(storagePath, url);
		this.csvFile = this.csvFile(storagePath, url);
		workingDirectory.delete();
	}
	
	private String gitDirectory(URL url) {
		final String link = url.toString();
		return link.substring(link.lastIndexOf('/') + 1, link.lastIndexOf('.'));
	}
	
	private File gitWorkingDirectory(Path storagePath, URL url) {
		return Paths.get(storagePath.toString(), this.gitDirectory(url)).toFile();
	}
	
	private File gitLogFile(Path storagePath, URL url) {
		return Paths.get(storagePath.toString(), this.gitDirectory(url) + ".txt")
				.toFile();
	}
	
	private File csvFile(Path storagePath, URL url) {
		return Paths.get(storagePath.toString(), this.gitDirectory(url) + ".csv")
				.toFile();
	}
	
	public void cloneRepository() throws Exception {
		execute(this.cloneCommand, this.storagePath);
	}
	
	public void pullUpdates() throws Exception {
		execute(this.pullCommand, this.storagePath);
	}
	
	public void saveLog() throws Exception {
		Path path = FileSystems.getDefault().getPath(".");
		executeToFile(this.logCommand, this.storagePath, new File("gitlog.csv"));
	}
	
	private void execute(String command, File directory)
			throws Exception {
		System.out.println("$ " + command);
		ProcessBuilder pb = new ProcessBuilder(command.split(" "));
		pb.directory(directory);
		pb.redirectErrorStream(true);
        pb.redirectOutput(Redirect.INHERIT);
        Process p = pb.start();
        p.waitFor();
	}
	
	private void executeToFile(
			String command, File workingDirectory, File destinationFile)
					throws Exception  {
		System.out.println("$ " + command + " > " + destinationFile);
		ProcessBuilder pb = new ProcessBuilder(command.split(" "));
		pb.directory(this.workingDirectory);
		pb.redirectOutput(destinationFile);
		Process p = pb.start();
        p.waitFor();
	}
	
	public List<Transaction> getCommits() {
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
	
	  /**
	   * Returns String with differences of fileName done by a shaCommit
	   * @param shaCommit
	   * @param fileName
	   * @return
	   */
	  public  String getDiff (String shaCommit, String fileName, Logger l)  {
		String result = "";
		File  localRepo1 = new File(workingDirectory+"");    
		try {
		org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.open(localRepo1); 
	    ObjectId  oldId = git.getRepository().resolve(shaCommit+"^^{tree}");
	    ObjectId headId = git.getRepository().resolve(shaCommit + "^{tree}");
	    ObjectReader reader = git.getRepository().newObjectReader();
	    CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
	    oldTreeIter.reset(reader, oldId);
	    CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
			newTreeIter.reset(reader, headId);
		    List<DiffEntry> diffs= git.diff()
		            .setNewTree(newTreeIter)
		            .setOldTree(oldTreeIter)
		            .call();  
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    DiffFormatter df = new DiffFormatter(out);
		   // df.setDiffComparator(RawTextComparator.WS_IGNORE_LEADING);
		    df.setRepository(git.getRepository());
		    
		    for(DiffEntry diff : diffs)
		    {
		      if (diff.getNewPath().contains(fileName)){
		    	// Print the contents of the DiffEntries
		    	  //System.out.println(diff.getNewPath());
		    	  df.format(diff);
		          diff.getOldId();
		          String diffText = out.toString("UTF-8");
		          result = diffText;
		          out.reset();
		          df.close();
		          break;
		      }
		    }
		} catch (IncorrectObjectTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			l.error(e);
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			l.error(e);
			return null;
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			l.error(e);
			return null;
		}
	    return result;
		}
	  
	  /**
	   * It gets removed lines from a commit starting from the diffString
	   * @param diffString
	   * @return
	   */
	  public  List<Integer> getLinesMinus(String diffString){
		 
		  int actualInt = 1;
		  boolean actualSet = false;
		  List<Integer> listMinus = new LinkedList<Integer>();
		  try{
		  Scanner scanner = new Scanner(diffString);
		  scanner.nextLine();
		  scanner.nextLine();
		  scanner.nextLine();
		  scanner.nextLine();
		  while (scanner.hasNextLine()) {
		    String line = scanner.nextLine();
		     switch(line.charAt(0)){
		     case '-':
		    	 actualInt++;
		    	 listMinus.add(actualInt);
		    	 break;
		     case '+': 
		     	break;
		     case '@':
		    	 int stringMinus = line.indexOf('-');
		    	 int stringCommma = line.indexOf(',');
		    	 stringMinus++;
		    	 String sM = line.substring(stringMinus, stringCommma);
		    	 actualInt = Integer.parseInt(sM);
		    	 line = scanner.nextLine();
		    	 actualSet = true;
		    	 break;
		     default:
		    	 if (actualSet)
		    		 actualInt++;
		    	 break;    			 
		     }
		  }
		  scanner.close();
		  }
		  catch(Exception e){
			  return null;
		  }
		  return listMinus;
		  
	  }
	  
	  
	  /**
	   * It gets blame of a file at a specific commit time
	   * index 0 of array ==> line 0
	   * index 1 of array ==> line 1
	   * index 2 of array ==> line 2
	   * @param commitSha
	   * @param file
	   * @param git
	   * @return
	   */
	  
	  public  String getBlameAt(String commitSha, String file,Logger l, int lineNumber) {
		  File  localRepo1 = new File(workingDirectory+"");  
		try {
			if (blame==null){
			  org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.open(localRepo1); 
			  Repository repository = git.getRepository();
		      BlameCommand blamer = new BlameCommand(repository);
		      ObjectId commitID;
			  commitID = repository.resolve(commitSha);
		      blamer.setStartCommit(commitID);
		      blamer.setFilePath(file);
		      blame = blamer.call();}
		      RevCommit commit = blame.getSourceCommit(lineNumber);
		      return commit.getName();
		} catch (Exception e) {
			return null;
		}
	  } 
	  
	  /**
	   * It gets commit object starting from a specific sha 
	   * 
	   */
	  public RevCommit getCommit(String sha, Logger l){
		  File  localRepo1 = new File(workingDirectory+"");     
		  //Repository repository = git.getRepository();
		  RevCommit commit = null;
		  try{
			  org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.open(localRepo1); 
			  Repository repository = git.getRepository();
			  RevWalk walk = new RevWalk( repository); 
			  ObjectId commitId = ObjectId.fromString( sha);
			   commit = walk.parseCommit( commitId );
			  //System.out.println(commit.getCommitTime());
			} catch (Exception e) {
				e.printStackTrace();
				l.error(e);
				return null;
			} 
		  return commit;
	  }
	  
	  /**
	   * Get Commit that changed the file before the parameter commit
	   * @param sha
	   * @param file
	   * @return
	   */
	  public String getPreviousCommit (String sha, String file, Logger l){
		  File  localRepo1 = new File(workingDirectory+"");   
		  Iterable<RevCommit> iterable;
		  String finalSha = "";	  
		  RevCommit latestCommit = null;
		  String path = file;
		  try {
			org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.open(localRepo1); 
			RevWalk revWalk = new RevWalk( git.getRepository() ); 
		    RevCommit revCommit = getCommit(sha, null);
		    revWalk.markStart( revCommit );
		    revWalk.sort( RevSort.COMMIT_TIME_DESC );
		    revWalk.setTreeFilter( AndTreeFilter.create( PathFilter.create( path ), TreeFilter.ANY_DIFF ) );		    
		    latestCommit = revWalk.next();
		    while (!latestCommit.getName().equals(sha))
		    	latestCommit = revWalk.next();
		    latestCommit = revWalk.next();
		    if (latestCommit == null)
		    	return null;
		    finalSha =  latestCommit.getName();
	
		  } catch (Exception e) {
			l.info("No Predecessor-Commits found for "+sha +"for file " + file);
			return null;
		}	  
		  return finalSha;
	  }
}
