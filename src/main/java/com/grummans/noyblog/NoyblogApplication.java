package com.grummans.noyblog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class NoyblogApplication {

	public static void main(String[] args) {
		SpringApplication.run(NoyblogApplication.class, args);
	}

}
