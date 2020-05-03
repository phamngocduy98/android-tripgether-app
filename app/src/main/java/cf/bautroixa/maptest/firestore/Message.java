package cf.bautroixa.maptest.firestore;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

public class Message extends Data {
    @Exclude
    public static final String FROM_USER = "fromUser";
    @Exclude
    public static final String TEXT = "text";
    @Exclude
    public static final String ATTACHMENT = "attachment";
    @Exclude
    public static final String TIME = "time";

    DocumentReference fromUser;
    String text, attachment;

    // TODO: time == null means message not delivered
    @ServerTimestamp
    Timestamp time;

    public Message() {
    }

    public Message(DocumentReference fromUser, String text) {
        this.fromUser = fromUser;
        this.text = text;
    }

    public Message(DocumentReference fromUser, String text, String attachment) {
        this.fromUser = fromUser;
        this.text = text;
        this.attachment = attachment;
    }

    public DocumentReference getFromUser() {
        return fromUser;
    }

    public void setFromUser(DocumentReference fromUser) {
        this.fromUser = fromUser;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    @Nullable
    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    @Override
    public void onDocumentSnapshot(DocumentSnapshot documentSnapshot) {

    }
}
