package cf.bautroixa.maptest.utils;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Arrays;
import java.util.List;

import cf.bautroixa.maptest.R;

public class DarkModeHelper {
    public static final int SYSTEM_MODE = 0;
    public static final int LIGHT_MODE = 1;
    public static final int DARK_MODE = 2;
    public static final int AUTO_MODE = 3;
    public static final List<Integer> androidNightModes = Arrays.asList(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
}
