package cf.bautroixa.maptest.model.firestore.objects;


import android.content.Context;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.core.Document;

public class Notification extends Document {
    @Exclude
    public static final String SEEN = "seen";

    @Exclude
    public static final List<String> highPriorityEvent = Arrays.asList(UserType.FRIEND_ACCEPTED, UserType.TRIP_JOIN_ACCEPTED, TripType.CHECKPOINT_GATHER_REQUEST, TripType.USER_SOS_ADDED);

    @Exclude
    int notificationId;
    String priority;
    String type;
    String avatar;
    List<String> messageParams;
    String payload;
    @ServerTimestamp
    Timestamp time;
    @Exclude
    boolean seen;

    public Notification() {
        this.withClass(Notification.class);
    }

    public Notification(String type, String avatar, List<String> messageParams, Timestamp time) {
        List<String> types = new ArrayList<>();
        types.addAll(TripType.tripTypes);
        types.addAll(UserType.userTypes);
        this.notificationId = types.indexOf(type);
        this.type = type;
        this.avatar = avatar;
        this.messageParams = messageParams;
        this.time = time;
        this.priority = highPriorityEvent.contains(type) ? "high" : "low";
        this.seen = false;
    }

    public Notification(String type, String avatar, List<String> messageParams, Timestamp time, String payload) {
        List<String> types = new ArrayList<>();
        types.addAll(TripType.tripTypes);
        types.addAll(UserType.userTypes);
        this.notificationId = types.indexOf(type);
        this.type = type;
        this.avatar = avatar;
        this.messageParams = messageParams;
        this.time = time;
        this.priority = highPriorityEvent.contains(type) ? "high" : "low";
        this.payload = payload;
        this.seen = false;
    }

    @Exclude
    public static List<String> getMessageParams(Context context, String type, @Nullable User user, @Nullable Trip trip, @Nullable Checkpoint checkpoint) {
        switch (type) {
            case UserType.ADD_FRIEND:
                return Collections.singletonList(user.getName());
            case UserType.FRIEND_ACCEPTED:
                return Collections.singletonList(user.getName());
            case UserType.INVITE_TO_TRIP:
                return Arrays.asList(user.getName(), trip.getName());
            case UserType.TRIP_JOIN_ACCEPTED:
                return Collections.singletonList(trip.getName());
            case UserType.TRIP_JOIN_REJECTED:
                return Collections.singletonList(trip.getName());

            case TripType.TRIP_JOIN_REQUEST:
                return Arrays.asList(user.getName(), trip.getName());
            case TripType.USER_ADDED:
                return Collections.singletonList(user.getName());
            case TripType.USER_REMOVED:
                return Collections.singletonList(user.getName());
            case TripType.USER_SOS_ADDED:
                return Arrays.asList(user.getName(), user.getSosRequest().getLeverText(context), user.getSosRequest().getDescription());
            case TripType.USER_SOS_RESOLVED:
                return Collections.singletonList(user.getName());
            case TripType.CHECKPOINT_ADDED:
                return Arrays.asList(checkpoint.getName(), checkpoint.getLocation());
            case TripType.CHECKPOINT_REMOVED:
                return Collections.singletonList(checkpoint.getName());
            case TripType.CHECKPOINT_GATHER_REQUEST:
                return Arrays.asList(checkpoint.getName(), checkpoint.getLocation());
            default:
                return Collections.EMPTY_LIST;
        }
    }

    @Exclude
    public int getNotificationId() {
        return notificationId;
    }

    @Exclude
    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getMessageParams() {
        return messageParams;
    }

    public void setMessageParams(List<String> messageParams) {
        this.messageParams = messageParams;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Exclude
    public boolean isSeen() {
        return seen;
    }

    @Exclude
    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    @Exclude
    public String getRenderedMessage(Context context, boolean enableDecoration) {
        String messageBase = "";
        switch (this.type) {
            case UserType.ADD_FRIEND:
                messageBase = context.getString(R.string.user_notification_add_friend);
                break;
            case UserType.FRIEND_ACCEPTED:
                messageBase = context.getString(R.string.user_notification_friend_accepted);
                break;
            case UserType.FRIEND_REJECTED:
                messageBase = context.getString(R.string.user_notification_friend_rejected);
                break;
            case UserType.INVITE_TO_TRIP:
                messageBase = context.getString(R.string.user_notification_invite_to_trip);
                break;
            case UserType.TRIP_JOIN_ACCEPTED:
                messageBase = context.getString(R.string.user_notification_trip_join_accepted);
                break;
            case UserType.TRIP_JOIN_REJECTED:
                messageBase = context.getString(R.string.user_notification_trip_join_rejected);
                break;
            case UserType.YOU_GET_LOST:
                messageBase = context.getString(R.string.user_notification_you_get_lost);
                break;

            case TripType.TRIP_JOIN_REQUEST:
                messageBase = context.getString(R.string.user_notification_trip_join_request);
                break;
            case TripType.USER_ADDED:
                messageBase = context.getString(R.string.trip_notification_user_added);
                break;
            case TripType.USER_REMOVED:
                messageBase = context.getString(R.string.trip_notification_user_removed);
                break;
            case TripType.USER_SOS_ADDED:
                messageBase = context.getString(R.string.trip_notification_user_sos_added);
                break;
            case TripType.USER_SOS_RESOLVED:
                messageBase = context.getString(R.string.trip_notification_user_sos_resolved);
                break;
            case TripType.CHECKPOINT_ADDED:
                messageBase = context.getString(R.string.trip_notification_checkpoint_added);
                break;
            case TripType.CHECKPOINT_REMOVED:
                messageBase = context.getString(R.string.trip_notification_checkpoint_removed);
                break;
            case TripType.CHECKPOINT_GATHER_REQUEST:
                messageBase = context.getString(R.string.trip_notification_checkpoint_gather_request);
                break;
        }
        if (enableDecoration) {
            ArrayList<String> messageDecoratedParams = new ArrayList<>();
            for (int i = 0; i < messageParams.size(); i++) {
                messageDecoratedParams.add("<b>" + messageParams.get(i) + "</b>");
            }
            return String.format(messageBase, messageDecoratedParams.toArray());
        } else {
            return String.format(messageBase, messageParams.toArray());
        }
    }

    @Exclude
    @Override
    protected void update(Document document) {
        Notification notification = (Notification) document;
        this.type = notification.type;
        this.avatar = notification.avatar;
        this.messageParams = notification.messageParams;
        this.time = notification.time;
    }

    public interface TripType {
        String TRIP_JOIN_REQUEST = "tripJoinRequest";
        String USER_ADDED = "userAdded";
        String USER_REMOVED = "userRemoved";
        String USER_SOS_ADDED = "userSosAdded";
        String USER_SOS_RESOLVED = "userSosResolved";
        String CHECKPOINT_ADDED = "checkpointAdded";
        String CHECKPOINT_REMOVED = "checkpointRemoved";
        String CHECKPOINT_GATHER_REQUEST = "checkpointGatherRequest";
        String USER_GET_LOST = "userGetLost";

        List<String> tripTypes = Arrays.asList(TRIP_JOIN_REQUEST, USER_ADDED, USER_REMOVED, USER_SOS_ADDED, USER_SOS_RESOLVED, CHECKPOINT_ADDED, CHECKPOINT_REMOVED, CHECKPOINT_REMOVED, CHECKPOINT_GATHER_REQUEST, USER_GET_LOST);
    }

    public interface UserType {
        String ADD_FRIEND = "addFriend";
        String FRIEND_ACCEPTED = "friendAccepted";
        String FRIEND_REJECTED = "friendRejected";
        String INVITE_TO_TRIP = "inviteToTrip";
        String TRIP_JOIN_ACCEPTED = "tripJoinAccepted";
        String TRIP_JOIN_REJECTED = "tripJoinRejected";
        String YOU_GET_LOST = "getLost";

        List<String> userTypes = Arrays.asList(ADD_FRIEND, FRIEND_ACCEPTED, FRIEND_REJECTED, INVITE_TO_TRIP, TRIP_JOIN_ACCEPTED, TRIP_JOIN_REJECTED, YOU_GET_LOST);
    }

    public interface TripIcon {
        int TRIP_JOIN_REQUEST = R.drawable.ic_person_black_24dp;
        int USER_ADDED = R.drawable.ic_person_add_black_24dp;
        int USER_REMOVED = R.drawable.ic_people_white_24dp;
        int USER_SOS_ADDED = R.drawable.ic_help;
        int USER_SOS_RESOLVED = R.drawable.ic_help;
        int CHECKPOINT_ADDED = R.drawable.ic_marker;
        int CHECKPOINT_REMOVED = R.drawable.ic_marker;
        int CHECKPOINT_GATHER_REQUEST = R.drawable.ic_assistant_photo_black_24dp;
        int USER_GET_LOST = R.drawable.ic_help;

        List<Integer> tripIcons = Arrays.asList(TRIP_JOIN_REQUEST, USER_ADDED, USER_REMOVED, USER_SOS_ADDED, USER_SOS_RESOLVED, CHECKPOINT_ADDED, CHECKPOINT_REMOVED, CHECKPOINT_REMOVED, CHECKPOINT_GATHER_REQUEST, USER_GET_LOST);
    }

    public interface UserIcon {
        int ADD_FRIEND = R.drawable.ic_person_add_black_24dp;
        int FRIEND_ACCEPTED = R.drawable.ic_person_add_black_24dp;
        int FRIEND_REJECTED = R.drawable.ic_close;
        int INVITE_TO_TRIP = R.drawable.ic_add_black_24dp;
        int TRIP_JOIN_ACCEPTED = R.drawable.ic_done_black_24dp;
        int TRIP_JOIN_REJECTED = R.drawable.ic_close;
        int YOU_GET_LOST = R.drawable.ic_help;

        List<Integer> userIcons = Arrays.asList(ADD_FRIEND, FRIEND_ACCEPTED, FRIEND_REJECTED, INVITE_TO_TRIP, TRIP_JOIN_ACCEPTED, TRIP_JOIN_REJECTED, YOU_GET_LOST);
    }

    public static class Priority {
        public static final String HIGH = "high", NORMAL = "normal", LOW = "low";
    }
}
