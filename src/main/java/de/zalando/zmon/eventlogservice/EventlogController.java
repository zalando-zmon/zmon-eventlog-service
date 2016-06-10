package de.zalando.zmon.eventlogservice;

import com.fasterxml.jackson.databind.node.ObjectNode;
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

    @Autowired
    EventStore storage;

    @RequestMapping(value="/", method=RequestMethod.GET)
    List<Event> getEvents(@RequestParam(value="types") List<Integer> types, @RequestParam(value="key") String key, @RequestParam(value="value") String value) {
        return storage.getEvents(key, value, types, 100);
    }

    @RequestMapping(value="/", method={RequestMethod.PUT, RequestMethod.POST}, consumes = "application/json")
    void putEvents(@RequestBody List<Event> events) {
        for(Event e:  events) {
            ObjectNode attributes = (ObjectNode) e.getAttributes();
            if(attributes.has("alertId")) {
                attributes.put("alertId", "" + attributes.get("alertId"));
            }
            if(attributes.has("checkId")) {
                attributes.put("checkId", "" + attributes.get("checkId"));
            }

            if(e.getAttributes().has("alertId")) {
                storage.putEvent(e, "alertId");
            }
            else if(e.getAttributes().has("checkId")) {
                storage.putEvent(e, "checkId");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(EventlogController.class, args);
    }
}