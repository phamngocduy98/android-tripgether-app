package cf.bautroixa.maptest.firestore;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class DatasManager<T extends Data> {
    private static final String TAG = "DatasManager";
    protected CollectionReference ref;
    protected HashMap<String, Integer> mapIdWithIndex;
    protected ArrayList<T> list;
    protected ArrayList<OnDatasChangedListener<T>> onDatasChangedListeners;

    public DatasManager() {
        this.mapIdWithIndex = new HashMap<>();
        this.list = new ArrayList<>();
        this.onDatasChangedListeners = new ArrayList<>();
    }

    public void put(T data) {
        String id = data.getId();
        Integer index = mapIdWithIndex.get(id);

        if (index != null) {
            update(index, data);
            for (OnDatasChangedListener<T> onDatasChangedListener : onDatasChangedListeners) {
                onDatasChangedListener.onItemChanged(index, data);
            }
        } else {
            list.add(data);
            mapIdWithIndex.put(id, list.size() - 1);
            for (OnDatasChangedListener<T> onDatasChangedListener : onDatasChangedListeners) {
                onDatasChangedListener.onItemInserted(list.size() - 1, data);
            }
        }
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
        TaskCompletionSource<T> source = new TaskCompletionSource<T>();
        if (data != null) {
            source.setResult(data);
            return source.getTask();
        }
        return ref.document(id).get().continueWith(new Continuation<DocumentSnapshot, T>() {
            @Override
            @Nullable
            public T then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                if (task.isSuccessful()) {
                    return documentSnapshotToObject(task.getResult());
                }
                Log.e(TAG, "requestGet task failed, return value is NULL");
                return null;
            }
        });
    }

    public void update(int index, T data) {
        list.set(index, data);
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
            for (OnDatasChangedListener<T> onDatasChangedListener : onDatasChangedListeners) {
                onDatasChangedListener.onItemRemoved(index, data);
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

    public int indexOf(Data data) {
        return indexOf(data.getId());
    }

    public ArrayList<T> getData() {
        return list;
    }

    public void clear() {
        for (Data data : list) {
            // remove listener and relate property (like latLng, marker) of each data
            data.onRemove();
        }
        list.clear();
        mapIdWithIndex.clear();
        onClear();
        for (OnDatasChangedListener<T> onDatasChangedListener : onDatasChangedListeners) {
            onDatasChangedListener.onDataSetChanged(list);
        }
    }

    public void onClear() {

    }

    public abstract T documentSnapshotToObject(DocumentSnapshot documentSnapshot);

    public DatasManager<T> addOnDatasChangedListener(OnDatasChangedListener<T> listener) {
        this.onDatasChangedListeners.add(listener);
        return this;
    }

    public DatasManager<T> removeOnDatasChangedListener(OnDatasChangedListener<T> listener) {
        this.onDatasChangedListeners.remove(listener);
        return this;
    }

    public interface OnDatasChangedListener<T extends Data> {
        void onItemInserted(int position, T data);
        void onItemChanged(int position, T data);
        void onItemRemoved(int position, T data);
        void onDataSetChanged(ArrayList<T> datas);
    }
}
