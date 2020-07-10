package cf.bautroixa.tripgether.presenter.bottomspace;

import android.os.Bundle;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.model.types.GeocodingResult;

public interface BottomSearchPlacePresenter {
    void handleBundle(Bundle bundle);

    Task<DocumentReference> addCheckpoint(Checkpoint checkpoint);

    interface View {
        void onLoading();

        void onStopLoading();

        void onLoadFailed();

        void updateView(GeocodingResult geocodingResult);

        void setUpButtons(GeocodingResult geocodingResult, boolean isUserTripLeader);
    }
}
