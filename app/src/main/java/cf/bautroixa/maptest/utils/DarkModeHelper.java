package cf.bautroixa.maptest.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.Arrays;
import java.util.List;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.constant.SharedPrefs;

public class DarkModeHelper {
    public static final int SYSTEM_MODE = 0;
    public static final int LIGHT_MODE = 1;
    public static final int DARK_MODE = 2;
    public static final int AUTO_MODE = 3;
    public static final List<Integer> nightModes = Arrays.asList(SYSTEM_MODE, LIGHT_MODE, DARK_MODE, AUTO_MODE);
    public static final List<Integer> androidNightModes = Arrays.asList(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);

    public static int getCurrentMode(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
        return sharedPreferences.getInt(SharedPrefs.DARK_MODE, SYSTEM_MODE);
    }

    public static void applyMode(Context context, SharedPreferences sharedPreferences, int mode) {
        sharedPreferences.edit().putInt(SharedPrefs.DARK_MODE, mode).commit();
        AppCompatDelegate.setDefaultNightMode(DarkModeHelper.androidNightModes.get(mode));
    }
}
