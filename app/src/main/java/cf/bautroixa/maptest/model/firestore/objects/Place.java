package cf.bautroixa.maptest.model.firestore.objects;

import com.google.firebase.firestore.GeoPoint;

import cf.bautroixa.maptest.model.firestore.core.Document;
import cf.bautroixa.maptest.utils.calculation.GeoHashUtils;

public class Place extends Document {
    String placeName, placeAddress;
    GeoPoint coordinate;

    public Place() {
    }

    public Place(Checkpoint checkpoint) {
        this.placeName = checkpoint.getPlaceName();
        this.placeAddress = checkpoint.getLocation();
        this.coordinate = checkpoint.getCoordinate();
        this.withId(GeoHashUtils.getGeoPointId(this.coordinate));
    }

    public Place(String placeName, String placeAddress, GeoPoint coordinate) {
        this.placeName = placeName;
        this.placeAddress = placeAddress;
        this.coordinate = coordinate;
        this.withId(GeoHashUtils.getGeoPointId(this.coordinate));
    }

    @Override
    protected void update(Document document) {
        Place place = (Place) document;
        this.placeName = place.placeName;
        this.placeAddress = place.placeAddress;
        this.coordinate = place.coordinate;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceAddress() {
        return placeAddress;
    }

    public void setPlaceAddress(String placeAddress) {
        this.placeAddress = placeAddress;
    }

    public GeoPoint getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(GeoPoint coordinate) {
        this.coordinate = coordinate;
    }
}
