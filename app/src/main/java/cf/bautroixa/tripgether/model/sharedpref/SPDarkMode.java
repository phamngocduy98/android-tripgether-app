package cf.bautroixa.tripgether.model.sharedpref;

import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.Arrays;
import java.util.List;

public class SPDarkMode {
    public static final int SYSTEM_MODE = 0;
    public static final int LIGHT_MODE = 1;
    public static final int DARK_MODE = 2;
    public static final int AUTO_MODE = 3;
    public static final List<Integer> nightModes = Arrays.asList(SYSTEM_MODE, LIGHT_MODE, DARK_MODE, AUTO_MODE);
    public static final List<Integer> androidNightModes = Arrays.asList(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);

    public static int getCurrentMode(SharedPreferences sharedPreferences) {
        return sharedPreferences.getInt(SharedPrefKeys.SETTING_DARK_MODE_TYPE, SYSTEM_MODE);
    }

    public static void applyMode(SharedPreferences sharedPreferences, int mode) {
        sharedPreferences.edit().putInt(SharedPrefKeys.SETTING_DARK_MODE_TYPE, mode).commit();
        AppCompatDelegate.setDefaultNightMode(SPDarkMode.androidNightModes.get(mode));
    }
}
