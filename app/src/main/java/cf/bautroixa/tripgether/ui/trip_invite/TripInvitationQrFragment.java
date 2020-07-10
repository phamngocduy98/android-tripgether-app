package cf.bautroixa.tripgether.ui.trip_invite;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.ybq.android.spinkit.SpinKitView;

import net.glxn.qrgen.android.QRCode;

import java.util.Objects;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.presenter.trip.TripInvitationQrPresenter;
import cf.bautroixa.tripgether.presenter.trip.TripInvitationQrPresenterImpl;
import cf.bautroixa.tripgether.utils.IntentHelper;
import cf.bautroixa.tripgether.utils.ui_utils.DateFormatter;

public class TripInvitationQrFragment extends Fragment implements TripInvitationQrPresenter.View {
    ModelManager manager;
    String tripId;
    CountDownTimer countDownTimer;
    TripInvitationQrPresenterImpl tripInvitationQrPresenter;

    ImageView imgQR;
    TextView tvCode, tvCountdown;
    Button btnShare;
    SpinKitView spinKitView;

    public TripInvitationQrFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = ModelManager.getInstance(context);
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
        spinKitView.setVisibility(View.VISIBLE);
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvCountdown.setText(DateFormatter.formatExactTimeLeft(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("Đang cập nhật...");
                tripInvitationQrPresenter.requestNewQR();
            }
        };
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tripInvitationQrPresenter = new TripInvitationQrPresenterImpl(this, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        tripInvitationQrPresenter.requestNewQR();
    }

    @Override
    public void onPause() {
        super.onPause();
        countDownTimer.cancel();
    }

    @Override
    public void updateQR(String tripId, String joinCode) {
        spinKitView.setVisibility(View.INVISIBLE);
        Bitmap myBitmap;
        if (joinCode == null) {
            tvCountdown.setVisibility(View.INVISIBLE);
            countDownTimer.cancel();
            myBitmap = QRCode.from(String.format("https://%s/share/trip/%s", getString(R.string.server_host), tripId)).bitmap();
        } else {
            tvCountdown.setVisibility(View.VISIBLE);
            countDownTimer.start();
            myBitmap = QRCode.from(String.format("https://%s/share/trip/%s/join/%s", getString(R.string.server_host), tripId, joinCode)).bitmap();
        }
        imgQR.setImageBitmap(myBitmap);
    }

    @Override
    public void onLoading() {
        spinKitView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFailed(String reason) {
        spinKitView.setVisibility(View.INVISIBLE);
        Toast.makeText(requireContext(), reason, Toast.LENGTH_LONG).show();
    }
}
