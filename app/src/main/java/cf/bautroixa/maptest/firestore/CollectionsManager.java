package cf.bautroixa.maptest.firestore;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

public abstract class CollectionsManager<T extends Data> extends DatasManager<T> {
    protected static final String TAG = "CollectionManager";
    protected String parentCollectionId = "";
    protected CollectionReference ref;
    protected FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;

    public CollectionsManager() {
        db = FirebaseFirestore.getInstance();
    }

    public DocumentReference create(@Nullable WriteBatch batch, T data) {
        DocumentReference newDataRef = data.getId() != null ? ref.document(data.getId()) : ref.document();
        if (batch != null) {
            batch.set(newDataRef, data);
        } else {
            newDataRef.set(data);
        }
        return newDataRef;
    }

    public DocumentReference delete(@Nullable WriteBatch batch, String id) {
        DocumentReference dataRef = ref.document(id);
        if (batch != null) {
            batch.delete(dataRef);
        } else {
            dataRef.delete();
        }
        return dataRef;
    }

    public void setCollectionListener(CollectionReference collectionReference, String id) {
        if (id == null || this.parentCollectionId.equals(id)) return;
        this.ref = collectionReference;
        final CollectionsManager thisManager = this;
        if (listenerRegistration != null) listenerRegistration.remove();
        listenerRegistration = collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, "Listen failed.", e);
                }
                if (queryDocumentSnapshots != null) {
                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                        if (documentChange.getType() == DocumentChange.Type.ADDED) {
                            DocumentSnapshot documentSnapshot = documentChange.getDocument();
                            //TODO: what??? why fetched data need to be refetched :V, edit ADDED
                            T data = documentSnapshotToObject(documentSnapshot).withId(documentSnapshot.getId()).withRef(documentSnapshot.getReference());
                            data.setListenerRegistration(thisManager, null);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onClear() {
        super.onClear();
        if (listenerRegistration != null) listenerRegistration.remove();
    }

    public abstract T documentSnapshotToObject(DocumentSnapshot documentSnapshot);
}
