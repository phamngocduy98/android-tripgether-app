package cf.bautroixa.maptest.firestore;

import java.util.ArrayList;
import java.util.HashMap;

public class DatasManager<T extends Data> {
    protected HashMap<String, Integer> mapIdWithIndex;
    protected ArrayList<T> list;
    protected ArrayList<OnItemInsertedListener> onItemInsertedListeners;
    protected ArrayList<OnItemChangedListener> onItemChangedListeners;
    protected ArrayList<OnItemRemovedListener<T>> onItemRemovedListeners;

    public DatasManager() {
        this.mapIdWithIndex = new HashMap<>();
        this.list = new ArrayList<>();
        this.onItemInsertedListeners = new ArrayList<>();
        this.onItemChangedListeners = new ArrayList<>();
        this.onItemRemovedListeners = new ArrayList<>();
    }

    public void put(T data){
        String id = data.getId();
        Integer index = mapIdWithIndex.get(id);
        if (index != null){
            update(index, data);
            for (OnItemChangedListener onItemChangedListener: onItemChangedListeners){
                onItemChangedListener.onItemChanged(index);
            }
        } else {
            list.add(data);
            mapIdWithIndex.put(id, list.size()-1);
            for (OnItemInsertedListener onItemInsertedListener: onItemInsertedListeners){
                onItemInsertedListener.onItemInserted(list.size()-1);
            }
        }
    }

    public T get(String id){
        Integer index = mapIdWithIndex.get(id);
        if (index != null){
            return list.get(index);
        }
        return null;
    }

    public void update(int index, T data){
        list.set(index, data);
    }

    public void remove(String id){
        Integer index = mapIdWithIndex.get(id);
        if (index != null){
            Data data = list.get(index);
            data.onRemove();
            list.remove(index.intValue());
            mapIdWithIndex.remove(id);
            for (int i=index;i<list.size();i++){
                mapIdWithIndex.put(list.get(i).getId(), i);
            }
            for (OnItemRemovedListener onItemRemovedListener: onItemRemovedListeners){
                onItemRemovedListener.onItemRemoved(index, data);
            }
        }
    }

    public  ArrayList<T> getData(){
        return list;
    }

    public void clear(){
        for (Data data : list){
            data.onRemove();
        }
        list.clear();
        mapIdWithIndex.clear();
    }

    public DatasManager addOnItemInsertedListener(OnItemInsertedListener listener){
        this.onItemInsertedListeners.add(listener);
        return this;
    }
    public DatasManager addOnItemChangedListener(OnItemChangedListener listener){
        this.onItemChangedListeners.add(listener);
        return this;
    }
    public DatasManager addOnItemRemovedListener(OnItemRemovedListener<T> listener){
        this.onItemRemovedListeners.add(listener);
        return this;
    }

    public DatasManager removeOnItemInsertedListener(OnItemInsertedListener listener){
        this.onItemInsertedListeners.remove(listener);
        return this;
    }
    public DatasManager removeOnItemChangedListener(OnItemChangedListener listener){
        this.onItemChangedListeners.remove(listener);
        return this;
    }
    public DatasManager removeOnItemRemovedListener(OnItemRemovedListener<T> listener){
        this.onItemRemovedListeners.remove(listener);
        return this;
    }

    public interface OnItemInsertedListener {
        void onItemInserted(int position);
    }
    public interface OnItemChangedListener {
        void onItemChanged(int position);
    }
    public interface OnItemRemovedListener<T extends Data> {
        void onItemRemoved(int position, T data);
    }
}
