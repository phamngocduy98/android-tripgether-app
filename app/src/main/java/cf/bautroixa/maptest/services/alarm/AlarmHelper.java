package cf.bautroixa.maptest.services.alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import java.util.Calendar;

import cf.bautroixa.maptest.model.constant.RequestCodes;
import cf.bautroixa.maptest.model.sharedpref.SharedPrefKeys;
import cf.bautroixa.maptest.receiver.LocationUpdateWakefulReceiver;

public class AlarmHelper {
    public static boolean isOn(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean(SharedPrefKeys.SETTING_SERVICE_ON, false);
    }

    public static void turnOn(Context context, SharedPreferences sharedPreferences) {
        sharedPreferences.edit().putBoolean(SharedPrefKeys.SETTING_SERVICE_ON, true).commit();
//        Intent serviceIntent = new Intent(context, ScheduledWorker.class);
        Intent alarmIntent = new Intent(context, LocationUpdateWakefulReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getService(context, 12345, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent recurringAlarm = PendingIntent.getBroadcast(context, RequestCodes.ALARM, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        Calendar updateTime = Calendar.getInstance();
        am.setRepeating(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis(), 3 * 60 * 1000, recurringAlarm);
    }

    public static void turnOnWithInterval(Context context, @Nullable SharedPreferences sharedPreferences, long interval) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(SharedPrefKeys.SETTING_SERVICE_ON, true).commit();
        }
        Intent alarmIntent = new Intent(context, LocationUpdateWakefulReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getService(context, 12345, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent recurringAlarm = PendingIntent.getBroadcast(context, RequestCodes.ALARM, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        Calendar updateTime = Calendar.getInstance();
        am.setRepeating(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis(), interval, recurringAlarm);
    }

    public static void turnOff(Context context, SharedPreferences sharedPreferences) {
        sharedPreferences.edit().putBoolean(SharedPrefKeys.SETTING_SERVICE_ON, false).commit();
        //        Intent serviceIntent = new Intent(context, ScheduledWorker.class);
        Intent alarmIntent = new Intent(context, LocationUpdateWakefulReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getService(context, 12345, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent recurringAlarm = PendingIntent.getBroadcast(context, RequestCodes.ALARM, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        am.cancel(recurringAlarm);
    }
}
