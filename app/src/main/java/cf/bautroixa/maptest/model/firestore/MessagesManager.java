package cf.bautroixa.maptest.model.firestore;

import com.google.firebase.firestore.CollectionReference;

public class MessagesManager extends CollectionManager<Message> {
    Message latestMessage;

    public MessagesManager(CollectionReference collectionReference) {
        super(Message.class, collectionReference);
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
