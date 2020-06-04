package cf.bautroixa.maptest.ui.trip;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;

import net.glxn.qrgen.android.QRCode;

import java.util.Objects;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.Trip;
import cf.bautroixa.maptest.utils.DateFormatter;
import cf.bautroixa.maptest.utils.IntentHelper;
import cf.bautroixa.maptest.utils.NumberGenerator;

public class TripInvitationQrFragment extends Fragment {
    ModelManager manager;
    String tripId;
    CountDownTimer countDownTimer;

    ImageView imgQR;
    TextView tvCode, tvCountdown;
    Button btnShare;
    SpinKitView spinKitView;

    public TripInvitationQrFragment() {
        manager = ModelManager.getInstance();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trip_invitation_qr, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvCode = view.findViewById(R.id.tv_code_activity_create_trip);
        imgQR = view.findViewById(R.id.img_qr_code_activity_create_trip);
        btnShare = view.findViewById(R.id.btn_share_activity_create_trip);

        tvCountdown = view.findViewById(R.id.tv_countdown_activity_trip_qr);
        spinKitView = view.findViewById(R.id.spin_kit_activity_trip_qr);

        tripId = Objects.requireNonNull(manager.getCurrentTripRef()).getId();
        tvCode.setText(tripId);

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentHelper.sendTripCodeIntent(requireContext(), tripId);
            }
        });


        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvCountdown.setText(DateFormatter.formatTimeLeft(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("Đang cập nhật...");
                updateTempJoinCode();
            }
        };
    }


    @Override
    public void onResume() {
        super.onResume();
        updateTempJoinCode();
    }

    @Override
    public void onPause() {
        super.onPause();
        countDownTimer.cancel();
    }

    public void updateTempJoinCode() {
        if (manager.isTripLeader()) {
            tvCountdown.setVisibility(View.VISIBLE);
            spinKitView.setVisibility(View.VISIBLE);
            final String tempJoinCode = NumberGenerator.generateNumberString(12);
            manager.getCurrentTrip().sendUpdate(null, Trip.JOIN_CODE_VALUE, tempJoinCode, Trip.JOIN_CODE_CREATE_TIME, FieldValue.serverTimestamp()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        spinKitView.setVisibility(View.INVISIBLE);
                        Bitmap myBitmap = QRCode.from(String.format("https://%s/trips/%s/join/%s", getString(R.string.server_host), tripId, tempJoinCode)).bitmap();
                        imgQR.setImageBitmap(myBitmap);
                        countDownTimer.start();
                    }
                }
            });
        } else {
            Bitmap myBitmap = QRCode.from(String.format("https://%s/trips/%s/", getString(R.string.server_host), tripId)).bitmap();
            imgQR.setImageBitmap(myBitmap);
            countDownTimer.start();
        }
    }
}
