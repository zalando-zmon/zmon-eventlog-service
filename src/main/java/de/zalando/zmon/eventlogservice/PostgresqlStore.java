package de.zalando.zmon.eventlogservice;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
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

        queryInsert = "INSERT INTO " + schema + ".events(e_type_id, e_created, e_data) VALUES(?,?,?::jsonb)";
        queryGet = "SELECT e_type_id, e_created, e_data, et_name FROM " + schema + ".events, " + schema + ".event_types WHERE et_id = e_type_id AND ";

    }

    @Override
    public void putEvent(Event event) {

        try (Connection conn = ds.getConnection()){
            PreparedStatement st = conn.prepareStatement(queryInsert);
            st.setInt(1, event.getTypeId());
            st.setTimestamp(2, new java.sql.Timestamp(event.getTime().getTime()));
            st.setString(3, mapper.writeValueAsString(event.getAttributes()));
            st.execute();

        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialize map", e);
        } catch (SQLException ex) {
            LOG.error("", ex);
        }
    }

    @Override
    public List<Event> getEvents(String key, String value, List<Integer> types, long from, long to, int limit) {
        try (Connection conn = ds.getConnection()) {
            Statement st = conn.createStatement();
            StringBuilder b = new StringBuilder();
            b.append(queryGet);
            // Utils.appendEscapedLiteral(b, "{\"" + key + "\":\"" + value + "\"}", true);
            if(key.equals("checkId")) {
                b.append("(e_data->'checkId')::text = '\"");
                Utils.escapeLiteral(b, value, true);
                b.append("\"'");
            }
            else if(key.equals("alertId")) {
                b.append("(e_data->'alertId')::text = '\"");
                Utils.escapeLiteral(b, value, true);
                b.append("\"'");
            }
            else {
                return null;
            }

            if (null != types && types.size() > 0) {
                b.append(" AND e_type_id IN (");
                boolean first = true;
                for (Integer t : types) {
                    if (!first) {
                        b.append(",");
                    }
                    first = false;
                    b.append(t.toString());
                }
                b.append(")");
            }
            b.append(" AND e_created BETWEEN TO_TIMESTAMP(" + (from / 1000) + ") AND TO_TIMESTAMP(" + (to / 1000) + ")");
            b.append(" ORDER BY e_created DESC ");
            b.append(" LIMIT " + limit);

            ResultSet rs = st.executeQuery(b.toString());

            List<Event> events = new ArrayList<>();
            while (rs.next()) {
                Event e = new Event();
                e.setTime(new java.util.Date(rs.getTimestamp(2).getTime()));
                e.setTypeId(rs.getInt(1));
                e.setTypeName(rs.getString(4));

                JsonNode node = mapper.readTree(rs.getString(3));
                e.setAttributes(node);
                events.add(e);
            }
            return events;
        } catch (SQLException ex) {
            LOG.error("", ex);
        } catch (JsonMappingException ex) {
            LOG.error("", ex);
        } catch (JsonParseException ex) {
            LOG.error("", ex);
        } catch (IOException ex) {
            LOG.error("", ex);
        }

        return new ArrayList<>(0);
    }
}
