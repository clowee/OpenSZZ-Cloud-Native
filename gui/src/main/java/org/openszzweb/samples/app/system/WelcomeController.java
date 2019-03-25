package org.openszzweb.samples.app.system;


import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

import org.openszzweb.samples.app.model.Analysis;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
class WelcomeController {
	
	 private final RabbitTemplate rabbitTemplate;
     private String jiraAPI = "/jira/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml";

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
    public String doAnalysis(@ModelAttribute Analysis analysis,BindingResult result, Model model) {
	       List<String> t = new LinkedList<String>();
	        t.add(analysis.getGitUrl());
	        t.add(analysis.getJiraUrl());
	        t.add(analysis.getEmail());
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
}
