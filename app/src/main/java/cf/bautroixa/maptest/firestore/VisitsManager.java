package cf.bautroixa.maptest.firestore;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

public class VisitsManager extends CollectionsManager<Visit> {
    @Override
    public Visit documentSnapshotToObject(DocumentSnapshot documentSnapshot) {
        return documentSnapshot.toObject(Visit.class);
    }

    public Task<Void> addVisit(User user) {
        return ref.document(user.getId()).set(new Visit());
    }
}
