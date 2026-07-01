package com.finflow.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.finflow.transaction.client")
public class FinflowTransactionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinflowTransactionServiceApplication.class, args);
	}

}