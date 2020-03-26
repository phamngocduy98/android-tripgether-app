package cf.bautroixa.maptest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.glxn.qrgen.android.QRCode;

public class CreateTripInvitationActivity extends AppCompatActivity {

    ImageView imgQR;
    TextView tvCode;
    Button btnShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip_invitation);

        imgQR = findViewById(R.id.img_create_trip_qr_code);
        tvCode = findViewById(R.id.tv_code_create_trip_invitation);
        btnShare = findViewById(R.id.btn_share_create_trip_invitation);

        Bitmap myBitmap = QRCode.from("http://www.example.com/join/1234.5678").bitmap();
        imgQR.setImageBitmap(myBitmap);

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, "Mã tham gia nhóm Tripgether của tôi là: "+tvCode.getText().toString());
                startActivity(Intent.createChooser(share, "Chia sẻ mã tham gia"));
            }
        });
    }
}
