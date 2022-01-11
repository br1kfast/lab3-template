package com.lab2.loyaltyservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;

@SpringBootApplication
@EnableEurekaClient
@EnableHystrix
public class LoyaltyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoyaltyServiceApplication.class, args);
	}

}
