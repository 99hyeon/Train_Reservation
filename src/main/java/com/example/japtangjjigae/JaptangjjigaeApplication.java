package com.example.japtangjjigae;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class JaptangjjigaeApplication {

    public static void main(String[] args) {
        SpringApplication.run(JaptangjjigaeApplication.class, args);
    }

}
