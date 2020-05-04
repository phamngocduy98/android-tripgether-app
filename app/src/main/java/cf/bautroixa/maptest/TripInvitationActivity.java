package cf.bautroixa.maptest;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import net.glxn.qrgen.android.QRCode;

import java.util.Objects;

import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.utils.IntentHelper;

public class TripInvitationActivity extends AppCompatActivity {
    MainAppManager manager;

    ImageView imgQR;
    TextView tvCode;
    Button btnShare, btnFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip_screen_2_invitation);

        manager = MainAppManager.getInstance();
        final String tripId = Objects.requireNonNull(manager.getCurrentTripRef()).getId();

        tvCode = findViewById(R.id.tv_code_activity_create_trip);
        imgQR = findViewById(R.id.img_qr_code_activity_create_trip);
        btnShare = findViewById(R.id.btn_share_activity_create_trip);
        btnFinish = findViewById(R.id.btn_finish_activity_create_trip);

        tvCode.setText(tripId);
        Bitmap myBitmap = QRCode.from(String.format("http://%s/trips/%s", getString(R.string.server_host), tripId)).bitmap();
        imgQR.setImageBitmap(myBitmap);

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentHelper.sendTripCodeIntent(TripInvitationActivity.this, tripId);
            }
        });
        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
