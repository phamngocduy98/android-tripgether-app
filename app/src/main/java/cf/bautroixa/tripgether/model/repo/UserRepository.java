package cf.bautroixa.tripgether.model.repo;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.Set;

import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.model.http.UserHttpService;
import cf.bautroixa.tripgether.model.repo.objects.UserPublic;
import cf.bautroixa.tripgether.utils.TaskHelper;

public class UserRepository extends Repository<UserPublic> {
    ModelManager manager;

    public UserRepository(Context context, SharedPreferences sharedPreferences) {
        super(sharedPreferences);
        manager = ModelManager.getInstance(context);
    }

    @Nullable
    @Override
    public UserPublic getFromCache(String id) {
        User user = manager.getBaseUsersManager().get(id);
        if (user != null) return new UserPublic(user);
        return super.getFromCache(id);
    }

    @Override
    protected Task<UserPublic> fetchItem(String id) {
        User user = manager.getBaseUsersManager().get(id);
        if (user != null) {
            return TaskHelper.getCompletedTask(new UserPublic(user));
        }
        return UserHttpService.getUserPublicData(id);
    }

    @Override
    protected Task<List<UserPublic>> fetchAllItem(Set<String> ids) {
        return UserHttpService.getBatchUserPublicData(ids);
    }

    @Override
    protected String toCacheString(UserPublic data) {
        return String.format("%d;%s;%s;%s", System.currentTimeMillis(), data.getName(), data.getAvatar(), data.getEmail());
    }

    @Override
    protected UserPublic fromCacheString(String id, @Nullable String dataString, boolean allowOldData) {
        if (dataString == null || dataString.length() == 0) return null;
        String[] data = dataString.split(";");
        String savedTimestampStr = data[0];
        long savedTimestamp = Long.parseLong(savedTimestampStr);
        if (!allowOldData && System.currentTimeMillis() - savedTimestamp > 24 * 60 * 60 * 1000) { // one day
            return null;
        }
        return new UserPublic(id, data[1], data[2], data[3]);
    }


}
