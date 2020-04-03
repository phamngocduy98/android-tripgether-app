package cf.bautroixa.maptest.firestore;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class MembersManager extends DatasManager<User> {
    private static final String TAG = "MembersManager";
    public MembersManager() {
    }

    public void updateRefList(List<DocumentReference> documentReferences){
        // clean up removed item
        for (Data data : list){
            if (!documentReferences.contains(data.getRef())){
                Log.d(TAG, "delete"+data.getId());
                remove(data.getId());
            }
        }
        // add or update
        for (final DocumentReference ref : documentReferences){
            final Integer index = mapIdWithIndex.get(ref.getId());
            if (index != null){
                // update
                ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "update "+ref.getId());
                            User user = task.getResult().toObject(User.class).withId(ref.getId()).withRef(ref);
                            update(index, user);
                        }
                    }
                });
            } else {
                // add
                Log.d(TAG, "add "+ref.getId());
                User user = new User().withId(ref.getId()).withRef(ref);
                user.setListenerRegistration(this, new Data.OnNewDocumentSnapshotListener<User>() {
                    @Override
                    public void onNewData(User user) {
                        put(user);
                    }
                });
            }
        }
    }

    @Override
    public void update(int index, User user) {
        User oldUser = list.get(index);
        if (oldUser != null && oldUser.getMarker() != null) {
            user.setMarker(oldUser.getMarker());
        }
        super.update(index, user);
    }
}
