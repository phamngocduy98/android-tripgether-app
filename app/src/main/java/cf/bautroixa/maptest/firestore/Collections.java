package cf.bautroixa.maptest.firestore;

public class Collections {
    public static final String USERS = "users";
    public static final String CHECKPOINTS = "checkpoints";
    public static final String TRIPS = "trips";
    public static String checkpoints(String tripId){
        return String.format("%s/%s/%s", TRIPS, tripId, CHECKPOINTS);
    }
}
