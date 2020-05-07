package cf.bautroixa.maptest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gauravbhola.ripplepulsebackground.RipplePulseLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import cf.bautroixa.maptest.data.FcmMessage;
import cf.bautroixa.maptest.data.NotificationItem;
import cf.bautroixa.maptest.data.SharedPrefs;
import cf.bautroixa.maptest.firestore.Event;
import cf.bautroixa.maptest.firestore.MainAppManager;

public class AlertActivity extends AppCompatActivity {
    MainAppManager manager;
    SharedPreferences sharedPref;
    Vibrator vibrator;

    ConstraintLayout root;
    TextView tvType, tvContent;
    Button btnAction;
    RipplePulseLayout ripplePulseLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        manager = MainAppManager.getInstance();
        sharedPref = getSharedPreferences(getString(R.string.shared_preference_name), MODE_PRIVATE);

        root = findViewById(R.id.root_activity_alert);
        tvType = findViewById(R.id.tv_type_activity_notify);
        tvContent = findViewById(R.id.tv_content_activity_notify);
        btnAction = findViewById(R.id.btn_action_activity_notify);
        ripplePulseLayout = findViewById(R.id.layout_ripplepulse);
        ripplePulseLayout.startRippleAnimation();

        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String eventId = bundle.getString(FcmMessage.EVENT_ID, null);
            String eventTime = bundle.getString(FcmMessage.EVENT_TIME, null);
            int eventType = Integer.parseInt(bundle.getString(FcmMessage.EVENT_TYPE, "0"));
            String priority = bundle.getString(FcmMessage.EVENT_PRIORITY, "low");

            if (priority.equals("high")) {
                root.setBackgroundResource(R.drawable.bg_gradient_sos_activity_alert);
            }

            tvType.setText(eventTime);
            tvContent.setText(Event.Type.ADDED_TYPES_STRING[eventType]);
            btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vibrator.cancel();
                    Intent intent = new Intent(AlertActivity.this, MainActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                }
            });

            if (eventId != null && eventTime != null) {
                Event event = manager.getEventsManager().get(eventId);
                if (event != null) {
                    event.getNotificationItem(manager).addOnCompleteListener(new OnCompleteListener<NotificationItem>() {
                        @Override
                        public void onComplete(@NonNull Task<NotificationItem> task) {
                            if (task.isSuccessful()) {
                                NotificationItem notificationItem = task.getResult();
                                tvType.setText(notificationItem.getTitle());
                                tvContent.setText(notificationItem.getDescription());
                            }
                        }
                    });
                }
            }
        }

        // Vibrate
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (sharedPref.getBoolean(SharedPrefs.VIBRATE, true) && vibrator.hasVibrator()) {
            long[] pattern = {0, 100, 1000};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
            } else {
                vibrator.vibrate(pattern, 0);
            }
        }
    }
}
