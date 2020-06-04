package cf.bautroixa.maptest.model.firestore;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;


public class RefsArrayManager<T extends Document> extends DocumentsManager<T> {
    private static final String TAG = "MembersManager";
    private DocumentsManager<T> parentDocumentsManager;
    private int requiredListSize = 0;
    private ArrayList<OnInitCompleteListener<T>> onInitCompleteListeners;

    public RefsArrayManager(Class<T> itemClass) {
        super(itemClass);
        this.onInitCompleteListeners = new ArrayList<>();
    }

    public RefsArrayManager(Class<T> itemClass, @NonNull DocumentsManager<T> parentDocumentsManager) {
        super(itemClass, parentDocumentsManager.ref);
        this.parentDocumentsManager = parentDocumentsManager;
        this.onInitCompleteListeners = new ArrayList<>();
    }

    public RefsArrayManager(Class<T> itemClass, @NonNull CollectionReference collectionReference) {
        super(itemClass, collectionReference);
        this.onInitCompleteListeners = new ArrayList<>();
    }

    @Override
    public void put(T data) {
        super.put(data);
        if (parentDocumentsManager != null) parentDocumentsManager.put(data);
        onListUpdated();
    }

    @Override
    public void remove(String id) {
        super.remove(id);
        onListUpdated();
    }

    public void updateRefList(List<DocumentReference> documentReferences) {
        requiredListSize = documentReferences.size();
        // clean up removed item
        for (int i = 0; i < list.size(); i++) {
            T data = list.get(i);
            if (!documentReferences.contains(data.getRef())) {
                Log.d(TAG, "delete" + data.getId());
                remove(data.getId());
                i--;
            }
        }
        // add or update
        for (final DocumentReference ref : documentReferences) {
            if (parentDocumentsManager == null) {
                final Integer index = mapIdWithIndex.get(ref.getId());
                if (index == null) {
                    // add
                    Log.d(TAG, "add " + ref.getId());
                    final T data;
                    try {
                        data = itemClass.newInstance();
                        data.withRef(ref).withClass(itemClass);
                        data.setListenerRegistration(this, null);
                    } catch (IllegalAccessException | InstantiationException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Log.d(TAG, "add/update " + ref.getId());
                parentDocumentsManager.requestGet(ref.getId()).addOnCompleteListener(new OnCompleteListener<T>() {
                    @Override
                    public void onComplete(@NonNull Task<T> task) {
                        if (task.isSuccessful()) {
                            T data = task.getResult();
                            if (data != null) put(data);
                        }
                    }
                });
            }
        }
    }

    public void removeOnInitCompleteListener(OnInitCompleteListener<T> onInitCompleteListener) {
        this.onInitCompleteListeners.remove(onInitCompleteListener);
    }

    public void addOnInitCompleteListener(final OnInitCompleteListener<T> onInitCompleteListener) {
        this.onInitCompleteListeners.add(new OnInitCompleteListener<T>() {
            @Override
            public void onComplete(ArrayList list) {
                onInitCompleteListener.onComplete(list);
                removeOnInitCompleteListener(onInitCompleteListener);
            }
        });
        if (isListComplete()) {
            onInitCompleteListener.onComplete(list);
            removeOnInitCompleteListener(onInitCompleteListener);
        }
    }

    public boolean isListComplete() {
        return requiredListSize == list.size();
    }

    public void onListUpdated() {
        if (isListComplete()) {
            for (OnInitCompleteListener<T> onInitCompleteListener : onInitCompleteListeners) {
                onInitCompleteListener.onComplete(list);
                removeOnInitCompleteListener(onInitCompleteListener);
            }
        }
    }


    public interface OnInitCompleteListener<T extends Document> {
        void onComplete(ArrayList<T> list);
    }
}
