package cf.bautroixa.maptest.model.firestore.objects;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;

import java.util.Arrays;
import java.util.List;

import cf.bautroixa.maptest.model.constant.Collections;
import cf.bautroixa.maptest.model.firestore.core.Document;
import cf.bautroixa.maptest.model.firestore.core.RefsArrayManager;
import cf.bautroixa.maptest.model.firestore.managers.MessagesManager;

public class Discussion extends Document {
    public static final String MEMBERS = "members", TYPE = "type";
    String name;
    List<DocumentReference> members;
    RefsArrayManager<User> membersManager;
    MessagesManager messagesManager;
    DocumentReference tripRef;
    String type;

    public Discussion() {
    }

    public Discussion(List<DocumentReference> members, DocumentReference tripRef, String name) {
        this.members = members;
        this.tripRef = tripRef;
        this.name = name;
        this.type = Type.GROUP;
    }

    public Discussion(DocumentReference firstUser, DocumentReference secondUser) {
        this.members = Arrays.asList(firstUser, secondUser);
        this.type = Type.SINGLE;
    }

    @Exclude
    public void initSubManager(RefsArrayManager<User> baseUsersManager) {
        if (membersManager == null) {
            membersManager = new RefsArrayManager<User>(User.class, baseUsersManager);
        }
        if (members != null) membersManager.updateRefList(members);
        if (messagesManager == null) {
            messagesManager = new MessagesManager(ref.collection(Collections.MESSAGES));
        }
        setSubManagerAvailable(true);
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public static class Type {
        public static final String SINGLE = "single";
        public static final String GROUP = "group";
    }
}
