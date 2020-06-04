package cf.bautroixa.maptest.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gauravbhola.ripplepulsebackground.RipplePulseLayout;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.constant.SharedPrefs;
import cf.bautroixa.maptest.model.firestore.Checkpoint;
import cf.bautroixa.maptest.model.firestore.Notification;
import cf.bautroixa.maptest.model.firestore.TripNotification;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.presenter.AlertActivityPresenter;
import cf.bautroixa.maptest.presenter.impl.AlertActivityPresenterImpl;
import cf.bautroixa.maptest.ui.adapter.MainActivityPagerAdapter;
import cf.bautroixa.maptest.ui.map.TabMapFragment;
import cf.bautroixa.maptest.utils.NavigableHelper;

public class AlertActivity extends AppCompatActivity implements AlertActivityPresenter.View {
    AlertActivityPresenterImpl alertActivityPresenter;
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
        root = findViewById(R.id.root_activity_alert);
        tvType = findViewById(R.id.tv_type_activity_notify);
        tvContent = findViewById(R.id.tv_content_activity_notify);
        btnAction = findViewById(R.id.btn_action_activity_notify);
        ripplePulseLayout = findViewById(R.id.layout_ripplepulse);
        ripplePulseLayout.startRippleAnimation();

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
        if (sharedPref.getBoolean(SharedPrefs.VIBRATE, true) && vibrator.hasVibrator()) {
            long[] pattern = {0, 100, 1000};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
            } else {
                vibrator.vibrate(pattern, 0);
            }
        }


        alertActivityPresenter = new AlertActivityPresenterImpl(this, this);
        final Bundle bundle = getIntent().getExtras();
        alertActivityPresenter.handleIntent(bundle);
    }

    @Override
    public void setUpView(final Notification notification) {
        if (notification.getPriority().equals("high")) {
            root.setBackgroundResource(R.drawable.bg_gradient_sos_activity_alert);
        }
        tvType.setText(notification.getType());
        tvContent.setText(notification.getRenderedMessage(AlertActivity.this, false));
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.cancel();
                if (notification instanceof TripNotification) {
                    if (notification.getType() == Notification.TripType.USER_SOS_ADDED) {
                        NavigableHelper.navigate(AlertActivity.this, MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_MEMBER_STATUS, User.class.getSimpleName(), ((TripNotification) notification).getUserRef().getId());
                    } else if (notification.getType() == Notification.TripType.CHECKPOINT_GATHER_REQUEST) {
                        NavigableHelper.navigate(AlertActivity.this, MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_CHECKPOINT, Checkpoint.class.getSimpleName(), ((TripNotification) notification).getId());
                    }
                }
                finish();
            }
        });
    }
}
