package cf.bautroixa.maptest.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class FcmMessage {
    @JsonIgnore
    public static final String EVENT_ID = "eventRefId";
    @JsonIgnore
    public static final String EVENT_TYPE = "type";
    @JsonIgnore
    public static final String EVENT_TIME = "time";
    @JsonIgnore
    public static final String EVENT_PRIORITY = "priority";

    String eventRefId;
    int type;
    String time;
    String priority;

    public FcmMessage() {
    }

    @JsonIgnore
    public static FcmMessage fromHashMap(Map<String, String> data) {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(data, FcmMessage.class);
    }

    public String getEventRefId() {
        return eventRefId;
    }

    public void setEventRefId(String eventRefId) {
        this.eventRefId = eventRefId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public interface Priority {
        String HIGH = "high";
        String LOW = "low";
    }
}
