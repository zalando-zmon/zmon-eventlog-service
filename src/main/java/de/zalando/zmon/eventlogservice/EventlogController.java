package de.zalando.zmon.eventlogservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@EnableAutoConfiguration
@Configuration
@ComponentScan
public class EventlogController {

    private final static Logger LOG = LoggerFactory.getLogger(EventlogController.class);

    @Autowired
    EventStore storage;

    @RequestMapping(value="/", method=RequestMethod.GET)
    List<Event> getEvents(@RequestParam(value="types") List<String> types, @RequestParam(value="key") String key, @RequestParam(value="value") String value)   {
        LOG.info("Get events");
        return null;
    }

    @RequestMapping(value="/", method=RequestMethod.PUT, consumes = "application/json")
    void putEvents(@RequestBody List<Event> events) {
        LOG.info("{}", events);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(EventlogController.class, args);
    }
}