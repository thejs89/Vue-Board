package com.board;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(scanBasePackages = { "com.board" }, exclude = { DataSourceAutoConfiguration.class })
public class BoardWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoardWebApplication.class, args);
	}

}

