package cf.bautroixa.tripgether.model.sharedpref;

public interface SharedPrefKeys {
    String SETTING_DARK_MODE_TYPE = "DARK_MODE";
    String SETTING_UNIT_TYPE = "UNIT";
    String SETTING_SHOW_NOTI_ON = "SHOW_NOTI";
    String SETTING_VIBRATE_ON = "VIBRATE";
    String SETTING_MAP_STYLE = "MAP_STYLE";
    String SETTING_SERVICE_ON = "SERVICE_ON";
    String SETTING_SAFE_DISTANCE_ON = "SETTING_SAFE_DISTANCE_ON";
    String SETTING_CHECKPOINT_REMINDER_ON = "SETTING_CHECKPOINT_REMINDER_ON";

    String AUTO_START_GRANTED = "AUTO_START_GRANTED";

    String SAFE_DISTANCE = "SAFE_DISTANCE";
    String LAST_LOST_TIME = "LAST_LOST_TIME";

    // last location of current user address
    String LAST_ADDRESS_LAT = "LAST_ADDRESS_LAT";
    String LAST_ADDRESS_LON = "LAST_ADDRESS_LON";
    String LAST_ADDRESS_PROVIDER = "LAST_ADDRESS_PROVIDER";
}