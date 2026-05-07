package com.spring.ai.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpringAiProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAiProjectApplication.class, args);
	}

}
