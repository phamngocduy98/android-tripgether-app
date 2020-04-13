package cf.bautroixa.maptest.firestore;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

public class Visit extends Data {
    @Exclude
    public static final String TIME = "time";
    @ServerTimestamp
    Timestamp time;

    public Visit() {
    }

    public Visit(Timestamp time) {
        this.time = time;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    @Exclude
    @Override
    public void onDocumentSnapshot(DocumentSnapshot documentSnapshot) {
        Visit visit = documentSnapshot.toObject(Visit.class);
        if (visit != null) {
            update(visit);
        }
    }

    @Exclude
    public void update(Visit visit) {

    }
}
