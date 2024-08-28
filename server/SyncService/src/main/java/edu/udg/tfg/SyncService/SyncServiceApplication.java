package edu.udg.tfg.SyncService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "edu.udg.tfg.FileManagement.feignClients")
public class SyncServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SyncServiceApplication.class, args);
	}

}
