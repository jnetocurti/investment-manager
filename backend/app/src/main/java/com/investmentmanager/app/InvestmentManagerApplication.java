package com.investmentmanager.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = "com.investmentmanager")
@EnableMongoRepositories(basePackages = "com.investmentmanager")
public class InvestmentManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvestmentManagerApplication.class, args);
    }
}
