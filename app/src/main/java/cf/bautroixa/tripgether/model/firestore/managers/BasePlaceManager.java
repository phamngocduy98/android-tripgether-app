package cf.bautroixa.tripgether.model.firestore.managers;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.WriteBatch;

import cf.bautroixa.tripgether.model.firestore.core.CollectionManager;
import cf.bautroixa.tripgether.model.firestore.objects.Place;
import cf.bautroixa.tripgether.utils.TaskHelper;
import uk.co.mgbramwell.geofire.android.GeoFire;

public class BasePlaceManager extends CollectionManager<Place> {
    GeoFire geoFire;

    public BasePlaceManager(CollectionReference collectionReference) {
        super(Place.class, collectionReference);
        geoFire = new GeoFire(collectionReference);
    }

    public Task<DocumentReference> getOrCreatePlace(Place place) {
        return requestGet(place.getId()).continueWith(new Continuation<Place, DocumentReference>() {
            @Override
            public DocumentReference then(@NonNull Task<Place> task) throws Exception {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        return task.getResult().getRef();
                    }
                    return null;
                }
                return null;
            }
        }).continueWithTask(new Continuation<DocumentReference, Task<DocumentReference>>() {
            @Override
            public Task<DocumentReference> then(@NonNull Task<DocumentReference> task) throws Exception {
                if (task.getResult() != null) {
                    return TaskHelper.getCompletedTask(task.getResult());
                } else {
                    final DocumentReference newPlaceRef = getDocumentReference(place.getId());
                    return create(place.withRef(newPlaceRef)).continueWith(new Continuation<Void, DocumentReference>() {
                        @Override
                        public DocumentReference then(@NonNull Task<Void> task) throws Exception {
                            if (task.isSuccessful()) {
                                geoFire.setLocation(newPlaceRef.getId(), place.getCoordinate().getLatitude(), place.getCoordinate().getLongitude());
                                return newPlaceRef;
                            }
                            return null;
                        }
                    });
                }
            }
        });
    }

    @Override
    @Deprecated
    public DocumentReference create(WriteBatch batch, Place data) {
        return super.create(batch, data);
    }
}
