package de.zalando.zmon.eventlogservice;

/**
 * Created by jmussler on 1/13/15.
 */
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Date;

public class Event {
    private Date time;
    private int typeId;
    private String typeName;
    private JsonNode attributes = null;

    public void setAttributes(JsonNode attributes) {
        this.attributes = attributes;
    }

    public JsonNode getAttributes() {
        return attributes;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(final Date time) {
        this.time = time;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(final int typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(final String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return "Event{" +
                "time=" + time +
                ", typeId=" + typeId +
                ", typeName='" + typeName + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
