package com.movelt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MoveltApplication {
    public static void main(String[] args) {
        SpringApplication.run(MoveltApplication.class, args);
    }
}
