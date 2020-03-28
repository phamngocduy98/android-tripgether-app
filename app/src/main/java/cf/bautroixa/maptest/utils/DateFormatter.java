package cf.bautroixa.maptest.utils;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {
    public static String format(Timestamp timestamp){
        return format(timestamp.toDate());
    }
    public static String format(Calendar calendar){
        return format(calendar.getTime());
    }
    public static String format(Date date){
        Calendar then = Calendar.getInstance();
        then.setTime(date);
        Calendar now = Calendar.getInstance();
        long deltaMinus = (then.getTimeInMillis()-now.getTimeInMillis())/1000/60;
        int deltaDay = then.get(Calendar.DAY_OF_YEAR) - now.get(Calendar.DAY_OF_YEAR);
        if (deltaMinus < -5){
            return format(date,"HH:mm dd/MM/yyyy");
        }
        if (deltaDay <= 1 && deltaMinus < 24*60){
            return format(date,"HH:mm");
        }
        if (deltaDay < 7){
            return format(date,"HH:mm E");
        }
        if (now.get(Calendar.YEAR) == then.get(Calendar.YEAR)){
            return format(date,"HH:mm dd MMM");
        }
        return format(date,"HH:mm dd/MM/yyyy");
    }
    public static String format(Date date, String format){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return simpleDateFormat.format(date);
    }
}
