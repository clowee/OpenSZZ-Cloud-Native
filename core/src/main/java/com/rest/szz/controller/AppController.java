package com.rest.szz.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rest.szz.git.Application;
import com.rest.szz.git.JiraRetriever;
import com.rest.szz.helpers.Email;

@RestController
public class AppController {
	private HashMap<String, Future> map = new HashMap<String, Future>();
	private HashMap<String, String> mapNames = new HashMap<String, String>();
	private HashMap<String, String> mapEmails = new HashMap<String, String>();
	private String jiraAPI = "/jira/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml";
	private String requestUrl = "";

	@Autowired
	Application a;

	/**
	 * jiraUrl input Format https://issues.apache.org/jira/projects/AMBARI/
	 *
	 * @param git
	 * @param jiraUrlProjects
	 * @return
	 */
	@RequestMapping("/SZZ")
	public String szz(@RequestParam(value = "git") String git,
			          @RequestParam(value = "jiraUrl") String jiraUrl,
			          @RequestParam(value = "email") String email,
			          HttpServletRequest request) {

		String[] array = null;
		String projectName = "";

		try {
			array = jiraUrl.split("/jira/projects/");
			projectName = array[1].replaceAll("/", "");
			jiraUrl = array[0] + jiraAPI;
		} catch (Exception e) {
			return "Invalid jiraUrl Format";
		}

		if (map.containsKey(projectName)) {
			if (!map.get(projectName).isDone())
				return "I am still analysing the project " + projectName;
			else {
				map.remove(projectName);
				return "The project " + projectName
						+ " has been already analysed. To analyse again, call this service another time.";
			}

		} else {
			try {
				String token = java.util.UUID.randomUUID().toString().split("-")[0];
				Future f = a.mineData(git, jiraUrl.replace("{0}", projectName), projectName,token);
				map.put(token, f);
				mapNames.put(token, projectName);
				mapEmails.put(token, email);
				requestUrl = request.getScheme() + "://" +   // "http" + "://
			             request.getServerName() +       // "myhost"
			             ":" +                           // ":"
			             request.getServerPort();    // "8080"
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			}
		}

		return "Analysing";
	}

	@RequestMapping("/getInducingCommits")
	public ResponseEntity<InputStreamResource> inducingCommits(
			@RequestParam(value = "token") String token) {
		File file = new File(token + ".csv");
		if (file.exists()) {
			HttpHeaders headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
			headers.add("Pragma", "no-cache");
			headers.add("content-disposition", "attachment; filename=" + mapNames.get(token) + "_BugInducingCommits.csv");
			headers.add("Expires", "0");
			InputStreamResource resource;
			try {
				resource = new InputStreamResource(new FileInputStream(file));
				return ResponseEntity.ok().headers(headers).contentLength(file.length())
						.contentType(MediaType.parseMediaType("application/csv")).body(resource);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		} else {
			return null;
		}

	}

	@RequestMapping("/test")
	public String test() {
		return "test";
	}

	@Scheduled(cron = "0 0/1 * * * *")
	public void sendNotificationEmails(){
		List<String> toRemove = new LinkedList<String>();
		for (String key : map.keySet()){
			if (map.get(key).isDone()){
				try {
					if ((boolean)(map.get(key).get())){
					Email e = new Email(mapEmails.get(key),key,mapNames.get(key),requestUrl+"/getInducingCommits?token="+key);
					e.sentEmail();}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				toRemove.add(key);
			}
		}
		for(String key : toRemove){
			map.remove(key);
		}

	}



}
