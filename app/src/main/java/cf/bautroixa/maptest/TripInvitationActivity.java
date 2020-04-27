package cf.bautroixa.maptest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import net.glxn.qrgen.android.QRCode;

import cf.bautroixa.maptest.firestore.Trip;

public class TripInvitationActivity extends AppCompatActivity {

    ImageView imgQR;
    TextView tvCode;
    Button btnShare, btnFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip_screen_2_invitation);

        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            finish();
            return;
        }
        String tripId = bundle.getString(Trip.ID, "Error: No trip");

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
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, "Mã tham gia nhóm Tripgether của tôi là: " + tvCode.getText().toString());
                startActivity(Intent.createChooser(share, "Chia sẻ mã tham gia"));
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
