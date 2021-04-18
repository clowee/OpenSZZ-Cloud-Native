package com.scheduler.szz.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.lite2073.emailvalidator.EmailValidationResult.State;
import com.github.lite2073.emailvalidator.EmailValidator;
import com.github.lite2073.emailvalidator.impl.IsEmailEmailValidator;
import com.scheduler.szz.helpers.DBEntryDao;
import com.scheduler.szz.model.Analysis;
import com.scheduler.szz.model.DBEntry;

@RestController
public class AppController {

	 @Autowired
	 DBEntryDao dbEntryDao;

	 private final RabbitTemplate rabbitTemplate;
     private String jiraAPI = "/jira/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml";

    /**
     * Controller for managing exchanges between gui and core containers
     * @param rabbitTemplate
     */
    public AppController (RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RequestMapping(value = "/removeAnalysis",method = RequestMethod.GET)
	public String remove(@RequestParam String token) {
    	dbEntryDao.deleteByToken(token);
    	return "remove";
    }

	@RequestMapping(value = "/doAnalysis",method = RequestMethod.GET)
	public String doAnalysis(
	    @RequestParam String git,
        @RequestParam(required = false) String jira,
        @RequestParam Boolean useJira,
        @RequestParam(required = false) String searchQuery,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String projectName,
        @RequestParam Boolean addAllBFCToResult,
        @RequestParam Boolean useIssueInfo,
        @RequestParam(required = false) String isBrokenByLinkName,
        Model model
    ) throws UnsupportedEncodingException {
		String token = java.util.UUID.randomUUID().toString().split("-")[0];
        Analysis analysis = new Analysis();
        analysis.setUseJira(useJira);
        analysis.setGitUrl(git);
        analysis.setAddAllBFCToResult(addAllBFCToResult);
        analysis.setUseIssueInfo(useIssueInfo);
		if (isBrokenByLinkName != null) {
			analysis.setIsBrokenByLinkName(isBrokenByLinkName);
		}
        if (useJira) {
            analysis.setJiraUrl(jira);
        } else {
            analysis.setSearchQuery(searchQuery);
        }
        if (projectName != null) {
            analysis.setProjectName(projectName);
        } else {
            Pattern pattern = Pattern.compile("[^/]+(?=.git$)");
            Matcher matcher = pattern.matcher(git);
            if (matcher.find())
            {
                analysis.setProjectName(matcher.group(0));
            }
        }
        if (email != null) {
            analysis.setEmail(email);
        }
        analysis.setToken(token);
        analysis.setStatus("PROCESSING");
        analysis.setDateStart(new Date().getTime());
        List<String> errors = checkCorrectness(analysis);
        if (errors.size() != 0){
            analysis.setMessage(String.join(",", errors));
            model.addAttribute("analysis", analysis);
            return "error";
        }
        List<String> t = new LinkedList<String>();
        t.add(git);
        t.add(jira);
        t.add(email);
        t.add(token);
        t.add(searchQuery);
        t.add(addAllBFCToResult.toString());
        t.add(useIssueInfo.toString());
        t.add(analysis.getIsBrokenByLinkName());
        rabbitTemplate.convertAndSend("szz-analysis-exchange", "project.name."+analysis.getProjectName(), t);
        insertAnalysisDb(analysis);
        model.addAttribute(analysis);
        return "resultPage";
	}

	@RequestMapping(value = "/getAnalyses",method = RequestMethod.GET)
	public List<Analysis> getAnalyses() {
		List<DBEntry> ded = dbEntryDao.findAll();
		List<Analysis> analyses = new LinkedList<Analysis>();
		for (DBEntry d : ded){
			Analysis a = new Analysis();
			a.setProjectName(d.getProjectName());
			a.setDateStart(d.getStartEpoch());
			a.setDateEnd(d.getEndEpoch());
			a.setEmail(d.getEmail());
			a.setGitUrl(d.getGitUrl());
			a.setJiraUrl(d.getJiraUrl());
			a.setStatus(d.getStatus().toString());
			a.setToken(d.getToken());
			if (a.getStatus().equals("ANALYSED"))
				a.setMessage(System.getenv("SERVER")+":"+System.getenv("DISPATCHER_PORT")+"/getInducingCommits?token="+a.getToken()+"&projectName="+a.getProjectName());
			analyses.add(a);
		}
		return analyses;
	}


	/**
	 * Inserts analysis into a mongo  db
	 * @param a
	 */
	private void insertAnalysisDb(Analysis a){
		DBEntry dbe = new DBEntry();
		dbe.setEmail(a.getEmail());
		dbe.setJiraUrl(a.getJiraUrl());
		dbe.setToken(a.getToken());
		dbe.setGitUrl(a.getGitUrl());
		dbe.setProjectName(a.getProjectName());
		Date now = new Date();
		dbe.setStartEpoch(now.getTime());
		dbe.setStatus(DBEntry.Status.PROCESSING);
		dbEntryDao.insert(dbe);
	}


	/**
	 * Checks whether the input are formerly correct
	 * @param analysis
	 * @return
	 */
	private List<String> checkCorrectness(Analysis analysis){
        List<String> errors = new ArrayList<>();

		if (!analysis.getGitUrl().endsWith(".git")) {
		    errors.add("Git URL is invalid");
        }

		//Checks url github
		try {
		    URL url = new URL(analysis.getGitUrl());
		    URLConnection conn = url.openConnection();
		    conn.connect();
		} catch (Exception e) {
            errors.add("Git URL is invalid");
		}

		if (analysis.getUseJira()) {
            //Checks url jira
            try {
                String[] array = analysis.getJiraUrl().split("/jira/projects/");
                String projectName = array[1].replaceAll("/", "");
                String jiraUrl = array[0] + jiraAPI;
                URL url = new URL(jiraUrl);
                URLConnection conn = url.openConnection();
                conn.connect();
            } catch (Exception e) {
                errors.add("Jira URL is invalid");
            }
        }
        if (analysis.getEmail() != null) {
            EmailValidator emailValidator = new IsEmailEmailValidator();
            State emailState = emailValidator.validate(analysis.getEmail()).getState();
            System.out.println(emailState);
            if (emailState != State.OK){
                System.out.println(emailState);
                errors.add("Email is not valid");
            }
        }

		return errors;
	}


	/**
	 * It returns results of a completed analysis
	 * @param token
	 * @param projectName
	 * @return
	 */
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

	public DBEntryDao getDao(){
		return dbEntryDao;
	}


}
