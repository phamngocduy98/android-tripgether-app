package cf.bautroixa.maptest.model.firestore;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;

public class NotificationsManager<T extends Notification> extends CollectionManager<T> {
    int notSeenCount = 0;
    @Nullable
    User notificationOwner;

    public NotificationsManager(Class<T> itemClass, @Nullable User notificationOwner) {
        super(itemClass);
        this.notificationOwner = notificationOwner;
    }

    public NotificationsManager(Class<T> itemClass, CollectionReference collectionReference, @Nullable User notificationOwner) {
        super(itemClass, collectionReference);
        this.notificationOwner = notificationOwner;
    }

    public NotificationsManager(Class<T> itemClass, CollectionReference collectionReference, Query query, @Nullable User notificationOwner) {
        super(itemClass, collectionReference, query);
        this.notificationOwner = notificationOwner;
    }

    public int getNotSeenCount() {
        return notSeenCount;
    }

    @Override
    public void add(String id, T data) {
        super.add(id, data);
        if (notificationOwner != null) {
            if (data instanceof TripNotification) {
                TripNotification tripNotification = (TripNotification) data;
                if (!tripNotification.getSeenList().contains(notificationOwner.getRef()))
                    notSeenCount++;

            }
            if (data instanceof UserNotification) {
                UserNotification userNotification = (UserNotification) data;
                if (!userNotification.isSeen()) notSeenCount++;
            }
        }
    }

    @Override
    public void update(int index, T data) {
        super.update(index, data);
        if (notificationOwner != null) {
            if (data instanceof TripNotification) {
                TripNotification tripNotification = (TripNotification) data;
                if (tripNotification.getSeenList().contains(notificationOwner.getRef())) {
                    notSeenCount--;
                    tripNotification.setSeen(true);
                }
            }
            if (data instanceof UserNotification) {
                UserNotification userNotification = (UserNotification) data;
                if (userNotification.isSeen()) notSeenCount--;
            }
        }
    }
}
