package cf.bautroixa.maptest.firestore;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class MembersManager extends DatasManager<User> {
    private static final String TAG = "MembersManager";
    public MembersManager() {
    }

    public void updateRefList(List<DocumentReference> documentReferences){
        // clean up removed item
        for (int i = 0; i < list.size(); i++) {
            User user = list.get(i);
            if (!documentReferences.contains(user.getRef())) {
                Log.d(TAG, "delete" + user.getId());
                remove(user.getId());
                i--;
            }
        }
        // add or update
        for (final DocumentReference ref : documentReferences){
            final Integer index = mapIdWithIndex.get(ref.getId());
            if (index == null) {
                // add
                Log.d(TAG, "add "+ref.getId());
                User user = new User().withId(ref.getId()).withRef(ref);
                user.setListenerRegistration(this, new Data.OnNewValueListener<User>() {
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
