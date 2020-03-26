package cf.bautroixa.maptest.types;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public final class Message {
    String fromId;
    String content;
    Calendar time;

    public Message(String from, String content, String time) throws ParseException {
        this.fromId = from;
        this.content = content;
        this.time = Calendar.getInstance();
        this.setTimeString(time);
    }

    public Message(String fromId, String content) {
        this.fromId = fromId;
        this.content = content;
        this.time = Calendar.getInstance();
    }

    public String getFrom() {
        return fromId;
    }

    public void setFrom(String from) {
        this.fromId = fromId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Calendar getTime() {
        return time;
    }

    public void setTime(Calendar time) {
        this.time = time;
    }

    public String getTimeString() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.getDefault());
        df.setTimeZone(tz);
        return df.format(this.time.getTime());
    }

    public void setTimeString(String time) throws ParseException {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.getDefault());
        df.setTimeZone(tz);
        this.time.setTime(df.parse(time));
    }
}