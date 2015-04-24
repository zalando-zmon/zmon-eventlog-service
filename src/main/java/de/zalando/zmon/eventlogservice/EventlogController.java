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
    List<Event> getEvents(@RequestParam(value="types") List<Integer> types, @RequestParam(value="key") String key, @RequestParam(value="value") String value) {
        return storage.getEvents(key, value, types, 100);
    }

    @RequestMapping(value="/", method=RequestMethod.PUT, consumes = "application/json")
    void putEvents(@RequestBody List<Event> events) {
        for(Event e:  events) {
            if(e.getAttributes().containsKey("alertId")) {
                storage.putEvent(e, "alertId");
            }
            else if(e.getAttributes().containsKey("checkId")) {
                storage.putEvent(e, "checkId");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(EventlogController.class, args);
    }
}