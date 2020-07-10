package cf.bautroixa.tripgether.services.tasks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.core.RefsArrayManager;
import cf.bautroixa.tripgether.model.firestore.objects.Notification;
import cf.bautroixa.tripgether.model.firestore.objects.Trip;
import cf.bautroixa.tripgether.model.firestore.objects.TripNotification;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.model.http.MapboxHttpService;
import cf.bautroixa.tripgether.model.sharedpref.SPGetLost;
import cf.bautroixa.tripgether.model.sharedpref.SPLocationAddress;
import cf.bautroixa.tripgether.model.sharedpref.SharedPrefKeys;
import cf.bautroixa.tripgether.model.types.FcmNotification;
import cf.bautroixa.tripgether.model.types.GeocodingResult;
import cf.bautroixa.tripgether.ui.AlertActivity;
import cf.bautroixa.tripgether.utils.BatteryHelper;
import cf.bautroixa.tripgether.utils.LocationHelper;
import cf.bautroixa.tripgether.utils.TaskHelper;
import cf.bautroixa.tripgether.utils.calculation.LatLngDistance;

import static android.content.Context.MODE_PRIVATE;

public class LocationBaseTask {
    private static final String TAG = "LocationBaseTask";

    private static Task<Void> updateLocationAndBattery(User currentUser, Location location, int battery, String placeAddress) {
        if (placeAddress != null) {
            return currentUser.sendUpdate(null, User.LAST_UPDATE, FieldValue.serverTimestamp(),
                    User.LOCATION, placeAddress,
                    User.COORD, new GeoPoint(location.getLatitude(), location.getLongitude()),
                    User.LOCATION_ACCURACY, location.getAccuracy(),
                    User.SPEED, Math.round(location.getSpeed()),
                    User.BATTERY, battery);
        } else {
            return currentUser.sendUpdate(null, User.LAST_UPDATE, FieldValue.serverTimestamp(),
                    User.COORD, new GeoPoint(location.getLatitude(), location.getLongitude()),
                    User.LOCATION_ACCURACY, location.getAccuracy(),
                    User.SPEED, Math.round(location.getSpeed()),
                    User.BATTERY, battery);
        }
    }

    private static Task<Void> doLocationAddressAndBatteryTask(final Context context, final SharedPreferences sharedPreferences, final User currentUser, final Location lastLocation) {
        final int battery = BatteryHelper.getBatteryPercentage(context);
        Location lastLocationWithName = SPLocationAddress.getLastLocationWithAddress(sharedPreferences);
        if (lastLocationWithName == null || lastLocation.distanceTo(lastLocationWithName) >= 50f) {
            SPLocationAddress.setLastLocationWithAddress(sharedPreferences, lastLocation);
            return MapboxHttpService.getGeocodingAddress(context, lastLocation.getLatitude(), lastLocation.getLongitude()).continueWithTask(new Continuation<GeocodingResult, Task<Void>>() {
                @Override
                public Task<Void> then(@NonNull Task<GeocodingResult> task) throws Exception {
                    if (task.isSuccessful()) {
                        String placeAddress = Objects.requireNonNull(task.getResult()).getFullPlaceName();
                        return updateLocationAndBattery(currentUser, lastLocation, battery, placeAddress);
                    } else {
                        return updateLocationAndBattery(currentUser, lastLocation, battery, null);
                    }
                }
            });
        } else {
            return updateLocationAndBattery(currentUser, lastLocation, battery, null);
        }
    }

    private static Task<Void> determinateGetLostStatus(final Context context, final SharedPreferences sharedPreferences, ModelManager manager, final User currentUser, Trip activeTrip) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        activeTrip.getMembersManager().waitUntilInitComplete(new RefsArrayManager.OnInitCompleteListener<User>() {
            @Override
            public void onComplete(ArrayList<User> members) {
                int safeDistance = SPGetLost.getSafeDistance(sharedPreferences);
                for (User user : members) {
                    double distanceInMeters = LatLngDistance.measureDistance(currentUser.getLatLng(), user.getLatLng());
                    if (distanceInMeters <= safeDistance) {
                        // user in safe distance to others
                        SPGetLost.setLastLostTime(sharedPreferences, SPGetLost.Defaults.LAST_LOST_TIME);
                        taskCompletionSource.setResult(null);
                        return;
                    }
                }
                // if user get lost
                long lastLostTimestamp = SPGetLost.getLastLostTime(sharedPreferences);
                long now = System.currentTimeMillis();
                if (lastLostTimestamp == SPGetLost.Defaults.LAST_LOST_TIME) {
                    SPGetLost.setLastLostTime(sharedPreferences, now);
                    lastLostTimestamp = now;
                }
                if (((now - lastLostTimestamp) / 1000 / 60) % 5 == 0) { // alert each 5 min
                    Intent intent1 = new Intent(context, AlertActivity.class);
                    intent1.putExtra(FcmNotification.NOTI_TYPE, cf.bautroixa.tripgether.model.firestore.objects.Notification.UserType.YOU_GET_LOST);
                    intent1.putExtra(FcmNotification.NOTI_MESSAGE_PARAMS, "");
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent1);
                }

                if ((now - lastLostTimestamp) / 1000 / 60 > 30) { // alert team after 30 min
                    String priority = (now - lastLostTimestamp) / 1000 / 60 > 60 ? Notification.Priority.HIGH : Notification.Priority.NORMAL;
                    activeTrip.initSubManager(manager.getBaseUsersManager(), currentUser);
                    activeTrip.getTripNotificationsManager().create(new TripNotification(context, Notification.TripType.USER_GET_LOST, currentUser, null, priority)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) taskCompletionSource.setResult(null);
                            else taskCompletionSource.setException(task.getException());
                        }
                    });
                }
                taskCompletionSource.setResult(null);
            }
        });
        return taskCompletionSource.getTask();
    }

    private static Task<Void> doGetLostTask(final Context context, final ModelManager manager, final SharedPreferences sharedPreferences) {
        if (!SPGetLost.isGetLostDetectorOn(sharedPreferences)) {
            TaskHelper.getCompletedTask(null);
        }
        return manager.getBaseUsersManager().requestGet(manager.getCurrentUser().getId()).continueWithTask(new Continuation<User, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<User> task) throws Exception {
                assert task.getResult() != null;
                User currentUser = task.getResult();
                if (currentUser.getSpeed() > 5.5554f) {
                    throw new Exception("[GET LOST PAUSED] User is driving too fast > 20km/h");
                }
                if (currentUser.getActiveTripRef() == null) {
                    throw new Exception("[GET LOST PAUSED] No active trip");
                }
                return manager.getBaseTripsManager().requestGet(currentUser.getActiveTripRef().getId()).continueWithTask(new Continuation<Trip, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<Trip> task) throws Exception {
                        assert task.getResult() != null;
                        return determinateGetLostStatus(context, sharedPreferences, manager, currentUser, task.getResult());
                    }
                });
            }
        });
    }

    public static Task<Void> onNewLocation(final Context context, final SharedPreferences sharedPreferences, final Location lastLocation) {
        Log.d(TAG, "Updating Location...");

        final ModelManager manager = ModelManager.getInstance(context);
        // UPDATE BATTERY AND LOCATION ADDRESS
        Task<Void> locationAddressAndBatteryTask;
        if (sharedPreferences.getBoolean(SharedPrefKeys.SETTING_SERVICE_ON, false)) {
            locationAddressAndBatteryTask = doLocationAddressAndBatteryTask(context, sharedPreferences, manager.getCurrentUser(), lastLocation);
        } else {
            locationAddressAndBatteryTask = TaskHelper.getCompletedTask(null);
        }

        // GET LOST FEATURE
        Task<Void> getLostTask = doGetLostTask(context, manager, sharedPreferences);

        return Tasks.whenAll(locationAddressAndBatteryTask, getLostTask);
    }

    public static void onNewState(final Context context, final SharedPreferences sharedPreferences, int state) {
        Log.d(TAG, "Updating State..." + state);
    }

    public static Task<Void> doTasks(final Context context) {
        Log.d(TAG, "STARTED! doTasks");
        final TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        final LocationHelper locationHelper = LocationHelper.getInstance(context);
        final SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_preference_name), MODE_PRIVATE);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        locationHelper.addListener(new LocationHelper.OnLocationChangedListener() {
            @Override
            public void newLocation(int state, Location lastAccurateLocation) {
                locationHelper.removeListener(this);
                if (mAuth.getCurrentUser() == null) {
                    taskCompletionSource.setException(new Exception("User not authenticated"));
                    return;
                }
                LocationBaseTask.onNewLocation(context, sharedPreferences, lastAccurateLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        taskCompletionSource.setResult(null);
                    }
                });
            }

            @Override
            public void newState(int state) {
                if (mAuth.getCurrentUser() == null) {
                    taskCompletionSource.setException(new Exception("User not authenticated"));
                    return;
                }
                onNewState(context, sharedPreferences, state);
            }
        });
        return taskCompletionSource.getTask();
    }
}
