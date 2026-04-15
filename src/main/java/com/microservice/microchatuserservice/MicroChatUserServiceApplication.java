package com.microservice.microchatuserservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MicroChatUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroChatUserServiceApplication.class, args);
    }

}
