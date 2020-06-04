package cf.bautroixa.maptest.model.firestore;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;

import java.util.List;

public class Discussion extends Document {
    public static final String MEMBERS = "members";
    String name;
    List<DocumentReference> members;
    RefsArrayManager<User> membersManager;
    MessagesManager messagesManager;
    DocumentReference tripRef;

    public Discussion() {
    }

    public Discussion(List<DocumentReference> members, DocumentReference tripRef, String name) {
        this.members = members;
        this.tripRef = tripRef;
        this.name = name;
    }

    public Discussion(List<DocumentReference> members) {
        this.members = members;
    }

    @Exclude
    void initSubManager(RefsArrayManager<User> baseUsersManager) {
        membersManager = new RefsArrayManager<User>(User.class, baseUsersManager);
        membersManager.updateRefList(members);
        messagesManager = new MessagesManager(ref.collection(Collections.MESSAGES));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DocumentReference> getMembers() {
        return members;
    }

    public void setMembers(List<DocumentReference> members) {
        this.members = members;
    }

    public DocumentReference getTripRef() {
        return tripRef;
    }

    public void setTripRef(DocumentReference tripRef) {
        this.tripRef = tripRef;
        // TODO: this may cause memory leak or something like that, check out if this is a problem
        if (tripRef != null) {
            tripRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Trip trip = Trip.newInstance(Trip.class, task.getResult());
                        setName(trip.getName());
                    }
                }
            });
        }
    }

    @Override
    @Exclude
    protected void update(Document document) {
        Discussion discussion = (Discussion) document;
        this.members = discussion.members;
        membersManager.updateRefList(members);
    }

    @Exclude
    public RefsArrayManager<User> getMembersManager() {
        return membersManager;
    }

    @Exclude
    public MessagesManager getMessagesManager() {
        return messagesManager;
    }
}
