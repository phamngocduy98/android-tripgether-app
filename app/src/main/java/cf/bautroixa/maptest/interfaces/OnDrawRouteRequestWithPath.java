package cf.bautroixa.maptest.interfaces;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public interface OnDrawRouteRequestWithPath {
    void drawRoute(List<LatLng> latlngs);
}
