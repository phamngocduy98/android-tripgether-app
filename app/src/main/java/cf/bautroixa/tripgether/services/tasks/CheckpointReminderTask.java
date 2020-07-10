package cf.bautroixa.tripgether.services.tasks;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.ArrayList;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.constant.NotificationIds;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.core.DocumentsManager;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.model.firestore.objects.Trip;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.model.sharedpref.SharedPrefHelper;
import cf.bautroixa.tripgether.utils.NotificationHelper;
import cf.bautroixa.tripgether.utils.TaskHelper;
import cf.bautroixa.tripgether.utils.ui_utils.DateFormatter;

import static android.content.Context.MODE_PRIVATE;

public class CheckpointReminderTask {
    private static final String TAG = "CheckpointReminderTask";

    public static Task<Void> doTasks(Context context) {
        final SharedPreferences sharedPreferences = SharedPrefHelper.getSharedPreferences(context);
        ModelManager manager = ModelManager.getInstance(context);
        return checkpointReminderTask(context, manager, sharedPreferences);

    }

    public static Task<Void> checkpointReminderTask(final Context context, final ModelManager manager, final SharedPreferences sharedPreferences) {
        if (!SharedPrefHelper.isCheckpointReminderOn(sharedPreferences)) {
            return TaskHelper.getCompletedTask(null);
        }
        return manager.getBaseUsersManager().requestGet(manager.getCurrentUser().getId()).continueWithTask(new Continuation<User, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<User> task) throws Exception {
                if (!task.isSuccessful()) throw task.getException();
                final User currentUser = task.getResult();
                if (currentUser.getActiveTripRef() == null) return TaskHelper.getCompletedTask(null);
                return manager.getBaseTripsManager().requestGet(currentUser.getActiveTripRef().getId()).continueWithTask(new Continuation<Trip, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<Trip> task) throws Exception {
                        if (!task.isSuccessful()) throw task.getException();
                        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
                        Trip trip = task.getResult();
                        trip.initSubManager(manager.getBaseUsersManager(), currentUser);
                        trip.getCheckpointsManager().waitUntilInitComplete(new DocumentsManager.OnInitCompleteListener<Checkpoint>() {
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
                        return taskCompletionSource.getTask();
                    }
                });
            }
        });
    }
}
