package cf.bautroixa.maptest.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.firestore.Collections;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.network_io.AppRequest;
import cf.bautroixa.maptest.network_io.HttpRequest;

public class UpdateLocationService extends Service {
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
        userName = sharedPref.getString("userName", userName);
        currentUserRef = db.collection(Collections.USERS).document(userName);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(), "Tripgether đang cập nhật vị trí của bạn...", Toast.LENGTH_LONG).show();
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
                                    User.LOCATION, response);
                        }

                        @Override
                        public void onFailure(String reason) {
                            currentUserRef.update(User.COORD, new GeoPoint(location.getLatitude(), location.getLongitude()));
                        }
                    });
                }
            }
        });
        return START_REDELIVER_INTENT;
    }
}
