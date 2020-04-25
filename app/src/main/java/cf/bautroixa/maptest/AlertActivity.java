package cf.bautroixa.maptest;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gauravbhola.ripplepulsebackground.RipplePulseLayout;

import cf.bautroixa.maptest.data.FcmMessage;
import cf.bautroixa.maptest.data.NotificationItem;
import cf.bautroixa.maptest.firestore.Event;
import cf.bautroixa.maptest.firestore.MainAppManager;

public class AlertActivity extends AppCompatActivity {
    TextView tvType, tvContent;
    Button btnAction;
    RipplePulseLayout ripplePulseLayout;
    MainAppManager manager;

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

        tvType = findViewById(R.id.tv_type_activity_notify);
        tvContent = findViewById(R.id.tv_content_activity_notify);
        btnAction = findViewById(R.id.btn_action_activity_notify);
        ripplePulseLayout = findViewById(R.id.layout_ripplepulse);
        ripplePulseLayout.startRippleAnimation();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String eventId = bundle.getString(FcmMessage.EVENT_ID, null);
            String eventTime = bundle.getString(FcmMessage.EVENT_TIME, null);
            int eventType = Integer.parseInt(bundle.getString(FcmMessage.EVENT_TYPE, "0"));

            tvType.setText(eventTime);
            tvContent.setText(Event.Type.ADDED_TYPES_STRING[eventType]);
            btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: intent to main activity with data
                    finish();
                }
            });

            if (eventId != null && eventTime != null) {
                Event event = manager.getEventsManager().get(eventId);
                if (event != null) {
                    NotificationItem notificationItem = event.getNotificationItem(manager);
                    if (notificationItem != null) {
                        tvType.setText(notificationItem.getIntroContent());
                        tvContent.setText(notificationItem.getShortContent());
                    }
                }
            }
        }
    }
}
