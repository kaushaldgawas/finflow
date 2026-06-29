package com.finflow.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer          // ← This single annotation turns it into a Config Server
public class FinflowConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinflowConfigServerApplication.class, args);
	}

}