package cf.bautroixa.maptest.model.firestore;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DocumentsManager<T extends Document> {
    private static final String TAG = "DatasManager";
    protected CollectionReference ref;
    protected Class<T> itemClass;
    protected HashMap<String, Integer> mapIdWithIndex;
    protected ArrayList<T> list;
    protected ArrayList<OnListChangedListener<T>> onListChangedListeners;

    public DocumentsManager(Class<T> itemClass) {
        this.itemClass = itemClass;
        this.mapIdWithIndex = new HashMap<>();
        this.list = new ArrayList<>();
        this.onListChangedListeners = new ArrayList<>();
    }

    public DocumentsManager(Class<T> itemClass, CollectionReference collectionReference) {
        this.ref = collectionReference;
        this.itemClass = itemClass;
        this.mapIdWithIndex = new HashMap<>();
        this.list = new ArrayList<>();
        this.onListChangedListeners = new ArrayList<>();
    }

    public DocumentReference create(WriteBatch batch, T data) {
        DocumentReference newDataRef = data.getId() != null ? ref.document(data.getId()) : ref.document();
        batch.set(newDataRef, data);
        return newDataRef;
    }

    public Task<Void> create(T data) {
        DocumentReference newDataRef = data.getId() != null ? ref.document(data.getId()) : ref.document();
        return newDataRef.set(data);
    }

    public void delete(@NonNull WriteBatch batch, String id) {
        DocumentReference dataRef = ref.document(id);
        batch.delete(dataRef);
    }

    public Task<Void> delete(String id) {
        DocumentReference dataRef = ref.document(id);
        return dataRef.delete();
    }

    public void put(T data) {
        String id = data.getId();
        Integer index = mapIdWithIndex.get(id);

        if (index != null) {
            update(index, data);
            for (OnListChangedListener<T> onListChangedListener : onListChangedListeners) {
                onListChangedListener.onItemChanged(index, data);
            }
        } else {
            add(id, data);
            for (OnListChangedListener<T> onListChangedListener : onListChangedListeners) {
                onListChangedListener.onItemInserted(list.size() - 1, data);
            }
        }
    }

    public void add(String id, T data) {
        list.add(data);
        mapIdWithIndex.put(id, list.size() - 1);
    }

    public void update(int index, T data) {
        list.set(index, data);
    }


    public T rawPut(DocumentSnapshot documentSnapshot) {
        T data = Document.newInstance(itemClass, documentSnapshot);
        put(data);
        return data;
    }

    @Nullable
    public T get(String id) {
        Integer index = mapIdWithIndex.get(id);
        if (index != null) {
            return list.get(index);
        }
        return null;
    }

    public Task<T> requestGet(String id) {
        T data = get(id);
        if (data != null) {
            TaskCompletionSource<T> source = new TaskCompletionSource<T>();
            source.setResult(data);
            return source.getTask();
        }
        return ref.document(id).get().continueWith(new Continuation<DocumentSnapshot, T>() {
            @Override
            @Nullable
            public T then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                if (task.isSuccessful() && task.getResult() != null) {
                    return Document.newInstance(itemClass, task.getResult());
                }
                Log.e(TAG, "requestGet task failed, return value is NULL");
                return null;
            }
        });
    }

    /**
     * query data to get
     * should only be called in baseDatasManager
     *
     * @param queryCreator
     * @return
     */
    public Task<List<T>> queryGet(QueryCreator queryCreator) {
        Query query = queryCreator.create(ref);
        return query.get().continueWith(new Continuation<QuerySnapshot, List<T>>() {
            @Override
            public List<T> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                if (task.isSuccessful() && task.getResult() != null) {
                    QuerySnapshot querySnapshot = task.getResult();
                    List<T> queryDatas = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : querySnapshot) {
                        T data = Document.newInstance(itemClass, documentSnapshot);
                        queryDatas.add(data);
                        // TODO: save got data to list, remember that, it should be saved in baseDatasManager to prevent unwanted item in list
                        put(data);
                    }
                    return queryDatas;
                }
                throw task.getException();
            }
        });
    }

    public void remove(String id) {
        Integer index = mapIdWithIndex.get(id);
        if (index != null) {
            T data = list.get(index);
            data.onRemove();
            list.remove(index.intValue());
            mapIdWithIndex.remove(id);
            for (int i = index; i < list.size(); i++) {
                mapIdWithIndex.put(list.get(i).getId(), i);
            }
            for (OnListChangedListener<T> onListChangedListener : onListChangedListeners) {
                onListChangedListener.onItemRemoved(index, data);
            }
        }
    }

    public boolean contains(String id) {
        return mapIdWithIndex.get(id) != null;
    }

    public int indexOf(String id) {
        Integer index = mapIdWithIndex.get(id);
        return index != null ? index : -1;
    }

    public int indexOf(Document document) {
        return indexOf(document.getId());
    }

    public ArrayList<T> getList() {
        return list;
    }

    public void clear() {
        for (Document document : list) {
            // remove listener and relate property (like latLng, marker) of each data
            document.onRemove();
        }
        list.clear();
        mapIdWithIndex.clear();
        onClear();
        for (OnListChangedListener<T> onListChangedListener : onListChangedListeners) {
            onListChangedListener.onDataSetChanged(list);
        }
    }

    public void onClear() {

    }

    public DocumentsManager<T> addOnDatasChangedListener(@NonNull OnListChangedListener<T> listener) {
        this.onListChangedListeners.add(listener);
        listener.onDataSetChanged(getList());
        return this;
    }

    public DocumentsManager<T> removeOnDatasChangedListener(OnListChangedListener<T> listener) {
        this.onListChangedListeners.remove(listener);
        return this;
    }

    public void attachSortedList(LifecycleOwner lifecycleOwner, final SortedList<T> sortedList) {
        final OnListChangedListener listener = new OnListChangedListener<T>() {
            @Override
            public void onItemInserted(int position, T data) {
                sortedList.add(data);
            }

            @Override
            public void onItemChanged(int position, T data) {
                sortedList.add(data);
            }

            @Override
            public void onItemRemoved(int position, T data) {
                sortedList.remove(data);
            }

            @Override
            public void onDataSetChanged(ArrayList<T> list) {
                if (list.size() == 0) sortedList.clear();
                sortedList.addAll(list);
            }
        };
        lifecycleOwner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            public void connectListener() {
                addOnDatasChangedListener(listener);
                listener.onDataSetChanged(list);
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            public void disconnectListener() {
                removeOnDatasChangedListener(listener);
            }
        });
    }

    public void attachAdapter(LifecycleOwner lifecycleOwner, final RecyclerView.Adapter adapter) {
        final OnListChangedListener listener = new OnListChangedListener<T>() {
            @Override
            public void onItemInserted(int position, T data) {
                adapter.notifyItemInserted(position);
            }

            @Override
            public void onItemChanged(int position, T data) {
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onItemRemoved(int position, T data) {
                adapter.notifyItemRemoved(position);
            }

            @Override
            public void onDataSetChanged(ArrayList<T> datas) {
                adapter.notifyDataSetChanged();
            }
        };
        attachListener(lifecycleOwner, listener);
    }

    public void attachListener(LifecycleOwner lifecycleOwner, @NonNull final OnListChangedListener onListChangedListener) {
        lifecycleOwner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            public void connectListener() {
                addOnDatasChangedListener(onListChangedListener);
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            public void disconnectListener() {
                removeOnDatasChangedListener(onListChangedListener);
            }
        });
    }

    public ArrayList<OnListChangedListener<T>> getListeners() {
        return this.onListChangedListeners;
    }

    public void restoreListeners(ArrayList<OnListChangedListener<T>> backupListeners) {
        this.onListChangedListeners.addAll(backupListeners);
        for (int i = 0; i < backupListeners.size(); i++) {
            backupListeners.get(i).onDataSetChanged(this.list);
        }
    }

    public interface OnListChangedListener<T extends Document> {
        void onItemInserted(int position, T data);

        void onItemChanged(int position, T data);

        void onItemRemoved(int position, T data);

        void onDataSetChanged(ArrayList<T> list);
    }

    public interface QueryCreator {
        Query create(CollectionReference collectionReference);
    }
}
