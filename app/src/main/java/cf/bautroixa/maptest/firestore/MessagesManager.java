package cf.bautroixa.maptest.firestore;

import com.google.firebase.firestore.DocumentSnapshot;

public class MessagesManager extends CollectionsManager<Message> {
    @Override
    public Message documentSnapshotToObject(DocumentSnapshot documentSnapshot) {
        return documentSnapshot.toObject(Message.class);
    }
}
