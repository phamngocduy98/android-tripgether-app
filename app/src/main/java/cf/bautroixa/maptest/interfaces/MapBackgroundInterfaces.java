package cf.bautroixa.maptest.interfaces;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

public interface MapBackgroundInterfaces {
    void targetMyLocation();

    void target(Object data);

    void cleanUpTempMarkerAndRoute();

    void drawRoute(@Nullable LatLng fromN, final LatLng to);
}
