package cf.bautroixa.tripgether.model.firestore.objects;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cf.bautroixa.tripgether.model.constant.Collections;
import cf.bautroixa.tripgether.model.firestore.core.CollectionManager;
import cf.bautroixa.tripgether.model.firestore.core.Document;
import cf.bautroixa.tripgether.model.firestore.core.RefsArrayManager;
import cf.bautroixa.tripgether.model.firestore.managers.NotificationsManager;
import cf.bautroixa.tripgether.utils.TaskHelper;
import cf.bautroixa.tripgether.utils.calculation.NumberGenerator;

public class Trip extends Document {
    @Exclude
    public static final String NAME = "name", LEADER = "leader", MEMBERS = "members", ACTIVE_CHECKPOINT_REF = "activeCheckpointRef";
    @Exclude
    public static final String WAITING_ROOM = "waitingRoom", INVITE_ROOM = "inviteRoom", JOIN_CODE = "joinCode";
    @Exclude
    public static final String JOIN_CODE_VALUE = "joinCode.value", JOIN_CODE_CREATE_TIME = "joinCode.createdTime";

    @Exclude
    @Nullable
    RefsArrayManager<User> membersManager;
    @Exclude
    @Nullable
    CollectionManager<Checkpoint> checkpointsManager;
    @Exclude
    @Nullable
    NotificationsManager<TripNotification> tripNotificationsManager;

    private String name;
    private DocumentReference leader;
    private List<DocumentReference> members, waitingRoom, inviteRoom;
    private DocumentReference discussionRef;
    @Nullable
    private DocumentReference activeCheckpointRef;
    @Nullable
    private JoinCode joinCode;

    public Trip() {
        this.withClass(Trip.class);
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
        this.inviteRoom = new ArrayList<>();
        this.members.add(leader);
        this.joinCode = new JoinCode(NumberGenerator.generateNumberString(12), new Timestamp(Calendar.getInstance().getTime()));
        this.discussionRef = discussionRef;
    }

    @Exclude
    public void initSubManager(RefsArrayManager<User> baseUsersManager, User notificationOwner) {
        // SubManager is independence of its parents
        if (!isSubManagerAvailable()) {
            membersManager = new RefsArrayManager<>(User.class, baseUsersManager);
            checkpointsManager = new CollectionManager<>(Checkpoint.class, ref.collection(Collections.CHECKPOINTS));
            tripNotificationsManager = new NotificationsManager<>(TripNotification.class, ref.collection(Collections.NOTIFICATIONS), notificationOwner);
            setSubManagerAvailable(true);
        }
        if (this.members != null) membersManager.updateRefList(this.members);
        if (!checkpointsManager.isListening()){
            checkpointsManager.startListening(ref.collection(Collections.CHECKPOINTS));
        }
        if (!tripNotificationsManager.isListening()){
            tripNotificationsManager.setParentDocument(notificationOwner);
            tripNotificationsManager.startListening(ref.collection(Collections.NOTIFICATIONS));
        }
    }

    @Exclude
    protected void update(Document document) {
        Trip trip = (Trip) document;
        this.name = trip.name;
        this.leader = trip.leader;
        setMembers(trip.getMembers());
        this.waitingRoom = trip.waitingRoom;
        this.inviteRoom = trip.inviteRoom;
        this.discussionRef = trip.discussionRef;
        this.joinCode = trip.joinCode;
        this.activeCheckpointRef = trip.activeCheckpointRef;
    }

    @Override
    @Exclude
    public void onRemove() {
        super.onRemove();
        if (isSubManagerAvailable()){
            membersManager.clear();
            checkpointsManager.clear();
            tripNotificationsManager.clear();
        }
    }

    @Exclude
    public RefsArrayManager<User> getMembersManager() {
        return membersManager;
    }

    @Exclude
    public CollectionManager<Checkpoint> getCheckpointsManager() {
        return checkpointsManager;
    }

    @Exclude
    public NotificationsManager<TripNotification> getTripNotificationsManager() {
        return tripNotificationsManager;
    }

    public void setMembers(List<DocumentReference> members) {
        this.members = members;
        if (membersManager != null && members != null){
            membersManager.updateRefList(members);
        }
    }

    @Exclude
    public Task<Checkpoint> getActiveCheckpoint() {
        if (activeCheckpointRef != null) {
            return checkpointsManager.waitGet(activeCheckpointRef.getId());
        }
        return TaskHelper.getCompletedTask(null);
    }

    @Exclude
    public boolean isActiveCheckpoint(Checkpoint checkpoint) {
        return activeCheckpointRef != null && activeCheckpointRef.getId().equals(checkpoint.getId());
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

    public List<DocumentReference> getWaitingRoom() {
        return waitingRoom;
    }

    public void setWaitingRoom(List<DocumentReference> waitingRoom) {
        this.waitingRoom = waitingRoom;
    }

    public List<DocumentReference> getInviteRoom() {
        return inviteRoom;
    }

    public void setInviteRoom(List<DocumentReference> inviteRoom) {
        this.inviteRoom = inviteRoom;
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