package cf.bautroixa.maptest.model.firestore;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

public abstract class DocumentWithGeopoint extends Document {
    @Exclude
    public static final String COORD = "coordinate";
    @Exclude
    public static final String LOCATION = "location";

    protected GeoPoint coordinate;
    protected String location;
    @Exclude
    LatLng latLng;
    @Exclude
    Marker marker;

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

    @Exclude
    public LatLng getLatLng() {
        if (this.latLng == null) {
            synchronized (this) {
                if (this.latLng == null && this.coordinate != null) {
                    this.latLng = new LatLng(this.coordinate.getLatitude(), this.coordinate.getLongitude());
                }
            }
        }
        return this.latLng;
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
    @Override
    public void onRemove() {
        super.onRemove();
        if (this.marker != null){
            marker.remove();
        }
    }

}
