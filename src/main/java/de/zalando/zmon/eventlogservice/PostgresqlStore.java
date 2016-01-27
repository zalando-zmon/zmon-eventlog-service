package de.zalando.zmon.eventlogservice;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.postgresql.core.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Async;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Created by jmussler on 1/22/15.
 */
public class PostgresqlStore implements EventStore {

    private final static Logger LOG = LoggerFactory.getLogger(PostgresqlStore.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbc;
    // private HikariDataSource ds;
    private final String queryGet;
    private final String queryInsert;

    private static final ObjectMapper mapper = new ObjectMapper();

    // @Deprecated
    // public PostgresqlStore(String host, int port, String database, String
    // user, String password, String schema) {
    // HikariConfig conf = new HikariConfig();
    // conf.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" +
    // database);
    // conf.setUsername(user);
    // conf.setPassword(password);
    // conf.setMaximumPoolSize(12);
    // ds = new HikariDataSource(conf);
    //
    // this.dataSource = null;
    // this.jdbc = null;
    //
    // queryInsert = "INSERT INTO " + schema
    // + ".events(e_type_id, e_created, e_instance_id, e_data)
    // VALUES(?,?,?,?::jsonb)";
    // queryGet = "SELECT e_type_id, e_created, e_instance_id, e_data, et_name
    // FROM " + schema + ".events, " + schema
    // + ".event_types WHERE et_id = e_type_id AND e_data @> '";
    //
    // }

    public PostgresqlStore(DataSource dataSource, String schema) {
        this.dataSource = dataSource;
        this.jdbc = new JdbcTemplate(this.dataSource);
        queryInsert = "INSERT INTO " + schema
                + ".events(e_type_id, e_created, e_instance_id, e_data) VALUES(?,?,?,?::jsonb)";
        queryGet = "SELECT e_type_id, e_created, e_instance_id, e_data, et_name FROM " + schema + ".events, " + schema
                + ".event_types WHERE et_id = e_type_id AND e_data @> '";
    }

    @Async
    @Override
    public void putEvent(Event event, String key) {
        // remove boilerplate resource-handling
        writeAttributes(event).ifPresent(attr -> this.writeEvent(event, key, attr));
        // attributes.ifPresent(attr -> this.writeEvent(event, key, attr));
        // if (attributes.isPresent()) {
        // jdbc.update(queryInsert, event.getTypeId(), new
        // java.sql.Timestamp(event.getTime().getTime()), 0,
        // attributes.get());
        // }
        //
        // Connection conn = null;
        //
        // try {
        // conn = dataSource.getConnection();
        // PreparedStatement st = conn.prepareStatement(queryInsert);
        // st.setInt(1, event.getTypeId());
        // st.setTimestamp(2, new
        // java.sql.Timestamp(event.getTime().getTime()));
        // st.setInt(3, 0);
        // st.setString(4, mapper.writeValueAsString(event.getAttributes()));
        // st.execute();
        //
        // } catch (JsonProcessingException e) {
        // LOG.error("Failed to serialize map", e);
        // } catch (SQLException ex) {
        // LOG.error("", ex);
        // } finally {
        // try {
        // if (conn != null) {
        // conn.close();
        // }
        // } catch (SQLException e) {
        // e.printStackTrace();
        // }
        // }

    }

    protected void writeEvent(Event event, String key, String attributes) {
        LOG.debug("SQL : {}, PARAMS : {}", queryInsert, new String[] { event.getTypeId() + "",
                new java.sql.Timestamp(event.getTime().getTime()).toString(), attributes });
        jdbc.update(queryInsert, event.getTypeId(), new java.sql.Timestamp(event.getTime().getTime()), 0, attributes);
    }

    protected Optional<String> writeAttributes(Event event) {
        try {
            return Optional.ofNullable(mapper.writeValueAsString(event.getAttributes()));
        } catch (JsonProcessingException e) {
            LOG.warn("Failed to serialize map", e);
            return Optional.empty();
        }
    }

    @Override
    public List<Event> getEvents(String key, String value, List<Integer> types, int limit) {

        List<Event> events = Lists.newArrayList();
        buildQueryString(key, value, types).ifPresent(sql -> this.executeQuery(sql, limit, events));
        return events;

        //
        // Connection conn = null;
        //
        // try {
        // conn = dataSource.getConnection();
        //
        // Statement st = conn.createStatement();
        // StringBuffer b = new StringBuffer();
        // b.append(queryGet);
        // Utils.appendEscapedLiteral(b, "{\"" + key + "\":\"" + value + "\"}",
        // true);
        // b.append("'");
        //
        // if (null != types && types.size() > 0) {
        // b.append(" AND e_type_id IN (");
        // boolean first = true;
        // for (Integer t : types) {
        // if (!first) {
        // b.append(",");
        // }
        // first = false;
        // b.append(t.toString());
        // }
        // b.append(")");
        // }
        //
        // ResultSet rs = st.executeQuery(b.toString());
        //
        // List<Event> events = new ArrayList<>();
        // while (rs.next()) {
        // Event e = new Event();
        // e.setTime(new java.util.Date(rs.getTimestamp(2).getTime()));
        // e.setTypeId(rs.getInt(1));
        // e.setTypeName(rs.getString(5));
        //
        // Map<String, String> data = mapper.readValue(rs.getString(4), new
        // TypeReference<Map<String, String>>() {
        // });
        // e.setAttributes(data);
        // events.add(e);
        // }
        // return events;
        // } catch (SQLException ex) {
        // LOG.error("", ex);
        // } catch (JsonMappingException ex) {
        // LOG.error("", ex);
        // } catch (JsonParseException ex) {
        // LOG.error("", ex);
        // } catch (IOException ex) {
        // LOG.error("", ex);
        // } finally {
        // try {
        // if (conn != null) {
        // conn.close();
        // }
        // } catch (SQLException e) {
        // LOG.error("", e);
        // }
        // }
        //
        // return new ArrayList<>(0);
    }

    private final RowMapper<Event> eventRowMapper = new EventRowMapper();

    protected void executeQuery(String query, int limit, List<Event> events) {
        events.addAll(jdbc.query(query, new Object[0], eventRowMapper));
    }

    protected Optional<String> buildQueryString(String key, String value, List<Integer> types) {
        StringBuffer b = new StringBuffer();
        b.append(queryGet);
        try {
            Utils.appendEscapedLiteral(b, "{\"" + key + "\":\"" + value + "\"}", true);
        } catch (SQLException e) {
            // this is a method bound to postgres-lib, thatswhy a SQL-Exception
            LOG.warn(e.getMessage(), e);
            return Optional.empty();
        }
        b.append("'");

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
        LOG.debug("QUERY : {}", b.toString());
        return Optional.ofNullable(b.toString());
    }

    static class EventRowMapper implements RowMapper<Event> {

        @Override
        public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
            Event e = new Event();
            e.setTime(new java.util.Date(rs.getTimestamp(2).getTime()));
            e.setTypeId(rs.getInt(1));
            e.setTypeName(rs.getString(5));

            Map<String, String> data = readValue(rs.getString(4));
            e.setAttributes(data);
            return e;
        }

        protected Map<String, String> readValue(String value) {
            Map<String, String> result = Maps.newHashMap();
            try {
                result = mapper.readValue(value, new TypeReference<Map<String, String>>() {
                });
            } catch (JsonParseException e) {
                LOG.warn(e.getMessage(), e);
            } catch (JsonMappingException e) {
                LOG.warn(e.getMessage(), e);
            } catch (IOException e) {
                LOG.warn(e.getMessage(), e);
            }
            return result;
        }
    }
}
