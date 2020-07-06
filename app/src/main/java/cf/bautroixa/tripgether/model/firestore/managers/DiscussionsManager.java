package cf.bautroixa.tripgether.model.firestore.managers;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import java.util.List;

import cf.bautroixa.tripgether.model.firestore.core.CollectionManager;
import cf.bautroixa.tripgether.model.firestore.core.DocumentsManager;
import cf.bautroixa.tripgether.model.firestore.core.RefsArrayManager;
import cf.bautroixa.tripgether.model.firestore.objects.Discussion;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.utils.TaskHelper;

public class DiscussionsManager extends CollectionManager<Discussion> {
    RefsArrayManager<User> baseUsersManager;
    DocumentReference userRef;

    public DiscussionsManager(RefsArrayManager<User> baseUsersManager, CollectionReference discussionCollectionReference, DocumentReference userRef) {
        super(Discussion.class, discussionCollectionReference, discussionCollectionReference.whereArrayContains(Discussion.MEMBERS, userRef));
        this.baseUsersManager = baseUsersManager;
        this.userRef = userRef;
    }

    @Override
    public void add(String id, Discussion discussion) {
        discussion.initSubManager(baseUsersManager);
        super.add(id, discussion);
    }


    @Override
    public void update(int index, Discussion data) {
        super.update(index, data);
        list.get(index).initSubManager(baseUsersManager);
    }

    public Task<DocumentReference> getOrCreateDiscussion(final DocumentReference currentUserRef, final DocumentReference toUserRef) {
        return queryGet(new DocumentsManager.QueryCreator() {
            @Override
            public Query create(CollectionReference collectionReference) {
                return collectionReference.whereEqualTo(Discussion.TYPE, Discussion.Type.SINGLE).whereArrayContains(Discussion.MEMBERS, currentUserRef);
            }
        }).continueWith(new Continuation<List<Discussion>, DocumentReference>() {
            @Override
            public DocumentReference then(@NonNull Task<List<Discussion>> task) throws Exception {
                List<Discussion> discussions = task.getResult();
                if (!task.isSuccessful()) throw task.getException();
                if (discussions != null && discussions.size() > 0) {
                    for (Discussion discussion : discussions) {
                        if (discussion.getMembers().size() == 2 && discussion.getMembers().contains(toUserRef)) {
                            return discussion.getRef();
                        }
                    }
                }
                return null;
            }
        }).continueWithTask(new Continuation<DocumentReference, Task<DocumentReference>>() {
            @Override
            public Task<DocumentReference> then(@NonNull Task<DocumentReference> task) throws Exception {
                if (task.getResult() != null) {
                    return TaskHelper.getCompletedTask(task.getResult());
                } else {
                    final DocumentReference discussionRef = getNewDocumentReference();
                    return create(new Discussion(currentUserRef, toUserRef).withRef(discussionRef)).continueWith(new Continuation<Void, DocumentReference>() {
                        @Override
                        public DocumentReference then(@NonNull Task<Void> task) throws Exception {
                            if (task.isSuccessful()) {
                                return discussionRef;
                            }
                            return null;
                        }
                    });
                }
            }
        });
    }
}
