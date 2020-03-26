package cf.bautroixa.maptest.data;

import android.content.Context;

import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.utils.BatteryHelper;

public class CurrentUserStatus {
    private static CurrentUserStatus instance = null;
    public User user;
    private Context context;

    private CurrentUserStatus(Context context) {
//        SharedPreferences sharedPref = context.getSharedPreferences(
//                context.getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
//        String userId = sharedPref.getString("userId");
        user = new User();
        this.context = context;
    }

    public static CurrentUserStatus getInstance(Context context) {
        if (instance == null) {
            synchronized (CurrentUserStatus.class) {
                if (instance == null) {
                    instance = new CurrentUserStatus(context);
                }
            }
        }
        return instance;
    }

    public void sendStatus() {
        user.setBattery(BatteryHelper.getBatteryPercentage(this.context));
    }
}
