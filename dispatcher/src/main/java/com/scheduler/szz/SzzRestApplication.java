package com.scheduler.szz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.scheduler.szz.helpers.DBEntryDao;
import com.scheduler.szz.helpers.MessageReceivedComponent;

import javax.annotation.PostConstruct;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableAutoConfiguration(exclude = RabbitAutoConfiguration.class)
public class SzzRestApplication {

	static final String topicExchangeSzz = "szz-results-exchange";
	static final String queueNameSzz = "szz-results";

	@Autowired
	private DBEntryDao dbEntryDao;

	public static void main(String[] args) {
		SpringApplication.run(SzzRestApplication.class, args);
	}

	@Bean
	public ConnectionFactory connectionFactory() {
		com.rabbitmq.client.ConnectionFactory connectionFactory = new com.rabbitmq.client.ConnectionFactory();
		connectionFactory.setHost("rabbitmq");
		connectionFactory.setUsername("guest");
		connectionFactory.setPassword("guest");
		connectionFactory.setVirtualHost("/");
		CachingConnectionFactory connectionFactoryq = new CachingConnectionFactory(connectionFactory);
		return connectionFactoryq;
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
		return BindingBuilder.bind(queue).to(exchange).with("project.results.#");
	}

	@Bean
	public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
		return new RabbitAdmin(connectionFactory());
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
		return rabbitTemplate;
	}

	@Bean
	SimpleMessageListenerContainer containerAnaylsis(ConnectionFactory connectionFactory) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory());
		container.setQueueNames(queueNameSzz);
		container.setMessageListener(new MessageReceivedComponent(rabbitTemplate(connectionFactory()), dbEntryDao));
		return container;
	}

}
