package cf.bautroixa.maptest.services;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;

import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.objects.Notification;
import cf.bautroixa.maptest.model.firestore.objects.User;
import cf.bautroixa.maptest.model.types.FcmNotification;
import cf.bautroixa.maptest.ui.AlertActivity;
import cf.bautroixa.maptest.utils.NotificationHelper;
import cf.bautroixa.maptest.utils.ui_utils.WakeLockHelper;

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
public class FCMService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private ModelManager manager;

    public FCMService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        manager = ModelManager.getInstance(this);
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
            Notification notification = fcmNotification.toNotification();
            NotificationHelper.sendNotification(this, notification.getNotificationId(), "Tripgether", notification.getRenderedMessage(FCMService.this, false));
            if (fcmNotification.getPriority().equals(FcmNotification.Priority.HIGH)) {
                //high priority fcmMessage => handleNow (within 10 seconds)
                handleViaAlertActivity(data);
            }
        }
    }

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
        Log.d(TAG, "startAlertActivity");
        WakeLockHelper.wakeLock(this);
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
}