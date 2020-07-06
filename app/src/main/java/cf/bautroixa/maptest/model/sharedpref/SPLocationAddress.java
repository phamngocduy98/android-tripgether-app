package cf.bautroixa.maptest.model.sharedpref;

import android.content.SharedPreferences;
import android.location.Location;

import androidx.annotation.Nullable;

public class SPLocationAddress {
    @Nullable
    public static Location getLastLocationWithAddress(SharedPreferences sharedPreferences) {
        float lat = sharedPreferences.getFloat(SharedPrefKeys.LAST_ADDRESS_LAT, 0);
        float lon = sharedPreferences.getFloat(SharedPrefKeys.LAST_ADDRESS_LON, 0);
        String provider = sharedPreferences.getString(SharedPrefKeys.LAST_ADDRESS_PROVIDER, "");
        if (lat == 0 && lon == 0 && (provider == null || provider.length() == 0)) return null;
        Location location = new Location(provider);
        location.setLatitude(lat);
        location.setLatitude(lon);
        return location;
    }

    public static void setLastLocationWithAddress(SharedPreferences sharedPreferences, Location location) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (location == null) return;
        editor.putFloat(SharedPrefKeys.LAST_ADDRESS_LAT, (float) location.getLatitude());
        editor.putFloat(SharedPrefKeys.LAST_ADDRESS_LON, (float) location.getLongitude());
        editor.putString(SharedPrefKeys.LAST_ADDRESS_PROVIDER, location.getProvider());
        editor.commit();
    }
}
