package cf.bautroixa.tripgether.model.sharedpref;

import android.content.SharedPreferences;

public class SPMapStyle {
    public static int getMapStyle(SharedPreferences sharedPreferences) {
        return sharedPreferences.getInt(SharedPrefKeys.SETTING_MAP_STYLE, MapStyle.STREET);
    }

    public static void setMapStyle(SharedPreferences sharedPreferences, int style) {
        sharedPreferences.edit().putInt(SharedPrefKeys.SETTING_MAP_STYLE, style);
    }

    public interface MapStyle {
        int STREET = 0;
        int SATELLITE = 1;
    }
}
