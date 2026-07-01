package com.finflow.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FinflowNotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinflowNotificationServiceApplication.class, args);
	}

}
