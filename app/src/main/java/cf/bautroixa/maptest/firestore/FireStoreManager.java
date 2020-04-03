package cf.bautroixa.maptest.firestore;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;

public class FireStoreManager {
    private static final String TAG = "FireStoreManager";
    private static FireStoreManager instance;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DocumentReference currentUserRef, currentTripRef = null;
    private User currentUser;
    private Trip currentTrip;

    OnInitCompleted onInitCompleted;
    boolean isInitComplete = false;

    private MembersManager membersManager;
    private CheckpointsManager checkpointsManager;

    private EventListener<DocumentSnapshot> userListeners;

    private FireStoreManager(String userName, OnInitCompleted onInitCompleted) {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null){
            Log.d(TAG, "login success");
        }
        currentUserRef = db.collection(Collections.USERS).document(userName);
        membersManager = new MembersManager();
        checkpointsManager = new CheckpointsManager();
        this.onInitCompleted = onInitCompleted;
        fetchCurrentUser();
    }

    public static FireStoreManager getInstance(String userName) {
        return getInstance(userName, null);
    }

    public static FireStoreManager getInstance(String userName, OnInitCompleted onInitCompleted) {
        if (instance == null) {
            synchronized (FirebaseFirestore.class) {
                if (instance == null) {
                    instance = new FireStoreManager(userName, onInitCompleted);
                }
            }
        }
        return instance;
    }

    private void fetchCurrentUser() {
        // listen change in user data
        currentUser = new User().withId(currentUserRef.getId()).withRef(currentUserRef);
        currentUser.setListenerRegistration(null, new Data.OnNewDocumentSnapshotListener<User>() {
            @Override
            public void onNewData(User user) {
                Log.d(TAG, currentUser.getId()+"currentUser: "+currentUser.getName());
                if (user.getActiveTrip() == null){
                    Log.d(TAG, "updateTrip: null");
                    membersManager.clear();
                    checkpointsManager.clear();
                    currentTripRef = null;
                    if (currentTrip != null) currentTrip.onRemove();
                    currentTrip = null;
                } else if ((currentTripRef == null || !currentTripRef.getId().equals(user.getActiveTrip().getId()))) {
                    currentTripRef = user.getActiveTrip();
                    Log.d(TAG, "updateTrip: "+currentTripRef.getId());
                    fetchCurrentTrip();
                }
            }
        });
    }

    private void fetchCurrentTrip() {
        if (currentUser.getActiveTrip() != null) {
            // listen change in trip data, usually members property
            currentTrip = new Trip().withId(currentUser.getActiveTrip().getId()).withRef(currentUser.getActiveTrip());
            currentTrip.setListenerRegistration(null, new Data.OnNewDocumentSnapshotListener<Trip>() {
                @Override
                public void onNewData(Trip trip) {
                    Log.d(TAG, "currentTrip: "+trip.getName());
                    if (trip.getMembers().size() > 0) {
                        membersManager.updateRefList(currentTrip.getMembers());
                        Log.d(TAG, "fetch members");
                    } else {
                        Log.d(TAG, "members.size = 0");
                    }
                }
            });
            checkpointsManager.setCollectionListener(getCurrentTripRef().getId());
        }
    }

    public void addRollUpPoint(DocumentReference checkpointRef) {
        if (getCurrentTripRef() != null) {
            getCurrentTripRef().update(Trip.ACTIVE_CHECKPOINT, checkpointRef);
        }
    }

    public void addRollUpPoint(String gatherPointName) {
        Checkpoint checkpoint = new Checkpoint(gatherPointName, currentUser.getCurrentCoord(), currentUser.getCurrentLocation(), new Timestamp(new Date()));
        if (getCurrentTripRef() != null) {
            checkpointsManager.addCheckpoint(checkpoint).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    if (task.isSuccessful()) addRollUpPoint(task.getResult());
                }
            });
        }
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
        void onComplete();
    }
}
