package cf.bautroixa.maptest.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.Notification;
import cf.bautroixa.maptest.model.firestore.TripNotification;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.model.types.FcmNotification;
import cf.bautroixa.maptest.ui.AlertActivity;
import cf.bautroixa.maptest.ui.SplashScreenActivity;

/**
 * NOTE: There can only be one service in each app that receives FCM messages. If multiple
 * are declared in the Manifest then the first one will be chosen.
 * <p>
 * In order to make this Java sample functional, you must remove the following from the Kotlin messaging
 * service in the AndroidManifest.xml:
 * <p>
 * <intent-filter>
 * <action android:name="com.google.firebase.MESSAGING_EVENT" />
 * </intent-filter>
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private ModelManager manager;

    public MyFirebaseMessagingService() {
        manager = ModelManager.getInstance();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        manager = ModelManager.getInstance();
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        Map<String, String> data = remoteMessage.getData();
        if (data.size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            FcmNotification fcmNotification = FcmNotification.fromHashMap(data);
            if (fcmNotification.getPriority().equals(FcmNotification.Priority.HIGH)) {
                // TODO: fcmMessage.getType() > 0 to only handle positive event like add checkpoint, request_check_in, add user (temporary fix)
                //high priority fcmMessage => handleNow (within 10 seconds)
                handleViaAlertActivity(data);
            } else {
                // low priority fcmMessage => show small notification
                manager.getCurrentTrip().getTripNotificationsManager().requestGet(fcmNotification.getNotiRefId()).addOnCompleteListener(new OnCompleteListener<TripNotification>() {
                    @Override
                    public void onComplete(@NonNull Task<TripNotification> task) {
                        if (task.isSuccessful()) {
                            TripNotification tripNotification = task.getResult();
                            sendNotification(Notification.TripType.tripTypes.indexOf(tripNotification.getType()), "Tripgether", tripNotification.getRenderedMessage(MyFirebaseMessagingService.this, false));
                        }
                    }
                });
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]


    // [START on_new_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(@NotNull final String token) {
        Log.d(TAG, "Refreshed token: " + token);
        if (manager.isLoggedIn()) {
            Task<Void> updateTask = manager.getCurrentUser().sendUpdate(null, User.FCM_TOKEN, token);
            if (updateTask != null)
                updateTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "Update token to firestore: " + token);
                    }
                });
        }
    }
    // [END on_new_token]

    /**
     * Schedule async work using WorkManager.
     */
    private void scheduleJob() {
        // [START dispatch_job]
//        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
//                .build();
//        WorkManager.getInstance().beginWith(work).enqueue();
        // [END dispatch_job]
    }

    /**
     * handleNowIntentToNotificationActivity
     * TODO: Undeleted note from Google: Handle time allotted to BroadcastReceivers.
     */
    private void handleViaAlertActivity(Map<String, String> data) {
        wakeLock();
        Intent intent = new Intent(this, AlertActivity.class);
        Iterator<Map.Entry<String, String>> it = data.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> pair = it.next();
            Log.d(TAG, pair.getKey() + " = " + pair.getValue());
            intent.putExtra(pair.getKey(), pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * Send notification
     *
     * @param notificationId unique id for each notification, 2 notification with the same id is replaced
     * @param title
     * @param messageBody
     */
    private void sendNotification(int notificationId, String title, String messageBody) {
        Intent intent = new Intent(this, SplashScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
//        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_tripgether_vector)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
//                        .setSound(defaultSoundUri)
                        .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
                        .setPriority(android.app.Notification.PRIORITY_MAX)
                        .setContentIntent(pendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Thông báo quan trọng (tập hợp, SOS)",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Thông báo quan trọng cần được hiển thị ngay tới người dùng (head-up notifications");
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(notificationId, notificationBuilder.build());
        wakeLock();
    }

    private void wakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = Build.VERSION.SDK_INT >= 20 ? pm.isInteractive() : pm.isScreenOn(); // check if screen is on
        if (!isScreenOn) {
            PowerManager.WakeLock screenOn = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "example");
            screenOn.acquire(5000);
        }
    }
}