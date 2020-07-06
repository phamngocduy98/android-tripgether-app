package cf.bautroixa.maptest.presenter.impl;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.http.HttpService;
import cf.bautroixa.maptest.model.http.TripHttpService;
import cf.bautroixa.maptest.model.repo.objects.UserPublic;
import cf.bautroixa.maptest.presenter.WaitingRoomItemPresenter;

public class WaitingRoomItemPresenterImpl implements WaitingRoomItemPresenter {
    Context context;
    View view;
    ModelManager manager;

    public WaitingRoomItemPresenterImpl(Context context, View view) {
        this.context = context;
        this.view = view;
        this.manager = ModelManager.getInstance(context);
    }

    @Override
    public void allowJoinTrip(UserPublic userPublic) {
        view.onLoading();
        TripHttpService.joinTrip(userPublic.getId(), manager.getCurrentTripRef().getId(), null).addOnCompleteListener(new OnCompleteListener<HttpService.APIResponse>() {
            @Override
            public void onComplete(@NonNull Task<HttpService.APIResponse> task) {
                if (task.isSuccessful()) {
                    view.onSuccess();
                } else {
                    view.onFailed(task.getException().getMessage());
                }
            }
        });
    }
}
