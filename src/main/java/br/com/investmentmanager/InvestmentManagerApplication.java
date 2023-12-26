package br.com.investmentmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class InvestmentManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvestmentManagerApplication.class, args);
    }

}
