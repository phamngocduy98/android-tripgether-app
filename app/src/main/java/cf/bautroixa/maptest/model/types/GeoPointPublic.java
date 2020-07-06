package cf.bautroixa.maptest.model.types;

import com.google.firebase.firestore.GeoPoint;

public class GeoPointPublic {
    public double _latitude, _longitude;
    com.google.firebase.firestore.GeoPoint firebaseGeoPoint;

    public GeoPointPublic() {
    }

    public GeoPointPublic(GeoPoint firebaseGeoPoint) {
        this.firebaseGeoPoint = firebaseGeoPoint;
        this._latitude = firebaseGeoPoint.getLatitude();
        this._longitude = firebaseGeoPoint.getLongitude();
    }

    public com.google.firebase.firestore.GeoPoint toFirebaseGeoPoint() {
        if (firebaseGeoPoint == null) {
            firebaseGeoPoint = new com.google.firebase.firestore.GeoPoint(_latitude, _longitude);
        }
        return firebaseGeoPoint;
    }
}