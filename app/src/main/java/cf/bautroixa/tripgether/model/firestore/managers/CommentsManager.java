package cf.bautroixa.tripgether.model.firestore.managers;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cf.bautroixa.tripgether.model.firestore.objects.Comment;

public class CommentsManager extends PostManager<Comment> {
    HashMap<String, List<String>> mapReplyCommentIdToCommentId;

    public CommentsManager(CollectionReference collectionReference, DocumentReference currentUserRef) {
        super(Comment.class, collectionReference, currentUserRef);
        mapReplyCommentIdToCommentId = new HashMap<>();
    }

    @Override
    public void put(Comment comment) {
        if (comment.getReplyCommentRef() != null) {
            Comment replyComment = findComment(comment.getReplyCommentRef());
            if (replyComment != null) {
                comment.setReplyComment(replyComment);
            } else {
                String replyId = comment.getReplyCommentRef().getId();
                List<String> commentIds = mapReplyCommentIdToCommentId.get(replyId);
                if (commentIds == null) commentIds = new ArrayList<>();
                commentIds.add(comment.getId());
                mapReplyCommentIdToCommentId.put(replyId, commentIds);
            }
        }
        List<String> beReplyOfComments = mapReplyCommentIdToCommentId.get(comment.getId());
        if (beReplyOfComments != null) {
            for (int i = 0; i < beReplyOfComments.size(); i++) {
                get(beReplyOfComments.get(i)).setReplyComment(comment);
            }
        }
        super.put(comment);
    }

    @Nullable
    public Comment findComment(DocumentReference commentRef) {
        for (int i = 0; i < list.size(); i++) {
            Comment comment = list.get(i);
            if (commentRef.getId().equals(comment.getRef().getId())) {
                return comment;
            }
        }
        return null;
    }
}
