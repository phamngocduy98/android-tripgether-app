package cf.bautroixa.tripgether.utils.ui_utils;

public class Formater {
    public static String formatDistance(double distanceInMeters) {
        if (distanceInMeters >= 1000) {
            return String.format("%.2fkm", distanceInMeters / 1000);
        } else {
            return String.format("%.0fm", distanceInMeters);
        }
    }
    public static String formatTime(double timeInSeconds){
        if (timeInSeconds < 60){
            return String.format("%.0f giây", timeInSeconds);
        } else if (timeInSeconds < 60*60){
            return String.format("%.0f phút", timeInSeconds/60);
        } else if (timeInSeconds < 24*60*60){
            double hour = Math.round(timeInSeconds/60/60), minute = (timeInSeconds-hour*60*60)/60;
            return String.format("%.0fh %.0fm", hour, minute);
        } else {
            return String.format("%.0f ngày", timeInSeconds/60/60/24);
        }
    }
}
