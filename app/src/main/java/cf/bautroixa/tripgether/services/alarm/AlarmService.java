package cf.bautroixa.tripgether.services.alarm;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Tasks;

import java.util.concurrent.ExecutionException;

import cf.bautroixa.tripgether.receiver.LocationUpdateWakefulReceiver;
import cf.bautroixa.tripgether.services.tasks.LocationBaseTask;

public class AlarmService extends IntentService {
    private static final String TAG = "LocationAlarmIntentService"; //getClass().getSimpleName();

    public AlarmService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent");
        try {
            Tasks.await(LocationBaseTask.doTasks(this));
            LocationUpdateWakefulReceiver.completeWakefulIntent(intent);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}
