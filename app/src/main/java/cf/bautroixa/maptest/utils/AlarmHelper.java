package cf.bautroixa.maptest.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.SystemClock;

import cf.bautroixa.maptest.services.UpdateLocationService;

public class AlarmHelper {
    public static void turnOn(Activity activity) {
        Intent serviceIntent = new Intent(activity.getApplicationContext(), UpdateLocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(activity.getApplicationContext(), 12345, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) activity.getSystemService(Activity.ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 60000, pendingIntent);
    }

    public static void turnOff(Activity activity) {
        Intent serviceIntent = new Intent(activity.getApplicationContext(), UpdateLocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(activity.getApplicationContext(), 12345, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) activity.getSystemService(Activity.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }
}
