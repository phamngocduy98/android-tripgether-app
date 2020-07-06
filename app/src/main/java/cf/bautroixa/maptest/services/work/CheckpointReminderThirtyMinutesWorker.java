package cf.bautroixa.maptest.services.work;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;

import java.util.concurrent.ExecutionException;

import cf.bautroixa.maptest.services.tasks.CheckpointReminderTask;

public class CheckpointReminderThirtyMinutesWorker extends Worker {
    private String TAG = "LocationScheduledWorker";

    public CheckpointReminderThirtyMinutesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Background Worker Running each 30 minutes");
        Context context = getApplicationContext();
        try {
            Log.d(TAG, "CheckpointReminderTask STARTED");
            Tasks.await(CheckpointReminderTask.doTasks(context));
            Log.d(TAG, "CheckpointReminderTask COMPLETED");
            return Result.success();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}
