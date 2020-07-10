package cf.bautroixa.tripgether.utils.ui_utils;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;

public class WakeLockHelper {
    public static void wakeLock(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = Build.VERSION.SDK_INT >= 20 ? pm.isInteractive() : pm.isScreenOn(); // check if screen is on
        if (!isScreenOn) {
            PowerManager.WakeLock screenOn = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "example");
            screenOn.acquire(5000);
        }
    }
}
