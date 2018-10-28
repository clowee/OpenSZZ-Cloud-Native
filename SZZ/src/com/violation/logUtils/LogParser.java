package com.violation.logUtils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import javax.swing.text.Document;

public class LogParser {

	//this method take a path o f a log file unstructured in and 
	//parse its information building in output a structured log in "*.csv" format
	//here the columns are :
	//`FILE`,`CHANGE_ID`,`DATE`,`COMMITTER_ID`,`LINES_ADDED`,`LINES_REMOVED`,`NOTE` 
	//for example:
	//('...-the-question-section-in-update-responses.patch', '5c716146e8aee16b46a2144c199b922c13fd00ba', '2010-02-17 16:34:33', 'Andrew_Tridgell', 6, 0, 'examples: add bind9 patches for TSIG-GSS support We will point at these from the Samba4 HOWTO ')
	public static void parseLog(String pathLogToParse,String pathLogOutParsed) throws IOException  {
	PrintStream prst=new PrintStream(new File(pathLogOutParsed));
	prst.println("FILE,CHANGE_ID,DATE,COMMITTER_ID,LINES_ADDED,LINES_REMOVED,NOTE");
	File logFile=new File(pathLogToParse);
	BufferedReader documentLog = new BufferedReader(new InputStreamReader(new FileInputStream(logFile),"utf8"));
	String line="",substring_to_remove="";
	boolean break_while=true;
	String file="",change_id="",date="",committer_id="",note="";
	int lines_added=0,lines_removed=0;
	line="";
	while(documentLog.ready())
	{
		break_while=true;
		if(line.length()>7)
		if(line.substring(0, 7).equals("commit "))
		{
		 //System.out.println(line);	
			change_id=line.replace("commit ", "").replace(" ","");
			change_id="'"+change_id+"'";
			//System.out.println(change_id);
		}
		if(line.length()>8)
		if(line.substring(0, 8).equals("Author: "))
		{
		 //System.out.println(line);	
			committer_id=line.replace("Author: ", "").replace(" ","_");
			committer_id=committer_id.substring(0, committer_id.indexOf("<")-1);
			committer_id=committer_id.toLowerCase();
					   
					   substring_to_remove="(";
					   committer_id=committer_id.replace(substring_to_remove, "") ;
					   substring_to_remove=")";
					   committer_id=committer_id.replace(substring_to_remove, "") ;					   
				   committer_id=committer_id.replaceAll("[.]", "") ;
			committer_id=LogParser.convertNonAscii(committer_id);
			committer_id="'"+committer_id+"'";
			//System.out.println(committer_id);
		}
		if(line.length()>8)
		if(line.substring(0, 8).equals("Date:   "))
		{
		 //System.out.println(line);	
			date=line.replace("Date:   ", "");
			date=date.substring(0, date.lastIndexOf(" "));
			date="'"+date+"'";
			//System.out.println(date);
			line=documentLog.readLine();
			note="";
			break_while=true;
			while(!line.contains("|") && documentLog.ready() && break_while==true)
			{
			    if(!line.equals(""))
			    {
			     note=note+" "+LogParser.removeJollyChar(line);
			    }
				line=documentLog.readLine();
				if(line.length()>8)
				if(line.substring(0, 7).equals("commit "))
				break_while=false;		
			}
			note="'"+note+"'";
			//System.out.println(note);
			while(line.contains(" | ") && documentLog.ready() && break_while==true)
			{
			    if(!line.equals(""))
			    {
			     file=line.substring(0,line.indexOf("|")-1);
			     file="'"+file.replaceAll(" ", "")+"'";
			     lines_added=LogParser.countOccurrences("+",line);
			     lines_removed=LogParser.countOccurrences("-",line);
			     //System.out.println(file);
			     //System.out.println(lines_added);
			     //System.out.println(lines_removed);
			     prst.println(file+","+change_id+","+date+","+committer_id+","+lines_added+","+lines_removed+","+note.replaceAll("     ", " "));
			    }
				line=LogParser.convertNonAscii(documentLog.readLine());
			}
		}
		if(break_while==true)
		line=LogParser.convertNonAscii(documentLog.readLine());
	}
	documentLog.close();
	prst.close();
	}
	
	public static int countOccurrences(String find, String string)
	  {
	    int count = 0;
	    int indexOf = 0;

	    while (indexOf > -1)
	    {
	      indexOf = string.indexOf(find, indexOf + 1);
	      if (indexOf > -1)
	        count++;
	    }

	    return count;
	  }
	
	private static String removeJollyChar(String corpus){		
		//String s = corpus.replaceAll("[-+.^:,!@#$%*~()_{}�&/;\"?<>]", " ");
		String s = corpus;
		s = s.replace("[", " ");		
		s = s.replace("]", " ");
		s = s.replace("-", " ");
		s = s.replace("+", " ");
		s = s.replace(".", " ");
		s = s.replace("^", " ");
		s = s.replace(":", " ");
		s = s.replace(",", " ");
		s = s.replace("!", " ");
		s = s.replace("@", " ");
		s = s.replace("#", " ");
		s = s.replace("$", " ");
		s = s.replace("%", " ");
		s = s.replace("*", " ");
		s = s.replace("~", " ");
		s = s.replace("(", " ");
		s = s.replace(")", " ");
		s = s.replace("_", " ");
		s = s.replace("{", " ");
		s = s.replace("}", " ");
		s = s.replace("�", " ");
		s = s.replace("&", " ");
		s = s.replace("/", " ");
		s = s.replace(";", " ");
		s = s.replace("\"", " ");
		s = s.replace("?", " ");
		s = s.replace("<", " ");
		s = s.replace(">", " ");		
		s = s.replace("'", " ");
		s = s.replace("=", " ");
		s = s.replace("|", " ");
		return s;
	}
	
	private static final String PLAIN_ASCII =
  	      "AaEeIiOoUu"    // grave
  	    + "AaEeIiOoUuYy"  // acute
  	    + "AaEeIiOoUuYy"  // circumflex
  	    + "AaOoNn"        // tilde
  	    + "AaEeIiOoUuYy"  // umlaut
  	    + "Aa"            // ring
  	    + "Cc"            // cedilla
  	    + "OoUu"          // double acute
  	    ;

  	    private static final String UNICODE =
  	     "\u00C0\u00E0\u00C8\u00E8\u00CC\u00EC\u00D2\u00F2\u00D9\u00F9"             
  	    + "\u00C1\u00E1\u00C9\u00E9\u00CD\u00ED\u00D3\u00F3\u00DA\u00FA\u00DD\u00FD" 
  	    + "\u00C2\u00E2\u00CA\u00EA\u00CE\u00EE\u00D4\u00F4\u00DB\u00FB\u0176\u0177" 
  	    + "\u00C3\u00E3\u00D5\u00F5\u00D1\u00F1"
  	    + "\u00C4\u00E4\u00CB\u00EB\u00CF\u00EF\u00D6\u00F6\u00DC\u00FC\u0178\u00FF" 
  	    + "\u00C5\u00E5"                                                             
  	    + "\u00C7\u00E7" 
  	    + "\u0150\u0151\u0170\u0171" 
  	    ;


  	    // remove accentued from a string and replace with ascii equivalent
  	    public static String convertNonAscii(String s) {
  	       if (s == null) return null;
  	       StringBuilder sb = new StringBuilder();
  	       int n = s.length();
  	       for (int i = 0; i < n; i++) {
  	          char c = s.charAt(i);
  	          int pos = UNICODE.indexOf(c);
  	          if (pos > -1){
  	              sb.append(PLAIN_ASCII.charAt(pos));
  	          }
  	          else {
  	              sb.append(c);
  	          }
  	       }
  	       return sb.toString();
  	    }
}
