package com.example.homologacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HomologacaoApplication {

	public static void main(String[] args) {
		SpringApplication.run(HomologacaoApplication.class, args);
	}

}
