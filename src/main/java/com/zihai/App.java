package com.zihai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


@SpringBootApplication
//(exclude= {DataSourceAutoConfiguration.class})
public class Example {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Example.class, args);
	}

}