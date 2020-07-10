package cf.bautroixa.tripgether.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.constant.NotificationIds;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.services.tasks.LocationBaseTask;
import cf.bautroixa.tripgether.ui.SplashScreenActivity;
import cf.bautroixa.tripgether.utils.LocationHelper;

public class AlwaysOnService extends Service {
    protected String TAG = getClass().getSimpleName();
    public static final String ARG_ONE_TIME = "ARG_ONE_TIME";
    ModelManager manager;
    LocationHelper locationHelper;
    SharedPreferences sharedPreferences;
    private LocationHelper.OnLocationChangedListener locationUpdate;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        manager = ModelManager.getInstance(this);
        sharedPreferences = getSharedPreferences(getString(R.string.shared_preference_name), MODE_PRIVATE);
        locationHelper = LocationHelper.getInstance(this);
        locationUpdate = new LocationHelper.OnLocationChangedListener() {
            @Override
            public void newLocation(int state, Location lastAccurateLocation) {
                LocationBaseTask.onNewLocation(AlwaysOnService.this, sharedPreferences, lastAccurateLocation);
            }

            @Override
            public void newState(int state) {
                LocationBaseTask.onNewState(AlwaysOnService.this, sharedPreferences, state);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        locationHelper.addListener(locationUpdate);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            onStartForeground();
        }
        return START_REDELIVER_INTENT;
    }

    void onStop() {
        locationHelper.removeListener(locationUpdate);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        }
        stopSelf();
    }

    void onStartForeground() {
        Intent notificationClickIntent = new Intent(this, SplashScreenActivity.class);
        notificationClickIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent goToAppIntent = PendingIntent.getActivity(this, 0, notificationClickIntent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NotificationIds.CHANNEL_ID_UPDATE_LOCATION_SERVICE, "Update location notification", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Show that tripgether is updating current user location in foreground");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this, NotificationIds.CHANNEL_ID_UPDATE_LOCATION_SERVICE)
                .setSmallIcon(R.drawable.ic_marker)
                .setContentTitle("Tripgther")
                .setContentText("Tripgether đang cập nhật vị trí của bạn ...")
                .setContentIntent(goToAppIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        startForeground(NotificationIds.NOTI_ID_UPDATE_LOCATION_SERVICE, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        locationHelper.removeListener(locationUpdate);
    }
}
