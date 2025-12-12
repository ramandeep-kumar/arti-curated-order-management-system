package com.articurated;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories
@EnableAsync
@EnableTransactionManagement
public class ArtiCuratedApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArtiCuratedApplication.class, args);
    }
}
