package de.zalando.zmon.eventlogservice;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.postgresql.core.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Async;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Created by jmussler on 1/22/15.
 */
public class PostgresqlStore implements EventStore, JsonSupport {

    private final static Logger LOG = LoggerFactory.getLogger(PostgresqlStore.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbc;
    private final String queryGet;
    private final String queryInsert;

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
        writeAsString(event.getAttributes()).ifPresent(attr -> this.writeEvent(event, attr));
    }

    protected void writeEvent(Event event, String attributes) {
        LOG.debug("SQL : {}, PARAMS : {}", queryInsert, new String[] { event.getTypeId() + "",
                new java.sql.Timestamp(event.getTime().getTime()).toString(), attributes });

        jdbc.update(queryInsert, event.getTypeId(), new java.sql.Timestamp(event.getTime().getTime()), 0, attributes);
    }

    @Override
    public boolean isBatchSupported() {
        return true;
    }

    @Async
    @Override
    public void storeInBatch(Iterable<Event> toStore) {
        int[] result = jdbc.batchUpdate(queryInsert, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                final Event event = Iterables.get(toStore, i);
                ps.setInt(1, event.getTypeId());
                ps.setTimestamp(2, new java.sql.Timestamp(event.getTime().getTime()));
                ps.setInt(3, 0);
                ps.setString(4, writeAsString(event.getAttributes()).orElse("{}"));
            }

            @Override
            public int getBatchSize() {
                return Iterables.size(toStore);
            }
        });
        LOG.info("SAVED IN BATCH : {}", result);
    }

    @Override
    public List<Event> getEvents(String key, String value, List<Integer> types, int limit) {

        List<Event> events = Lists.newArrayList();
        buildQueryString(key, value, types).ifPresent(sql -> this.executeQuery(sql, limit, events));
        return events;
    }

    private final RowMapper<Event> eventRowMapper = new EventRowMapper();

    protected void executeQuery(String query, int limit, List<Event> events) {
        events.addAll(jdbc.query(query, new Object[0], eventRowMapper));
    }

    /**
     * Builds the 'Query-SQL-Statement' based on parameters.
     * 
     * @param key
     * @param value
     * @param types
     * 
     * @return Optional<String> , the sql-statement, {@link Optional#empty()} on
     *         error
     */
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

    static class EventRowMapper implements RowMapper<Event>, JsonSupport {

        private static final TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {
        };

        @Override
        public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
            Event e = new Event();
            e.setTime(new java.util.Date(rs.getTimestamp(2).getTime()));
            e.setTypeId(rs.getInt(1));
            e.setTypeName(rs.getString(5));
            Map<String, String> data = readValue(rs.getString(4), typeRef).orElse(Maps.newHashMap());
            e.setAttributes(data);
            return e;
        }
    }
}
