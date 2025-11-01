package com.example.lastvaluepriceservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 Main application class.
 @SpringBootApplication - enables component scan, auto-configuration and property support.
*/
@SpringBootApplication
public class LastValuePriceServiceApplication {
    // Main method — entry point when running 'mvn spring-boot:run' or the packaged jar
    public static void main(String[] args) {
        SpringApplication.run(LastValuePriceServiceApplication.class, args);
    }
}
