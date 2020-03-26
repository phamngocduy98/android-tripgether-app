package cf.bautroixa.maptest.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.GeomagneticField;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

public class GPSHelper {
    private static GPSHelper instance = null;
    String TAG = "LogGPS";
    LocationManager locationManager;
    Criteria crit;
    String bestLocationProvider;
    GeomagneticField geoField;
    GPSLocationListener listener;

    public interface GPSLocationListener {
        void onNewLocation(Location location);
    }

    public static GPSHelper getInstance(Context context){
        if (instance == null){
            synchronized (GPSHelper.class){
                if (instance == null) {
                    instance = new GPSHelper(context);
                }
            }
        }
        return instance;
    }

    private GPSHelper(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        crit = new Criteria();
        crit.setAccuracy(Criteria.ACCURACY_FINE);
        bestLocationProvider = locationManager.getBestProvider(crit, true);
    }

    @SuppressLint("MissingPermission")
    public void start(){
        locationManager.requestLocationUpdates(bestLocationProvider, 2000, 2, locationListener);
    }
    public void stop(){
        locationManager.removeUpdates(locationListener);
    }

    @SuppressLint("MissingPermission")
    public LatLng getLastKnownLocation(){
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null){
            return new LatLng(location.getLatitude(), location.getLongitude());
        }
        return new LatLng(21.0245, 105.84117);
    }

    public void setListener(GPSLocationListener listener){
        this.listener = listener;
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
//            geoField = new GeomagneticField(
//                    Double.valueOf(location.getLatitude()).floatValue(),
//                    Double.valueOf(location.getLongitude()).floatValue(),
//                    Double.valueOf(location.getAltitude()).floatValue(),
//                    System.currentTimeMillis()
//            );
            if (listener != null) {
                listener.onNewLocation(location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}
