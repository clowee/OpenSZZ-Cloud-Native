package org.openszzweb.samples.app.system;


import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.openszzweb.samples.app.model.Analysis;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
class WelcomeController {

	 private final RabbitTemplate rabbitTemplate;
     private String jiraAPI = "/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml";
     final String port = System.getenv("DISPATCHER_PORT");
     final String analysisUri = "http://results:"+port+"/doAnalysis";
     final String analysesUri  = "http://results:"+port+"/getAnalyses";
     final String removeUri  = "http://results:"+port+"/removeAnalysis";

	 private static final String topicExchangeSzz = "szz-analysis-exchange";
	 private static final String queueNameSzz = "szz-analysis";

    public WelcomeController (RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

	@RequestMapping(value = "/", method = RequestMethod.GET)
    public String welcome(Model model) {
    	    model.addAttribute(new Analysis());
        return "welcome";
    }

	@PostMapping("/doAnalysis")
    public String doAnalysis(@ModelAttribute Analysis analysis,BindingResult result, Model model) throws UnsupportedEncodingException {
	 System.out.println(analysisUri);
		RestTemplate restTemplate = new RestTemplate();
		List<String> errors = checkCorrectness(analysis);
        if (errors.size() != 0){
            analysis.setMessage(String.join(",", errors));
            model.addAttribute("analysis", analysis);
            return "error";
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(analysisUri)
            .queryParam("git", analysis.getGitUrl())
            .queryParam("useJira", analysis.getUseJira())
            .queryParam("addAllBFCToResult", analysis.getAddAllBFCToResult())
            .queryParam("useIssueInfo", analysis.getUseIssueInfo())
            .queryParam("reuseWorkingFiles",analysis.getReuseWorkingFiles())
            .queryParam("ignoreCommentChanges",analysis.getIgnoreCommentChanges());

        if (analysis.getJiraUrl().length() > 0) {
            builder.queryParam("jira", analysis.getJiraUrl());
        }
        if (analysis.getSearchQuery().length() > 0) {
            String encodedSearchQuery = URLEncoder.encode(analysis.getSearchQuery(), StandardCharsets.UTF_8.name());
            builder.queryParam("searchQuery", encodedSearchQuery);
        }
        if (analysis.getProjectName().length() > 0) {
            builder.queryParam("projectName", analysis.getProjectName());
        }
        if (analysis.getEmail().length() > 0) {
            builder.queryParam("email", analysis.getEmail());
        }

        String temp = builder.build().encode().toUri().toString();
        return restTemplate.getForEntity(temp, String.class).getBody();
    }

	@RequestMapping(value = "/adminPage", method = RequestMethod.GET)
    public String getAdminPage(Model model) {
		RestTemplate restTemplate = new RestTemplate();
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
	        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(analysesUri);
			String temp = builder.build().encode().toUri().toString();
			//List<Analysis> analyses = restTemplate.getForObject(temp, Analysis.class);
			ResponseEntity<List<Analysis>> response = restTemplate.exchange(
					  analysesUri,
					  HttpMethod.GET,
					  null,
					  new ParameterizedTypeReference<List<Analysis>>(){});
					List<Analysis> analyses = response.getBody();
			model.addAttribute("analyses", analyses);
			return "adminPage";
	}

	@PostMapping("/remove")
    public String remove(@ModelAttribute Analysis analysis,BindingResult result,Model model) {
		    HttpHeaders headers = new HttpHeaders();
	        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

	        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(removeUri)
	                .queryParam("token", analysis.getToken());

			String temp = builder.build().encode().toUri().toString();
			RestTemplate restTemplate = new RestTemplate();
			try{
			restTemplate.getForEntity(temp, String.class).getBody();
			}
			catch(Exception e){}
	        headers = new HttpHeaders();
	        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
	        builder = UriComponentsBuilder.fromHttpUrl(analysesUri);
			temp = builder.build().encode().toUri().toString();
			//List<Analysis> analyses = restTemplate.getForObject(temp, Analysis.class);
			ResponseEntity<List<Analysis>> response = restTemplate.exchange(
					  analysesUri,
					  HttpMethod.GET,
					  null,
					  new ParameterizedTypeReference<List<Analysis>>(){});
			List<Analysis> analyses = response.getBody();
			model.addAttribute("analyses", analyses);

			return "adminPage";
	}


	private List<String> checkCorrectness(Analysis analysis){
        List<String> errors = new ArrayList<>();
		//Checks url github
		try {
		    URL url = new URL(analysis.getGitUrl());
		    URLConnection conn = url.openConnection();
		    conn.connect();
		} catch (Exception e) {
            errors.add("Git URL is invalid");
		}
		if (analysis.getUseJira()) {
            try {
                String[] array = analysis.getJiraUrl().split("/projects/");
                String projectName = array[1].replaceAll("/", "");
                String jiraUrl = array[0] + jiraAPI;
                URL url = new URL(jiraUrl);
                URLConnection conn = url.openConnection();
                conn.connect();
            } catch (Exception e) {
                errors.add("Jira URL is invalid");
            }
        }
		return errors;
	}
}
