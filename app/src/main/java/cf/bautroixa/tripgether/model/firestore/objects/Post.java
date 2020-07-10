package cf.bautroixa.tripgether.model.firestore.objects;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.List;

import cf.bautroixa.tripgether.model.constant.Collections;
import cf.bautroixa.tripgether.model.firestore.core.CollectionManager;
import cf.bautroixa.tripgether.model.firestore.core.Document;
import cf.bautroixa.tripgether.model.firestore.core.RefsArrayManager;
import cf.bautroixa.tripgether.model.firestore.managers.CommentsManager;

public class Post extends Document {
    DocumentReference ownerRef;
    String body;
    List<Media> medias;
    List<DocumentReference> places;
    @Nullable
    DocumentReference tripRef;

    List<DocumentReference> likes;
    @ServerTimestamp
    Timestamp time;

    @Exclude
    CommentsManager commentsManager;
    @Exclude
    RefsArrayManager<Place> placeManager;

    @Exclude
    boolean isUserLiked;

    public Post() {
    }

    public Post(DocumentReference ownerRef, String body, @Nullable List<Media> medias, @Nullable DocumentReference tripRef, @Nullable List<DocumentReference> places) {
        this.ownerRef = ownerRef;
        this.body = body;
        this.medias = medias;
        this.tripRef = tripRef;
        this.places = places;
        this.likes = new ArrayList<>();
    }

    @Exclude
    public void initSubManager(DocumentReference currentUserRef, CollectionManager<Place> basePlaceManager) {
        if (commentsManager == null) {
            commentsManager = new CommentsManager(ref.collection(Collections.COMMENTS), currentUserRef);
            placeManager = new RefsArrayManager<>(Place.class, basePlaceManager);
            if (places != null) placeManager.updateRefList(places);
        }
    }

    @Exclude
    @Nullable
    public CommentsManager getCommentsManager() {
        return commentsManager;
    }

    @Exclude
    @Nullable
    public RefsArrayManager<Place> getPlaceManager() {
        return placeManager;
    }

    @Override
    protected void update(Document document) {
        Post post = (Post) document;
        this.ownerRef = post.ownerRef;
        this.body = post.body;
        this.medias = post.medias;
        this.time = post.time;
        this.likes = post.likes;
        this.tripRef = post.tripRef;
        this.places = post.places;

        if (placeManager != null && this.places != null)
            this.placeManager.updateRefList(this.places);

        this.isUserLiked = post.isUserLiked;
    }

    public DocumentReference getOwnerRef() {
        return ownerRef;
    }

    public void setOwnerRef(DocumentReference ownerRef) {
        this.ownerRef = ownerRef;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<Media> getMedias() {
        return medias;
    }

    public void setMedias(List<Media> medias) {
        this.medias = medias;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    @Nullable
    public DocumentReference getTripRef() {
        return tripRef;
    }

    public void setTripRef(@Nullable DocumentReference tripRef) {
        this.tripRef = tripRef;
    }

    public List<DocumentReference> getLikes() {
        return likes;
    }

    public void setLikes(List<DocumentReference> likes) {
        this.likes = likes;
    }

    public List<DocumentReference> getPlaces() {
        return places;
    }

    public void setPlaces(List<DocumentReference> places) {
        this.places = places;
        if (placeManager != null && places != null) this.placeManager.updateRefList(places);
    }

    @Exclude
    public boolean isUserLiked() {
        return isUserLiked;
    }

    @Exclude
    public void setUserLiked(boolean userLiked) {
        isUserLiked = userLiked;
    }

    public static class Media {
        String type, url;

        public Media() {
        }

        public Media(String type, String url) {
            this.type = type;
            this.url = url;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public interface Type {
            String IMAGE = "image";
            String VIDEO = "video";
        }
    }
}
