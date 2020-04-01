package cf.bautroixa.maptest.firestore;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

public class User {
    @Exclude public static final String NO_USER = "notLoggedIn";
    @Exclude String userName;
    @Exclude public static final String NAME = "name";
    String name;
    @Exclude public static final String AVATAR = "avatar";
    String avatar;
    @Exclude public static final String IMAGENAME = "imageName";
    String imageName;
    @Exclude public static final String PHONE = "phoneNumber";
    String phoneNumber;
    @Exclude public static final String Email = "phoneNumber";
    String email;
    @Exclude public static final String COORD = "currentCoord";
    GeoPoint currentCoord;
    @Exclude public static final String LOCATION = "currentLocation";
    String currentLocation;
    @Exclude public static final String SPEED = "speed";
    Long speed;
    @Exclude public static final String BATTERY = "battery";
    int battery;
    @Exclude public static final String ACTIVE_TRIP = "activeTrip";
    DocumentReference activeTrip;
    @Exclude LatLng latLng;
    @Exclude Marker marker;

    public User() {
    }
    @Exclude
    public String getUserName() {
        return userName;
    }
    @Exclude
    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    @Exclude
    public LatLng getLatLng(){
        if (this.latLng == null){
            synchronized(this){
                if (this.latLng == null && this.currentCoord!= null){
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
}