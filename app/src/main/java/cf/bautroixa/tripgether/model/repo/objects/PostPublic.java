package cf.bautroixa.tripgether.model.repo.objects;

import androidx.annotation.Nullable;

import cf.bautroixa.tripgether.model.firestore.objects.Post;
import cf.bautroixa.tripgether.model.types.GeoPointPublic;
import cf.bautroixa.tripgether.model.types.TimestampPublic;

public class PostPublic {
    String id, ownerId, avatar, ownerName, body, tripId;
    @Nullable
    Post.Media media;
    int likes;
    TimestampPublic time;
    boolean userLiked;
    @Nullable
    GeoPointPublic coordinate;

    public PostPublic() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getShortName() {
        String[] names = getOwnerName().split(" ");
        if (names.length >= 2) {
            return "" + names[0].charAt(0) + names[names.length - 1].charAt(0);
        } else {
            return getOwnerName();
        }
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public boolean isUserLiked() {
        return userLiked;
    }

    public void setUserLiked(boolean userLiked) {
        this.userLiked = userLiked;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public Post.Media getMedia() {
        return media;
    }

    public void setMedia(Post.Media media) {
        this.media = media;
    }

    public TimestampPublic getTime() {
        return time;
    }

    public void setTime(TimestampPublic time) {
        this.time = time;
    }

    @Nullable
    public GeoPointPublic getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(@Nullable GeoPointPublic coordinate) {
        this.coordinate = coordinate;
    }
}
