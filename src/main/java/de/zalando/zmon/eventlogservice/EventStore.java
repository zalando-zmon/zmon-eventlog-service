package de.zalando.zmon.eventlogservice;

import java.util.List;

/**
 * Created by jmussler on 1/14/15.
 */
public interface EventStore {
    void putEvent(Event event);
    List<Event> getEvents(String key, String value, List<Integer> types, long from, long to, int limit);
}
