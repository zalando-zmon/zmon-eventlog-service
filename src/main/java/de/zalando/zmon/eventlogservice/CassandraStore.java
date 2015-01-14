package de.zalando.zmon.eventlogservice;

import com.datastax.driver.core.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by jmussler on 1/14/15.
 */
@Service
public class CassandraStore implements EventStore {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraStore.class);

    private final String host;
    private final int port;
    private final String keyspace;

    private final Cluster cluster;

    @Autowired
    public CassandraStore(@Value("${cassandra.host}") String host,@Value("${cassandra.port}") int port,@Value("${cassandra.keyspace}") String keyspace) {
        this.host = host;
        this.port = port;
        this.keyspace = keyspace;

        LOG.info("Cassandra settings: {} {} {}", host, port, keyspace);

        cluster = Cluster.builder()
                .addContactPoint(host)
                .build();
    }

    @Override
    public void putEvent(Event event, String key) {

    }

    @Override
    public List<Event> getEvents(String key, String value) {
        return null;
    }
}
