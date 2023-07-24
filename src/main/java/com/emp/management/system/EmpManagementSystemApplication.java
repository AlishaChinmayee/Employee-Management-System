package com.emp.management.system;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan(basePackages = "com.emp")
@SpringBootApplication
@EnableScheduling
public class EmpManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmpManagementSystemApplication.class, args);
    
    }
}