package cf.bautroixa.maptest.utils;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {
    public static String format(Date date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return simpleDateFormat.format(date);
    }

    public static String format(Timestamp timestamp) {
        return format(timestamp.toDate());
    }

    public static String format(Calendar calendar) {
        return format(calendar.getTime());
    }

    public static String format(Date date) {
        Calendar then = Calendar.getInstance();
        then.setTime(date);
        Calendar now = Calendar.getInstance();
        long deltaMinus = (then.getTimeInMillis() - now.getTimeInMillis()) / 1000 / 60;
        long deltaHour = deltaMinus / 60;
        int deltaDay = then.get(Calendar.DAY_OF_YEAR) - now.get(Calendar.DAY_OF_YEAR);
        if (deltaDay < -1) {
            return format(date, "HH:mm dd/MM/yyyy");
        }
        if (deltaHour > 0 && deltaHour < 3) {
            return String.format("%d giờ trước", deltaHour);
        }
        if (deltaHour == 0) {
            if (deltaMinus == 0) return "Vừa xong";
            return String.format("%d phút %s", Math.abs(deltaMinus), deltaMinus < 0 ? "trước" : "nữa");
        }
        if (deltaDay * deltaDay <= 1) {
            return format(date, "HH:mm ") + formatDate(date);
        }
        if (deltaDay < 7) {
            return format(date, "HH:mm ") + formatDate(date);
        }
        if (now.get(Calendar.YEAR) == then.get(Calendar.YEAR)) {
            return format(date, "HH:mm dd MMM");
        }
        return format(date, "HH:mm dd/MM/yyyy");
    }

    public static String formatDateTime(Timestamp timestamp) {
        return formatTime(timestamp) + " " + formatDate(timestamp);
    }

    public static String formatTime(Date date) {
        return format(date, "HH:mm");
    }

    public static String formatTime(Timestamp timestamp) {
        return formatTime(timestamp.toDate());
    }

    public static String formatDate(Timestamp timestamp) {
        return formatDate(timestamp.toDate());
    }

    public static String formatDate(Date date) {
        Calendar then = Calendar.getInstance();
        then.setTime(date);
        Calendar now = Calendar.getInstance();
        long deltaMinus = (then.getTimeInMillis() - now.getTimeInMillis()) / 1000 / 60;
        int deltaDay = then.get(Calendar.DAY_OF_YEAR) - now.get(Calendar.DAY_OF_YEAR);
        // TODO: English-sub for today, yesterday, ...
        if (deltaDay < -1) return String.format("%d ngày trước", -deltaDay);
        if (deltaDay == -1) return "Hôm qua";
        if (deltaDay == 0) return "Hôm nay";
        if (deltaDay == 1) return "Ngày mai";
        return String.format("%d ngày sau", deltaDay);
    }

    public static String formatTimeLeft(long millis) {
        long seconds = millis / 1000, minutes = seconds / 60, hours = minutes / 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}
