package com.engstrategy.alugai_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AlugaiApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlugaiApiApplication.class, args);
	}

}
