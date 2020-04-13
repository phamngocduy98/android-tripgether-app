package cf.bautroixa.maptest.firestore;

import com.google.firebase.firestore.DocumentSnapshot;

public class EventsManager extends CollectionsManager<Event> {
    private static final String TAG = "EventsManager";

    @Override
    public Event documentSnapshotToObject(DocumentSnapshot documentSnapshot) {
        return documentSnapshot.toObject(Event.class);
    }
}
