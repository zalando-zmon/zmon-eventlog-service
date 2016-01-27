package de.zalando.zmon.eventlogservice;

import java.util.Date;
import java.util.List;

import org.assertj.core.util.Lists;

public class Fixtures {

    public static List<Event> buildEventList() {
        List<Event> result = Lists.newArrayList();

        for (int i = 0; i < 10; i++) {
            Event e = new Event();
            e.setTime(new Date());
            e.setFlowId("FLOW_" + i);
            e.setTypeId(213263);
            e.setTypeName("TEST_EVENT_" + i);
            // e.setAttribute("KEY1", "VALUE1");
            // e.setAttribute("KEY2", "VALUE2");
            e.setAttribute("alertId", "142");
            result.add(e);
        }
        return result;
    }

}
