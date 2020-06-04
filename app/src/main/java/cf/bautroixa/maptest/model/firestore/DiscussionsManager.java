package cf.bautroixa.maptest.model.firestore;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;

public class DiscussionsManager extends CollectionManager<Discussion> {
    RefsArrayManager<User> baseUsersManager;

    public DiscussionsManager(RefsArrayManager<User> baseUsersManager, CollectionReference discussionCollectionReference) {
        super(Discussion.class, discussionCollectionReference);
        this.baseUsersManager = baseUsersManager;
    }

    public DiscussionsManager(RefsArrayManager<User> baseUsersManager, CollectionReference discussionCollectionReference, Query query) {
        super(Discussion.class, discussionCollectionReference, query);
        this.baseUsersManager = baseUsersManager;
    }

    @Override
    public void put(Discussion discussion) {
        discussion.initSubManager(baseUsersManager);
        super.put(discussion);
    }
}
