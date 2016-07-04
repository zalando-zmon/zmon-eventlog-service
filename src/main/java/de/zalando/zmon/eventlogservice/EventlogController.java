package de.zalando.zmon.eventlogservice;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@RestController
@EnableAutoConfiguration
@Configuration
@ComponentScan
public class EventlogController {

    private static int DEFAULT_LIMIT = 100;
    private static Duration DEFAULT_DURATION = Duration.of(7, ChronoUnit.DAYS);

    @Autowired
    EventStore storage;

    @RequestMapping(value = {"/events", "/"}, method = RequestMethod.GET)
    List<Event> getEvents(@RequestParam(value = "types") List<Integer> types, @RequestParam(value = "key") String key, @RequestParam(value = "value") String value,
                          @RequestParam(value = "from") Optional<Long> from,
                          @RequestParam(value = "to") Optional<Long> to,
                          @RequestParam(value = "limit") Optional<Integer> limit) {
        long now = System.currentTimeMillis();
        long defaultFrom = now - DEFAULT_DURATION.toMillis();
        return storage.getEvents(key, value, types, from.orElse(defaultFrom), to.orElse(now), limit.orElse(DEFAULT_LIMIT));
    }

    @RequestMapping(value = {"/events", "/", "/api/v1"}, method = {RequestMethod.PUT, RequestMethod.POST}, consumes = "application/json")
    void putEvents(@RequestBody List<Event> events) {
        for (Event e : events) {
            ObjectNode attributes = (ObjectNode) e.getAttributes();
            if (attributes.has("alertId")) {
                attributes.put("alertId", "" + attributes.get("alertId"));
            }
            if (attributes.has("checkId")) {
                attributes.put("checkId", "" + attributes.get("checkId"));
            }

            storage.putEvent(e);
        }
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(EventlogController.class, args);
    }
}