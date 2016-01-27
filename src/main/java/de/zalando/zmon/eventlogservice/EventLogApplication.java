package de.zalando.zmon.eventlogservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EventLogApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(EventlogController.class, args);
    }

}
