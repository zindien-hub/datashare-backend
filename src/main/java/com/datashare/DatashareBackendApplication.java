package com.datashare;

import com.datashare.configuration.FileUploadProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(FileUploadProperties.class)
public class DatashareBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatashareBackendApplication.class, args);
	}
}
