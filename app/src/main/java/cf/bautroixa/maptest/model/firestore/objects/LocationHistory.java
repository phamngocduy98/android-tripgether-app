package cf.bautroixa.maptest.model.firestore.objects;

import com.google.firebase.firestore.GeoPoint;

public class LocationHistory {
    private GeoPoint coordinate;
    private float bearing;
    private int state;

    public LocationHistory() {
    }

}
