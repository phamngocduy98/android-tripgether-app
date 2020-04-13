package cf.bautroixa.maptest.firestore;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

public class User extends Data {
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
    boolean isLeader;

    String name;
    String avatar;

    @Exclude public static final String IMAGENAME = "imageName";
    String imageName;
    String phoneNumber;
    @Exclude public static final String Email = "phoneNumber";
    String email;

    GeoPoint currentCoord;
    String currentLocation;

    Long speed;
    int battery;
    DocumentReference activeTrip;
    String fcmToken;

    @Exclude
    LatLng latLng;
    @Exclude
    Marker marker;

    public User() {
    }

    @Exclude
    public boolean isLeader() {
        return isLeader;
    }

    @Exclude
    public void setLeader(boolean leader) {
        isLeader = leader;
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
        return speed;
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
        this.latLng = new LatLng(currentCoord.getLatitude(), currentCoord.getLongitude());
        if (this.marker != null){
            marker.setPosition(this.latLng);
        }
    }

    @Exclude
    @Override
    public void onRemove() {
        super.onRemove();
        if (this.marker != null){
            marker.remove();
        }
    }
}