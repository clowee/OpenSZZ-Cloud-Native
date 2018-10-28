package com.SZZ.logUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import javax.naming.spi.DirectoryManager;

public class CreateLogFile {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public CreateLogFile(String path_bin_P, String path_systemGit,String systemP) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		//Compute the relative path to understand where is the src2srcML script 
				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
				URL url = classLoader.getResource("");
				//name of the git system considered
				String system=systemP;
								
				
				//path bin
				String path_bin = path_bin_P;
				System.out.println("path bin: "+path_bin);
				//url of the git system to analyze				
				String path_git_system = path_systemGit;
				System.out.println("path git system: "+path_git_system);
				//url e dir of the file log to buid
				String dirLog=path_bin.replace("/bin/", "/temp_log/");
				System.out.println("path file log to buid: "+dirLog);
				String urlLog=path_bin.replace("/bin/", "/temp_log/log_"+system+".txt");
				System.out.println("path file log to buid: "+urlLog);
				//new directory to create:"dirLog"
				    boolean success = (new File(dirLog)).mkdir();
				    if (success)
				    {
				      System.out.println("Created Folder: " + dirLog);
				    }else{
				      if((new File(dirLog)).exists())
				      {
				    	  System.out.println("Folder already exists: " + dirLog);
				      }
				      else
				    	  System.out.println("Impossible to create: " + dirLog);
				    }
			   //we create a ".bat" file to build the log file of the git system considered
					File commands = new File(dirLog+"execute.sh");
					commands.createNewFile();
					
					PrintWriter pw = new PrintWriter(commands);
					pw.println("cd "+path_git_system+" & git log --stat --date iso >"+ urlLog);
					//pw.println("");
					pw.close();
					
					Runtime rt = Runtime.getRuntime();
					
					String cmd = dirLog+"execute.sh";
					Process process = rt.exec(cmd);
				
					String line = null;
					
					BufferedReader stdoutReader = 
						      new BufferedReader (new InputStreamReader (process.getInputStream()));
					while ((line = stdoutReader.readLine()) != null) {
					    //System.out.println(line);
					}
					
					
					BufferedReader stderrReader = new BufferedReader(
					         new InputStreamReader(process.getErrorStream()));
					while ((line = stderrReader.readLine()) != null) {
					    //System.out.println(line);
					}
					process.waitFor();
	}

}
