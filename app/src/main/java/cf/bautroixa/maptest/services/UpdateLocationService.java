package cf.bautroixa.maptest.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.SplashScreenActivity;
import cf.bautroixa.maptest.data.NotificationIds;
import cf.bautroixa.maptest.firestore.Collections;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.network_io.AppRequest;
import cf.bautroixa.maptest.network_io.HttpRequest;
import cf.bautroixa.maptest.utils.BatteryHelper;

public class UpdateLocationService extends Service implements NotificationIds {
    private static final String TAG = "UpdateLocationService";
    FirebaseFirestore db;
    FirebaseAuth mAuth;
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
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mAuth.getCurrentUser() == null) return START_NOT_STICKY;
        userName = mAuth.getUid();
        currentUserRef = db.collection(Collections.USERS).document(userName);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            onStartForeground();
        }
        Log.d(TAG, "Tripgether đang cập nhật vị trí của bạn...");
//        Toast.makeText(getApplicationContext(), "Tripgether đang cập nhật vị trí của bạn...", Toast.LENGTH_SHORT).show();
        final int battery = BatteryHelper.getBatteryPercentage(this);
        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    final Location location = task.getResult();
                    if (location == null) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                onStop();
                            }
                        }, 3000);
                        return;
                    }
                    AppRequest.getGeocodingAddress(UpdateLocationService.this, location, new HttpRequest.Callback<String>() {
                        @Override
                        public void onResponse(String response) {
                            currentUserRef.update(
                                    User.LAST_UPDATE, FieldValue.serverTimestamp(),
                                    User.COORD, new GeoPoint(location.getLatitude(), location.getLongitude()),
                                    User.LOCATION, response,
                                    User.BATTERY, battery).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    onStop();
                                }
                            });
                        }

                        @Override
                        public void onFailure(String reason) {
                            currentUserRef.update(
                                    User.LAST_UPDATE, FieldValue.serverTimestamp(),
                                    User.COORD, new GeoPoint(location.getLatitude(), location.getLongitude()),
                                    User.BATTERY, battery
                            ).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    onStop();
                                }
                            });
                        }
                    });
                }
            }
        });
        return START_NOT_STICKY;
    }

    void onStop() {
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
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_UPDATE_LOCATION_SERVICE, "Update location notification", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Show that tripgether is updating current user location in foreground");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_UPDATE_LOCATION_SERVICE)
                .setSmallIcon(R.drawable.ic_marker)
                .setContentTitle("Tripgther")
                .setContentText("Tripgether đang cập nhật vị trí của bạn ...")
                .setContentIntent(goToAppIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        startForeground(NOTI_ID_UPDATE_LOCATION_SERVICE, notification);
    }
}
