package cf.bautroixa.maptest.firestore;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import cf.bautroixa.maptest.utils.FailedTask;

public class MainAppManager {
    private static final String TAG = "MainAppManager";
    private static MainAppManager instance = null;
    OnInitCompleted onInitCompleted;
    private FirebaseFirestore db;
    private DocumentReference currentUserRef, tripRef = null;
    private User currentUser;
    private Trip currentTrip;
    private AtomicInteger stepCompleteCount;

    private MembersManager membersManager;
    private CheckpointsManager checkpointsManager;
    private SosRequestsManager sosRequestsManager;
    private EventsManager eventsManager;

    private EventListener<DocumentSnapshot> userListeners;

    private MainAppManager(FirebaseAuth mAuth, OnInitCompleted onInitCompleted) {
        db = FirebaseFirestore.getInstance();
        // create empty user and trip instance for other activity to assign listener
        currentUser = new User();
        currentTrip = new Trip();
        membersManager = new MembersManager();
        checkpointsManager = new CheckpointsManager();
        sosRequestsManager = new SosRequestsManager();
        eventsManager = new EventsManager();
        stepCompleteCount = new AtomicInteger(0);
        this.onInitCompleted = onInitCompleted;
        if (mAuth.getCurrentUser() != null) {
            login(mAuth);
        } else {
            increaseInitProgress(true, 0, "construct MainAppManager instance", "user not logged in");
        }
    }

    public static MainAppManager getInstance() {
        return getInstance(null);
    }

    public static MainAppManager getInstance(OnInitCompleted onInitCompleted) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        return getInstance(mAuth, onInitCompleted);
    }

    public static MainAppManager getInstance(FirebaseAuth mAuth, OnInitCompleted onInitCompleted) {
        if (instance == null) {
            synchronized (FirebaseFirestore.class) {
                if (instance == null) {
                    instance = new MainAppManager(mAuth, onInitCompleted);
                }
            }
        }
        return instance;
    }

    public void increaseInitProgress(boolean forceComplete, int stepNumber, String stepName, String log) {
        Log.d(TAG, "STEP " + stepName + " " + stepNumber + "/3: " + log);
        if (forceComplete || stepCompleteCount.incrementAndGet() == 3) {
            if (onInitCompleted != null) onInitCompleted.onComplete(this);
        }
    }

    public boolean isLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public void login(FirebaseAuth mAuth) {
        if (mAuth.getCurrentUser() != null && (currentUserRef == null || currentUser == null)) {
            // is logged in
            User recentUser = currentUser;
            currentUserRef = db.collection(Collections.USERS).document(mAuth.getUid());
            currentUser = new User().withId(currentUserRef.getId()).withRef(currentUserRef);
            if (recentUser != null) {
                // restore assigned listener
                currentUser.restoreListeners(recentUser.getListeners());
            }
            currentUser.setListenerRegistration(null, new Data.OnNewValueListener<User>() {
                @Override
                public void onNewData(User user) {
                    Log.d(TAG, user.getFcmToken() + "currentUser: " + user.getName());
                    initFcmToken();
                    if (user.getActiveTrip() == null) {
                        leaveTrip();
                        increaseInitProgress(true, 1, "leave trip", "no active trip");
                    } else if ((tripRef == null || !tripRef.getId().equals(user.getActiveTrip().getId()))) {
                        // join trip
                        joinTrip();
                    }
                }
            });
        } else {
            increaseInitProgress(true, 0, "login", "login failed or already logged in");
        }
    }

    public void logout() {
        leaveTrip();
        currentUserRef = null;
        if (currentUser != null) currentUser.onRemove();
    }

    private void initFcmToken() {
        if (currentUser.getFcmToken() == null || currentUser.getFcmToken().length() == 0) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "get FCM token failed failed", task.getException());
                        return;
                    }
                    String token = task.getResult().getToken();
                    currentUser.sendUpdate(null, User.FCM_TOKEN, token);
                    Log.d(TAG, "init token = " + token);
                }
            });
        }
    }

    private void joinTrip() {
        tripRef = currentUser.getActiveTrip();
        Trip recentTrip = currentTrip;
        currentTrip = new Trip().withId(currentUser.getActiveTrip().getId()).withRef(currentUser.getActiveTrip());
        if (recentTrip != null) {
            // restore assigned listener
            currentTrip.restoreListeners(recentTrip.getListeners());
        }
        // listen change in trip data
        currentTrip.setListenerRegistration(null, new Data.OnNewValueListener<Trip>() {
            @Override
            public void onNewData(Trip trip) {
                membersManager.updateRefList(currentTrip.getMembers());
                increaseInitProgress(false, 2, "join trip", "{name = " + trip.getName() + ", members.size = " + trip.getMembers().size() + "}");
            }
        });
        String tripId = getCurrentTripRef().getId();
        checkpointsManager.setCollectionListener(db.collection(Collections.checkpoints(tripId)), tripId);
        sosRequestsManager.setCollectionListener(db.collection(Collections.sos(tripId)), tripId);
        eventsManager.setCollectionListener(db.collection(Collections.events(tripId)), tripId);
        increaseInitProgress(false, 3, "init trip managers", "");
    }

    private void leaveTrip() {
        Log.d(TAG, "leave trip");
        if (currentTrip != null) currentTrip.onRemove();
        membersManager.clear();
        checkpointsManager.clear();
        sosRequestsManager.clear();
    }

    public void subscribeNotification() {
        FirebaseMessaging.getInstance().subscribeToTopic("broadcast")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "subscribeTrip";
                        if (!task.isSuccessful()) {
                            msg = "subscribe trip failed";
                        }
                        Log.d(TAG, msg);
                    }
                });
    }

    public void sendJoinTrip(@Nullable WriteBatch batch, DocumentReference tripRef, OnCompleteListener<Void> onCompleteListener) {
        if (getCurrentTripRef() != null || currentUser == null) {
            onCompleteListener.onComplete(new FailedTask<Void>("User not logged in or already join a trip"));
            return;
        }
        if (batch == null) batch = db.batch();
        currentUser.sendUpdate(batch, User.ACTIVE_TRIP, tripRef);
        eventsManager.create(batch, new Event(Event.Type.USER_ADDED, null, getCurrentUserRef(), null));
        currentTrip.sendUpdate(batch, Trip.MEMBERS, FieldValue.arrayUnion(currentUserRef));

        batch.commit().addOnCompleteListener(onCompleteListener);
    }

    public void sendJoinTrip(String tripRefId, OnCompleteListener<Void> onCompleteListener) {
        sendJoinTrip(null, db.collection(Collections.TRIPS).document(tripRefId), onCompleteListener);
    }

    public void sendLeaveTrip(@Nullable WriteBatch batch, OnCompleteListener<Void> onCompleteListener) {
        if (getCurrentTripRef() == null || currentUser == null) {
            onCompleteListener.onComplete(new FailedTask<Void>("User not logged in or user not in any trip"));
            return;
        }
        if (batch == null) batch = db.batch();
        currentTrip.sendUpdate(batch, Trip.MEMBERS, FieldValue.arrayRemove(currentUserRef));
        currentUser.sendUpdate(batch, User.ACTIVE_TRIP, null);
        eventsManager.create(batch, new Event(Event.Type.USER_REMOVED, null, getCurrentUserRef(), null));

        batch.commit().addOnCompleteListener(onCompleteListener);
    }

    public void sendAddRollUpPoint(@Nullable WriteBatch batch, DocumentReference checkpointRef, OnCompleteListener<Void> onCompleteListener) {
        if (getCurrentTripRef() != null) {
            if (batch == null) batch = db.batch();
            currentTrip.sendUpdate(batch, Trip.ACTIVE_CHECKPOINT, checkpointRef);
            eventsManager.create(batch, new Event(Event.Type.CHECKPOINT_ROLL_UP_ADDED, null, getCurrentUserRef(), checkpointRef));
            batch.commit().addOnCompleteListener(onCompleteListener);
        }
    }

    public void sendAddRollUpPoint(String gatherPointName, OnCompleteListener<Void> onCompleteListener) {
        Checkpoint checkpoint = new Checkpoint(gatherPointName, currentUser.getCurrentCoord(), currentUser.getCurrentLocation(), new Timestamp(new Date()));
        if (getCurrentTripRef() != null) {
            WriteBatch writeBatch = db.batch();
            DocumentReference newCheckpointRef = checkpointsManager.create(writeBatch, checkpoint);
            sendAddRollUpPoint(writeBatch, newCheckpointRef, onCompleteListener);
        }
    }

    /**
     * Create a new Trip with name and checkpoints
     *
     * @param tripName
     * @param checkpoints
     * @param onCompleteListener
     * @return DocumentReference to newly added Trip
     */
    public DocumentReference sendCreateTrip(String tripName, ArrayList<Checkpoint> checkpoints, OnCompleteListener<Void> onCompleteListener) {
        WriteBatch batch = db.batch();
        DocumentReference tripRef = db.collection(Collections.TRIPS).document();
        // create trip
        batch.set(tripRef, new Trip(tripName, currentUserRef));
        // add checkpoints
        for (Checkpoint checkpoint : checkpoints) {
            batch.set(db.collection(Collections.checkpoints(tripRef.getId())).document(), checkpoint);
        }
        // change leader activeTrip
        currentUser.sendUpdate(batch, User.ACTIVE_TRIP, tripRef);

        batch.commit().addOnCompleteListener(onCompleteListener);
        return tripRef;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Trip getCurrentTrip() {
        return currentTrip;
    }

    public MembersManager getMembersManager() {
        return membersManager;
    }

    public CheckpointsManager getCheckpointsManager() {
        return checkpointsManager;
    }

    public SosRequestsManager getSosRequestsManager() {
        return sosRequestsManager;
    }

    public EventsManager getEventsManager() {
        return eventsManager;
    }

    public ArrayList<Checkpoint> getCheckpoints() {
        return checkpointsManager.getData();
    }

    public ArrayList<User> getMembers() {
        return membersManager.getData();
    }

    public DocumentReference getCurrentUserRef() {
        return currentUserRef;
    }

    public DocumentReference getCurrentTripRef() {
        return currentUser != null ? currentUser.getActiveTrip() : null;
    }

    public interface OnInitCompleted {
        void onComplete(MainAppManager manager);
    }
}