package cf.bautroixa.tripgether.presenter.impl;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.WriteBatch;

import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.objects.Notification;
import cf.bautroixa.tripgether.model.firestore.objects.Trip;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.model.firestore.objects.UserNotification;
import cf.bautroixa.tripgether.presenter.TripInvitationFriendsPresenter;

public class TripInvitationFriendsPresenterImpl implements TripInvitationFriendsPresenter {
    ModelManager manager;
    Context context;
    View view;

    public TripInvitationFriendsPresenterImpl(Context context, View view) {
        this.manager = ModelManager.getInstance(context);
        this.context = context;
        this.view = view;
    }

    @Override
    public void inviteFriend(User user) {
        view.onInviting();
        WriteBatch batch = manager.newWriteBatch();
        manager.getCurrentTrip().sendUpdate(batch, Trip.INVITE_ROOM, FieldValue.arrayUnion(user.getRef()));
        user.getUserNotificationsManager().create(batch, new UserNotification(context, Notification.UserType.INVITE_TO_TRIP, manager.getCurrentUser(), manager.getCurrentTrip()));
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
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
