package cf.bautroixa.tripgether.model.firestore.managers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

import cf.bautroixa.tripgether.model.firestore.core.CollectionManager;
import cf.bautroixa.tripgether.model.firestore.core.Document;
import cf.bautroixa.tripgether.model.firestore.objects.Notification;
import cf.bautroixa.tripgether.model.firestore.objects.TripNotification;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.model.firestore.objects.UserNotification;

public class NotificationsManager<T extends Notification> extends CollectionManager<T> {
    int notSeenCount = 0;
    ArrayList<OnNotificationCountChanged> onNotificationCountChangeds;


    public NotificationsManager(Class<T> itemClass, CollectionReference collectionReference, User parentDocument) {
        super(itemClass, collectionReference, parentDocument);
        this.onNotificationCountChangeds = new ArrayList<>();
    }

    public int getNotSeenCount() {
        return notSeenCount;
    }

    @Override
    public void add(String id, T data) {
        super.add(id, data);
        if (parentDocument != null) {
            if (data instanceof TripNotification) {
                TripNotification tripNotification = (TripNotification) data;
                if (!tripNotification.getSeenList().contains(parentDocument.getRef())) {
                    notSeenCount++;
                    tripNotification.setSeen(true);
                    notifyNotificationCountChanged();
                }

            }
            if (data instanceof UserNotification) {
                UserNotification userNotification = (UserNotification) data;
                if (!userNotification.isSeen()) {
                    notSeenCount++;
                    notifyNotificationCountChanged();
                }
            }
        }
    }

    @Override
    public void update(int index, T data) {
        super.update(index, data);
        if (parentDocument != null) {
            if (data instanceof TripNotification) {
                TripNotification tripNotification = (TripNotification) data;
                if (tripNotification.getSeenList().contains(parentDocument.getRef())) {
                    notSeenCount--;
                    tripNotification.setSeen(true);
                    notifyNotificationCountChanged();
                }
            }
            if (data instanceof UserNotification) {
                UserNotification userNotification = (UserNotification) data;
                if (userNotification.isSeen()) {
                    notSeenCount--;
                    notifyNotificationCountChanged();
                }
            }
        }
    }

    void notifyNotificationCountChanged() {
        for (OnNotificationCountChanged onNotificationCountChanged : onNotificationCountChangeds) {
            onNotificationCountChanged.onChanged(notSeenCount);
        }
    }

    public void attachOnNotificationCountChangedListener(LifecycleOwner lifecycleOwner, final OnNotificationCountChanged onNotificationCountChanged) {
        lifecycleOwner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            public void onAttach() {
                onNotificationCountChangeds.add(onNotificationCountChanged);
                onNotificationCountChanged.onChanged(notSeenCount);
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            public void onDetach() {
                onNotificationCountChangeds.remove(onNotificationCountChanged);
            }
        });
    }

    public interface OnNotificationCountChanged {
        void onChanged(int notSeenCount);
    }
}
