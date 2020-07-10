package cf.bautroixa.tripgether.receiver;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import androidx.legacy.content.WakefulBroadcastReceiver;

import com.google.android.gms.tasks.Tasks;

import java.util.concurrent.ExecutionException;

import cf.bautroixa.tripgether.services.tasks.LocationBaseTask;

public class LocationUpdateWakefulReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {

        Log.d("LocationWakefulReceiver", "Starting service @ " + SystemClock.elapsedRealtime());
//        Intent service = new Intent(context, AlarmService.class);
//        startWakefulService(context, service);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Tasks.await(LocationBaseTask.doTasks(context));
                    LocationUpdateWakefulReceiver.completeWakefulIntent(intent);
                    Log.d("LocationWakefulReceiver", "Task completed @ " + SystemClock.elapsedRealtime());
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
