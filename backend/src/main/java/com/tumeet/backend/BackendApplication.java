package com.tumeet.backend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class BackendApplication {

	@PostConstruct
	public void initTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Bangkok"));
	}

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Bangkok"));
		SpringApplication.run(BackendApplication.class, args);
	}

}
