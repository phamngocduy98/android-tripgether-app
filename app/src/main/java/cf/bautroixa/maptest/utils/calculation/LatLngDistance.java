package cf.bautroixa.maptest.utils.calculation;

import com.google.android.gms.maps.model.LatLng;

/**
 * https://stackoverflow.com/a/11172685/9385297
 */
public class LatLngDistance {
    /**
     * measureDistance
     * @param latLng1
     * @param latLng2
     * @return distance two latlng in meters
     */
    public static double measureDistance(LatLng latLng1, LatLng latLng2){  // generally used geo measurement function
        if (latLng1 == null || latLng2 == null) return Double.MAX_VALUE;
        double R = 6378.137; // Radius of earth in KM
        double dLat = latLng2.latitude * Math.PI / 180 - latLng1.latitude * Math.PI / 180;
        double dLon = latLng2.longitude * Math.PI / 180 - latLng1.longitude * Math.PI / 180;
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(latLng1.latitude * Math.PI / 180) * Math.cos(latLng2.latitude * Math.PI / 180) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;
        return d * 1000; // meters
    }
}
