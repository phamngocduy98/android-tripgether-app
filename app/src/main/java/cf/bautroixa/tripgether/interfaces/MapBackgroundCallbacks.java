package cf.bautroixa.tripgether.interfaces;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public interface MapBackgroundCallbacks {
    boolean onMarkerClick(Marker marker);

    void onMapClick(LatLng latLng);

    void onMapLongClick(LatLng latLng);
}
