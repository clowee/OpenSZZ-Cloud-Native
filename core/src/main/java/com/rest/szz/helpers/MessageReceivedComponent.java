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

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rest.szz.git.Application;

@Component
public class MessageReceivedComponent implements MessageListener {

	RabbitTemplate rabbitTemplate;
	private String jiraAPI = "/jira/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml";

	
	@Autowired
	Application a;

	public MessageReceivedComponent(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public void onMessage(Message message) {
		{
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
				String jiraUrl = list.get(1);
				String email = list.get(2);
				array = jiraUrl.split("/jira/projects/");
				projectName = array[1].replaceAll("/", "");
				jiraUrl = array[0] + jiraAPI;
				String token = java.util.UUID.randomUUID().toString().split("-")[0];
				Future f = a.mineData(gitUrl, jiraUrl.replace("{0}", projectName), projectName, token);
				while(!f.isDone()){
					Thread.sleep(10);
				}
			    
				ois.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public void sendNotificationEmails(String email, String projectName, String token, String requestUrl){
		Email e = new Email(email,token,projectName,requestUrl+"/getInducingCommits?token="+token);
	    e.sentEmail();
	}
	
	
	
	
	
	
}
