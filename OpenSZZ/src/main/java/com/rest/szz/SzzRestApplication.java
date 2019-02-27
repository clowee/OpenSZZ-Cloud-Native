package com.rest.szz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SzzRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(SzzRestApplication.class, args);
	}

}

