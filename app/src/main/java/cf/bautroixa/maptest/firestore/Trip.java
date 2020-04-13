package cf.bautroixa.maptest.firestore;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

public class Trip extends Data {
    @Exclude public static final String NO_TRIP = "noTrip";
    @Exclude public static final String NAME = "name";
    @Exclude public static final String LEADER = "leader";
    @Exclude public static final String MEMBERS = "members";
    @Exclude public static final String ACTIVE_CHECKPOINT = "activeCheckpoint";

    private String name;
    private DocumentReference leader;
    private List<DocumentReference> members;
    private DocumentReference activeCheckpoint;

    public Trip() {
    }

    /**
     * Construct a Trip, leader automatically added to members
     *
     * @param name
     * @param leader
     */
    public Trip(String name, DocumentReference leader) {
        this.name = name;
        this.leader = leader;
        this.members = new ArrayList<>();
        this.members.add(leader);
    }

    @Override
    @Exclude
    public void onDocumentSnapshot(DocumentSnapshot documentSnapshot) {
        Trip trip = documentSnapshot.toObject(Trip.class);
        if (trip != null) update(trip);
    }
    @Exclude
    public void update(Trip trip) {
        this.name = trip.name;
        this.leader = trip.leader;
        this.members = trip.members;
        this.activeCheckpoint = trip.activeCheckpoint;
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

    public DocumentReference getActiveCheckpoint() {
        return activeCheckpoint;
    }

    public void setActiveCheckpoint(DocumentReference activeCheckpoint) {
        this.activeCheckpoint = activeCheckpoint;
    }
}