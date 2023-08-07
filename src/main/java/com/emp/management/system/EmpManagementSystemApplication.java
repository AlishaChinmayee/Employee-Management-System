package com.emp.management.system;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class EmpManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmpManagementSystemApplication.class, args);
    
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}