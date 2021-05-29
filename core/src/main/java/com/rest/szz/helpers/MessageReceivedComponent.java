package com.rest.szz.helpers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;


import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import com.rest.szz.git.Application;

@Component
@RestController
public class MessageReceivedComponent implements MessageListener {

	RabbitTemplate rabbitTemplate;
	private String jiraAPI = "/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml";

	private Application a;

	public MessageReceivedComponent(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public void onMessage(Message message) {
		{
			Logger l = Logger.getLogger(MessageReceivedComponent.class);
			String routingKey = message.getMessageProperties().getReceivedRoutingKey();
			int index = routingKey.lastIndexOf(".") + 1;
			String projectName = routingKey.substring(index);
			List<String> settings = new LinkedList<String>();
			LinkedList<String> list = new LinkedList<String>();
			ObjectInputStream ois = null;
			String[] array = null;
			try {
				ois = new ObjectInputStream(new ByteArrayInputStream(message.getBody()));
				list = (LinkedList<String>) ois.readObject();
				String gitUrl = list.get(0);
				l.info(gitUrl);
				String jiraUrl = list.get(1);
				l.info(jiraUrl);
				String email = list.get(2);
				if (jiraUrl != null) {
                    array = jiraUrl.split("/projects/");
                    projectName = array[1].replaceAll("/", "");
                    jiraUrl = array[0] + jiraAPI;
                    jiraUrl = jiraUrl.replace("{0}", projectName);
                }
                String token = list.get(3);
                System.out.println(token);
                String searchQuery = list.get(4);
                Boolean addAllBFCToResult = Boolean.parseBoolean(list.get(5));
                Boolean useIssueInfo = Boolean.parseBoolean(list.get(6));
                String isBrokenByLinkName = list.get(7);
                if (searchQuery != null) {
                    searchQuery = URLDecoder.decode(searchQuery, StandardCharsets.UTF_8.name());
                    searchQuery = URLDecoder.decode(searchQuery, StandardCharsets.UTF_8.name());
                }
                Boolean reuseWorkingFiles = Boolean.parseBoolean(list.get(8));
                Boolean ignoreCommentChanges = Boolean.parseBoolean(list.get(9));

                File workingDirectory = Paths.get(System.getProperty("user.dir") + File.separator + "home" + File.separator + projectName).toFile();
                workingDirectory.mkdirs();
                
				if (!reuseWorkingFiles) {
                    FileUtils.cleanDirectory(workingDirectory);
                }
                a = new Application(workingDirectory.toString());
				if (a.mineData(gitUrl, jiraUrl, projectName, searchQuery, token, addAllBFCToResult, useIssueInfo, isBrokenByLinkName, ignoreCommentChanges)){
					File file = new File(workingDirectory.toString() + File.separator + token + ".csv");
					ObjectOutputStream objectOutputStream =
						    new ObjectOutputStream(new FileOutputStream("object.data"));
					objectOutputStream.writeObject(Files.readAllBytes(file.toPath()));
					objectOutputStream.close();

					Message m = MessageBuilder.withBody(Files.readAllBytes(file.toPath())).setHeader("ContentType", "text/csv").build();

					if (email == null) {
                        email = "example@email.com";
                    }
                    rabbitTemplate.convertAndSend("szz-results-exchange", "project.results."+projectName +"." +token+"."+email, m);
				}
				ois.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	public void sendNotificationEmails(String email, String projectName, String token){
		Email e = new Email(email,token,projectName,System.getenv("SERVER")+":8888/getInducingCommits?token="+token+"&projectName="+projectName);
	    e.sentEmail();
	}






}
