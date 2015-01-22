package de.zalando.zmon.eventlogservice;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.core.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jmussler on 1/22/15.
 */
public class PostgresqlStore implements EventStore {

    private final static Logger LOG = LoggerFactory.getLogger(PostgresqlStore.class);

    private final HikariDataSource ds;
    private final String queryGet;
    private final String queryInsert;

    private static final ObjectMapper mapper = new ObjectMapper();

    public PostgresqlStore(String host, int port, String database, String user, String password, String schema) {
        HikariConfig conf = new HikariConfig();
        conf.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + database);
        conf.setUsername(user);
        conf.setPassword(password);
        conf.setMaximumPoolSize(12);
        ds = new HikariDataSource(conf);

        queryInsert = "INSERT INTO "+schema+".events(e_type_id, e_created, e_instance_id, e_data) VALUES(?,?,?,?::jsonb)";
        queryGet = "SELECT e_type_id, e_created, e_instance_id, e_data, et_name FROM "+schema+".events, zmon_events.event_types WHERE et_id = e_type_id AND e_data @> '";

    }

    @Override
    public void putEvent(Event event, String key) {
        Connection conn = null;

        try {
            conn = ds.getConnection();
            PreparedStatement st = conn.prepareStatement(queryInsert);
            st.setInt(1, event.getTypeId());
            st.setTimestamp(2, new java.sql.Timestamp(event.getTime().getTime()));
            st.setInt(3, 0);
            st.setString(4, mapper.writeValueAsString(event.getAttributes()));
            st.execute();

        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialize map", e);
        }
        catch (SQLException ex) {
            LOG.error("", ex);
        }
        finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public List<Event> getEvents(String key, String value, List<Integer> types, int limit) {
        Connection conn = null;

        try {
            conn = ds.getConnection();

            Statement st = conn.createStatement();
            StringBuffer b = new StringBuffer();
            b.append(queryGet);
            Utils.appendEscapedLiteral(b,"{\""+key+"\":\""+value+"\"}", true);
            b.append("'");

            ResultSet rs = st.executeQuery(b.toString());

            List<Event> events = new ArrayList<>();
            while(rs.next()) {
                Event e = new Event();
                e.setTime(rs.getTime(2));
                e.setTypeId(rs.getInt(1));
                e.setTypeName(rs.getString(5));

                Map<String, String> data = mapper.readValue(rs.getString(4), new TypeReference<Map<String,String>>(){});
                e.setAttributes(data);
                events.add(e);
            }
            return events;
        }
        catch (SQLException ex) {
            LOG.error("", ex);
        } catch (JsonMappingException ex) {
            LOG.error("", ex);
        } catch (JsonParseException ex) {
            LOG.error("", ex);
        } catch (IOException ex) {
            LOG.error("", ex);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                LOG.error("", e);
            }
        }

        return new ArrayList<>(0);
    }
}
