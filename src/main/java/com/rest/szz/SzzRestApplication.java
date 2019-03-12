package com.rest.szz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.rest.szz.helpers.Email;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class SzzRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(SzzRestApplication.class, args);
	}

}

