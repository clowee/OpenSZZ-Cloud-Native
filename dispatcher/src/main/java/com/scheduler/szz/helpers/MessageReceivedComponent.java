package com.scheduler.szz.helpers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;


@Component
@RestController
public class MessageReceivedComponent implements MessageListener {

	RabbitTemplate rabbitTemplate;

	public MessageReceivedComponent(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public void onMessage(Message message) {
			Logger l = Logger.getLogger(MessageReceivedComponent.class);
			String routingKey = message.getMessageProperties().getReceivedRoutingKey();
            System.out.println(routingKey);
            routingKey = routingKey + "";
            System.out.println(routingKey);
			String[] array = routingKey.split("\\.");
			String projectName = array[2];
			String token = array[3];
			String email = "";
			for (int i = 4; i < array.length; i++)
				email += array[i] + ".";
			email = email.substring(0,email.length()-1);
			int index = routingKey.lastIndexOf(".") + 1;
			ObjectInputStream ois = null;
			try {
				byte[] resource = message.getBody();
				FileUtils.writeByteArrayToFile(new File("mydata/"+ token + ".csv"), resource);
				sendNotificationEmails(email,projectName,token);
			}
			catch (Exception e){
				e.printStackTrace();
			}
			
		}
	
	
	public void sendNotificationEmails(String email, String projectName, String token){
		Email e = new Email(email,token,projectName,System.getenv("SERVER")+":8888/getInducingCommits?token="+token+"&projectName="+projectName);
	    e.sentEmail();
	}
	
	
	
	
	
	
}
