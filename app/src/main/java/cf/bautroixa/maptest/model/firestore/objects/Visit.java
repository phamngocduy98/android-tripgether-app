package cf.bautroixa.maptest.model.firestore.objects;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Calendar;

import cf.bautroixa.maptest.model.firestore.core.Document;

public class Visit extends Document {
    @Exclude
    public static final String TIME = "time";
    @ServerTimestamp
    Timestamp time;

    public Visit() {
        this.withClass(Visit.class);
    }

    public Visit(Timestamp time) {
        this.time = time;
    }

    public Timestamp getTime() {
        if (time != null) return time;
        return new Timestamp(Calendar.getInstance().getTime());
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    @Exclude
    protected void update(Document document) {
        Visit visit = (Visit) document;
        this.time = visit.time;
    }
}
