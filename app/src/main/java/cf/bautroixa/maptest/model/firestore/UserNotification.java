package cf.bautroixa.maptest.model.firestore;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentReference;

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
        UserNotification tripNotification = (UserNotification) document;
        super.update(tripNotification);
        this.userRef = tripNotification.userRef;
        this.tripRef = tripNotification.tripRef;
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
