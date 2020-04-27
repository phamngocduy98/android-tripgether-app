package cf.bautroixa.maptest.firestore;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

public class Checkpoint extends Data {
    @Exclude public static final String NAME = "name";
    @Exclude public static final String COORD = "coordinate";
    @Exclude public static final String LOCATION = "location";
    @Exclude public static final String TIME = "time";

    @Exclude Marker marker;
    @Exclude LatLng latLng;
    @Exclude
    VisitsManager visitsManager;

    String name;
    GeoPoint coordinate;
    String location;
    Timestamp time;

    public Checkpoint() {
    }

    public Checkpoint(String name, GeoPoint coordinate, String location, Timestamp time) {
        this.name = name;
        this.coordinate = coordinate;
        this.location = location;
        this.time = time;
    }

    @Exclude
    @Override
    public <T extends Data> T withRef(DocumentReference ref) {
        T thisT = super.withRef(ref);
        this.initVisitsManager();
        return thisT;
    }

    @Exclude
    public void initVisitsManager() {
        this.visitsManager = new VisitsManager();
        this.visitsManager.setCollectionListener(ref.collection(Collections.VISITORS), id);
    }

    @Override
    @Exclude
    public void onDocumentSnapshot(DocumentSnapshot documentSnapshot) {
        Checkpoint checkpoint = documentSnapshot.toObject(Checkpoint.class);
        if (checkpoint != null) {
            checkpoint.withId(documentSnapshot.getId()).withRef(documentSnapshot.getReference());
            update(checkpoint);
        }
    }

    @Exclude
    public void update(Checkpoint checkpoint){
        this.name = checkpoint.name;
        this.coordinate = checkpoint.coordinate;
        this.location = checkpoint.location;
        this.time = checkpoint.time;
        this.latLng = new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
        if (this.marker != null){
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
    public VisitsManager getVisitsManager() {
        return visitsManager;
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

    @Exclude
    @Override
    public void onRemove() {
        super.onRemove();
        if (this.marker != null){
            marker.remove();
        }
    }
}
