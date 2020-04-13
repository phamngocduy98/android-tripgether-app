package cf.bautroixa.maptest.data;

import com.google.firebase.Timestamp;

import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.Event;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.SosRequest;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.utils.DateFormatter;

public class NotificationItem {
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
        public static NotificationItem factory(MainAppManager manager, Event event) {
            String message, introMessage, shortMessage;
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
                    } else {
                        shortMessage = String.format("Ai đó");
                        message = String.format("<b>%s</b> đã %s nhóm!", user.getName(), actionName);
                    }

                    return new NotificationItem(event.getType(), user.getAvatar(), message, introMessage, shortMessage, event.getTime(), userId);
                case Event.Type.USER_SOS_ADDED:
                case Event.Type.USER_SOS_RESOLVED:
                    userId = event.getSosRef().getId(); // sosRequestId is equal to userId
                    sosRequest = manager.getSosRequestsManager().get(userId);
                    return sosRequest.getNotificationItem(manager);
                case Event.Type.CHECKPOINT_ADDED:
                case Event.Type.CHECKPOINT_REMOVED:
                    checkpointId = event.getCheckpointRef().getId();
                    leader = manager.getMembersManager().get(manager.getCurrentTrip().getLeader().getId());
                    checkpoint = manager.getCheckpointsManager().get(checkpointId);

                    introMessage = "Trưởng nhóm thêm địa điểm";
                    shortMessage = String.format("%s", checkpoint.getName());
                    message = String.format("Trưởng nhóm đã thêm địa điểm mới <b>%s</b> tại vị trí %s", checkpoint.getName(), checkpoint.getLocation());

                    return new NotificationItem(event.getType(), leader.getAvatar(), message, introMessage, shortMessage, event.getTime(), checkpointId);
                case Event.Type.CHECKPOINT_ROLL_UP_ADDED:
                    checkpointId = event.getCheckpointRef().getId();
                    leader = manager.getMembersManager().get(manager.getCurrentTrip().getLeader().getId());
                    checkpoint = manager.getCheckpointsManager().get(checkpointId);

                    introMessage = "Yêu cầu tập hợp";
                    shortMessage = String.format("%s", checkpoint.getName());
                    message = String.format("Trưởng nhóm yêu cầu tập hợp tại điểm đến <b>%s</b> tại vị trí %s", checkpoint.getName(), checkpoint.getLocation());

                    return new NotificationItem(event.getType(), leader.getAvatar(), message, introMessage, shortMessage, event.getTime(), checkpointId);
                default:
                    return null;
            }
        }

        public static NotificationItem factory(MainAppManager manager, SosRequest sosRequest) {
            String userId = sosRequest.getId(); // sosRequestId is equal to userId
            User user = manager.getMembersManager().get(userId);
            sosRequest = manager.getSosRequestsManager().get(userId);

            String introMessage = String.format("%s yêu cầu hỗ trợ", user.getName());
            String shortMessage = String.format("%s", sosRequest.getDescription());
            String message = String.format("<b>%s</b> đang cầu cứu sự trợ giúp: %s", user.getName(), sosRequest.getDescription());

            return new NotificationItem(sosRequest.isResolved() ? Event.Type.USER_SOS_RESOLVED : Event.Type.USER_SOS_ADDED, user.getAvatar(), message, introMessage, shortMessage, sosRequest.getTime(), userId);
        }
    }
}
