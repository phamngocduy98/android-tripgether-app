package cf.bautroixa.tripgether.model.firestore;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.List;
import java.util.Objects;

import cf.bautroixa.tripgether.model.constant.Collections;
import cf.bautroixa.tripgether.model.firestore.core.Document;
import cf.bautroixa.tripgether.model.firestore.core.RefsArrayManager;
import cf.bautroixa.tripgether.model.firestore.managers.BasePlaceManager;
import cf.bautroixa.tripgether.model.firestore.managers.DiscussionsManager;
import cf.bautroixa.tripgether.model.firestore.managers.PostManager;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.model.firestore.objects.Discussion;
import cf.bautroixa.tripgether.model.firestore.objects.Message;
import cf.bautroixa.tripgether.model.firestore.objects.Notification;
import cf.bautroixa.tripgether.model.firestore.objects.Post;
import cf.bautroixa.tripgether.model.firestore.objects.SosRequest;
import cf.bautroixa.tripgether.model.firestore.objects.Trip;
import cf.bautroixa.tripgether.model.firestore.objects.TripNotification;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.ui.SplashScreenActivity;
import cf.bautroixa.tripgether.utils.calculation.LatLngDistance;
import timber.log.Timber;

public class ModelManager {
    private static ModelManager mInstance = null;
    private Context mContext;
    private FirebaseFirestore db;
    private DocumentReference currentUserRef, tripRef = null;
    private User currentUser;
    private Trip currentTrip;
    private RefsArrayManager<User> baseUsersManager;
    private DiscussionsManager discussionsManagers;
    private RefsArrayManager<Trip> baseTripsManager;
    private PostManager<Post> basePostsManager;
    private BasePlaceManager basePlaceManager;
    private String savedFCM;

    private ModelManager(Context context, FirebaseAuth mAuth) {
        mContext = context;
        db = FirebaseFirestore.getInstance();
        // create empty user and trip instance for other activity to assign listener
        currentUser = new User();
        currentTrip = new Trip();
        this.baseUsersManager = new RefsArrayManager<>(User.class, db.collection(Collections.USERS));
        this.baseTripsManager = new RefsArrayManager<>(Trip.class, db.collection(Collections.TRIPS));
        this.basePlaceManager = new BasePlaceManager(db.collection(Collections.PLACES));
        if (mAuth.getCurrentUser() != null) login(mAuth);
    }

    public static ModelManager getInstance(Context context) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        return getInstance(context, mAuth);
    }

    public static ModelManager getInstance(Context context, FirebaseAuth mAuth) {
        if (mInstance == null) {
            synchronized (ModelManager.class) {
                if (mInstance == null) {
                    mInstance = new ModelManager(context, mAuth);
                }
            }
        }
        return mInstance;
    }

    public boolean isLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public void login(final FirebaseAuth mAuth) {
        FirebaseUser mUser = mAuth.getCurrentUser();
        if (mUser != null) { // is logged in
            String uid = mUser.getUid();
            if (currentUserRef != null && !Objects.equals(currentUserRef.getId(), uid) && !currentUser.isRemoved()) {
                // remove old user object
                currentUser.onRemove();
            }
            currentUserRef = db.collection(Collections.USERS).document(uid);
            currentUser.withRef(currentUserRef);
            initTokens(mUser);
            currentUser.initSubManager(baseUsersManager);
            if (this.discussionsManagers != null) this.discussionsManagers.clear();
            this.discussionsManagers = new DiscussionsManager(baseUsersManager, db.collection(Collections.MESSAGES), currentUserRef);
            if (this.basePostsManager != null) this.basePostsManager.clear();
            this.basePostsManager = new PostManager<>(Post.class, db.collection(Collections.POSTS), currentUserRef);

            // TODO: TEST GC
            System.gc();

            currentUser.setListenerRegistration(1000, new Document.OnValueChangedListener<User>() {
                @Override
                public void onValueChanged(User user) {
                    if (user.getFcmToken() != null && savedFCM != null && !user.getFcmToken().equals(savedFCM)) {
                        logout();
                    }
                    DocumentReference activeTripRef = user.getActiveTripRef();
                    if (activeTripRef == null) {
                        tripRef = null;
                        if (currentTrip != null) currentTrip.onRemove();
                    } else if (tripRef == null || !tripRef.getId().equals(activeTripRef.getId())) {
                        if (tripRef != null && !currentTrip.isRemoved()) {
                            // remove old user object
                            currentTrip.onRemove();
                        }
                        tripRef = activeTripRef;
                        currentTrip.withRef(tripRef);
                        currentTrip.setListenerRegistration(1000, null);
                        currentTrip.initSubManager(baseUsersManager, currentUser);
                    }
                }
            });
        }
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        currentUserRef = null;
        savedFCM = null;
        if (currentUser != null) currentUser.onRemove();

        Intent logoutIntent = new Intent(mContext, SplashScreenActivity.class);
        logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK & Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(logoutIntent);
    }

    private void initTokens(FirebaseUser mUser) {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful() || task.getResult() == null) {
                    Timber.e(task.getException(), "get FCM token failed failed");
                    return;
                }
                savedFCM = task.getResult().getToken();
                currentUser.sendUpdate(null, User.FCM_TOKEN, savedFCM);
                Timber.d("init FCM token = %s", savedFCM);
            }
        });
        mUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    Timber.d("init token = %s", task.getResult().getToken());
                }
            }
        });
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
                    currentUser.sendUpdate(batch, User.ACTIVE_TRIP_REF, tripRef);
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
        currentTrip.getTripNotificationsManager().create(batch, new TripNotification(context, Notification.TripType.USER_SOS_ADDED, currentUser, null, Notification.Priority.HIGH));
        String tripDiscussionId = currentTrip.getDiscussionRef().getId();
        discussionsManagers.get(tripDiscussionId).getMessagesManager().create(batch, new Message(currentUserRef, "đang yêu cầu trợ giúp: " + sosRequest.getDescription()));
        return batch.commit();
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

    public DiscussionsManager getDiscussionsManagers() {
        return discussionsManagers;
    }

    public PostManager<Post> getBasePostsManager() {
        return basePostsManager;
    }

    public BasePlaceManager getBasePlaceManager() {
        return basePlaceManager;
    }

    @Nullable
    public DocumentReference getCurrentTripRef() {
        return currentUser != null ? currentUser.getActiveTripRef() : null;
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public WriteBatch newWriteBatch() {
        return db.batch();
    }
}
