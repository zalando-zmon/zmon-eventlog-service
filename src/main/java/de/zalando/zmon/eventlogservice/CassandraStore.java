package de.zalando.zmon.eventlogservice;

import com.datastax.driver.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jmussler on 1/14/15.
 */
public class CassandraStore implements EventStore {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraStore.class);

    private final String host;
    private final int port;
    private final String keyspace;

    private final Cluster cluster;
    private final Session session;

    private PreparedStatement getByAlertId;
    private PreparedStatement getByCheckId;

    private PreparedStatement putByAlertId;
    private PreparedStatement putByCheckId;

    private void setupCassandra() {
        ResultSet rs = session.execute("SELECT * FROM system.schema_keyspaces WHERE keyspace_name = 'eventlog'");
        List<Row> rows = rs.all();
        if(rows.size()==0) {
            LOG.info("Creating eventlog keyspace");
            session.execute("CREATE KEYSPACE eventlog WITH replication = {'class':'SimpleStrategy', 'replication_factor': 2};");
        }

        SimpleStatement st = new SimpleStatement("select * from system.schema_columnfamilies where keyspace_name = 'eventlog' and columnfamily_name = 'events_by_alert_id';");
        st.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        rs = session.execute(st);
        rows = rs.all();
        if(rows.size()==0) {
            LOG.info("Creating table for events by alert id");
            session.execute("CREATE TABLE eventlog.events_by_alert_id(alert_id int, created timestamp, type int, entity text, instance_id int, data map<text,text>, PRIMARY KEY(alert_id, created)) WITH CLUSTERING ORDER BY (created DESC);");
        }

        st = new SimpleStatement("select * from system.schema_columnfamilies where keyspace_name = 'eventlog' and columnfamily_name = 'events_by_check_id';");
        st.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        rs = session.execute(st);
        rows = rs.all();
        if(rows.size()==0) {
            LOG.info("Creating table for events by check id");
            session.execute("CREATE TABLE eventlog.events_by_check_id(check_id int, created timestamp, type int, entity text, instance_id int, data map<text,text>, PRIMARY KEY(check_id, created)) WITH CLUSTERING ORDER BY (created DESC);");
        }

    }

    private void prepareStatements() {
        putByAlertId = session.prepare("INSERT INTO eventlog.events_by_alert_id(alert_id,created,type,entity,instance_id,data) VALUES(?,?,?,?,?,?)");
        putByCheckId = session.prepare("INSERT INTO eventlog.events_by_check_id(check_id,created,type,entity,instance_id,data) VALUES(?,?,?,?,?,?)");

        getByAlertId = session.prepare("SELECT * FROM eventlog.events_by_alert_id WHERE alert_id = ? LIMIT ?");
        getByCheckId = session.prepare("SELECT * FROM eventlog.events_by_check_id WHERE check_id = ? LIMIT ?");
    }

    public CassandraStore(String host, int port, String keyspace) {
        this.host = host;
        this.port = port;
        this.keyspace = keyspace;

        LOG.info("Cassandra settings: {} {} {}", host, port, keyspace);

        cluster = Cluster.builder()
                .addContactPoint(host)
                .build();

        session = cluster.newSession();

        setupCassandra();

        prepareStatements();
    }

    @Override
    public void putEvent(Event event, String key) {
        BoundStatement bst = null;
        if("alertId".equals(key)) {
            bst = putByAlertId.bind(event.getAttributes().get("alertId").asInt(), event.getTime(), event.getTypeId(), event.getAttributes().get("entity"), 0, event.getAttributes());
        }
        else if ("checkId".equals(key)) {
            bst = putByCheckId.bind(event.getAttributes().get("checkId").asInt(), event.getTime(), event.getTypeId(), event.getAttributes().get("entity"), 0, event.getAttributes());
        }

        if(bst!=null) {
            session.execute(bst);
        }
    }

    @Override
    public List<Event> getEvents(String key, String value, List<Integer> types, int limit) {
        List<Event> l = new ArrayList<>(limit);

        BoundStatement bst = null;
        if("alertId".equals(key)) {
            bst = getByAlertId.bind(Integer.parseInt(value), limit);
        }
        else if ("checkId".equals(key)) {
            bst = getByAlertId.bind(Integer.parseInt(value), limit);
        }

        if(bst==null) {
            return l;
        }

        ResultSet rs = session.execute(bst);

        for(Row r : rs) {
            if(!types.contains(r.getInt("type"))) {
                // sadly type IN(?) does not work with cassandra
                continue;
            }

            Event e = new Event();
            e.setTypeId(r.getInt("type"));
            e.setTime(r.getDate("created"));
            // e.setAttributes(r.getMap("data", String.class, String.class));
            l.add(e);
        }

        return l;
    }
}
