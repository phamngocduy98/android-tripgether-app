package cf.bautroixa.maptest.firestore;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

public class SosRequestsManager extends CollectionsManager<SosRequest> {
    private static final String TAG = "SosManager";

    @Override
    public SosRequest documentSnapshotToObject(DocumentSnapshot documentSnapshot) {
        return documentSnapshot.toObject(SosRequest.class);
    }

    @Nullable
    public Task<Void> create(@Nullable WriteBatch batch, User currentUser, SosRequest sosRequest) {
        DocumentReference newSosRequestRef = this.ref.document(currentUser.getId());
        sosRequest.withRef(newSosRequestRef).withId(newSosRequestRef.getId());
        if (batch != null) {
            batch.set(newSosRequestRef, sosRequest);
            return null;
        }
        return this.ref.document(currentUser.getId()).set(sosRequest);
    }
}
