package com.finflow.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.finflow.analytics.client")
public class FinflowAnalyticsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinflowAnalyticsServiceApplication.class, args);
	}

}
