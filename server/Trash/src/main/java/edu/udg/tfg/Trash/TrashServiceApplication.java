package edu.udg.tfg.Trash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TrashServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrashServiceApplication.class, args);
	}

}
