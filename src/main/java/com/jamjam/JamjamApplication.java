package com.jamjam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class JamjamApplication {

	public static void main(String[] args) {
		SpringApplication.run(JamjamApplication.class, args);
	}

}
