package cf.bautroixa.maptest.presenter.impl;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import cf.bautroixa.maptest.model.firestore.Checkpoint;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.Notification;
import cf.bautroixa.maptest.model.firestore.TripNotification;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.model.http.HttpRequest;
import cf.bautroixa.maptest.presenter.MainActivityPresenter;
import cf.bautroixa.maptest.ui.adapter.MainActivityPagerAdapter;
import cf.bautroixa.maptest.ui.map.TabMapFragment;

public class MainActivityPresenterImpl implements MainActivityPresenter {
    private static final String TAG = "MainActivityPresenterImpl";
    ModelManager manager;
    Context context;
    View view;

    public MainActivityPresenterImpl(Context context, View view) {
        this.manager = ModelManager.getInstance();
        this.context = context;
        this.view = view;
    }

    @Override
    public void handleTripNotification(String tripNotificationId) {
        view.selectTab(MainActivityPagerAdapter.Tabs.TAB_MAP);
        manager.getCurrentTrip().getTripNotificationsManager().requestGet(tripNotificationId).addOnCompleteListener(new OnCompleteListener<TripNotification>() {
            @Override
            public void onComplete(@NonNull Task<TripNotification> task) {
                if (task.isSuccessful()) {
                    TripNotification tripNotification = task.getResult();
                    DocumentReference checkpointRef = tripNotification.getCheckpointRef(), userRef = tripNotification.getUserRef();
                    switch (tripNotification.getType()) {
                        case Notification.TripType.CHECKPOINT_GATHER_REQUEST:
                            if (checkpointRef == null) {
                                Log.e(TAG, "invalid CHECKPOINT_GATHER_REQUEST event");
                                return;
                            }
                            manager.getCurrentTrip().getCheckpointsManager().requestGet(checkpointRef.getId()).addOnCompleteListener(new OnCompleteListener<Checkpoint>() {
                                @Override
                                public void onComplete(@NonNull Task<Checkpoint> task) {
                                    if (task.isSuccessful()) {
                                        Checkpoint checkpoint = task.getResult();
                                        view.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_CHECKPOINT, checkpoint);
                                    }
                                }
                            });
                            break;
                        case Notification.TripType.USER_SOS_ADDED:
                            if (userRef == null) {
                                Log.e(TAG, "invalid USER_SOS_ADDED event");
                                return;
                            }
                            manager.getCurrentUser().getFriendsManager().requestGet(userRef.getId()).addOnCompleteListener(new OnCompleteListener<User>() {
                                @Override
                                public void onComplete(@NonNull Task<User> task) {
                                    if (task.isSuccessful()) {
                                        User user = task.getResult();
                                        view.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_SOS_REQUEST, user);
                                    }
                                }
                            });
                            break;
                    }
                }
            }
        });
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
    public Task<HttpRequest.APIResponse> sendLeaveTrip() {
        return manager.sendLeaveTrip();
    }
}
