package com.rest.szz.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties.Tomcat.Resource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rest.szz.git.Application;
import com.rest.szz.git.JiraRetriever;

@RestController
public class AppController {
	private HashMap<String, Future> map = new HashMap<String, Future>();
	private String jiraAPI = "/jira/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml";

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
	public String szz(@RequestParam(value = "git") String git, @RequestParam(value = "jiraUrl") String jiraUrl) {

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
				Future f = a.mineData(git, jiraUrl.replace("{0}", projectName), projectName);
				map.put(projectName, f);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			}
		}

		return "Analysing";
	}

	@RequestMapping("/getInducingCommits")
	public ResponseEntity<InputStreamResource> inducingCommits(
			@RequestParam(value = "projectName") String projectName) {
		File file = new File(projectName + "_BugInducingCommits.csv");
		if (file.exists()) {
			HttpHeaders headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
			headers.add("Pragma", "no-cache");
			headers.add("content-disposition", "attachment; filename=" + projectName + "_BugInducingCommits.csv");
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

}
