package cf.bautroixa.maptest.firestore;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

public class Checkpoint {
    String name;
    GeoPoint coordinate;
    String location;
    Timestamp time;
    @Exclude Marker marker;
    @Exclude LatLng latLng;

    public Checkpoint() {
    }

    public Checkpoint(String name, GeoPoint coordinate, String location, Timestamp time) {
        this.name = name;
        this.coordinate = coordinate;
        this.location = location;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeoPoint getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(GeoPoint coordinate) {
        this.coordinate = coordinate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    @Exclude
    public Marker getMarker() {
        return marker;
    }
    @Exclude
    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    @Exclude
    public LatLng getLatLng(){
        if (this.latLng == null){
            synchronized(this){
                if (this.latLng == null){
                    this.latLng = new LatLng(this.coordinate.getLatitude(), this.coordinate.getLongitude());
                }
            }
        }
        return this.latLng;
    }
}
