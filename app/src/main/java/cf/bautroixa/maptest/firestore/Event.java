package cf.bautroixa.maptest.firestore;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import cf.bautroixa.maptest.data.NotificationItem;

public class Event extends Data {
    @Exclude
    public static final String USER_REF = "userRef";
    @Exclude
    public static final String SOS_REF = "sosRef";
    @Exclude
    public static final String CHECKPOINT_REF = "checkpointRef";
    @Exclude
    public static final String TIME = "time";
    @Exclude
    public static final String TYPE = "type";

    private int type;
    @ServerTimestamp
    private Timestamp time;
    private DocumentReference sosRef, userRef, checkpointRef;

    @Exclude
    private NotificationItem notificationItem;

    public Event() {
    }

    public Event(int type, DocumentReference sosRef, DocumentReference userRef, DocumentReference checkpointRef) {
        this.type = type;
        this.sosRef = sosRef;
        this.userRef = userRef;
        this.checkpointRef = checkpointRef;
    }

    public DocumentReference getUserRef() {
        return userRef;
    }

    public void setUserRef(DocumentReference userRef) {
        this.userRef = userRef;
    }

    public DocumentReference getSosRef() {
        return sosRef;
    }

    public void setSosRef(DocumentReference sosRef) {
        this.sosRef = sosRef;
    }

    public DocumentReference getCheckpointRef() {
        return checkpointRef;
    }

    public void setCheckpointRef(DocumentReference checkpointRef) {
        this.checkpointRef = checkpointRef;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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

    ;

    @Exclude
    @Override
    public void onDocumentSnapshot(DocumentSnapshot documentSnapshot) {
        Event event = documentSnapshot.toObject(Event.class);
        if (event != null) {
            update(event);
        }
    }

    @Exclude
    public void update(Event event) {
        this.userRef = event.userRef;
        this.sosRef = event.sosRef;
        this.checkpointRef = event.checkpointRef;
        this.time = event.time;
        this.type = event.type;
        this.notificationItem = null;
    }

    public interface Type {
        int USER_ADDED = 1;
        int USER_REMOVED = -1;
        int USER_SOS_ADDED = 2;
        int USER_SOS_RESOLVED = -2;
        int CHECKPOINT_ADDED = 3;
        int CHECKPOINT_REMOVED = -3;
        int CHECKPOINT_ROLL_UP_ADDED = 4;
        String[] ADDED_TYPES_STRING = {"", "Tham gia nhóm", "Yêu cầu hỗ trợ", "Thêm điểm đến", "Yêu cầu tập hợp"};
        String[] REMOVED_TYPES_STRING = {"", "Rời nhóm", "Đã được giải quyết", "Xóa điểm đến"};
    }
}
