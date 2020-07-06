package cf.bautroixa.tripgether.model.types;

public class TimestampPublic {
    public long _seconds;
    public int _nanoseconds;
    com.google.firebase.Timestamp firebaseTimestamp;

    public com.google.firebase.Timestamp toFirebaseTimestamp() {
        if (firebaseTimestamp == null) {
            firebaseTimestamp = new com.google.firebase.Timestamp(_seconds, _nanoseconds);
        }
        return firebaseTimestamp;
    }
}