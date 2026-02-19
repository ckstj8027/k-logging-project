package com.k8s.cnapp.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CnappAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(CnappAgentApplication.class, args);
	}

}
