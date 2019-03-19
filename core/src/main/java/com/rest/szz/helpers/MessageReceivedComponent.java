package com.rest.szz.helpers;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageReceivedComponent implements MessageListener {

	RabbitTemplate rabbitTemplate;

	public MessageReceivedComponent(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public void onMessage(Message message) {
		System.out.println(message.toString());
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*try {
			String routingKey = message.getMessageProperties().getReceivedRoutingKey();
			int index = routingKey.lastIndexOf(".") + 1;
			String projectName = routingKey.substring(index);
			List<String> settings = new LinkedList<String>();
			LinkedList<String> list = new LinkedList<String>();
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(message.getBody()));
			try {
			    list  = (LinkedList<String>) ois.readObject();
			} finally {
			    ois.close();
			}
		    
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

	
}
