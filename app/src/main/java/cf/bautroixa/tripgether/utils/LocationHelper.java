package cf.bautroixa.tripgether.utils;

import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class LocationHelper {
    static LocationHelper instance;
    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationCallback locationCallback;
    protected String TAG = getClass().getSimpleName();
    LocationRequest locationRequest;
    ArrayList<OnLocationChangedListener> onLocationChangedListeners;
    int currentState;
    Location currentLocation;
    long recentlyHandleStateTimestamp = 0;
    boolean isInBackground = true, isRunning = true;
    int stayIntervalCount = 0;

    private LocationHelper(Context context) {
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.onLocationChangedListeners = new ArrayList<>();
        this.locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                List<Location> locations = locationResult.getLocations();
                if (locations.isEmpty()) return;
                Location bestLocation = locations.get(0);
                float bestSpeed = bestLocation.getSpeed();
//                float bestSpeedAcuracy = bestLocation.getSpeedAccuracyMetersPerSecond();
                for (int i = 1; i < locations.size(); i++) {
                    Location location = locations.get(i);
//                    if (location.getSpeedAccuracyMetersPerSecond() < bestSpeedAcuracy)
//                        bestSpeed = location.getSpeed();
                    float deltaTime = Math.abs(SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos());
                    if (location.getAccuracy() < bestLocation.getAccuracy() && deltaTime < 3000000000L && compareLocation(location, bestLocation) >= 0) {
                        bestLocation = location;
                    }
                }
                currentLocation = bestLocation;
                for (OnLocationChangedListener locationChangedListener : onLocationChangedListeners) {
                    locationChangedListener.newLocation(currentState, bestLocation);
                }
                long timeElapsed = System.currentTimeMillis() - recentlyHandleStateTimestamp;
                Log.d(TAG, "New location: speed = " + bestSpeed + " timeElapsed = " + timeElapsed);
                if (currentState == State.STATE_INITIATE || timeElapsed > 30000L) {
                    handleNewSpeed(bestSpeed);
                }
            }
        };
        turnOn();
    }

    public static int compareLocation(Location location1, Location location2) {
        long now = SystemClock.elapsedRealtimeNanos();
        float delta1Time = Math.abs(location1.getElapsedRealtimeNanos() - now);
        float delta2Time = Math.abs(location2.getElapsedRealtimeNanos() - now);
        return -Float.compare(location1.getAccuracy() * delta1Time, location2.getAccuracy() * delta2Time);
    }

    public static LocationHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (LocationHelper.class) {
                if (instance == null) instance = new LocationHelper(context);
            }
        }
        return instance;
    }

    public boolean isInBackground() {
        return isInBackground;
    }

    public void setInBackground(boolean inBackground) {
        if (inBackground ^ isInBackground) {
            isInBackground = inBackground;
            handleState(currentState);
        }
    }

    private void handleNewSpeed(float bestSpeed) {
        if (bestSpeed > 5.55 && currentState != State.STATE_DRIVING) { // driving speed > 5.55m/s ~ 20km/h
            handleState(State.STATE_DRIVING);
        } else if (bestSpeed > 2.77 && currentState != State.STATE_CYCLING) { // 10km/h < cycling speed < 20km/h
            handleState(State.STATE_CYCLING);
        } else if (bestSpeed > 1 && currentState != State.STATE_WALKING) { // 3.6km/h < walking speed < 10km/h
            handleState(State.STATE_WALKING);
        } else {
            // may be there is GPS problem that cause speed < 1m/s but in fact user is driving
            if (currentState == State.STATE_DRIVING) {
                handleState(State.STATE_WALKING);
            } else if (currentState == State.STATE_INITIATE || currentState == State.STATE_WALKING) {
                handleState(State.STATE_STAYING);
                stayIntervalCount = 0;
            } else if (currentState == State.STATE_STAYING && ++stayIntervalCount > 2) {
                handleState(State.STATE_IDLE);
            }
        }
    }

    public int getAppropriateState(int state) {
        if (state == -1) return -1;
        if (isInBackground && state < 10) return 10 + state;
        if (!isInBackground && state >= 10) return state - 10;
        return state;
    }

    public void handleState(int state) {
        state = getAppropriateState(state);
        Log.d(TAG, "New state: " + state);
        recentlyHandleStateTimestamp = System.currentTimeMillis();
        currentState = state;
        if (state == State.STATE_INITIATE) { // get avg speed in first 1 minutes (short term super accurate location)
            setLocationRequest(5000, 5000, 0, LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
        // NORMAL
        if (state == State.STATE_IDLE) { // STAYING FOR A LONG TIME
            setLocationRequest(60000, 60000, 0, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }
        if (state == State.STATE_STAYING) { // avg speed < 1m/s
            setLocationRequest(60000, 60000, 0, LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
        if (state == State.STATE_WALKING) { // 1m/s ~ 3.6km/h < avg speed < 10km/h
            setLocationRequest(30000, 30000, 0, LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
        if (state == State.STATE_CYCLING) { // 10km/h < avg speed < 20km/h
            setLocationRequest(15000, 15000, 0, LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
        if (state == State.STATE_DRIVING) { // driving speed > 5.55m/s ~ 20km/h
            setLocationRequest(5000, 5000, 0, LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
        // BACKGROUND
        if (state == State.STATE_IDLE_BACKGROUND) { // STAYING FOR A LONG TIME
            setLocationRequest(5 * 60000, 60000, 5, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }
        if (state == State.STATE_STAYING_BACKGROUND) { // avg speed < 1m/s
            setLocationRequest(60000, 60000, 5, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }
        if (state == State.STATE_WALKING_BACKGROUD) { // 1m/s ~ 3.6km/h < avg speed < 20km/h
            setLocationRequest(60000, 30000, 0, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }
        if (state == State.STATE_CYCLING_BACKGROUD) { //  10km/h < avg speed < 20km/h
            setLocationRequest(60000, 30000, 0, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }
        if (state == State.STATE_DRIVING_BACKGROUD) { // driving speed > 5.55m/s ~ 20km/h
            setLocationRequest(60000, 30000, 0, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }
        for (OnLocationChangedListener locationChangedListener : onLocationChangedListeners) {
            locationChangedListener.newState(state);
        }
    }

    public void setLocationRequest(long interval, long fastestInterval, float smallestDisplacement, int priority) {
        turnOff();
        this.locationRequest = LocationRequest.create();
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(fastestInterval);
        locationRequest.setSmallestDisplacement(smallestDisplacement);
        locationRequest.setPriority(priority);
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public void turnOn() {
        currentState = State.STATE_INITIATE;
        handleState(State.STATE_INITIATE);
        isRunning = true;
    }

    public void turnOff() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        isRunning = false;
    }

    public void addListener(OnLocationChangedListener locationChangedListener) {
        onLocationChangedListeners.add(locationChangedListener);
        if (!isRunning) turnOn();
    }

    public void removeListener(OnLocationChangedListener locationChangedListener) {
        onLocationChangedListeners.remove(locationChangedListener);
        if (onLocationChangedListeners.size() == 0) {
            Log.d(TAG, "TURN OFF due to no listener attached");
            turnOff();
        }
    }

    public void attachListener(LifecycleOwner lifecycleOwner, final OnLocationChangedListener locationChangedListener) {

        LifecycleObserver lifecycleObserver = new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            public void connectListener() {
                onLocationChangedListeners.add(locationChangedListener);
                locationChangedListener.newLocation(currentState, currentLocation);
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            public void disconnectListener() {
                onLocationChangedListeners.remove(locationChangedListener);
            }
        };
        lifecycleOwner.getLifecycle().addObserver(lifecycleObserver);
    }

    public interface State {
        int STATE_INITIATE = -1;
        int STATE_IDLE = 0, STATE_STAYING = 1, STATE_WALKING = 2, STATE_CYCLING = 3, STATE_DRIVING = 4;
        int STATE_IDLE_BACKGROUND = 11, STATE_STAYING_BACKGROUND = 11, STATE_WALKING_BACKGROUD = 12, STATE_CYCLING_BACKGROUD = 13, STATE_DRIVING_BACKGROUD = 14;
    }

    public static class OnLocationChangedListener {
        public void newLocation(int state, @Nullable Location lastAccurateLocation) {

        }

        public void newState(int state) {

        }
    }
}
