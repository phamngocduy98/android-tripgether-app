package cf.bautroixa.maptest.model.sharedpref;

import android.content.Context;
import android.content.SharedPreferences;

import cf.bautroixa.maptest.R;

public class SharedPrefHelper {
    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(context.getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
    }

    public static boolean isCheckpointReminderOn(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean(SharedPrefKeys.SETTING_CHECKPOINT_REMINDER_ON, SharedPrefDefaults.SETTING_CHECKPOINT_REMINDER_ON);
    }

    public static boolean isAutoStartGranted(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean(SharedPrefKeys.AUTO_START_GRANTED, false);
    }

    public static void setAutoStartGranted(SharedPreferences sharedPreferences, boolean isGranted) {
        sharedPreferences.edit().putBoolean(SharedPrefKeys.AUTO_START_GRANTED, isGranted).commit();
    }

    public interface SharedPrefDefaults {
        boolean SETTING_CHECKPOINT_REMINDER_ON = false;
    }
}
