package cf.bautroixa.maptest.model.firestore.objects;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;

import cf.bautroixa.maptest.model.firestore.core.Document;

public class Comment extends Post {
    DocumentReference replyCommentRef;
    @Exclude
    Comment replyComment;

    public Comment() {
    }

    public Comment(DocumentReference ownerRef, String body, DocumentReference replyCommentRef) {
        super(ownerRef, body, null, null, null);
        this.replyCommentRef = replyCommentRef;
    }

    @Override
    protected void update(Document document) {
        Comment comment = (Comment) document;
        this.replyCommentRef = comment.getReplyCommentRef();
        super.update(document);
    }

    public DocumentReference getReplyCommentRef() {
        return replyCommentRef;
    }

    public void setReplyCommentRef(DocumentReference replyCommentRef) {
        this.replyCommentRef = replyCommentRef;
    }

    @Exclude
    public Comment getReplyComment() {
        return replyComment;
    }

    @Exclude
    public void setReplyComment(Comment replyComment) {
        this.replyComment = replyComment;
    }
}
