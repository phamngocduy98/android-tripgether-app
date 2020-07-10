package cf.bautroixa.tripgether.presenter;

import android.content.Context;

import com.google.android.gms.tasks.Task;

import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.model.http.HttpService;
import cf.bautroixa.tripgether.model.http.TripHttpService;
import cf.bautroixa.tripgether.presenter.MainActivityPresenter;

public class MainActivityPresenterImpl implements MainActivityPresenter {
    private static final String TAG = "MainActivityPresenterImpl";
    ModelManager manager;
    Context context;
    View view;

    public MainActivityPresenterImpl(Context context, View view) {
        this.manager = ModelManager.getInstance(context);
        this.context = context;
        this.view = view;
    }

    @Override
    public Checkpoint getCheckpoint(String checkpointId) {
        return manager.getCurrentTrip().getCheckpointsManager().get(checkpointId);
    }

    @Override
    public User getUser(String userId) {
        return manager.getBaseUsersManager().get(userId);
    }

    @Override
    public Task<HttpService.APIResponse> sendLeaveTrip() {
        return TripHttpService.leaveTrip();
    }
}
