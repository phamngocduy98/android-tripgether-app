package cf.bautroixa.maptest.ui.auth;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.ui.theme.OneDialog;
import cf.bautroixa.maptest.ui.theme.ViewAnim;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private EditText mUsernameField;
    private EditText mPasswordField;
    private EditText mConfirmPasswordField;
    private Button mRegisterButton;
    private FirebaseAuth mAuth;
//    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mUsernameField = findViewById(R.id.et_username_register);
        mPasswordField = findViewById(R.id.et_password_register);
        mConfirmPasswordField = findViewById(R.id.et_confirm_password_register);
        mRegisterButton = findViewById(R.id.btn_register);
        mAuth = FirebaseAuth.getInstance();
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mProgressDialog = new ProgressDialog(RegisterActivity.this);
//                mProgressDialog.setContentView(R.layout.cus);
                String username = mUsernameField.getText().toString();
                String password = mPasswordField.getText().toString();
                String confirmPassword = mConfirmPasswordField.getText().toString();
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
                if (password.equals(confirmPassword) && password.length() >= 8) {
                    register(username, password);
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this,
                            R.string.dialog_message_repeat_password_not_matched, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(RegisterActivity.this,
                            R.string.dialog_message_password_too_short, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void register(String username, String password) {
        ViewAnim.toggleLoading(RegisterActivity.this, mRegisterButton, true, "");
        mAuth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                ViewAnim.toggleLoading(RegisterActivity.this, mRegisterButton, false, getString(R.string.btn_create_account));
                if (task.isSuccessful()) {
                    new OneDialog.Builder().message(R.string.dialog_message_register_successful)
                            .buttonClickListener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .show(getSupportFragmentManager(), "register_successful");
                } else {
                    new OneDialog.Builder().message(R.string.dialog_message_register_failed)
                            .show(getSupportFragmentManager(), "register_failed");
                }
            }
        });
    }
}
