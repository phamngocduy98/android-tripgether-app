package cf.bautroixa.maptest.model.constant;

public class Collections {
    public static final String USERS = "users";
    public static final String NOTIFICATIONS = "notifications";
    public static final String CHECKPOINTS = "checkpoints";
    public static final String TRIPS = "trips";
    public static final String SOS = "sos";
    public static final String EVENTS = "events";
    public static final String MESSAGES = "messages";
    public static final String VISITORS = "visitors";
    public static final String SENT_FRIEND_REQUEST = "sentFriendRequests";
    public static final String RECEIVED_FRIEND_REQUEST = "friendRequests";
    public static final String POSTS = "posts";
    public static final String COMMENTS = "comments";
    public static final String PLACES = "places";

    public static String userNotifications(String userId) {
        return String.format("%s/%s/%s", USERS, userId, NOTIFICATIONS);
    }

    public static String tripNotifications(String tripId) {
        return String.format("%s/%s/%s", TRIPS, tripId, NOTIFICATIONS);
    }

    public static String checkpoints(String tripId){
        return String.format("%s/%s/%s", TRIPS, tripId, CHECKPOINTS);
    }

    public static String sos(String tripId) {
        return String.format("%s/%s/%s", TRIPS, tripId, SOS);
    }

    public static String events(String tripId) {
        return String.format("%s/%s/%s", TRIPS, tripId, EVENTS);
    }

    public static String messages(String tripId) {
        return String.format("%s/%s/%s", TRIPS, tripId, MESSAGES);
    }

    public static String visitors(String tripId, String checkpointId) {
        return String.format("%s/%s/%s/%s/%s", TRIPS, tripId, CHECKPOINTS, checkpointId, VISITORS);
    }
}
