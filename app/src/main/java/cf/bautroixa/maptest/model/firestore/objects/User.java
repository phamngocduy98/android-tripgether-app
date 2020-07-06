package cf.bautroixa.maptest.model.firestore.objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.List;
import java.util.Objects;

import cf.bautroixa.maptest.interfaces.LatLngOwner;
import cf.bautroixa.maptest.model.constant.Collections;
import cf.bautroixa.maptest.model.firestore.core.Document;
import cf.bautroixa.maptest.model.firestore.core.RefsArrayManager;
import cf.bautroixa.maptest.model.firestore.managers.NotificationsManager;

public class User extends Document implements LatLngOwner {
    @Exclude
    public static final String DEFAULT_AVATAR = "https://sites.google.com/site/masoibot/user/user.png", NO_USER = "notLoggedIn";
    @Exclude
    public static final String USER_NAME = "userName", NAME = "name", AVATAR = "avatar", IMAGE_NAME = "imageName";
    @Exclude
    public static final String LOCATION = "currentLocation", PHONE = "phoneNumber", EMAIL = "email";
    @Exclude
    public static final String COORD = "currentCoord", SPEED = "speed", BATTERY = "battery";
    @Exclude
    public static final String ACTIVE_TRIP_REF = "activeTripRef", FRIENDS = "friends", LOCATION_ACCURACY = "locationAccuracy";
    @Exclude
    public static final String SOS_REQUEST = "sosRequest", FCM_TOKEN = "fcmToken", LAST_UPDATE = "lastUpdate";
    SosRequest sosRequest;
    @ServerTimestamp
    Timestamp lastUpdate;

    @Exclude
    RefsArrayManager<User> friendsManager, friendRequestsManager;
    @Exclude
    NotificationsManager<UserNotification> userNotificationsManager;
    @Exclude
    LatLng latLng;
    private String name, avatar, imageName, phoneNumber, email, currentLocation, fcmToken;
    private GeoPoint currentCoord;
    private int speed, battery;
    private float locationAccuracy;
    private List<DocumentReference> friends, friendRequests, sentFriendRequests;
    @Nullable
    private DocumentReference activeTripRef;

    public User() {
        this.withClass(User.class);
    }

    @Exclude
    public void initSubManager(RefsArrayManager<User> baseUsersManager) {
        if (!isSubManagerAvailable()) {
            this.friendsManager = new RefsArrayManager<>(User.class, baseUsersManager);
            this.friendRequestsManager = new RefsArrayManager<>(User.class, baseUsersManager);
            this.userNotificationsManager = new NotificationsManager<>(UserNotification.class, ref.collection(Collections.NOTIFICATIONS), this);
            setSubManagerAvailable(true);
        }
        if (this.friends != null) {
            this.friendsManager.updateRefList(this.friends);
        }
        if (this.friendRequests != null) {
            this.friendRequestsManager.updateRefList(this.friendRequests);
        }
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

    public float getLocationAccuracy() {
        return locationAccuracy;
    }

    public void setLocationAccuracy(float locationAccuracy) {
        this.locationAccuracy = locationAccuracy;
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

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    @Nullable
    public DocumentReference getActiveTripRef() {
        return activeTripRef;
    }

    public void setActiveTripRef(DocumentReference activeTripRef) {
        this.activeTripRef = activeTripRef;
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

    public List<DocumentReference> getFriends() {
        return friends;
    }

    public void setFriends(List<DocumentReference> friends) {
        this.friends = friends;
        if (isSubManagerAvailable()) this.friendsManager.updateRefList(friends);

    }

    public List<DocumentReference> getFriendRequests() {
        return friendRequests;
    }

    public void setFriendRequests(List<DocumentReference> friendRequests) {
        this.friendRequests = friendRequests;
        if (isSubManagerAvailable()) this.friendRequestsManager.updateRefList(friendRequests);
    }

    public List<DocumentReference> getSentFriendRequests() {
        return sentFriendRequests;
    }

    public void setSentFriendRequests(List<DocumentReference> sentFriendRequests) {
        this.sentFriendRequests = sentFriendRequests;
    }

    @Nullable
    public SosRequest getSosRequest() {
        return sosRequest;
    }

    public void setSosRequest(SosRequest sosRequest) {
        this.sosRequest = sosRequest;
    }

    @Nullable
    @Exclude
    public RefsArrayManager<User> getFriendsManager() {
        return friendsManager;
    }

    @Nullable
    @Exclude
    public RefsArrayManager<User> getFriendRequestsManager() {
        return friendRequestsManager;
    }


    @Exclude
    @NonNull
    public NotificationsManager<UserNotification> getUserNotificationsManager() {
        if (userNotificationsManager == null)
            userNotificationsManager = new NotificationsManager<>(UserNotification.class, ref.collection(Collections.NOTIFICATIONS), this);
        return userNotificationsManager;
    }

    @Exclude
    public String getShortName() {
        String[] names = getName().split(" ");
        if (names.length >= 2) {
            return "" + names[0].charAt(0) + names[names.length - 1].charAt(0);
        } else {
            return getName();
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
    protected void update(Document document) {
        User user = (User) document;
        this.name = user.name;
        this.avatar = user.avatar;
        this.phoneNumber = user.phoneNumber;
        this.email = user.email;

        this.currentCoord = user.currentCoord;
        this.latLng = new LatLng(currentCoord.getLatitude(), currentCoord.getLongitude());
        this.currentLocation = user.currentLocation;

        this.sosRequest = user.sosRequest;
        this.speed = user.speed;
        this.battery = user.battery;

        this.activeTripRef = user.activeTripRef;

        this.friends = user.friends;
        this.friendRequests = user.friendRequests;
        this.sentFriendRequests = user.sentFriendRequests;
        if (this.friends != null && this.friendsManager != null)
            this.friendsManager.updateRefList(friends);
        if (this.friendRequests != null && this.friendRequestsManager != null)
            this.friendRequestsManager.updateRefList(friendRequests);

        this.fcmToken = user.fcmToken;
    }

    @Exclude
    @Override
    public void onRemove() {
        super.onRemove();
        if (friendsManager != null) friendsManager.clear();
        if (friendRequestsManager != null) friendRequestsManager.clear();
        if (userNotificationsManager != null) userNotificationsManager.clear();
    }

    @Exclude
    public boolean isOnline() {
        if (lastUpdate != null) {
            return System.currentTimeMillis() - lastUpdate.toDate().getTime() < 5 * 60 * 1000;
        }
        return false;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!super.equals(obj)) return false;
        User user = (User) obj;
        return Objects.equals(this.name, user.getName()) && Objects.equals(this.avatar, user.getAvatar())
                && Objects.equals(this.phoneNumber, user.getPhoneNumber()) && Objects.equals(this.currentCoord, user.getCurrentCoord())
                && Objects.equals(this.currentLocation, user.getCurrentLocation())
                && Objects.equals(this.speed, user.getSpeed()) && Objects.equals(this.battery, user.getBattery())
                && Objects.equals(this.friends, user.getFriends()) && Objects.equals(this.lastUpdate, user.getLastUpdate())
                && Objects.equals(this.activeTripRef, user.getActiveTripRef()) && Objects.equals(this.imageName, user.getImageName())
                && Objects.equals(this.fcmToken, user.getFcmToken()) && Objects.equals(this.email, user.getEmail())
                && Objects.equals(this.sosRequest, user.getSosRequest());
    }

    public interface FriendStatus {
        int SENT = 1;
        int RECEIVED = 2;
        int BE_FRIEND = 3;
        int NONE = 4;
    }
}