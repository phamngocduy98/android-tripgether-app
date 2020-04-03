package cf.bautroixa.maptest.services;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import cf.bautroixa.maptest.LoginActivity;
import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.firestore.Collections;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.network_io.AppRequest;
import cf.bautroixa.maptest.network_io.HttpRequest;
import cf.bautroixa.maptest.utils.BatteryHelper;

public class UpdateLocationService extends Service {
    public final int NOTIFY_ID = 100;
    public final String CHANNEL_ID = "Update Location Service - Tripgether";
    FirebaseFirestore db;
    SharedPreferences sharedPref;
    String userName = User.NO_USER;
    DocumentReference currentUserRef;
    private FusedLocationProviderClient fusedLocationClient;

    public UpdateLocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();
        sharedPref = getSharedPreferences(getString(R.string.shared_preference_name), MODE_PRIVATE);
        userName = sharedPref.getString(User.USER_NAME, userName);
        currentUserRef = db.collection(Collections.USERS).document(userName);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onForeground();
        Toast.makeText(getApplicationContext(), "Tripgether đang cập nhật vị trí của bạn...", Toast.LENGTH_SHORT).show();
        final int battery = BatteryHelper.getBatteryPercentage(this);
        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    final Location location = task.getResult();
                    if (location == null) return;
                    AppRequest.getGeocodingAddress(UpdateLocationService.this, location, new HttpRequest.Callback<String>() {
                        @Override
                        public void onResponse(String response) {
                            currentUserRef.update(
                                    User.COORD, new GeoPoint(location.getLatitude(), location.getLongitude()),
                                    User.LOCATION, response,
                                    User.BATTERY, battery);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                stopForeground(STOP_FOREGROUND_REMOVE);
                            }
                        }

                        @Override
                        public void onFailure(String reason) {
                            currentUserRef.update(
                                    User.COORD, new GeoPoint(location.getLatitude(), location.getLongitude()),
                                    User.BATTERY, battery
                            );
                        }
                    });
                }
            }
        });
        return START_NOT_STICKY;
    }

    void onForeground(){
        Intent notificationClickIntent = new Intent(this, LoginActivity.class);
        notificationClickIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent goToAppIntent = PendingIntent.getActivity(this, 0, notificationClickIntent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Active notification", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("For keeping our service running in the foreground");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_marker)
                .setContentTitle("Tripgther")
                .setContentText("Tripgether đang cập nhật vị trí của bạn ...")
                .setContentIntent(goToAppIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        startForeground(NOTIFY_ID, notification);
    }
}
