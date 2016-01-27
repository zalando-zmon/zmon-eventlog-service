package de.zalando.zmon.eventlogservice;

import static com.google.common.collect.Lists.newArrayList;
import static de.zalando.zmon.eventlogservice.Fixtures.buildEventList;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.atLeastOnce;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.common.collect.Lists;

public class EventLogControllerTest implements Resources {

    private MockMvc mockMvc;

    private EventStore eventStore;

    @Before
    public void setUp() {
        eventStore = Mockito.mock(EventStore.class);
        this.mockMvc = MockMvcBuilders.standaloneSetup(new EventlogController(eventStore))
                .alwaysDo(MockMvcResultHandlers.print()).build();
    }

    @Test
    public void testGetEvents() throws Exception {

        when(eventStore.getEvents(Mockito.eq("aKey"), Mockito.eq("aValue"), Mockito.eq(newArrayList(1, 2, 3)),
                Mockito.eq(100))).thenReturn(buildEventList());

        this.mockMvc.perform(get("/?types=1,2,3&key=aKey&value=aValue")).andExpect(status().isOk());

        Mockito.verify(eventStore, VerificationModeFactory.atMost(1)).getEvents(Mockito.eq("aKey"),
                Mockito.eq("aValue"), Mockito.eq(Lists.newArrayList(1, 2, 3)), Mockito.eq(100));

    }

    @Test
    public void putEventsAlertId() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders.put("/").contentType(MediaType.APPLICATION_JSON)
                .content(resourceToString(jsonResource("eventList")))).andExpect(status().isOk());

        Mockito.verify(eventStore, atLeastOnce()).putEvent(Mockito.any(Event.class), Mockito.eq("alertId"));

    }

    @Test
    public void putEventsCheckId() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders.put("/").contentType(MediaType.APPLICATION_JSON)
                .content(resourceToString(jsonResource("eventListCheckId")))).andExpect(status().isOk());

        Mockito.verify(eventStore, atLeastOnce()).putEvent(Mockito.any(Event.class), Mockito.eq("checkId"));

    }
}
