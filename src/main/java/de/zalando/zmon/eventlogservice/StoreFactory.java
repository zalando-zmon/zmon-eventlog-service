package de.zalando.zmon.eventlogservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(StoreFactory.class);

    @Value("${postgresql.host:localhost}")
    String postgresqlHost;

    @Value("${postgresql.port:5432}")
    int postgresqlPort;

    @Value("${postgresql.database:local_eventlog_db}")
    String postgresqlDatabase;

    @Value("${postgresql.user:postgres}")
    String postgresqlUser;

    @Value("${postgresql.password:postgres}")
    String postgresqlPassword;

    @Value("${postgresql.schema:zmon_eventlog}")
    String postgresqlSchema;

    @Bean
    EventStore getStore() {
        return new PostgresqlStore(postgresqlHost, postgresqlPort, postgresqlDatabase, postgresqlUser, postgresqlPassword, postgresqlSchema);
    }
}
