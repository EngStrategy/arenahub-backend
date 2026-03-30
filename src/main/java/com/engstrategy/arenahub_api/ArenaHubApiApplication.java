package com.engstrategy.arenahub_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication (exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableAsync
@EnableScheduling
public class ArenaHubApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArenaHubApiApplication.class, args);
	}

}
