package cf.bautroixa.maptest.firestore;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;

public class FireStoreManager {
    private static final String TAG = "FireStoreManager";
    private static FireStoreManager instance;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DocumentReference currentUserRef, currentTripRef;
    private User currentUser;
    private Trip currentTrip;
    private HashMap<String, Checkpoint> checkpoints;

    private FireStoreManager(String userName) {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword("example1@gmail.com", "123456").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    AuthResult authResult = task.getResult();
                    FirebaseUser authUser = authResult.getUser();
                }
            }
        });
        currentUserRef = db.collection(Collections.USERS).document(userName);
        checkpoints = new HashMap<>();
        fetchCurrentUser();
    }

    private void fetchCurrentUser() {
        currentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null) {
                        currentUser = documentSnapshot.toObject(User.class);
                        fetchCurrentTrip();
                    }
                }
            }
        });
    }

    private void fetchCurrentTrip() {
        currentTripRef = currentUser.getActiveTrip();
        if (currentTripRef != null) {
            currentTripRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot != null) {
                            currentTrip = documentSnapshot.toObject(Trip.class);
                        }
                    }
                }
            });
            fetchCheckpoints();
        }
    }

    private void fetchCheckpoints() {
        db.collection(Collections.checkpoints(currentTripRef.getId())).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Checkpoint checkpoint = document.toObject(Checkpoint.class);
                        checkpoint.setId(document.getId());
                        checkpoints.put(document.getId(), checkpoint);
                    }
                }
            }
        });
        db.collection(Collections.checkpoints(currentTripRef.getId())).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Checkpoint checkpoint = documentSnapshot.toObject(Checkpoint.class);
                    String id = documentSnapshot.getId();
                    checkpoints.put(id, checkpoint);
                }
            }
        });
    }

    public static FireStoreManager getInstance(String userName) {
        if (instance == null) {
            synchronized (FirebaseFirestore.class) {
                if (instance == null) {
                    instance = new FireStoreManager(userName);
                }
            }
        }
        return instance;
    }

    public Task<DocumentReference> addCheckpoint(Checkpoint checkpoint) {
        return db.collection(Collections.checkpoints(currentTripRef.getId())).add(checkpoint);
    }

    public void createGatherPoint(DocumentReference checkpointRef) {
        if (currentTripRef != null) {
            currentTripRef.update(Trip.ACTIVE_CHECKPOINT, checkpointRef);
        }
    }

    public void createGatherPoint(String gatherPointName) {
        Checkpoint checkpoint = new Checkpoint(gatherPointName, currentUser.getCurrentCoord(), currentUser.getCurrentLocation(), new Timestamp(new Date()));
        if (currentTripRef != null) {
            addCheckpoint(checkpoint).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    if (task.isSuccessful()) createGatherPoint(task.getResult());
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

    public HashMap<String, Checkpoint> getCheckpoints() {
        return checkpoints;
    }

    public DocumentReference getCurrentUserRef() {
        return currentUserRef;
    }

    public DocumentReference getCurrentTripRef() {
        return currentTripRef;
    }

    public interface OnCurrentUserChanged {
        void onCurrentUserChanged(User user);
    }

    public interface OnCurrentTripChanged {
        void onCurrentTripChanged(Trip trip);
    }

    public interface OnInitCompleted {
        void OnInitCompleted();
    }
}
