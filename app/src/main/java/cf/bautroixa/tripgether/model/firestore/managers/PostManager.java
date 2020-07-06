package cf.bautroixa.tripgether.model.firestore.managers;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import cf.bautroixa.tripgether.model.firestore.core.CollectionManager;
import cf.bautroixa.tripgether.model.firestore.objects.Post;

public class PostManager<T extends Post> extends CollectionManager<T> {
    DocumentReference currentUserRef;

    public PostManager(Class<T> itemClass, CollectionReference collectionReference, DocumentReference currentUserRef) {
        super(itemClass, collectionReference);
        this.currentUserRef = currentUserRef;
    }

    public void setCurrentUserRef(DocumentReference currentUserRef) {
        this.currentUserRef = currentUserRef;
    }

    @Override
    public void put(T post) {
        post.setUserLiked(post.getLikes().contains(currentUserRef));
        super.put(post);
    }
}
