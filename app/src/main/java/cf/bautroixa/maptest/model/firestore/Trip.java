package cf.bautroixa.maptest.model.firestore;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.List;

public class Trip extends Document {
    @Exclude
    public static final String NO_TRIP = "noTrip";
    @Exclude
    public static final String NAME = "name";
    @Exclude
    public static final String LEADER = "leader";
    @Exclude
    public static final String MEMBERS = "members";
    @Exclude
    public static final String WAITING_ROOM = "waitingRoom";
    @Exclude
    public static final String JOIN_CODE = "joinCode";
    @Exclude
    public static final String JOIN_CODE_VALUE = "joinCode.value";
    @Exclude
    public static final String JOIN_CODE_CREATE_TIME = "joinCode.createdTime";
    @Exclude
    public static final String ACTIVE_CHECKPOINT_REF = "activeCheckpointRef";

    @Exclude
    @Nullable
    RefsArrayManager<User> membersManager;
    @Exclude
    @Nullable
    CollectionManager<Checkpoint> checkpointsManager;
    @Exclude
    @Nullable
    NotificationsManager<TripNotification> tripNotificationsManager;
    @Exclude
    @Nullable
    CollectionManager<Message> messagesManager;

    private String name;
    private DocumentReference leader;
    private List<DocumentReference> members, waitingRoom;
    private DocumentReference discussionRef;
    @Nullable
    private JoinCode joinCode;
    @Nullable
    private DocumentReference activeCheckpointRef;

    public Trip() {
        this.withClass(Trip.class);
        membersManager = new RefsArrayManager<>(User.class);
        checkpointsManager = new CollectionManager<>(Checkpoint.class);
        tripNotificationsManager = new NotificationsManager<>(TripNotification.class, null);
        messagesManager = new CollectionManager<>(Message.class);
    }

    /**
     * Construct a Trip, leader automatically added to members
     *
     * @param name
     * @param leader
     */
    public Trip(String name, DocumentReference leader, DocumentReference discussionRef) {
        this.name = name;
        this.leader = leader;
        this.members = new ArrayList<>();
        this.waitingRoom = new ArrayList<>();
        this.members.add(leader);
        this.discussionRef = discussionRef;
    }

    @Exclude
    public void initSubManager(RefsArrayManager<User> baseUsersManager, User tripOwner) {
        membersManager = new RefsArrayManager<>(User.class, baseUsersManager);
        checkpointsManager = new CollectionManager<>(Checkpoint.class, ref.collection(Collections.CHECKPOINTS));
        tripNotificationsManager = new NotificationsManager<>(TripNotification.class, ref.collection(Collections.NOTIFICATIONS), tripOwner);
        messagesManager = new CollectionManager<>(Message.class, ref.collection(Collections.MESSAGES));
    }

    @Exclude
    void restoreSubManagerListeners(Trip oldTrip) {
        membersManager.restoreListeners(oldTrip.getMembersManager().getListeners());
        checkpointsManager.restoreListeners(oldTrip.getCheckpointsManager().getListeners());
        tripNotificationsManager.restoreListeners(oldTrip.getTripNotificationsManager().getListeners());
        messagesManager.restoreListeners(oldTrip.getMessagesManager().getListeners());
    }

    @Exclude
    protected void update(Document document) {
        Trip trip = (Trip) document;
        this.name = trip.name;
        this.leader = trip.leader;
        this.members = trip.members;
        this.waitingRoom = trip.waitingRoom;
        this.discussionRef = trip.discussionRef;
        this.joinCode = trip.joinCode;
        this.activeCheckpointRef = trip.activeCheckpointRef;
        membersManager.updateRefList(trip.members);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DocumentReference getLeader() {
        return leader;
    }

    public void setLeader(DocumentReference leader) {
        this.leader = leader;
    }

    public List<DocumentReference> getMembers() {
        return members;
    }

    public void setMembers(List<DocumentReference> members) {
        this.members = members;
    }

    public List<DocumentReference> getWaitingRoom() {
        return waitingRoom;
    }

    public void setWaitingRoom(List<DocumentReference> waitingRoom) {
        this.waitingRoom = waitingRoom;
    }

    public DocumentReference getDiscussionRef() {
        return discussionRef;
    }

    public void setDiscussionRef(DocumentReference discussionRef) {
        this.discussionRef = discussionRef;
    }

    @Nullable
    public DocumentReference getActiveCheckpointRef() {
        return activeCheckpointRef;
    }

    public void setActiveCheckpointRef(@Nullable DocumentReference activeCheckpointRef) {
        this.activeCheckpointRef = activeCheckpointRef;
    }

    @Nullable
    public JoinCode getJoinCode() {
        return joinCode;
    }

    public void setJoinCode(JoinCode joinCode) {
        this.joinCode = joinCode;
    }

    @Exclude
    @Nullable
    public RefsArrayManager<User> getMembersManager() {
        return membersManager;
    }

    @Exclude
    @Nullable
    public CollectionManager<Checkpoint> getCheckpointsManager() {
        return checkpointsManager;
    }

    @Exclude
    @Nullable
    public NotificationsManager<TripNotification> getTripNotificationsManager() {
        return tripNotificationsManager;
    }

    @Exclude
    @Nullable
    public CollectionManager<Message> getMessagesManager() {
        return messagesManager;
    }

    @Exclude
    public Task<Checkpoint> getActiveCheckpoint() {
        if (activeCheckpointRef != null) {
            return checkpointsManager.requestGet(activeCheckpointRef.getId());
        }
        TaskCompletionSource<Checkpoint> taskCompletionSource = new TaskCompletionSource<>();
        taskCompletionSource.setResult(null);
        return taskCompletionSource.getTask();
    }

    @Override
    @Exclude
    public void onRemove() {
        super.onRemove();
        if (membersManager != null) membersManager.clear();
        if (checkpointsManager != null) checkpointsManager.clear();
        if (tripNotificationsManager != null) tripNotificationsManager.clear();
        if (messagesManager != null) messagesManager.clear();
    }

    public static class JoinCode {
        String value;
        @ServerTimestamp
        Timestamp createdTime;

        public JoinCode() {
        }

        public JoinCode(String value, Timestamp createdTime) {
            this.value = value;
            this.createdTime = createdTime;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Nullable
        public Timestamp getCreatedTime() {
            return createdTime;
        }

        public void setCreatedTime(Timestamp createdTime) {
            this.createdTime = createdTime;
        }
    }
}