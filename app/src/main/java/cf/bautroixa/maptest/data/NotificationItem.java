package cf.bautroixa.maptest.data;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.Event;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.SosRequest;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.utils.DateFormatter;

public class NotificationItem {
    private static final String TAG = "NotificationItem";
    public static final NotificationItem errorNotificationItem = new NotificationItem(0, User.DEFAULT_AVATAR, "Unknown event", "Unknown event", "", null, "");

    int eventType;
    String refId;
    String avatar;
    String time;
    /**
     * content to show in notification
     */
    String content;
    /**
     * title to show in alert activity
     */
    String title;
    /**
     * description to show in alert activity
     */
    String description;


    public NotificationItem(int eventType, String avatar, String content, String title, String description, @Nullable Timestamp time, String refId) {
        this.eventType = eventType;
        this.avatar = avatar;
        this.content = content;
        this.title = title;
        this.description = description;
        if (time != null) {
            this.time = DateFormatter.format(time);
        } else {
            this.time = "Time is unknown";
        }
        this.refId = refId;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static class Factory {
        public static Task<NotificationItem> factory(final MainAppManager manager, final Event event) {
            final DocumentReference checkpointRef = event.getCheckpointRef(), userRef = event.getUserRef(), sosRef = event.getSosRef();
            switch (event.getType()) {
                case Event.Type.USER_ADDED:
                case Event.Type.USER_REMOVED:
                    if (userRef == null) {
                        Log.e(TAG, "invalid USER_ADDED/USER_REMOVED event");
                        TaskCompletionSource<NotificationItem> source = new TaskCompletionSource<NotificationItem>();
                        source.setResult(errorNotificationItem);
                        return source.getTask();
                    }
                    return manager.getMembersManager().requestGet(userRef.getId()).continueWith(new Continuation<User, NotificationItem>() {
                        @Override
                        public NotificationItem then(@NonNull Task<User> task) throws Exception {
                            if (task.isSuccessful()) {
                                User user = task.getResult();
                                String actionName = event.getType() == Event.Type.USER_ADDED ? "tham gia" : "rời";
                                String message = String.format("<b>%s</b> đã %s nhóm!", user.getName(), actionName);
                                return new NotificationItem(event.getType(), user.getAvatar(), message, "", "", event.getTime(), userRef.getId());
                            }
                            return errorNotificationItem;
                        }
                    });
                case Event.Type.USER_SOS_ADDED:
                case Event.Type.USER_SOS_RESOLVED:
                    if (sosRef == null) {
                        Log.e(TAG, "invalid USER_SOS_ADDED/USER_SOS_RESOLVED event");
                        TaskCompletionSource<NotificationItem> source = new TaskCompletionSource<NotificationItem>();
                        source.setResult(errorNotificationItem);
                        return source.getTask();
                    }
                    return manager.getSosRequestsManager().requestGet(sosRef.getId()).continueWithTask(new Continuation<SosRequest, Task<NotificationItem>>() {
                        @Override
                        public Task<NotificationItem> then(@NonNull Task<SosRequest> task) throws Exception {
                            if (task.isSuccessful()) {
                                return factory(manager, task.getResult());
                            }
                            TaskCompletionSource<NotificationItem> source = new TaskCompletionSource<NotificationItem>();
                            source.setResult(errorNotificationItem);
                            return source.getTask();
                        }
                    });
                case Event.Type.CHECKPOINT_ADDED:
                case Event.Type.CHECKPOINT_REMOVED:
                    if (checkpointRef == null) {
                        Log.e(TAG, "invalid CHECKPOINT_ADDED/CHECKPOINT_REMOVED event");
                        TaskCompletionSource<NotificationItem> source = new TaskCompletionSource<NotificationItem>();
                        source.setResult(errorNotificationItem);
                        return source.getTask();
                    }
                    return manager.getCheckpointsManager().requestGet(checkpointRef.getId()).continueWith(new Continuation<Checkpoint, NotificationItem>() {
                        @Override
                        public NotificationItem then(@NonNull Task<Checkpoint> task) throws Exception {
                            if (task.isSuccessful()) {
                                Checkpoint checkpoint = task.getResult();
                                String actionName = event.getType() == Event.Type.CHECKPOINT_ADDED ? "thêm" : "xóa";
                                String message = String.format("Trưởng nhóm đã %s địa điểm <b>%s</b> tại vị trí %s", actionName, checkpoint.getName(), checkpoint.getLocation());
                                // TODO: get avatar of leader
                                return new NotificationItem(event.getType(), User.DEFAULT_AVATAR, message, "", "", event.getTime(), checkpointRef.getId());
                            }
                            return errorNotificationItem;
                        }
                    });
                case Event.Type.CHECKPOINT_ROLL_UP_ADDED:
                    if (checkpointRef == null) {
                        Log.e(TAG, "invalid CHECKPOINT_ADDED/CHECKPOINT_REMOVED event");
                        TaskCompletionSource<NotificationItem> source = new TaskCompletionSource<NotificationItem>();
                        source.setResult(errorNotificationItem);
                        return source.getTask();
                    }
                    return manager.getCheckpointsManager().requestGet(checkpointRef.getId()).continueWith(new Continuation<Checkpoint, NotificationItem>() {
                        @Override
                        public NotificationItem then(@NonNull Task<Checkpoint> task) throws Exception {
                            if (task.isSuccessful()) {
                                Checkpoint checkpoint = task.getResult();
                                String message = String.format("Trưởng nhóm yêu cầu tập hợp tại điểm đến <b>%s</b> tại vị trí %s", checkpoint.getName(), checkpoint.getLocation());
                                // TODO: get avatar of leader
                                return new NotificationItem(event.getType(), User.DEFAULT_AVATAR, message, "Yêu cầu tập hợp", String.format("%s", checkpoint.getName()), event.getTime(), checkpointRef.getId());
                            }
                            return errorNotificationItem;
                        }
                    });
                default:
                    TaskCompletionSource<NotificationItem> source = new TaskCompletionSource<NotificationItem>();
                    source.setResult(errorNotificationItem);
                    return source.getTask();
            }
        }

        public static Task<NotificationItem> factory(MainAppManager manager, final SosRequest sosRequest) {
            return manager.getMembersManager().requestGet(sosRequest.getId()).continueWith(new Continuation<User, NotificationItem>() {
                @Override
                public NotificationItem then(@NonNull Task<User> task) throws Exception {
                    if (task.isSuccessful()) {
                        User user = task.getResult();
                        String messageTitle = String.format("%s yêu cầu hỗ trợ", user.getName());
                        String messageDesc = String.format("%s", sosRequest.getDescription());
                        String message = String.format("<b>%s</b> đang cầu cứu sự trợ giúp: %s", user.getName(), sosRequest.getDescription());
                        return new NotificationItem(sosRequest.isResolved() ? Event.Type.USER_SOS_RESOLVED : Event.Type.USER_SOS_ADDED, user.getAvatar(), message, messageTitle, messageDesc, sosRequest.getTime(), sosRequest.getId());
                    }
                    return errorNotificationItem;
                }
            });
        }
    }
}
