package cf.bautroixa.maptest.model.repo;

import android.content.Context;

import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.sharedpref.SharedPrefHelper;

public class RepositoryManager {
    private static RepositoryManager mInstance;
    private UserRepository userRepository;

    public static RepositoryManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (ModelManager.class) {
                if (mInstance == null) {
                    mInstance = new RepositoryManager(context);
                }
            }
        }
        return mInstance;
    }

    private RepositoryManager(Context context) {
        this.userRepository = new UserRepository(context, SharedPrefHelper.getSharedPreferences(context));
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }
}
