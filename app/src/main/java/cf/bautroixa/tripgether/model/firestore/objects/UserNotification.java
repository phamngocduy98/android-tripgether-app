package cf.bautroixa.tripgether.model.firestore.objects;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;

import cf.bautroixa.tripgether.model.firestore.core.Document;

public class UserNotification extends Notification {
    DocumentReference userRef;
    DocumentReference tripRef;
    boolean seen;

    public UserNotification() {
    }

    public UserNotification(Context context, @NonNull String type, @Nullable User user, @Nullable Trip trip) {
        super(type, user.getAvatar(), Notification.getMessageParams(context, type, user, trip, null), null);
        this.userRef = user.getRef();
        this.tripRef = trip.getRef();
    }

    public UserNotification(String type, String avatar, List<String> messageParams, Timestamp time) {
        super(type, avatar, messageParams, time);
        this.seen = false;
    }

    public DocumentReference getUserRef() {
        return userRef;
    }

    public void setUserRef(DocumentReference userRef) {
        this.userRef = userRef;
    }

    public DocumentReference getTripRef() {
        return tripRef;
    }

    public void setTripRef(DocumentReference tripRef) {
        this.tripRef = tripRef;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    @Override
    protected void update(Document document) {
        UserNotification userNotification = (UserNotification) document;
        super.update(userNotification);
        this.userRef = userNotification.userRef;
        this.tripRef = userNotification.tripRef;
        this.seen = userNotification.seen;
    }

    public interface TripType {
        String USER_ADDED = "userAdded";
        String USER_REMOVED = "userRemoved";
        String USER_SOS_ADDED = "userSosAdded";
        String USER_SOS_RESOLVED = "userSosResolved";
        String CHECKPOINT_ADDED = "checkpointAdded";
        String CHECKPOINT_REMOVED = "checkpointRemoved";
        String CHECKPOINT_GATHER_REQUEST = "checkpointGatherRequest";
        String TRIP_JOIN_REQUEST = "tripJoinRequest";
    }

    public interface UserType {
        String ADD_FRIEND = "addFriend";
        String FRIEND_ACCEPTED = "friendAccepted";
        String INVITE_TO_TRIP = "inviteToTrip";
        String TRIP_JOIN_ACCEPTED = "tripJoinAccepted";
        String TRIP_JOIN_REJECTED = "tripJoinRejected";
    }
}
