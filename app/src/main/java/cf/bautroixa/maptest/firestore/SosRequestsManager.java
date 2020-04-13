package cf.bautroixa.maptest.firestore;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

public class SosRequestsManager extends CollectionsManager<SosRequest> {
    private static final String TAG = "SosManager";

    @Override
    public SosRequest documentSnapshotToObject(DocumentSnapshot documentSnapshot) {
        return documentSnapshot.toObject(SosRequest.class);
    }

    public Task<Void> addSosRequest(SosRequest sosRequest) {
        String uuid = FirebaseAuth.getInstance().getUid();
        Log.d(TAG, "add SOS" + uuid);
        return this.ref.document(FirebaseAuth.getInstance().getUid()).set(sosRequest);
    }
}
