package de.zalando.zmon.eventlogservice;

import org.junit.rules.ExternalResource;

import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig.Credentials;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig.Net;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig.Storage;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig.Timeout;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;
import ru.yandex.qatools.embed.postgresql.distribution.Version;

/**
 * @author jbellmann
 *
 */
public class PostgreSqlRule extends ExternalResource {

    private PostgresProcess process;
    private final int port;

    public PostgreSqlRule(int port) {
        this.port = port;
    }

    public PostgreSqlRule() {
        this(5432);
    }

    @Override
    protected void before() throws Throwable {
        PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter.getDefaultInstance();
        Net net = new PostgresConfig.Net("localhost", port);
        Credentials c = new PostgresConfig.Credentials("postgres", "postgres");
        Storage storage = new PostgresConfig.Storage("test");
        PostgresConfig config = new PostgresConfig(Version.V9_4_4, net, storage, new Timeout(), c);
        config.credentials().username();
        config.credentials().password();
        PostgresExecutable exec = runtime.prepare(config);
        process = exec.start();
    }

    @Override
    protected void after() {
        if (process != null) {
            process.stop();
        }
    }
}
