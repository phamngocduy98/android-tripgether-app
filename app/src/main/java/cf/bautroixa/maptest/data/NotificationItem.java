package cf.bautroixa.maptest.data;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;

import java.util.Calendar;

import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.Event;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.SosRequest;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.utils.DateFormatter;

public class NotificationItem {
    private static final String TAG = "NotificationItem";

    int eventType;
    String avatar;
    String content;
    String introContent;
    String shortContent;
    String time;
    String refId;

    public NotificationItem(int eventType, String avatar, String content, String introContent, String shortContent, Timestamp time, String refId) {
        this.eventType = eventType;
        this.avatar = avatar;
        this.content = content;
        this.introContent = introContent;
        this.shortContent = shortContent;
        this.time = DateFormatter.format(time);
        this.refId = refId;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getIntroContent() {
        return introContent;
    }

    public void setIntroContent(String introContent) {
        this.introContent = introContent;
    }

    public String getShortContent() {
        return shortContent;
    }

    public void setShortContent(String shortContent) {
        this.shortContent = shortContent;
    }

    public static class Factory {
        @Nullable
        public static NotificationItem factory(MainAppManager manager, Event event) {
            String message, introMessage, shortMessage, avatar;
            String userId, checkpointId;
            User user, leader;
            Checkpoint checkpoint;
            SosRequest sosRequest;
            switch (event.getType()) {
                case Event.Type.USER_ADDED:
                case Event.Type.USER_REMOVED:
                    userId = event.getUserRef().getId();
                    user = manager.getMembersManager().get(userId);
                    String actionName = event.getType() == Event.Type.USER_ADDED ? "tham gia" : "rời";
                    introMessage = "Chào đón thành viên mới";
                    if (user != null) {
                        shortMessage = String.format("%s", user.getName());
                        message = String.format("<b>%s</b> đã %s nhóm!", user.getName(), actionName);
                        return new NotificationItem(event.getType(), user.getAvatar(), message, introMessage, shortMessage, event.getTime(), userId);
                    } else {
                        shortMessage = "User Not fetched";
                        message = String.format("<b>User Not fetched</b> đã %s nhóm!", actionName);
                        return new NotificationItem(event.getType(), "", message, introMessage, shortMessage, event.getTime(), userId);
                    }
                case Event.Type.USER_SOS_ADDED:
                case Event.Type.USER_SOS_RESOLVED:
                    userId = event.getSosRef().getId(); // sosRequestId is equal to userId
                    sosRequest = manager.getSosRequestsManager().get(userId);
                    return factory(manager, sosRequest);
                case Event.Type.CHECKPOINT_ADDED:
                case Event.Type.CHECKPOINT_REMOVED:
                    checkpointId = event.getCheckpointRef().getId();
                    leader = manager.getMembersManager().get(manager.getCurrentTrip().getLeader().getId());
                    checkpoint = manager.getCheckpointsManager().get(checkpointId);

                    introMessage = "Trưởng nhóm " + (event.getType() == Event.Type.CHECKPOINT_ADDED ? "thêm" : "xóa") + " 1 địa điểm";
                    if (checkpoint != null) {
                        shortMessage = String.format("%s", checkpoint.getName());
                        message = String.format("Trưởng nhóm đã thêm địa điểm mới <b>%s</b> tại vị trí %s", checkpoint.getName(), checkpoint.getLocation());
                    } else {
                        shortMessage = "";
                        message = "Trưởng nhóm đã thêm địa điểm mới";
                    }
                    avatar = leader == null ? User.DEFAULT_AVATAR : leader.getAvatar();
                    return new NotificationItem(event.getType(), avatar, message, introMessage, shortMessage, event.getTime(), checkpointId);
                case Event.Type.CHECKPOINT_ROLL_UP_ADDED:
                    checkpointId = event.getCheckpointRef().getId();
                    leader = manager.getMembersManager().get(manager.getCurrentTrip().getLeader().getId());
                    checkpoint = manager.getCheckpointsManager().get(checkpointId);

                    if (checkpoint != null) {
                        shortMessage = String.format("%s", checkpoint.getName());
                        message = String.format("Trưởng nhóm yêu cầu tập hợp tại điểm đến <b>%s</b> tại vị trí %s", checkpoint.getName(), checkpoint.getLocation());
                    } else {
                        shortMessage = "";
                        message = "Trưởng nhóm yêu cầu tập hợp";
                    }
                    introMessage = "Yêu cầu tập hợp";
                    avatar = leader == null ? User.DEFAULT_AVATAR : leader.getAvatar();
                    return new NotificationItem(event.getType(), avatar, message, introMessage, shortMessage, event.getTime(), checkpointId);
                default:
                    return null;
            }
        }

        public static NotificationItem factory(MainAppManager manager, SosRequest sosRequest) {
            if (sosRequest == null) {
                Log.w(TAG, "sosRequest not fetched when alert fcm comes");
                sosRequest = new SosRequest(SosRequest.SosLever.LOW, "NO DATA", false);
            }
            if (sosRequest.getTime() == null)
                sosRequest.setTime(new Timestamp(Calendar.getInstance().getTime()));
            String userId = sosRequest.getId(); // sosRequestId is equal to userId
            User user = manager.getMembersManager().get(userId);

            String userName = "Ai đó đã";
            String avatar = User.DEFAULT_AVATAR;
            if (user != null) {
                userName = user.getName();
                avatar = user.getAvatar();
            }
            String introMessage = String.format("%s yêu cầu hỗ trợ", userName);
            String shortMessage = String.format("%s", sosRequest.getDescription());
            String message = String.format("<b>%s</b> đang cầu cứu sự trợ giúp: %s", userName, sosRequest.getDescription());

            return new NotificationItem(sosRequest.isResolved() ? Event.Type.USER_SOS_RESOLVED : Event.Type.USER_SOS_ADDED, avatar, message, introMessage, shortMessage, sosRequest.getTime(), userId);
        }
    }
}
