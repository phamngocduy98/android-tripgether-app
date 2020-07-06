package cf.bautroixa.maptest.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.ui.SplashScreenActivity;

public class NotificationHelper {
    /**
     * Send notification
     *
     * @param notificationId unique id for each notification, 2 notification with the same id is replaced
     * @param title
     * @param messageBody
     */
    public static void sendNotification(Context context, int notificationId, String title, String messageBody) {
        Intent intent = new Intent(context, SplashScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        // as declared in manifest, do not fix this value in codes
        String channelId = context.getString(R.string.default_notification_channel_id);
//        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.ic_tripgether2_noti_icon)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
//                        .setSound(defaultSoundUri)
                        .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
                        .setPriority(android.app.Notification.PRIORITY_MAX)
                        .setContentIntent(pendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Thông báo quan trọng",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Thông báo quan trọng (tập hợp, SOS) cần được hiển thị ngay tới người dùng");
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
