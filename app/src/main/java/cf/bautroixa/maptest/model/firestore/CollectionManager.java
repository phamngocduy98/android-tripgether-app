package cf.bautroixa.maptest.model.firestore;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class CollectionManager<T extends Document> extends DocumentsManager<T> {
    protected static final String TAG = "CollectionManager";
    //    protected Query query;
    private ListenerRegistration listenerRegistration;

    public CollectionManager(Class<T> itemClass) {
        super(itemClass);
    }

    public CollectionManager(Class<T> itemClass, CollectionReference collectionReference) {
        super(itemClass, collectionReference);
        this.setCollectionListener(collectionReference);
    }

    public CollectionManager(Class<T> itemClass, CollectionReference collectionReference, Query query) {
        super(itemClass, collectionReference);
//        this.query = query;
        this.setCollectionListener(query);
    }

    public void setCollectionListener(Query query) {
        if (listenerRegistration != null) listenerRegistration.remove();
        listenerRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, "Listen failed.", e);
                }
                if (queryDocumentSnapshots != null) {
                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                        DocumentSnapshot documentSnapshot = documentChange.getDocument();
                        if (documentChange.getType() != DocumentChange.Type.REMOVED) {
                            put(T.newInstance(itemClass, documentSnapshot));
                        } else {
                            remove(documentSnapshot.getId());
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
}
