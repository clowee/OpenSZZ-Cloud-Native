package com.rest.szz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import com.rest.szz.helpers.Email;
import com.rest.szz.helpers.MessageReceivedComponent;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class SzzRestApplication {
	
	static final String topicExchangeSzz = "szz-analysis-exchange";
    static final String queueNameSzz = "szz-analysis";

	public static void main(String[] args) {
		SpringApplication.run(SzzRestApplication.class, args);
	}
	
    @Bean
    Queue queue() {
        return new Queue(queueNameSzz, false);
    }
    
    @Bean
    TopicExchange exchange() {
        return new TopicExchange(topicExchangeSzz);
    }
    
    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("project.name.#");
    }
    
    @Bean
    SimpleMessageListenerContainer containerAnaylsis(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueNameSzz);
        container.setMessageListener(new MessageReceivedComponent(rabbitTemplate(connectionFactory)));
        return container;
    }
    
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }


}

