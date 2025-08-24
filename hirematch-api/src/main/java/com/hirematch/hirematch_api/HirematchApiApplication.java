package com.hirematch.hirematch_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HirematchApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(HirematchApiApplication.class, args);
	}

}
