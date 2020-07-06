package cf.bautroixa.maptest.utils.calculation;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;

import ch.hsr.geohash.GeoHash;
import uk.co.mgbramwell.geofire.android.GeoFire;
import uk.co.mgbramwell.geofire.android.model.Distance;
import uk.co.mgbramwell.geofire.android.model.DistanceUnit;
import uk.co.mgbramwell.geofire.android.model.QueryLocation;

public class GeoHashUtils {
    public static String getGeoPointId(GeoPoint geoPoint) {
        GeoHash geoHash = GeoHash.withCharacterPrecision(geoPoint.getLatitude(), geoPoint.getLongitude(), 9);
        return geoHash.toBase32();
    }

    public static Query queryNearby(CollectionReference collectionReference, double latitude, double longitude, double distanceInKm) {
        GeoFire geoFire = new GeoFire(collectionReference);
        QueryLocation queryLocation = QueryLocation.fromDegrees(latitude, longitude);
        Distance searchDistance = new Distance(distanceInKm, DistanceUnit.KILOMETERS);
        return geoFire.query()
                .whereNearTo(queryLocation, searchDistance)
                .build();
    }
}
