package cf.bautroixa.tripgether.model.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Map;

import cf.bautroixa.tripgether.model.firestore.objects.Notification;
import cf.bautroixa.tripgether.utils.calculation.TimeConverterHelper;

public class FcmNotification {
    @JsonIgnore
    public static final String NOTI_REF_ID = "notiRefId";
    @JsonIgnore
    public static final String NOTI_TYPE = "type";
    @JsonIgnore
    public static final String NOTI_PRIORITY = "priority";
    @JsonIgnore
    public static final String NOTI_MESSAGE_PARAMS = "messageParams";
    @JsonIgnore
    public static final String NOTI_AVATAR = "avatar";
    @JsonIgnore
    public static final String NOTI_TIME = "time";

    String notiRefId;
    String type;
    String messageParams;
    String priority;
    String avatar;
    String time;

    public FcmNotification() {
    }

    @JsonIgnore
    public static FcmNotification fromHashMap(Map<String, String> data) {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(data, FcmNotification.class);
    }

    public String getNotiRefId() {
        return notiRefId;
    }

    public void setNotiRefId(String notiRefId) {
        this.notiRefId = notiRefId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessageParams() {
        return messageParams;
    }

    public void setMessageParams(String messageParams) {
        this.messageParams = messageParams;
    }

    public String[] getMessageParamsArray() {
        return messageParams.split(",");
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Notification toNotification() {
        int indexOfType = Notification.TripType.tripTypes.indexOf(type);
        if (indexOfType != -1) {
            return new Notification(type, avatar, Arrays.asList(messageParams.split(",")), TimeConverterHelper.toFirestoreTimestamp(time));
        }
        indexOfType = Notification.UserType.userTypes.indexOf(type);
        if (indexOfType != -1) {
            return new Notification(type, avatar, Arrays.asList(messageParams.split(",")), TimeConverterHelper.toFirestoreTimestamp(time));
        }
        return null;
    }

    public interface Priority {
        String HIGH = "high";
        String LOW = "low";
    }
}
