package cf.bautroixa.maptest.model.sharedpref;

import android.content.SharedPreferences;

public class SPGetLost {
    public static boolean isGetLostDetectorOn(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean(SharedPrefKeys.SETTING_SAFE_DISTANCE_ON, Defaults.SETTING_SAFE_DISTANCE_ON);
    }

    public static void turnOnOff(SharedPreferences sharedPreferences, boolean onOff) {
        sharedPreferences.edit().putBoolean(SharedPrefKeys.SETTING_SAFE_DISTANCE_ON, onOff).commit();
    }

    public static void setSafeDistance(SharedPreferences sharedPreferences, int distance) {
        sharedPreferences.edit().putInt(SharedPrefKeys.SAFE_DISTANCE, distance).commit();
    }

    public static int getSafeDistance(SharedPreferences sharedPreferences) {
        return sharedPreferences.getInt(SharedPrefKeys.SAFE_DISTANCE, Defaults.SAFE_DISTANCE);
    }

    public static long getLastLostTime(SharedPreferences sharedPreferences) {
        return sharedPreferences.getLong(SharedPrefKeys.LAST_LOST_TIME, Defaults.LAST_LOST_TIME);
    }

    public static void setLastLostTime(SharedPreferences sharedPreferences, long time) {
        sharedPreferences.edit().putLong(SharedPrefKeys.LAST_LOST_TIME, time).commit();
    }

    public interface Defaults {
        boolean SETTING_SAFE_DISTANCE_ON = false;
        int SAFE_DISTANCE = 1000;
        long LAST_LOST_TIME = -1;
    }
}
