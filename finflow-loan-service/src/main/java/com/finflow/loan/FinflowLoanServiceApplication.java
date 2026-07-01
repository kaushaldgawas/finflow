package com.finflow.loan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.finflow.loan.client")
public class FinflowLoanServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinflowLoanServiceApplication.class, args);
	}

}