package cf.bautroixa.maptest.model.firestore;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

import java.util.Objects;

import cf.bautroixa.maptest.interfaces.LatLngOwner;

public class Checkpoint extends Document implements LatLngOwner {
    @Exclude
    public static final String NAME = "name";
    @Exclude
    public static final String COORD = "coordinate";
    @Exclude
    public static final String LOCATION = "location";
    @Exclude
    public static final String PLACE_NAME = "placeName";
    @Exclude
    public static final String TIME = "time";

    @Exclude
    Marker marker;
    @Exclude
    LatLng latLng;
    @Exclude
    CollectionManager<Visit> visitsManager;

    String name;
    GeoPoint coordinate;
    String location;
    String placeName;
    Timestamp time;

    public Checkpoint() {
        this.withClass(Checkpoint.class);
    }

    public Checkpoint(String name, GeoPoint coordinate, String location, Timestamp time) {
        this.name = name;
        this.coordinate = coordinate;
        this.location = location;
        this.time = time;
    }

    public Checkpoint(String name, double latitude, double longitude, String location, Timestamp time) {
        this.name = name;
        this.coordinate = new GeoPoint(latitude, longitude);
        this.location = location;
        this.time = time;
    }

    @Exclude
    @Override
    public <T extends Document> T withRef(DocumentReference ref) {
        T thisT = super.withRef(ref);
        this.initVisitsManager();
        return thisT;
    }

    @Exclude
    public void initVisitsManager() {
        this.visitsManager = new CollectionManager<>(Visit.class, ref.collection(Collections.VISITORS));
    }

    @Override
    protected void update(Document document) {
        Checkpoint checkpoint = (Checkpoint) document;
        this.name = checkpoint.name;
        this.coordinate = checkpoint.coordinate;
        this.location = checkpoint.location;
        this.time = checkpoint.time;
        this.latLng = new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
        if (this.marker != null) {
            marker.setPosition(this.latLng);
        }
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

    /**
     * get full location: placeName, address
     *
     * @return
     */
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * placeName only
     *
     * @return
     */
    public String getPlaceName() {
        // make it compatible with old checkpoint instance, TODO: normalize all checkpoints in Firestore
        if (placeName == null) {
            String[] locations = location.split(",");
            if (locations.length > 0) {
                setPlaceName(locations[0]);
            } else {
                setPlaceName(location);
            }
        }
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
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
    public CollectionManager<Visit> getVisitsManager() {
        return visitsManager;
    }

    @Exclude
    public LatLng getLatLng() {
        if (this.latLng == null) {
            synchronized (this) {
                if (this.latLng == null) {
                    this.latLng = new LatLng(this.coordinate.getLatitude(), this.coordinate.getLongitude());
                }
            }
        }
        return this.latLng;
    }

    @Exclude
    @Override
    public void onRemove() {
        super.onRemove();
        if (this.marker != null) marker.remove();
        if (visitsManager != null) visitsManager.clear();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!super.equals(obj)) return false;
        Checkpoint checkpoint = (Checkpoint) obj;
        return Objects.equals(name, checkpoint.getName()) && Objects.equals(coordinate, checkpoint.getCoordinate()) && Objects.equals(location, checkpoint.getLocation()) && Objects.equals(time, checkpoint.getTime());
    }
}
