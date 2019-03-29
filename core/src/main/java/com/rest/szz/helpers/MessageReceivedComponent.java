package com.rest.szz.helpers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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
				String token = java.util.UUID.randomUUID().toString().split("-")[0];
				a = new Application();
				if (a.mineData(gitUrl, jiraUrl.replace("{0}", projectName), projectName, token));
				  sendNotificationEmails(email,projectName,token);
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
