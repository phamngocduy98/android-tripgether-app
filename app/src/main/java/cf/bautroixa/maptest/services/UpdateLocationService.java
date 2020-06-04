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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.SharedPrefHelper;
import cf.bautroixa.maptest.model.constant.NotificationIds;
import cf.bautroixa.maptest.model.firestore.Collections;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.RefsArrayManager;
import cf.bautroixa.maptest.model.firestore.Trip;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.model.http.AppRequest;
import cf.bautroixa.maptest.model.types.FcmNotification;
import cf.bautroixa.maptest.model.types.GeocodingResult;
import cf.bautroixa.maptest.ui.AlertActivity;
import cf.bautroixa.maptest.ui.SplashScreenActivity;
import cf.bautroixa.maptest.utils.AlarmHelper;
import cf.bautroixa.maptest.utils.BatteryHelper;
import cf.bautroixa.maptest.utils.LatLngDistance;

public class UpdateLocationService extends Service implements NotificationIds {
    private static final String TAG = "UpdateLocationService";
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    ModelManager manager;
    String userName = User.NO_USER;
    DocumentReference currentUserRef;
    SharedPreferences sharedPreferences;
    private FusedLocationProviderClient fusedLocationClient;

    public UpdateLocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        manager = ModelManager.getInstance(mAuth);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (mAuth.getCurrentUser() == null) return START_NOT_STICKY;
        sharedPreferences = getSharedPreferences(getString(R.string.shared_preference_name), MODE_PRIVATE);
        userName = mAuth.getUid();
        currentUserRef = db.collection(Collections.USERS).document(userName);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            onStartForeground();
        }
        Log.d(TAG, "Tripgether đang cập nhật vị trí của bạn...");
//        Toast.makeText(getApplicationContext(), "Tripgether đang cập nhật vị trí của bạn...", Toast.LENGTH_SHORT).show();
        final int battery = BatteryHelper.getBatteryPercentage(this);
        if (battery < 10) {
            AlarmHelper.turnOn(this, null, 60000 * 30); // 30 minutes
        } else if (battery < 30) {
            AlarmHelper.turnOn(this, null, 60000 * 5); // 5 minutes
        }
//        fusedLocationClient.getLastLocation().continueWithTask(new Continuation<Location, Task<Void>>() {
//            @Override
//            public Task<Void> then(@NonNull Task<Location> task) throws Exception {
//                if (task.isSuccessful()) {
//                    Location location = task.getResult();
//                    if (location != null){
//                        return currentUserRef.update(
//                                User.LAST_UPDATE, FieldValue.serverTimestamp(),
//                                User.COORD, new GeoPoint(location.getLatitude(), location.getLongitude()),
//                                User.BATTERY, battery
//                        );
//                    }
//                }
//                return null;
//            }
//        });
        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    final Location location = task.getResult();
                    if (location == null) return;
                    Task<User> task1 = manager.getBaseUsersManager().requestGet(currentUserRef.getId()).addOnCompleteListener(new OnCompleteListener<User>() {
                        @Override
                        public void onComplete(@NonNull Task<User> task) {
                            if (task.isSuccessful()) {
                                final User currentUser = task.getResult();
                                if (currentUser.getActiveTrip() != null) {
                                    manager.getBaseTripsManager().requestGet(currentUser.getActiveTrip().getId()).addOnCompleteListener(new OnCompleteListener<Trip>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Trip> task) {
                                            Trip currentTrip = task.getResult();
                                            currentTrip.initSubManager(manager.getBaseUsersManager(), currentUser);
                                            currentTrip.getMembersManager().addOnInitCompleteListener(new RefsArrayManager.OnInitCompleteListener<User>() {
                                                @Override
                                                public void onComplete(ArrayList<User> members) {
                                                    int safeDistance = SharedPrefHelper.getSafeDistance(sharedPreferences);
                                                    for (User user : members) {
                                                        double distanceInMeters = LatLngDistance.measureDistance(currentUser.getLatLng(), user.getLatLng());
                                                        if (distanceInMeters <= safeDistance) {
                                                            SharedPrefHelper.resetLastLostTime(sharedPreferences);
                                                            return;
                                                        }
                                                    }
                                                    // if user get lost
                                                    long lastLostTimestamp = SharedPrefHelper.getLastLostTime(sharedPreferences);
                                                    Calendar calendar = Calendar.getInstance();
                                                    long now = calendar.getTimeInMillis();
                                                    if (lastLostTimestamp == SharedPrefHelper.SharedPrefDefaults.LAST_LOST_TIME) {
                                                        SharedPrefHelper.setLastLostTime(sharedPreferences, now);
                                                    } else {
                                                        if (((now - lastLostTimestamp) / 1000 / 60) % 5 == 0) { // alert each 5 mins
                                                            Intent intent1 = new Intent(UpdateLocationService.this, AlertActivity.class);
                                                            intent1.putExtra(FcmNotification.NOTI_TYPE, cf.bautroixa.maptest.model.firestore.Notification.UserType.YOU_GET_LOST);
                                                            intent1.putExtra(FcmNotification.NOTI_MESSAGE_PARAMS, "");
                                                            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                            startActivity(intent1);
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    });

                    Task<GeocodingResult> task2 = AppRequest.getGeocodingAddress(UpdateLocationService.this, new LatLng(location.getLatitude(), location.getLongitude())).addOnCompleteListener(new OnCompleteListener<GeocodingResult>() {
                        @Override
                        public void onComplete(@NonNull Task<GeocodingResult> task) {
                            if (task.isSuccessful()) {
                                currentUserRef.update(
                                        User.LAST_UPDATE, FieldValue.serverTimestamp(),
                                        User.COORD, new GeoPoint(location.getLatitude(), location.getLongitude()),
                                        User.LOCATION, Objects.requireNonNull(task.getResult()).getFullPlaceName(),
                                        User.BATTERY, battery
                                );
                            } else {
                                currentUserRef.update(
                                        User.LAST_UPDATE, FieldValue.serverTimestamp(),
                                        User.COORD, new GeoPoint(location.getLatitude(), location.getLongitude()),
                                        User.BATTERY, battery
                                );
                            }
                        }
                    });

                    Tasks.whenAll(task1, task2).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            onStop();
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
