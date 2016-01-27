package de.zalando.zmon.eventlogservice;

import java.net.URI;
import java.util.Date;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
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

        List<Event> eventList = buildEventList();
        RequestEntity<List<Event>> request = RequestEntity.put(URI.create("http://localhost:" + port + "/"))
                .contentType(MediaType.APPLICATION_JSON).body(eventList);

        ResponseEntity<String> response = rest.exchange(request, String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    private List<Event> buildEventList() {
        List<Event> result = Lists.newArrayList();

        for (int i = 0; i < 10; i++) {
            Event e = new Event();
            e.setTime(new Date());
            e.setFlowId("FLOW_" + i);
            e.setTypeId(1234);
            e.setTypeName("TEST_EVENT_" + i);
            e.setAttribute("KEY1", "VALUE1");
            e.setAttribute("KEY2", "VALUE2");
            e.setAttribute("alertId", "142");
            result.add(e);
        }
        return result;
    }

}
