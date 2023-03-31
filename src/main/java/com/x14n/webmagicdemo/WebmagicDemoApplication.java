package com.x14n.webmagicdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WebmagicDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebmagicDemoApplication.class, args);
    }

}
