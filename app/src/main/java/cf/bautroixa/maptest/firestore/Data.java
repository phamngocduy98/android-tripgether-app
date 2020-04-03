package cf.bautroixa.maptest.firestore;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

public abstract class Data {
    @Exclude
    protected static final String TAG = "DataClass";
    @Exclude
    public static final String ID = "id";
    @Exclude
    protected ListenerRegistration listenerRegistration;
    @Exclude
    protected DocumentReference ref;
    @Exclude
    protected String id;
    @Exclude
    protected ArrayList<OnNewDocumentSnapshotListener> onNewDocumentSnapshotListeners = new ArrayList<>();

    public Data() {
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public <T extends Data> T withId(String id) {
        this.id = id;
        return (T) this;
    }

    @Exclude
    public DocumentReference getRef() {
        return ref;
    }

    @Exclude
    public <T extends Data> T withRef(DocumentReference ref) {
        this.ref = ref;
        return (T) this;
    }

    @Exclude
    public void cancelListenerRegistration() {
        listenerRegistration.remove();
    }

    @Exclude
    public void addOnNewDocumentSnapshotListener(OnNewDocumentSnapshotListener listener) {
        this.onNewDocumentSnapshotListeners.add(listener);
    }

    @Exclude
    public void removeOnNewDocumentSnapshotListener(OnNewDocumentSnapshotListener listener) {
        this.onNewDocumentSnapshotListeners.remove(listener);
    }


    @Exclude
    public void setListenerRegistration(final DatasManager dataManager, OnNewDocumentSnapshotListener initListener) {
        if (initListener != null) onNewDocumentSnapshotListeners.add(initListener);
        final Data thisData = this;
        this.listenerRegistration = this.ref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                }
                Log.d(TAG, "onDocumentSnapshot");
                if (documentSnapshot != null) {
                    if (documentSnapshot.exists()) {
                        onDocumentSnapshot(documentSnapshot);
                        if (dataManager != null) dataManager.put(thisData);
                    } else {
                        if (dataManager != null) {
                            dataManager.remove(thisData.getId());
                        } else {
                            onRemove();
                        }
                    }
                    for (OnNewDocumentSnapshotListener listener : onNewDocumentSnapshotListeners) {
                        listener.onNewData(thisData);
                    }
                }
            }
        });
    }

    public abstract void onDocumentSnapshot(DocumentSnapshot documentSnapshot);

    public void onRemove() {
        cancelListenerRegistration();
    }

    public interface OnNewDocumentSnapshotListener<T extends Data> {
        void onNewData(T data);
    }
}
