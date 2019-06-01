package com.scheduler.szz.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scheduler.szz.model.Analysis;

@RestController
public class AppController {
	
	 private final RabbitTemplate rabbitTemplate;
     private String jiraAPI = "/jira/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml";

	 private static final String topicExchangeSzz = "szz-analysis-exchange";
	 private static final String queueNameSzz = "szz-analysis";
	
    public AppController (RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
	
	
	@RequestMapping("/test")
	public String test() {
		return "test";
	}
	
	@RequestMapping(value = "/doAnalysis",method = RequestMethod.GET)
	public String doAnalysis(@RequestParam String git,@RequestParam String jira,@RequestParam String email, Model model) {
	       List<String> t = new LinkedList<String>();
	        t.add(git);
	        t.add(jira);
	        t.add(email); 
	        Analysis analysis = new Analysis();
	        analysis.setEmail(email);
	        analysis.setGitUrl(git);
	        analysis.setJiraUrl(jira);
	        rabbitTemplate.convertAndSend("szz-analysis-exchange", "project.name."+analysis.getProjectName(), t);
	        if (!checkCorrectness(analysis))
	        		return "error";
	        model.addAttribute(analysis);
	        return "resultPage";
	}
	
	private boolean checkCorrectness(Analysis analysis){
		//Checks url github
		try {
		    URL url = new URL(analysis.getGitUrl());
		    URLConnection conn = url.openConnection();
		    conn.connect();
		} catch (Exception e) {
		    return false;
		} 
		//Checks url github
		try {
			String[] array = analysis.getJiraUrl().split("/jira/projects/");
			String projectName = array[1].replaceAll("/", "");
			String jiraUrl = array[0] + jiraAPI;
		    URL url = new URL(jiraUrl);
		    URLConnection conn = url.openConnection();
		    conn.connect();
		} catch (Exception e) {
		    return false;
		} 
		return true;
	}
	
	@RequestMapping("/getInducingCommits")
	public ResponseEntity<InputStreamResource> inducingCommits(
			@RequestParam(value = "token") String token,
			@RequestParam(value = "projectName") String projectName
			) {
		File file = new File("mydata/"+ token + ".csv");
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


}
