package cf.bautroixa.maptest.services.tasks;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.ArrayList;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.constant.NotificationIds;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.core.DocumentsManager;
import cf.bautroixa.maptest.model.firestore.objects.Checkpoint;
import cf.bautroixa.maptest.model.firestore.objects.Trip;
import cf.bautroixa.maptest.model.firestore.objects.User;
import cf.bautroixa.maptest.model.sharedpref.SharedPrefHelper;
import cf.bautroixa.maptest.utils.NotificationHelper;
import cf.bautroixa.maptest.utils.ui_utils.DateFormatter;

import static android.content.Context.MODE_PRIVATE;

public class CheckpointReminderTask {
    private static final String TAG = "CheckpointReminderTask";

    public static Task<Void> doTasks(Context context) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_preference_name), MODE_PRIVATE);
        ModelManager manager = ModelManager.getInstance(context);
        return checkpointReminderTask(context, manager, sharedPreferences);

    }

    public static Task<Void> checkpointReminderTask(final Context context, final ModelManager manager, final SharedPreferences sharedPreferences) {
        final TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        if (!SharedPrefHelper.isCheckpointReminderOn(sharedPreferences)) {
            taskCompletionSource.setResult(null);
            return taskCompletionSource.getTask();
        }
        manager.getBaseUsersManager().oneTimeListenGet(manager.getCurrentUser().getId(), new DocumentsManager.OnDocumentGotListener<User>() {
            @Override
            public void onGot(final User currentUser) {
                if (currentUser.getActiveTripRef() != null) {
                    manager.getBaseTripsManager().oneTimeListenGet(currentUser.getActiveTripRef().getId(), new DocumentsManager.OnDocumentGotListener<Trip>() {
                        @Override
                        public void onGot(Trip trip) {
                            trip.getCheckpointsManager().addOneTimeInitCompleteListener(new DocumentsManager.OnInitCompleteListener<Checkpoint>() {
                                @Override
                                public void onComplete(ArrayList<Checkpoint> checkpoints) {
                                    for (int i = 0; i < checkpoints.size(); i++) {
                                        Checkpoint checkpoint = checkpoints.get(i);
                                        long timeLeft = checkpoint.getTime().toDate().getTime() - System.currentTimeMillis();
                                        if (-15 * 60 * 1000 < timeLeft && timeLeft < 30 * 60 * 1000) {
                                            NotificationHelper.sendNotification(context, NotificationIds.CHECKPOINT_REMINDER + i,
                                                    "Tripgether địa điểm tiếp theo",
                                                    String.format(context.getString(R.string.user_notification_checkpoint_remind), DateFormatter.formatExactTimeLeft(timeLeft), checkpoint.getName())
                                            );
                                        }
                                    }
                                    taskCompletionSource.setResult(null);
                                }
                            });
                        }
                    });
                } else {
                    taskCompletionSource.setResult(null);
                }
            }
        });
        return taskCompletionSource.getTask();
    }
}
