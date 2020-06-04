package cf.bautroixa.maptest.model;

import android.content.SharedPreferences;

import cf.bautroixa.maptest.model.constant.SharedPrefs;

public class SharedPrefHelper {
    public static int getSafeDistance(SharedPreferences sharedPreferences) {
        return sharedPreferences.getInt(SharedPrefs.SAFE_DISTANCE, SharedPrefDefaults.SAFE_DISTANCE);
    }

    public static long getLastLostTime(SharedPreferences sharedPreferences) {
        return sharedPreferences.getLong(SharedPrefs.LAST_LOST_TIME, SharedPrefDefaults.LAST_LOST_TIME);
    }

    public static void resetLastLostTime(SharedPreferences sharedPreferences) {
        sharedPreferences.edit().putLong(SharedPrefs.LAST_LOST_TIME, SharedPrefDefaults.LAST_LOST_TIME).commit();
    }

    public static void setLastLostTime(SharedPreferences sharedPreferences, long time) {
        sharedPreferences.edit().putLong(SharedPrefs.LAST_LOST_TIME, time).commit();
    }

    public interface SharedPrefDefaults {
        int SAFE_DISTANCE = 1000;
        long LAST_LOST_TIME = -1;
    }
}
