package cf.bautroixa.maptest.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import androidx.annotation.Nullable;

import cf.bautroixa.maptest.model.constant.SharedPrefs;
import cf.bautroixa.maptest.services.UpdateLocationService;

public class AlarmHelper {
    public static boolean isOn(Context context, SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean(SharedPrefs.SERVICE_ON, false);
    }

    public static void turnOn(Context context, SharedPreferences sharedPreferences) {
        sharedPreferences.edit().putBoolean(SharedPrefs.SERVICE_ON, true).commit();
        Intent serviceIntent = new Intent(context, UpdateLocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 12345, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 60000, pendingIntent);
    }

    public static void turnOn(Context context, @Nullable SharedPreferences sharedPreferences, long interval) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(SharedPrefs.SERVICE_ON, true).commit();
        }
        Intent serviceIntent = new Intent(context, UpdateLocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 12345, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), interval, pendingIntent);
    }

    public static void turnOff(Context context, SharedPreferences sharedPreferences) {
        sharedPreferences.edit().putBoolean(SharedPrefs.SERVICE_ON, false).commit();
        Intent serviceIntent = new Intent(context, UpdateLocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 12345, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }
}
