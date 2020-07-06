package cf.bautroixa.maptest.model.firestore.objects;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

import cf.bautroixa.maptest.model.firestore.core.Document;

public class TripNotification extends Notification {
    @Exclude
    public static final String SEEN_LIST = "seenList";

    DocumentReference userRef;
    DocumentReference checkpointRef;
    List<DocumentReference> seenList;

    public TripNotification() {
    }

    public TripNotification(Context context, @NonNull String type, User user, @Nullable Checkpoint checkpoint, String priority) {
        super(type, user.getAvatar(), Notification.getMessageParams(context, type, user, null, checkpoint), null);
        this.userRef = user.getRef();
        this.seenList = new ArrayList<>();
        if (checkpoint != null) this.checkpointRef = checkpoint.getRef();
        this.priority = priority;
    }

    public TripNotification(String type, String avatar, List<String> messageParams, Timestamp time) {
        super(type, avatar, messageParams, time);
        this.seenList = new ArrayList<>();
    }

    public DocumentReference getUserRef() {
        return userRef;
    }

    public void setUserRef(DocumentReference userRef) {
        this.userRef = userRef;
    }

    public DocumentReference getCheckpointRef() {
        return checkpointRef;
    }

    public void setCheckpointRef(DocumentReference checkpointRef) {
        this.checkpointRef = checkpointRef;
    }

    public List<DocumentReference> getSeenList() {
        return seenList;
    }

    public void setSeenList(List<DocumentReference> seenList) {
        this.seenList = seenList;
    }

    @Override
    protected void update(Document document) {
        TripNotification tripNotification = (TripNotification) document;
        super.update(tripNotification);
        this.userRef = tripNotification.getUserRef();
        this.checkpointRef = tripNotification.getCheckpointRef();
        this.seenList = tripNotification.getSeenList();
    }
}
