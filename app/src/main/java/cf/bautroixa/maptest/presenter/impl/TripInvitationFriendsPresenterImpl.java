package cf.bautroixa.maptest.presenter.impl;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.Notification;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.model.firestore.UserNotification;

public class TripInvitationFriendsPresenterImpl implements TripInvitationFriendsPresenter {
    ModelManager manager;
    Context context;
    View view;

    public TripInvitationFriendsPresenterImpl(Context context, View view) {
        this.manager = ModelManager.getInstance();
        this.context = context;
        this.view = view;
    }

    @Override
    public void inviteFriend(User user) {
        view.onInviting();
        user.getUserNotificationsManager().create(new UserNotification(context, Notification.UserType.INVITE_TO_TRIP, manager.getCurrentUser(), manager.getCurrentTrip()))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            view.onInvited();
                        } else {
                            view.onInviteFailed();
                        }
                    }
                });
    }
}
