package cf.bautroixa.maptest.firestore;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;

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
    protected ArrayList<OnNewValueListener> onNewValueListeners = new ArrayList<>();

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
        if (listenerRegistration != null) listenerRegistration.remove();
    }

    @Exclude
    public void addOnNewValueListener(OnNewValueListener listener) {
        this.onNewValueListeners.add(listener);
//        listener.onNewData(this); TODO: send current value on first register
    }

    @Exclude
    public ArrayList<OnNewValueListener> getListeners() {
        return this.onNewValueListeners;
    }

    @Exclude
    public void restoreListeners(ArrayList<OnNewValueListener> backupListeners) {
        this.onNewValueListeners.addAll(backupListeners);
    }

    @Exclude
    public void removeOnNewValueListener(OnNewValueListener listener) {
        this.onNewValueListeners.remove(listener);
    }


    @Exclude
    public void setListenerRegistration(final DatasManager dataManager, OnNewValueListener initListener) {
        if (initListener != null) onNewValueListeners.add(initListener);
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
                    for (OnNewValueListener listener : onNewValueListeners) {
                        listener.onNewData(thisData);
                    }
                }
            }
        });
    }

    @Exclude
    public abstract void onDocumentSnapshot(DocumentSnapshot documentSnapshot);

    @Exclude
    public void onRemove() {
        cancelListenerRegistration();
    }

    @Exclude
    @Nullable
    public Task<Void> sendUpdate(@Nullable WriteBatch batch, @NonNull String field, @Nullable Object value, Object... moreFieldsAndValues) {
        if (batch != null) {
            batch.update(this.ref, field, value, moreFieldsAndValues);
            return null;
        }
        return this.ref.update(field, value, moreFieldsAndValues);
    }

    public interface OnNewValueListener<T extends Data> {
        void onNewData(T data);
    }
}
