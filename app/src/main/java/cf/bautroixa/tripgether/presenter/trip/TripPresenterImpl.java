package cf.bautroixa.tripgether.presenter.trip;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.http.HttpService;
import cf.bautroixa.tripgether.model.http.TripHttpService;
import cf.bautroixa.tripgether.model.repo.objects.TripPublic;

public class TripPresenterImpl implements TripPresenter {
    ModelManager manager;
    View view;
    Activity activity;
    String tripId, joinCode;

    public TripPresenterImpl(Activity activity, View view) {
        this.activity = activity;
        this.view = view;
        this.manager = ModelManager.getInstance(activity);
    }

    @Override
    public void init(String tripId, String joinCode) {
        view.onLoading();
        this.tripId = tripId;
        this.joinCode = joinCode;
        TripHttpService.getTrip(tripId).addOnCompleteListener(activity, new OnCompleteListener<TripPublic>() {
            @Override
            public void onComplete(@NonNull Task<TripPublic> task) {
                if (task.isSuccessful()) {
                    TripPublic tripPublic = task.getResult();
                    view.setupView(tripPublic);
                } else {
                    view.onFailed(task.getException().getMessage());
                }
            }
        });
    }

    @Override
    public void joinTrip() {
        view.onLoading();
        TripHttpService.joinTrip(manager.getCurrentUser().getId(), tripId, joinCode).addOnCompleteListener(new OnCompleteListener<HttpService.APIResponse>() {
            @Override
            public void onComplete(@NonNull Task<HttpService.APIResponse> task) {
                if (task.isSuccessful()) {
                    view.onJoinComplete();
                } else {
                    view.onFailed(task.getException().getMessage());
                }
            }
        });
    }
}
