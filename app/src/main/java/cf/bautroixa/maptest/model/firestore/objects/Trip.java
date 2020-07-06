package cf.bautroixa.maptest.model.firestore.objects;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cf.bautroixa.maptest.model.constant.Collections;
import cf.bautroixa.maptest.model.firestore.core.CollectionManager;
import cf.bautroixa.maptest.model.firestore.core.Document;
import cf.bautroixa.maptest.model.firestore.core.RefsArrayManager;
import cf.bautroixa.maptest.model.firestore.managers.NotificationsManager;
import cf.bautroixa.maptest.utils.TaskHelper;
import cf.bautroixa.maptest.utils.calculation.NumberGenerator;

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
    public static final String INVITE_ROOM = "inviteRoom";
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

    private String name;
    private DocumentReference leader;
    private List<DocumentReference> members, waitingRoom, inviteRoom;
    private DocumentReference discussionRef;
    @Nullable
    private JoinCode joinCode;
    @Nullable
    private DocumentReference activeCheckpointRef;

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
        if (!isSubManagerAvailable()) {
            membersManager = new RefsArrayManager<>(User.class, baseUsersManager);
            checkpointsManager = new CollectionManager<>(Checkpoint.class, ref.collection(Collections.CHECKPOINTS));
            tripNotificationsManager = new NotificationsManager<>(TripNotification.class, ref.collection(Collections.NOTIFICATIONS), notificationOwner);
            setSubManagerAvailable(true);
        }
    }

    @Exclude
    public void restoreSubManagerListeners(Trip oldTrip) {
        if (isSubManagerAvailable() && oldTrip.isSubManagerAvailable()) {
            membersManager.restoreListeners(oldTrip.getMembersManager().getListeners());
            checkpointsManager.restoreListeners(oldTrip.getCheckpointsManager().getListeners());
            tripNotificationsManager.restoreListeners(oldTrip.getTripNotificationsManager().getListeners());
        }
    }

    @Exclude
    protected void update(Document document) {
        Trip trip = (Trip) document;
        this.name = trip.name;
        this.leader = trip.leader;
        this.members = trip.members;
        this.waitingRoom = trip.waitingRoom;
        this.inviteRoom = trip.inviteRoom;
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

    @Exclude
    @Nullable
    public RefsArrayManager<User> getMembersManager() {
        return membersManager;
    }

    @Exclude
    public CollectionManager<Checkpoint> getCheckpointsManager() {
        if (checkpointsManager == null) {
            checkpointsManager = new CollectionManager<>(Checkpoint.class, ref.collection(Collections.CHECKPOINTS));
        }
        return checkpointsManager;
    }

    @Exclude
    @Nullable
    public NotificationsManager<TripNotification> getTripNotificationsManager() {
        return tripNotificationsManager;
    }

    @Exclude
    public Task<Checkpoint> getActiveCheckpoint() {
        if (activeCheckpointRef != null) {
            return checkpointsManager.requestGet(activeCheckpointRef.getId());
        }
        return TaskHelper.getCompletedTask(null);
    }

    @Override
    @Exclude
    public void onRemove() {
        super.onRemove();
        if (membersManager != null) membersManager.clear();
        if (checkpointsManager != null) checkpointsManager.clear();
        if (tripNotificationsManager != null) tripNotificationsManager.clear();
    }

    @Exclude
    public boolean isActiveCheckpoint(Checkpoint checkpoint) {
        return activeCheckpointRef != null && activeCheckpointRef.getId().equals(checkpoint.getId());
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