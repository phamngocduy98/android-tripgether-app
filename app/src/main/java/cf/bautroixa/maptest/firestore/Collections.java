package cf.bautroixa.maptest.firestore;

public class Collections {
    public static final String USERS = "users";
    public static final String CHECKPOINTS = "checkpoints";
    public static final String TRIPS = "trips";
    public static final String SOS = "sos";
    public static final String EVENTS = "events";
    public static final String VISITORS = "visitors";
    public static String checkpoints(String tripId){
        return String.format("%s/%s/%s", TRIPS, tripId, CHECKPOINTS);
    }

    public static String sos(String tripId) {
        return String.format("%s/%s/%s", TRIPS, tripId, SOS);
    }

    public static String events(String tripId) {
        return String.format("%s/%s/%s", TRIPS, tripId, EVENTS);
    }

    public static String visitors(String tripId, String checkpointId) {
        return String.format("%s/%s/%s/%s/%s", TRIPS, tripId, CHECKPOINTS, checkpointId, VISITORS);
    }
}
