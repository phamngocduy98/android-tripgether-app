package cf.bautroixa.tripgether.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.presenter.user.PhoneVerificationPresenter;
import cf.bautroixa.tripgether.presenter.user.PhoneVerificationPresenterImpl;
import cf.bautroixa.tripgether.ui.MainActivity;
import cf.bautroixa.tripgether.ui.profile.DetailProfileActivity;
import cf.bautroixa.tripgether.utils.ui_utils.DateFormatter;

public class PhoneVerificationActivity extends AppCompatActivity implements PhoneVerificationPresenter.View {
    Button btnVerify, btnSendVerifySms;
    EditText etPhoneNum, etVerifyCode;
    PhoneVerificationPresenterImpl phoneVerificationPresenter;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);
        phoneVerificationPresenter = new PhoneVerificationPresenterImpl(this, this);

        etPhoneNum = findViewById(R.id.et_phone_num_activity_phone_verification);
        etVerifyCode = findViewById(R.id.et_verify_code_activity_phone_verification);
        btnVerify = findViewById(R.id.btn_verify_activity_phone_verification);
        btnSendVerifySms = findViewById(R.id.btn_send_verify_sms_activity_phone_verification);
        btnSendVerifySms.setEnabled(false);
        btnSendVerifySms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneVerificationPresenter.sendVerificationSms(etPhoneNum.getText().toString());
            }
        });

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneVerificationPresenter.verifyCode(etVerifyCode.getText().toString());
            }
        });

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                btnSendVerifySms.setText(DateFormatter.formatExactTimeLeft(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                Toast.makeText(PhoneVerificationActivity.this, "Hết giờ", Toast.LENGTH_LONG).show();
            }
        };

        phoneVerificationPresenter.init();
    }

    @Override
    protected void onPause() {
        super.onPause();
        countDownTimer.cancel();
    }

    @Override
    public void onInitDone() {
        btnSendVerifySms.setEnabled(true);
    }

    @Override
    public void onVerificationSent() {
        etVerifyCode.setVisibility(View.VISIBLE);
        btnVerify.setVisibility(View.VISIBLE);
        btnSendVerifySms.setEnabled(false);
        countDownTimer.start();
    }

    @Override
    public void onVerificationSuccess(boolean continueWithProfile) {
        if (continueWithProfile) {
            Intent intent = new Intent(PhoneVerificationActivity.this, DetailProfileActivity.class);
            intent.putExtra("ARG_FISRT_TIME_SETUP", true);
            startActivity(intent);
        } else {
            startActivity(new Intent(PhoneVerificationActivity.this, MainActivity.class));
        }
        finish();
    }

    @Override
    public void onVerificationTimeout() {
        btnSendVerifySms.setEnabled(true);
        btnSendVerifySms.setText("Gửi lại mã");
    }

    @Override
    public void onVerificationFailed(String reason) {
        Toast.makeText(this, reason, Toast.LENGTH_LONG).show();
    }
}
