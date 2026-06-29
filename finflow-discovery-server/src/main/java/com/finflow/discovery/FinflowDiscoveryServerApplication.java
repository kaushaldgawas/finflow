package com.finflow.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer          // ← This single annotation turns it into a Discovery Server
public class FinflowDiscoveryServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinflowDiscoveryServerApplication.class, args);
	}

}