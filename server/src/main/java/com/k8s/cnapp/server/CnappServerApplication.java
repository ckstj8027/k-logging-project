package com.k8s.cnapp.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CnappServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CnappServerApplication.class, args);
	}

}
