package cf.bautroixa.tripgether.model.firestore.managers;

import com.google.firebase.firestore.CollectionReference;

import cf.bautroixa.tripgether.model.firestore.core.CollectionManager;
import cf.bautroixa.tripgether.model.firestore.objects.Message;

public class MessagesManager extends CollectionManager<Message> {
    Message latestMessage;

    public MessagesManager(CollectionReference collectionReference) {
        super(Message.class, collectionReference, collectionReference.orderBy(Message.TIME).limitToLast(25), true);
    }

    @Override
    public void put(Message data) {
        super.put(data);
        if (latestMessage == null || data.getTime() == null || (latestMessage.getTime() != null && data.getTime().compareTo(latestMessage.getTime()) > 0)) {
            latestMessage = data;
        }
    }

    public Message getLatestMessage() {
        return latestMessage;
    }
}
