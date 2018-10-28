package com.violation.logUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;

import javax.naming.spi.DirectoryManager;

public class ParsingLog {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public ParsingLog(String path_bin_P, String path_systemGit,String systemP) throws IOException, InterruptedException {
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
				//url e dir of the file log to parse
				String dirLog=path_bin.replace("/bin/", "/temp_log/");
				System.out.println("path dir of the file log to parse: "+dirLog);
				String urlLog=path_bin.replace("/bin/", "/temp_log/log_"+system+".txt");
				System.out.println("path log file to parse: "+urlLog);
				String urlLogParsed=path_bin.replace("/bin/", "/temp_log/log_"+system+"_parsed.txt");
				System.out.println("path log file parsed: "+urlLogParsed+"/log_"+system+"_parsed.txt");
				LogParser.parseLog(urlLog+"/log_"+system+".txt", urlLogParsed+"/log_"+system+"_parsed.txt");
	}

}
