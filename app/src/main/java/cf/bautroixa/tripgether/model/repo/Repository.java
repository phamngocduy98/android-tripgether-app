package cf.bautroixa.tripgether.model.repo;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cf.bautroixa.tripgether.utils.TaskHelper;

public abstract class Repository<T> {
    private final SharedPreferences sharedPref;
    protected HashMap<String, T> mapIdWithValue;

    public Repository(SharedPreferences sharedPreferences) {
        this.mapIdWithValue = new HashMap<>();
        this.sharedPref = sharedPreferences;
    }

    protected abstract Task<T> fetchItem(String id);

    protected abstract Task<List<T>> fetchAllItem(Set<String> ids);

    protected abstract String toCacheString(T data);

    protected abstract T fromCacheString(String id, String dataString, boolean allowOldData);

    @Nullable
    public T get(String id) {
        // in RAM
        T value = mapIdWithValue.get(id);
        if (value != null) return value;
        // cached in SharedPref
        T cached = getFromCache(id);
        if (cached != null) {
            mapIdWithValue.put(id, cached);
            return cached;
        }
        return null;
    }

    @Nullable
    public T getFromCache(String id) {
        return getFromCache(id, true);
    }

    @Nullable
    public T getFromCache(String id, boolean allowOldData) {
        String cached = sharedPref.getString("u" + id, null);
        return fromCacheString(id, cached, allowOldData);
    }

    private void put(String id, T value) {
        mapIdWithValue.put(id, value);
        sharedPref.edit().putString("u" + id, toCacheString(value)).commit();
    }

    private void putAll(Set<String> idSet, List<T> values) {
        String[] ids = new String[idSet.size()];
        idSet.toArray(ids);
        if (idSet.size() != values.size()) {
            throw new IllegalArgumentException("idSet size and values size is not equals");
        }
        for (int i = 0; i < idSet.size(); i++) {
            put(ids[i], values.get(i));
        }
    }

    public Task<T> requestGet(String id) {
        T data = mapIdWithValue.get(id);
        if (data != null) {
            return TaskHelper.getCompletedTask(data);
        }
        return fetchItem(id).continueWith(new Continuation<T, T>() {
            @Override
            public T then(@NonNull Task<T> task) throws Exception {
                if (task.isSuccessful()) {
                    T data = task.getResult();
                    put(id, data);
                    return data;
                }
                throw task.getException();
            }
        });
    }

    public Task<Void> updateRepo(String id) {
        if (get(id) == null) {
            return fetchItem(id).continueWith(new Continuation<T, Void>() {
                @Override
                public Void then(@NonNull Task<T> task) throws Exception {
                    if (!task.isSuccessful()) throw task.getException();
                    T data = task.getResult();
                    put(id, data);
                    return null;
                }
            });
        } else {
            return TaskHelper.getCompletedTask(null);
        }
    }

    public Task<Void> updateRepo(List<String> ids) {
        Set<String> needIds = new HashSet<>();
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            if (mapIdWithValue.get(id) == null && getFromCache(id, false) == null) {
                needIds.add(id);
            }
        }
        if (needIds.size() > 0) {
            return fetchAllItem(needIds).continueWith(new Continuation<List<T>, Void>() {
                @Override
                public Void then(@NonNull Task<List<T>> task) throws Exception {
                    if (!task.isSuccessful()) throw task.getException();
                    List<T> result = task.getResult();
                    putAll(needIds, result);
                    return null;
                }
            });
        }
        return TaskHelper.getCompletedTask(null);
    }

    @Deprecated
    public Task<List<T>> requestGetAll(List<String> ids) {
        Set<String> needIds = new HashSet<>();
        List<T> available = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            T item = mapIdWithValue.get(id);
            if (item == null) needIds.add(id);
            else available.add(item);
        }
        return fetchAllItem(needIds).continueWith(new Continuation<List<T>, List<T>>() {
            @Override
            public List<T> then(@NonNull Task<List<T>> task) throws Exception {
                if (!task.isSuccessful()) throw task.getException();
                List<T> result = task.getResult();
                result.addAll(available);
                putAll(needIds, result);
                return result;
            }
        });
    }
}
