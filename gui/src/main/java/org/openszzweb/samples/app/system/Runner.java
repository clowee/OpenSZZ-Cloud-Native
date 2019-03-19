package org.openszzweb.samples.app.system;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Component
@RestController
public class Runner implements CommandLineRunner {

    private final RabbitTemplate rabbitTemplate;

    public Runner (RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        /*System.out.println("Sending message...");
        List<String> t = new LinkedList<String>();
        t.add("rrr");
        t.add("rrr3");
        rabbitTemplate.convertAndSend("szz-analysis-exchange", "project.name.bcel", t);*/
        
        //receiver.getLatch().await(10000, TimeUnit.MILLISECONDS);
    }
    
    @RequestMapping("/test")
	public String newProject(@RequestParam(value = "message") String message){
        System.out.println("Sending message...");
        List<String> t = new LinkedList<String>();
        t.add("rrr");
        t.add("rrr3");
        rabbitTemplate.convertAndSend("szz-analysis-exchange", "project.name.bcel", message);
        return  "OK";
	}

}