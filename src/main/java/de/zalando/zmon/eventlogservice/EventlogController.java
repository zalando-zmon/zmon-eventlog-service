package de.zalando.zmon.eventlogservice;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class EventlogController {

    private final EventStore storage;

    @Autowired
    EventlogController(EventStore eventStore) {
        Assert.notNull(eventStore, "'EventStore' should never be null");
        this.storage = eventStore;
    }

    @RequestMapping("/")
    List<Event> getEvents(@RequestParam(value = "types") List<Integer> types, @RequestParam(value = "key") String key,
            @RequestParam(value = "value") String value) {
        return storage.getEvents(key, value, types, 100);
    }

    @RequestMapping(value = "/", method = RequestMethod.PUT, consumes = "application/json")
    void putEvents(@RequestBody List<Event> events) {
        if (storage.isBatchSupported()) {
            // in postgres we do not really care about attributes,right?
            // but I do the filtering
            List<Event> filtered = events.stream().filter(EVENT_FILTER).collect(Collectors.toList());
            storage.storeInBatch(filtered);
        } else {
            for (Event e : events) {
                if (e.getAttributes().containsKey("alertId")) {
                    storage.putEvent(e, "alertId");
                } else if (e.getAttributes().containsKey("checkId")) {
                    storage.putEvent(e, "checkId");
                }
            }
        }
    }

    private static final Predicate<Event> EVENT_FILTER = new EventFilterByAttribute("alertId")
            .or(new EventFilterByAttribute("checkId"));

    static class EventFilterByAttribute implements Predicate<Event> {

        private final String attributesKey;

        public EventFilterByAttribute(String attributeKey) {
            Assert.hasText(attributeKey, "'attributeKey' should never be null or empty");
            this.attributesKey = attributeKey;
        }

        @Override
        public boolean test(Event t) {
            return t.getAttributes().containsKey(attributesKey);
        }

    }
}