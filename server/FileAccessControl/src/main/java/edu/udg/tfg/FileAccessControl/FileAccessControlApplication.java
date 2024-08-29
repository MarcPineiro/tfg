package edu.udg.tfg.FileAccessControl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FileAccessControlApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileAccessControlApplication.class, args);
	}

}
