package de.zalando.zmon.eventlogservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by jmussler on 1/22/15.
 */
@EnableAutoConfiguration
@Configuration
public class StoreFactory {

    @Value(value="${cassandra.host:null}")
    String cassandraHost;

    @Value("${cassandra.port:0}")
    int cassandraPort;

    @Value("${cassandra.keyspace:null}")
    String cassandraKeyspace;

    @Value("${postgresql.host:null}")
    String postgresqlHost;

    @Value("${postgresql.port:0}")
    int postgresqlPort;

    @Value("${postgresql.database:eventlog}")
    String postgresqlDatabase;

    @Value("${postgresql.user:zmon_eventlog_service}")
    String postgresqlUser;

    @Value("${postgresql.password}")
    String postgresqlPassword;

    @Bean
    EventStore getStore() {
        if(postgresqlHost!=null) {
            return new PostgresqlStore(postgresqlHost, postgresqlPort, postgresqlDatabase, postgresqlUser, postgresqlPassword);
        }
        else {
            return new CassandraStore(cassandraHost, cassandraPort, cassandraKeyspace);
        }
    }
}
