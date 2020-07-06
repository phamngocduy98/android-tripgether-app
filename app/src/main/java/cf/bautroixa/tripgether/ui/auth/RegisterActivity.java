package cf.bautroixa.tripgether.ui.auth;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.presenter.RegisterPresenter;
import cf.bautroixa.tripgether.presenter.impl.RegisterPresenterImpl;
import cf.bautroixa.tripgether.ui.dialogs.LoadingDialogHelper;
import cf.bautroixa.tripgether.ui.theme.OneDialog;
import cf.bautroixa.tripgether.ui.theme.ViewAnim;

public class RegisterActivity extends AppCompatActivity implements RegisterPresenter.View {
    private static final String TAG = "RegisterActivity";
    RegisterPresenterImpl registerPresenter;
    private EditText mUsernameField;
    private EditText mPasswordField;
    private EditText mConfirmPasswordField;
    private Button mRegisterButton;
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        registerPresenter = new RegisterPresenterImpl(this, this);

        mUsernameField = findViewById(R.id.et_username_register);
        mPasswordField = findViewById(R.id.et_password_register);
        mConfirmPasswordField = findViewById(R.id.et_confirm_password_register);
        mRegisterButton = findViewById(R.id.btn_register);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateBeforeRegister();
            }
        });
    }

    private void validateBeforeRegister() {
        String username = mUsernameField.getText().toString();
        String password = mPasswordField.getText().toString();
        String confirmPassword = mConfirmPasswordField.getText().toString();
        // (View) validateBeforeRegister()
        if (username.length() == 0) {
            mUsernameField.setHintTextColor(Color.RED);
            return;
        }
        if (password.length() == 0) {
            mPasswordField.setHintTextColor(Color.RED);
            return;
        }
        if (confirmPassword.length() == 0) {
            mConfirmPasswordField.setHintTextColor(Color.RED);
            return;
        }
        if (!password.equals(confirmPassword)) {
            mConfirmPasswordField.setHintTextColor(Color.RED);
            Toast.makeText(this, R.string.dialog_message_repeat_password_not_matched, Toast.LENGTH_LONG).show();
            return;
        }
        if (password.length() < 8) {
            mPasswordField.setHintTextColor(Color.RED);
            Toast.makeText(this, R.string.dialog_message_password_too_short, Toast.LENGTH_LONG).show();
            return;
        }
        registerPresenter.register(username, password);
    }

    @Override
    public void onLoading() {
        ViewAnim.toggleLoading(RegisterActivity.this, mRegisterButton, true, "");
        loadingDialog = LoadingDialogHelper.create(this, "Đang tạo tài khoản");
    }

    @Override
    public void onRegisterSuccess() {
        if (loadingDialog != null) loadingDialog.dismiss();
        ViewAnim.toggleLoading(RegisterActivity.this, mRegisterButton, false, getString(R.string.btn_create_account));
        new OneDialog.Builder().message(R.string.dialog_message_register_successful)
                .buttonClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show(getSupportFragmentManager(), "register_successful");
    }

    @Override
    public void onRegisterFailed(String reason) {
        if (loadingDialog != null) loadingDialog.dismiss();
        ViewAnim.toggleLoading(RegisterActivity.this, mRegisterButton, false, getString(R.string.btn_create_account));
        new OneDialog.Builder().message(R.string.dialog_message_register_failed)
                .show(getSupportFragmentManager(), "register_failed");
    }
}
