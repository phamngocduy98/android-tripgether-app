package cf.bautroixa.maptest.firestore;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

public class DatasManager<T extends Data> {
    protected HashMap<String, Integer> mapIdWithIndex;
    protected ArrayList<T> list;
    protected ArrayList<OnItemInsertedListener<T>> onItemInsertedListeners;
    protected ArrayList<OnItemChangedListener<T>> onItemChangedListeners;
    protected ArrayList<OnItemRemovedListener<T>> onItemRemovedListeners;
    protected ArrayList<OnDataSetChangedListener<T>> onDataSetChangedListeners;

    public DatasManager() {
        this.mapIdWithIndex = new HashMap<>();
        this.list = new ArrayList<>();
        this.onItemInsertedListeners = new ArrayList<>();
        this.onItemChangedListeners = new ArrayList<>();
        this.onItemRemovedListeners = new ArrayList<>();
        this.onDataSetChangedListeners = new ArrayList<>();
    }

    public void put(T data) {
        String id = data.getId();
        Integer index = mapIdWithIndex.get(id);
        if (index != null) {
            update(index, data);
            for (OnItemChangedListener<T> onItemChangedListener : onItemChangedListeners) {
                onItemChangedListener.onItemChanged(index, data);
            }
        } else {
            list.add(data);
            mapIdWithIndex.put(id, list.size() - 1);
            for (OnItemInsertedListener<T> onItemInsertedListener : onItemInsertedListeners) {
                onItemInsertedListener.onItemInserted(list.size() - 1, data);
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
            for (OnItemRemovedListener<T> onItemRemovedListener : onItemRemovedListeners) {
                onItemRemovedListener.onItemRemoved(index, data);
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
        for (OnDataSetChangedListener<T> onDataSetChangedListener : onDataSetChangedListeners) {
            onDataSetChangedListener.onDataSetChanged(list);
        }
    }

    public void onClear() {

    }

    public DatasManager<T> addOnItemInsertedListener(OnItemInsertedListener<T> listener) {
        this.onItemInsertedListeners.add(listener);
        return this;
    }

    public DatasManager<T> addOnItemChangedListener(OnItemChangedListener<T> listener) {
        this.onItemChangedListeners.add(listener);
        return this;
    }

    public DatasManager<T> addOnItemRemovedListener(OnItemRemovedListener<T> listener) {
        this.onItemRemovedListeners.add(listener);
        return this;
    }

    public DatasManager<T> addOnDataSetChangedListener(OnDataSetChangedListener<T> listener) {
        this.onDataSetChangedListeners.add(listener);
        return this;
    }

    public DatasManager<T> removeOnItemInsertedListener(OnItemInsertedListener<T> listener) {
        this.onItemInsertedListeners.remove(listener);
        return this;
    }

    public DatasManager<T> removeOnItemChangedListener(OnItemChangedListener<T> listener) {
        this.onItemChangedListeners.remove(listener);
        return this;
    }

    public DatasManager<T> removeOnItemRemovedListener(OnItemRemovedListener<T> listener) {
        this.onItemRemovedListeners.remove(listener);
        return this;
    }

    public DatasManager<T> removeOnDataSetChangedListener(OnDataSetChangedListener<T> listener) {
        this.onDataSetChangedListeners.remove(listener);
        return this;
    }

    public interface OnItemInsertedListener<T extends Data> {
        void onItemInserted(int position, T data);
    }

    public interface OnItemChangedListener<T extends Data> {
        void onItemChanged(int position, T data);
    }

    public interface OnItemRemovedListener<T extends Data> {
        void onItemRemoved(int position, T data);
    }

    public interface OnDataSetChangedListener<T extends Data> {
        void onDataSetChanged(ArrayList<T> datas);
    }
}
