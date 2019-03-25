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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rest.szz.git.Application;
import com.rest.szz.helpers.Email;

@RestController
public class AppController {
	private HashMap<String, Future> map = new HashMap<String, Future>();
	private HashMap<String, String> mapNames = new HashMap<String, String>();
	private HashMap<String, String> mapEmails = new HashMap<String, String>();
	private String jiraAPI = "/jira/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml";
	private String requestUrl = "";


	@RequestMapping("/getInducingCommits")
	public ResponseEntity<InputStreamResource> inducingCommits(
			@RequestParam(value = "token") String token,
			@RequestParam(value = "projectName") String projectName
			) {
		File file = new File(token + ".csv");
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
