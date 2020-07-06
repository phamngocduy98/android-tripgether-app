package cf.bautroixa.tripgether.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gauravbhola.ripplepulsebackground.RipplePulseLayout;
import com.google.firebase.firestore.GeoPoint;
import com.mapbox.api.staticmap.v1.MapboxStaticMap;
import com.mapbox.api.staticmap.v1.StaticMapCriteria;
import com.mapbox.api.staticmap.v1.models.StaticMarkerAnnotation;
import com.mapbox.geojson.Point;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Arrays;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.model.firestore.objects.Notification;
import cf.bautroixa.tripgether.model.firestore.objects.TripNotification;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.model.firestore.objects.UserNotification;
import cf.bautroixa.tripgether.model.sharedpref.SharedPrefKeys;
import cf.bautroixa.tripgether.presenter.AlertActivityPresenter;
import cf.bautroixa.tripgether.presenter.impl.AlertActivityPresenterImpl;
import cf.bautroixa.tripgether.ui.adapter.pager_adapter.MainActivityPagerAdapter;
import cf.bautroixa.tripgether.ui.map.TabMapFragment;
import cf.bautroixa.tripgether.utils.NavigableHelper;

public class AlertActivity extends AppCompatActivity implements AlertActivityPresenter.View {
    private static final String TAG = "AlertActivity";
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

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.VERTICAL)
                .sensitivity(1f)
                .scrimColor(Color.BLACK)
                .scrimStartAlpha(0.8f)
                .scrimEndAlpha(0f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .edge(true)
                .edgeSize(0.18f) // The % of the screen that counts as the edge, default 18%
                .build();
        Slidr.attach(this, config);

        root = findViewById(R.id.root_activity_alert);
        tvType = findViewById(R.id.tv_type_activity_notify);
        tvContent = findViewById(R.id.tv_name_activity_notify);
        btnAction = findViewById(R.id.btn_action_activity_notify);
        ripplePulseLayout = findViewById(R.id.layout_ripplepulse);
        ripplePulseLayout.startRippleAnimation();

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
        if (sharedPref.getBoolean(SharedPrefKeys.SETTING_VIBRATE_ON, true) && vibrator.hasVibrator()) {
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
        if (notification.getType().equals(Notification.TripType.USER_SOS_ADDED)) {
            tvType.setText("Yêu cầu hỗ trợ");
            root.setBackgroundResource(R.drawable.bg_gradient_sos_activity_alert);
        } else if (notification.getType().equals(Notification.TripType.CHECKPOINT_GATHER_REQUEST)) {
            tvType.setText("Tập trung tại");
        } else {
            tvType.setText(notification.getType());
        }
        tvContent.setText(notification.getMessageParams().get(0));

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.cancel();
                if (notification instanceof TripNotification) {
                    if (notification.getType().equals(Notification.TripType.USER_SOS_ADDED)) {
                        NavigableHelper.navigate(AlertActivity.this, MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_MEMBER_STATUS, User.class.getSimpleName(), ((TripNotification) notification).getUserRef().getId());
                    } else if (notification.getType().equals(Notification.TripType.CHECKPOINT_GATHER_REQUEST)) {
                        NavigableHelper.navigate(AlertActivity.this, MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_CHECKPOINT, Checkpoint.class.getSimpleName(), ((TripNotification) notification).getCheckpointRef().getId());
                    } else {
                        startActivity(new Intent(AlertActivity.this, SplashScreenActivity.class));
                    }
                }
                if (notification instanceof UserNotification) {
                    startActivity(new Intent(AlertActivity.this, SplashScreenActivity.class));
                }
                finish();
            }
        });
    }

    @Override
    public void staticMap(GeoPoint myLocation, GeoPoint coordinate) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        Point point = Point.fromLngLat(coordinate.getLongitude(), coordinate.getLatitude());
        StaticMarkerAnnotation staticMarkerAnnotation = StaticMarkerAnnotation.builder().lnglat(point).name(StaticMapCriteria.SMALL_PIN).color(255, 0, 0).build();
        StaticMarkerAnnotation myLocationMarker = StaticMarkerAnnotation.builder().lnglat(Point.fromLngLat(myLocation.getLongitude(), myLocation.getLatitude())).iconUrl("https://sites.google.com/site/masoibot/user/marker_my_location_50x50.png").build();

        point = Point.fromLngLat(coordinate.getLongitude(), coordinate.getLatitude() - 0.002f);

        MapboxStaticMap staticImage = MapboxStaticMap.builder()
                .accessToken(getString(R.string.config_mapbox_map_api_key))
                .styleId(StaticMapCriteria.DARK_STYLE)
                .cameraPoint(point)
                .cameraZoom(16)
                .width(displayMetrics.widthPixels / 2)
                .height(displayMetrics.heightPixels / 2)
                .staticMarkerAnnotations(Arrays.asList(staticMarkerAnnotation, myLocationMarker))
                .build();

        String url = staticImage.url().toString();
        Picasso.get().load(url).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                root.setBackground(new BitmapDrawable(getResources(), bitmap));
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Toast.makeText(AlertActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }
}
