package com.rest.szz.helpers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import com.rest.szz.git.Application;

@Component
@RestController
public class MessageReceivedComponent implements MessageListener {

	RabbitTemplate rabbitTemplate;
	private String jiraAPI = "/jira/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml";

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
				array = jiraUrl.split("/jira/projects/");
				projectName = array[1].replaceAll("/", "");
				jiraUrl = array[0] + jiraAPI;
                String token = list.get(3);
                System.out.println(token);
				a = new Application();
				if (a.mineData(gitUrl, jiraUrl.replace("{0}", projectName), projectName, token)){
					File file = new File("home/"+ token + ".csv");
					ObjectOutputStream objectOutputStream = 
						    new ObjectOutputStream(new FileOutputStream("object.data"));
					objectOutputStream.writeObject(Files.readAllBytes(file.toPath()));
					objectOutputStream.close();
					
					Message m = MessageBuilder.withBody(Files.readAllBytes(file.toPath())).setHeader("ContentType", "text/csv").build();
							
					rabbitTemplate.convertAndSend("szz-results-exchange", "project.results."+projectName +"." +token+"."+email, m);
					FileUtils.cleanDirectory(new File("home")); 
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
