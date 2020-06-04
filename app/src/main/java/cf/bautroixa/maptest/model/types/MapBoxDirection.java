package cf.bautroixa.maptest.model.types;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MapBoxDirection {
    public static final String DRIVING_TRAFFIC = "driving-traffic";
    public static final String DRIVING = "driving";
    public static final String WALKING = "walking";
    public static final String CYCLING = "cycling";

    public double distance, duration;
    public ArrayList<LatLng> latLngs;

    public MapBoxDirection(double distance, double duration, ArrayList<LatLng> latLngs) {
        this.distance = distance;
        this.duration = duration;
        this.latLngs = latLngs;
    }
}