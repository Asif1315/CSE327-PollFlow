package com.pollflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PollFlowApplication {
    public static void main(String[] args) {
        SpringApplication.run(PollFlowApplication.class, args);
    }
}
