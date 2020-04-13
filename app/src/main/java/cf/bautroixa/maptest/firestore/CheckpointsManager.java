package cf.bautroixa.maptest.firestore;

import com.google.firebase.firestore.DocumentSnapshot;

public class CheckpointsManager extends CollectionsManager<Checkpoint> {
    private static final String TAG = "CheckpointsManager";

    public CheckpointsManager() {
    }

    @Override
    public void update(int index, Checkpoint checkpoint) {
        Checkpoint oldCheckpoint = list.get(index);
        if (oldCheckpoint != null && oldCheckpoint.getMarker() != null) {
            checkpoint.setMarker(oldCheckpoint.getMarker());
        }
        super.update(index, checkpoint);
    }

    @Override
    public Checkpoint documentSnapshotToObject(DocumentSnapshot documentSnapshot) {
        return documentSnapshot.toObject(Checkpoint.class);
    }
}
