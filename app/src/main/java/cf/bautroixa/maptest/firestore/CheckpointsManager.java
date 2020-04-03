package cf.bautroixa.maptest.firestore;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

public class CheckpointsManager extends DatasManager<Checkpoint> {
    private static final String TAG = "CheckpointsManager";
    private String tripId = "";
    FirebaseFirestore db;
    public CheckpointsManager() {
        db = FirebaseFirestore.getInstance();
    }

    public void setCollectionListener(String tripId){
        if (this.tripId.equals(tripId)) return;
        final CheckpointsManager thisManager = this;
        db.collection(Collections.checkpoints(tripId)).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                }
                if (queryDocumentSnapshots != null) {
                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                        if (documentChange.getType() == DocumentChange.Type.ADDED) {
                            DocumentSnapshot documentSnapshot = documentChange.getDocument();
                            Checkpoint checkpoint = documentSnapshot.toObject(Checkpoint.class).withId(documentSnapshot.getId()).withRef(documentSnapshot.getReference());
                            checkpoint.setListenerRegistration(thisManager, null);
                        }
                        Log.d(TAG, "fetch Checkpoints");
                    }
                }
            }
        });
    }

    @Override
    public void update(int index, Checkpoint checkpoint) {
        Checkpoint oldCheckpoint = list.get(index);
        if (oldCheckpoint != null && oldCheckpoint.getMarker() != null) {
            checkpoint.setMarker(oldCheckpoint.getMarker());
        }
        super.update(index, checkpoint);
    }

    public Task<DocumentReference> addCheckpoint(Checkpoint checkpoint) {
        return db.collection(Collections.checkpoints(tripId)).add(checkpoint);
    }

}
