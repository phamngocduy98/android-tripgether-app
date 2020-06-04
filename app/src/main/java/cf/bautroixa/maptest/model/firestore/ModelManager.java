package cf.bautroixa.maptest.model.firestore;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import cf.bautroixa.maptest.model.http.HttpRequest;
import cf.bautroixa.maptest.utils.LatLngDistance;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModelManager {
    private static final String TAG = "MainAppManager";
    private static ModelManager instance = null;
    private FirebaseFirestore db;
    private DocumentReference currentUserRef, tripRef = null;
    private User currentUser;
    private Trip currentTrip;
    private RefsArrayManager<User> baseUsersManager;
    private DiscussionsManager discussionsManagers;
    private RefsArrayManager<Trip> baseTripsManager;

    private ModelManager(FirebaseAuth mAuth) {
        db = FirebaseFirestore.getInstance();
        // create empty user and trip instance for other activity to assign listener
        currentUser = new User();
        currentTrip = new Trip();
        this.baseUsersManager = new RefsArrayManager<User>(User.class, db.collection(Collections.USERS));
        this.baseTripsManager = new RefsArrayManager<Trip>(Trip.class, db.collection(Collections.TRIPS));
        if (mAuth.getCurrentUser() != null) login(mAuth);
    }

    public static ModelManager getInstance() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        return getInstance(mAuth);
    }

    public static ModelManager getInstance(FirebaseAuth mAuth) {
        if (instance == null) {
            synchronized (FirebaseFirestore.class) {
                if (instance == null) {
                    instance = new ModelManager(mAuth);
                }
            }
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public void login(FirebaseAuth mAuth) {
        if (mAuth.getCurrentUser() != null && mAuth.getUid() != null && (currentUserRef == null || currentUser == null)) {
            // is logged in
            User recentUser = currentUser;
            currentUserRef = db.collection(Collections.USERS).document(mAuth.getUid());
            currentUser = new User().withRef(currentUserRef);
            currentUser.initSubManager(db, baseUsersManager);
            discussionsManagers = new DiscussionsManager(baseUsersManager, db.collection(Collections.MESSAGES), db.collection(Collections.MESSAGES).whereArrayContains(Discussion.MEMBERS, currentUserRef));

            if (recentUser != null) {
                // restore assigned listener
                currentUser.restoreListeners(recentUser.getListeners());
            }
            currentUser.setListenerRegistration(baseUsersManager, new Document.OnValueChangedListener<User>() {
                @Override
                public void onValueChanged(User user) {
                    Log.d(TAG, user.getFcmToken() + "currentUser: " + user.getName());
                    initFcmToken();
                    if (user.getActiveTrip() == null) {
                        leaveTrip();
                    } else if ((tripRef == null || !tripRef.getId().equals(user.getActiveTrip().getId()))) {
                        // join trip
                        joinTrip();
                    }
                }
            });
        } else {
        }
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        leaveTrip();
        currentUserRef = null;
        if (currentUser != null) currentUser.onRemove();
    }

    private void initFcmToken() {
        if (currentUser.getFcmToken() == null || currentUser.getFcmToken().length() == 0) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        Log.e(TAG, "get FCM token failed failed", task.getException());
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
        // TODO: baseTripsManager.updateRefList(currentUser.getActiveTrips());
        tripRef = currentUser.getActiveTrip();
        Trip recentTrip = currentTrip;
        currentTrip = new Trip().withRef(tripRef);
        if (recentTrip != null) {
            // restore assigned listener
            currentTrip.restoreListeners(recentTrip.getListeners());
        }
        currentTrip.setListenerRegistration(baseTripsManager, null);
        currentTrip.initSubManager(baseUsersManager, currentUser);
        currentTrip.restoreSubManagerListeners(recentTrip);
    }

    private void leaveTrip() {
        Log.d(TAG, "leave trip");
        tripRef = null;
        if (currentTrip != null) currentTrip.onRemove();
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

//    public Task<Void> sendJoinTrip(final Context context, String tripRefId, final String joinCode) {
//        final DocumentReference tripRef = db.collection(Collections.TRIPS).document(tripRefId);
//        return baseTripsManager.requestGet(tripRefId).continueWithTask(new Continuation<Trip, Task<Void>>() {
//            @Override
//            public Task<Void> then(@NonNull Task<Trip> task) throws Exception {
//                if (task.isSuccessful()){
//                    Trip newTrip = task.getResult();
//                    newTrip.initSubManager(baseUsersManager);
//                    if (!currentUser.isAvailable() || !newTrip.isAvailable()) {
//                        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
//                        taskCompletionSource.setException(new Exception("User or Trip is not available"));
//                        return taskCompletionSource.getTask();
//                    }
//                    WriteBatch batch = db.batch();
//                    if (Objects.equals(joinCode, newTrip.getJoinCode().value)){
//                        currentUser.sendUpdate(batch, User.ACTIVE_TRIP, tripRef);
//                        newTrip.getTripNotificationsManager().create(batch, new TripNotification(context, Notification.TripType.USER_ADDED, currentUser, null));
//                        newTrip.sendUpdate(batch, Trip.MEMBERS, FieldValue.arrayUnion(currentUserRef));
//                        return batch.commit();
//                    } else {
//                        newTrip.getTripNotificationsManager().create(batch, new TripNotification(context, Notification.TripType.TRIP_JOIN_REQUEST, currentUser, null));
//                        newTrip.sendUpdate(batch, Trip.WAITING_ROOM, FieldValue.arrayUnion(currentUserRef));
//                        return batch.commit();
//                    }
//                }
//                return null;
//            }
//        });
//    }
//    public Task<Void> sendLeaveTrip(Context context, @Nullable WriteBatch batch) {
//        if (!currentUser.isAvailable() || !currentTrip.isAvailable()) {
//            TaskCompletionSource<Void> source = new TaskCompletionSource<Void>();
//            source.setException(new Exception("User not logged in or user not in any trip"));
//            return source.getTask();
//        }
//        if (batch == null) batch = db.batch();
//        currentTrip.sendUpdate(batch, Trip.MEMBERS, FieldValue.arrayRemove(currentUserRef));
//        currentUser.sendUpdate(batch, User.ACTIVE_TRIP, null);
//        currentTrip.getTripNotificationsManager().create(batch, new TripNotification(context, Notification.TripType.USER_REMOVED, currentUser, null));
//
//        return batch.commit();
//    }

    public Task<HttpRequest.APIResponse> sendJoinTrip(String tripRefId, String joinCode) {
        final TaskCompletionSource<HttpRequest.APIResponse> taskCompletionSource = new TaskCompletionSource<>();
        HttpRequest.getInstance().getTripService().joinTrip(currentUserRef.getId(), tripRefId, joinCode).enqueue(new Callback<HttpRequest.APIResponse>() {
            @Override
            public void onResponse(@NotNull Call<HttpRequest.APIResponse> call, @NotNull Response<HttpRequest.APIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HttpRequest.APIResponse res = response.body();
                    taskCompletionSource.setResult(res);
                }
                Log.d(TAG, "sendJoinTrip response");
            }

            @Override
            public void onFailure(@NotNull Call<HttpRequest.APIResponse> call, @NotNull Throwable t) {
                Log.d(TAG, "sendJoinTrip failed: " + t.getMessage());
                taskCompletionSource.setResult(new HttpRequest.APIResponse(false, "Không thể kết nối tới Internet!", null));
            }
        });
        return taskCompletionSource.getTask();
    }

    public Task<HttpRequest.APIResponse> sendLeaveTrip() {
        final TaskCompletionSource<HttpRequest.APIResponse> taskCompletionSource = new TaskCompletionSource<>();
        HttpRequest.getInstance().getTripService().leaveTrip(currentUserRef.getId()).enqueue(new Callback<HttpRequest.APIResponse>() {
            @Override
            public void onResponse(Call<HttpRequest.APIResponse> call, Response<HttpRequest.APIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HttpRequest.APIResponse res = response.body();
                    taskCompletionSource.setResult(res);
                }
                Log.d(TAG, "sendLeaveTrip response");
            }

            @Override
            public void onFailure(Call<HttpRequest.APIResponse> call, Throwable t) {
                Log.d(TAG, "sendJoinTrip failed: " + t.getMessage());
                taskCompletionSource.setResult(new HttpRequest.APIResponse(false, "Không thể kết nối tới Internet!", null));
            }
        });
        return taskCompletionSource.getTask();
    }

    public Task<Void> sendAddCheckInLocation(final Context context, @Nullable WriteBatch batch, DocumentReference checkpointRef) {
        if (currentTrip.isAvailable()) {
            if (batch == null) batch = db.batch();
            currentTrip.sendUpdate(batch, Trip.ACTIVE_CHECKPOINT_REF, checkpointRef);
            final WriteBatch finalBatch = batch;
            return currentTrip.getCheckpointsManager().requestGet(checkpointRef.getId()).continueWithTask(new Continuation<Checkpoint, Task<Void>>() {
                @Override
                public Task<Void> then(@NonNull Task<Checkpoint> task) throws Exception {
                    if (task.isSuccessful()) {
                        Checkpoint checkpoint = task.getResult();
                        currentTrip.getTripNotificationsManager().create(finalBatch, new TripNotification(context, Notification.TripType.CHECKPOINT_GATHER_REQUEST, currentUser, checkpoint));
                        return finalBatch.commit();
                    }
                    return null;
                }
            });
        }
        return null;
    }

    public Task<Void> sendAddCheckInLocation(Context context, String checkpointName) {
        Checkpoint checkpoint = new Checkpoint(checkpointName, currentUser.getCurrentCoord().getLatitude(), currentUser.getCurrentCoord().getLongitude(), currentUser.getCurrentLocation(), new Timestamp(new Date()));
        if (currentTrip.isAvailable()) {
            WriteBatch writeBatch = db.batch();
            DocumentReference newCheckpointRef = currentTrip.getCheckpointsManager().create(writeBatch, checkpoint);
            return sendAddCheckInLocation(context, writeBatch, newCheckpointRef);
        }
        return null;
    }

    /**
     * Create a new Trip with name and checkpoints
     *
     * @param tripName
     * @param checkpoints
     * @return DocumentReference to newly added Trip
     */
    public Task<DocumentReference> sendCreateTrip(final String tripName, final List<Checkpoint> checkpoints) {
        final DocumentReference tripRef = db.collection(Collections.TRIPS).document();
        final DocumentReference discussionRef = db.collection(Collections.MESSAGES).document();
        // create trip
        return tripRef.set(new Trip(tripName, currentUserRef, discussionRef)).continueWithTask(new Continuation<Void, Task<DocumentReference>>() {
            @Override
            public Task<DocumentReference> then(@NonNull Task<Void> task) throws Exception {
                if (task.isSuccessful()) {
                    WriteBatch batch = db.batch();
                    // add checkpoints
                    for (Checkpoint checkpoint : checkpoints) {
                        batch.set(db.collection(Collections.checkpoints(tripRef.getId())).document(), checkpoint);
                    }
                    // change leader activeTrip
                    currentUser.sendUpdate(batch, User.ACTIVE_TRIP, tripRef);
                    // add group chat
                    batch.set(discussionRef, new Discussion(java.util.Collections.singletonList(currentUserRef), tripRef, tripName));
                    return batch.commit().continueWith(new Continuation<Void, DocumentReference>() {
                        @Override
                        public DocumentReference then(@NonNull Task<Void> task) throws Exception {
                            if (task.isSuccessful()) {
                                return tripRef;
                            }
                            return null;
                        }
                    });
                }
                return null;
            }
        });
    }

    public Task<Void> sendSosRequest(Context context, SosRequest sosRequest) {
        WriteBatch batch = db.batch();
        currentUser.sendUpdate(batch, User.SOS_REQUEST, sosRequest);
        currentUser.setSosRequest(sosRequest);
        currentTrip.getTripNotificationsManager().create(batch, new TripNotification(context, Notification.TripType.USER_SOS_ADDED, currentUser, null));
        return batch.commit();
    }

    public Task<Void> sendCheckIn() {
        return currentTrip.getActiveCheckpoint().continueWithTask(new Continuation<Checkpoint, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<Checkpoint> task) throws Exception {
                if (task.isSuccessful()) {
                    Checkpoint activeCheckpoint = task.getResult();
                    if (isReadyToCheckIn(activeCheckpoint)) {
                        return activeCheckpoint.getVisitsManager().create((Visit) new Visit().withRef(currentUser.getRef()));
                    }
                    throw new Exception("User not ready to check in");
                }
                throw task.getException();
            }
        });
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Trip getCurrentTrip() {
        return currentTrip;
    }

    public boolean isTripLeader() {
        if (currentTrip.isAvailable() && currentTrip.getLeader() != null) {
            return Objects.equals(currentUser.getId(), currentTrip.getLeader().getId());
        }
        return false;
    }

    public Task<Boolean> isReadyToCheckIn() {
        return currentTrip.getActiveCheckpoint().continueWith(new Continuation<Checkpoint, Boolean>() {
            @Override
            public Boolean then(@NonNull Task<Checkpoint> task) throws Exception {
                if (task.isSuccessful()) {
                    Checkpoint activeCheckpoint = task.getResult();
                    return isReadyToCheckIn(activeCheckpoint);
                }
                throw task.getException();
            }
        });
    }

    public boolean isReadyToCheckIn(Checkpoint activeCheckpoint) {
        if (activeCheckpoint == null) return false;
        return LatLngDistance.measureDistance(currentUser.getLatLng(), activeCheckpoint.getLatLng()) < 50; // less than 50 meter
    }

    public Task<Boolean> isUserCheckedIn() {
        return currentTrip.getActiveCheckpoint().continueWith(new Continuation<Checkpoint, Boolean>() {
            @Override
            public Boolean then(@NonNull Task<Checkpoint> task) throws Exception {
                if (task.isSuccessful()) {
                    Checkpoint activeCheckpoint = task.getResult();
                    if (activeCheckpoint == null) return false;
                    return activeCheckpoint.getVisitsManager().contains(currentUser.getId()); // visitId == userId
                }
                throw task.getException();
            }
        });
    }

    public boolean isUserCheckedIn(Checkpoint activeCheckpoint) {
        if (activeCheckpoint == null) return false;
        return activeCheckpoint.getVisitsManager().contains(currentUser.getId()); // visitId == userId
    }

    public DocumentReference getCurrentUserRef() {
        return currentUserRef;
    }

    public RefsArrayManager<User> getBaseUsersManager() {
        return baseUsersManager;
    }

    public RefsArrayManager<Trip> getBaseTripsManager() {
        return baseTripsManager;
    }

    public CollectionManager<Discussion> getDiscussionsManagers() {
        return discussionsManagers;
    }

    @Nullable
    public DocumentReference getCurrentTripRef() {
        return currentUser != null ? currentUser.getActiveTrip() : null;
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public interface OnInitCompleted {
        void onComplete(ModelManager manager);
    }

    public interface OnComplete {
        void onComplete(boolean isSuccessful, @Nullable String errorMessage);
    }
}
