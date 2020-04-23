package cf.bautroixa.maptest.firestore;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import cf.bautroixa.maptest.data.NotificationItem;

public class SosRequest extends Data {
    @Exclude
    public static final String LEVER = "lever";
    @Exclude
    public static final String DESCRIPTION = "description";
    @Exclude
    public static final String RESOLVED = "resolved";
    @Exclude
    public static final String TIME = "time";
    int lever;
    String description;
    boolean resolved;
    @ServerTimestamp
    Timestamp time;

    @Exclude
    NotificationItem notificationItem;

    public SosRequest() {
    }

    public SosRequest(int lever, String description, boolean resolved) {
        this.lever = lever;
        this.description = description;
        this.resolved = resolved;
    }

    public SosRequest(int lever, String description) {
        this.lever = lever;
        this.description = description;
    }

    public int getLever() {
        return lever;
    }

    public void setLever(int lever) {
        this.lever = lever;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    @Exclude
    public NotificationItem getNotificationItem(MainAppManager manager) {
        if (notificationItem == null) {
            synchronized (this) {
                if (notificationItem == null) {
                    notificationItem = NotificationItem.Factory.factory(manager, this);
                }
            }
        }
        return notificationItem;
    }

    @Exclude
    @Override
    public void onDocumentSnapshot(DocumentSnapshot documentSnapshot) {
        SosRequest sosRequest = documentSnapshot.toObject(SosRequest.class);
        if (sosRequest != null) {
            update(sosRequest);
        }
    }

    @Exclude
    public void update(SosRequest sosRequest) {
        this.lever = sosRequest.lever;
        this.description = sosRequest.description;
    }

    public interface SosLever {
        int HIGH = 1;
        int MEDIUM = 2;
        int LOW = 3;
    }
}
