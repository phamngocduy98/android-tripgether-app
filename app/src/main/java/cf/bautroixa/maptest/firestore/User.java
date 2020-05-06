package cf.bautroixa.maptest.firestore;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import cf.bautroixa.maptest.utils.LatLngDistance;

public class User extends Data {
    @Exclude
    public static final String DEFAULT_AVATAR = "https://sites.google.com/site/masoibot/user/user.png";
    @Exclude
    public static final String NO_USER = "notLoggedIn";
    @Exclude
    public static final String FCM_TOKEN = "fcmToken";
    @Exclude
    public static final String USER_NAME = "userName";
    @Exclude
    public static final String NAME = "name";
    @Exclude
    public static final String AVATAR = "avatar";
    @Exclude
    public static final String PHONE = "phoneNumber";
    @Exclude
    public static final String COORD = "currentCoord";
    @Exclude
    public static final String LOCATION = "currentLocation";
    @Exclude
    public static final String EMAIL = "email";
    @Exclude
    public static final String SPEED = "speed";
    @Exclude
    public static final String BATTERY = "battery";
    @Exclude
    public static final String ACTIVE_TRIP = "activeTrip";
    @Exclude
    public static final String LAST_UPDATE = "lastUpdate";
    @Exclude
    public static final String IMAGENAME = "imageName";
    @Exclude
    public static final String Email = "phoneNumber";
    String name;
    String avatar;
    String imageName;
    String phoneNumber;
    String email;

    GeoPoint currentCoord;
    String currentLocation;

    Long speed;
    @Exclude
    long estimatedSpeed;
    int battery;
    @Nullable
    DocumentReference activeTrip;
    String fcmToken;

    @ServerTimestamp
    Timestamp lastUpdate;

    @Exclude
    LatLng latLng;
    @Exclude
    Marker marker;

    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    public GeoPoint getCurrentCoord() {
        return currentCoord;
    }

    public void setCurrentCoord(GeoPoint currentCoord) {
        this.currentCoord = currentCoord;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public Long getSpeed() {
        // TODO: testing
        return estimatedSpeed;
    }

    public void setSpeed(Long speed) {
        this.speed = speed;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    @Nullable
    public DocumentReference getActiveTrip() {
        return activeTrip;
    }

    public void setActiveTrip(DocumentReference activeTrip) {
        this.activeTrip = activeTrip;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    @Nullable
    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Exclude
    public String getShortName() {
        String[] names = getName().split(" ");
        if (names.length >= 2) {
            return "" + names[0].charAt(0) + names[names.length - 1].charAt(0);
        } else {
            return getName().substring(0, 2);
        }
    }

    @Exclude
    public LatLng getLatLng() {
        if (this.latLng == null) {
            synchronized (this) {
                if (this.latLng == null && this.currentCoord != null) {
                    this.latLng = new LatLng(this.currentCoord.getLatitude(), this.currentCoord.getLongitude());
                }
            }
        }
        return this.latLng;
    }

    @Exclude
    @Nullable
    public Marker getMarker() {
        return marker;
    }

    @Exclude
    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    @Exclude
    @Override
    public void onDocumentSnapshot(DocumentSnapshot documentSnapshot) {
        User user = documentSnapshot.toObject(User.class);
        if (user != null) update(user);
    }

    @Exclude
    public void update(User user) {
        this.name = user.name;
        this.avatar = user.avatar;
        this.phoneNumber = user.phoneNumber;
        this.currentCoord = user.currentCoord;
        this.currentLocation = user.currentLocation;
        this.email = user.email;
        this.speed = user.speed;
        this.battery = user.battery;
        this.activeTrip = user.activeTrip;
//        this.fcmToken = user.fcmToken; TODO: fix here

        LatLng oldLatLng = this.latLng;
        this.latLng = new LatLng(currentCoord.getLatitude(), currentCoord.getLongitude());
        Timestamp oldLastUpdate = this.lastUpdate;
        if (user.lastUpdate != null) {
            this.lastUpdate = user.lastUpdate;
            if (oldLatLng != null) {
                double estimatedMetersDistance = LatLngDistance.measureDistance(oldLatLng, this.latLng);
                long elapsedSeconds = this.lastUpdate.getSeconds() - oldLastUpdate.getSeconds();
                estimatedSpeed = (long) (estimatedMetersDistance / elapsedSeconds); // m/s
            }
        }

        if (this.marker != null) {
            marker.setPosition(this.latLng);
        }
    }

    @Exclude
    @Override
    public void onRemove() {
        super.onRemove();
        if (this.marker != null) {
            marker.remove();
        }
    }
}