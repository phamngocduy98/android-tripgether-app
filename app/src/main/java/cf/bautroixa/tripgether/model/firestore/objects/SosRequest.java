package cf.bautroixa.tripgether.model.firestore.objects;

import android.content.Context;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Calendar;
import java.util.Objects;

import cf.bautroixa.tripgether.R;

public class SosRequest {
    @Exclude
    public static final String LEVER = "sosRequest.lever", DESCRIPTION = "sosRequest.description", RESOLVED = "sosRequest.resolved";
    @Exclude
    public static final String TIME = "sosRequest.time", IMAGE_URL = "sosRequest.imageUrl";

    int lever;
    String description, imageUrl;
    boolean resolved;
    @ServerTimestamp
    Timestamp time;

    public SosRequest() {
    }

    public SosRequest(int lever, String description, String imageUrl, boolean resolved) {
        this.lever = lever;
        this.description = description;
        this.imageUrl = imageUrl;
        this.resolved = resolved;
    }

    @Exclude
    public String getLeverText(Context context) {
        return context.getResources().getStringArray(R.array.sos_lever)[lever];
    }

    public int getLever() {
        return lever;
    }

    public void setLever(int lever) {
        this.lever = lever;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Timestamp getTime() {
        if (time != null) return time;
        return new Timestamp(Calendar.getInstance().getTime());
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    @Exclude
    public void update(SosRequest sosRequest) {
        this.lever = sosRequest.lever;
        this.description = sosRequest.description;
        this.imageUrl = sosRequest.imageUrl;
        this.resolved = sosRequest.resolved;
        if (sosRequest.time != null) this.time = sosRequest.time;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!super.equals(obj)) return false;
        SosRequest sosRequest = (SosRequest) obj;
        return Objects.equals(this.lever, sosRequest.getLever()) && Objects.equals(this.description, sosRequest.getDescription())
                && Objects.equals(this.resolved, sosRequest.isResolved()) && Objects.equals(this.time, sosRequest.getTime());
    }

    public interface SosLever {
        int HIGH = 2;
        int MEDIUM = 1;
        int LOW = 0;
    }
}
