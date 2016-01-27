package de.zalando.zmon.eventlogservice;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Maps;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * @author jbellmann
 *
 */
@SpringApplicationConfiguration(classes = { EventLogApplication.class })
@WebIntegrationTest(randomPort = true)
@ActiveProfiles("postgres")
public class EventlogPostgresIT {

    @Rule
    public SpringMethodRule methodRule = new SpringMethodRule();

    @ClassRule
    public static final PostgreSqlRule postgres = new PostgreSqlRule(10432);

    @ClassRule
    public static final SpringClassRule clazzRule = new SpringClassRule();

    @Value("${local.server.port}")
    private int port;

    @Test
    public void run() throws InterruptedException {
        RestTemplate rest = new RestTemplate();

        List<Event> eventList = Fixtures.buildEventList();
        RequestEntity<List<Event>> request = RequestEntity.put(URI.create("http://localhost:" + port + "/"))
                .contentType(MediaType.APPLICATION_JSON).body(eventList);

        ResponseEntity<String> response = rest.exchange(request, String.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String, Object> uriVariables = Maps.newHashMap();
        uriVariables.put("types", "");
        uriVariables.put("key", "alertId");
        uriVariables.put("value", "142");

        ResponseEntity<String> response2 = rest.exchange(
                "http://localhost:" + port + "/?types={types}&key={key}&value={value}", HttpMethod.GET, null,
                String.class, uriVariables);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println(response2.getBody());

    }

}
