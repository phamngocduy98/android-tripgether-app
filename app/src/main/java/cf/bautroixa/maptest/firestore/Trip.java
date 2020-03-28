package cf.bautroixa.maptest.firestore;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

public class Trip {
    @Exclude public static final String NO_TRIP = "noTrip";
    @Exclude public static final String ID = "id";
    @Exclude public static final String NAME = "name";
    String name;
    @Exclude public static final String LEADER = "leader";
    DocumentReference leader;
    @Exclude public static final String MEMBERS = "members";
    List<DocumentReference> members;

    public Trip() {
    }

    public Trip(String name, DocumentReference leader) {
        this.name = name;
        this.leader = leader;
        this.members = new ArrayList<>();
        this.members.add(leader);
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
}