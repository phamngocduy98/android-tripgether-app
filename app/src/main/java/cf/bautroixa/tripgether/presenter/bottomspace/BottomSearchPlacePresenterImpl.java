package cf.bautroixa.tripgether.presenter.bottomspace;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;

import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.model.http.MapboxHttpService;
import cf.bautroixa.tripgether.model.types.GeocodingResult;
import cf.bautroixa.tripgether.model.types.SearchResult;
import cf.bautroixa.tripgether.presenter.bottomspace.BottomSearchPlacePresenter;

import static cf.bautroixa.tripgether.ui.bottomspace.BottomSearchPlaceFragment.ARG_LATITUDE;
import static cf.bautroixa.tripgether.ui.bottomspace.BottomSearchPlaceFragment.ARG_LONGITUDE;

public class BottomSearchPlacePresenterImpl implements BottomSearchPlacePresenter {
    private final ModelManager manager;
    Context context;
    View view;

    public BottomSearchPlacePresenterImpl(Context context, View view) {
        this.manager = ModelManager.getInstance(context);
        this.context = context;
        this.view = view;
    }

    @Override
    public void handleBundle(Bundle arg) {
        if (arg != null) {
            GeoPoint coord = new GeoPoint(arg.getDouble(ARG_LATITUDE, 0f), arg.getDouble(ARG_LONGITUDE, 0f));
            String placeName = arg.getString(SearchResult.PLACE_NAME, null);
            String placeAddress = arg.getString(SearchResult.PLACE_ADDRESS, null);
            if (placeName == null || placeAddress == null) {
                view.onLoading();
                MapboxHttpService.getGeocodingAddress(context, coord.getLatitude(), coord.getLongitude()).addOnCompleteListener(new OnCompleteListener<GeocodingResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<GeocodingResult> task) {
                        if (task.isSuccessful()) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    view.onStopLoading();
                                    GeocodingResult geocodingResult = task.getResult();
                                    assert geocodingResult != null;
                                    view.updateView(geocodingResult);
                                    view.setUpButtons(geocodingResult, manager.isTripLeader());
                                }
                            }, 2000);
                        } else {
                            view.onLoadFailed();
                        }
                    }
                });
            } else {
                view.onStopLoading();
                GeocodingResult geocodingResult = new GeocodingResult(coord.getLatitude(), coord.getLongitude(), placeName, placeAddress);
                view.updateView(geocodingResult);
                view.setUpButtons(geocodingResult, manager.isTripLeader());
            }
        }
    }

    @Override
    public Task<DocumentReference> addCheckpoint(Checkpoint checkpoint) {
        return manager.getCurrentTrip().getCheckpointsManager().create(checkpoint);
    }
}
