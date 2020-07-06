package cf.bautroixa.tripgether.ui.auth;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.ui.dialogs.FailedDialogFragment;
import cf.bautroixa.tripgether.ui.theme.OneDialog;
import cf.bautroixa.tripgether.ui.theme.ViewAnim;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText mEmailField;
    private Button mResetPasswordButton;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        mEmailField = findViewById(R.id.et_email_reset_password);
        mResetPasswordButton = findViewById(R.id.btn_reset_password);
        mResetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailField.getText().toString();
                if (email.length() > 0) {
                    onResetPassword(email);
                } else {
                    mEmailField.setHintTextColor(Color.RED);
                }
            }
        });
    }

    private void onResetPassword(String email) {
        ViewAnim.toggleLoading(ForgotPasswordActivity.this, mResetPasswordButton, true, getString(R.string.btn_reset_password));
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ViewAnim.toggleLoading(ForgotPasswordActivity.this, mResetPasswordButton, false, getString(R.string.btn_reset_password));
                        if (task.isSuccessful()) {
                            new OneDialog.Builder()
                                    .message(R.string.dialog_message_reset_password)
                                    .buttonClickListener(new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }).show(getSupportFragmentManager(), "reset pass ok");
                        } else {
                            new FailedDialogFragment().show(getSupportFragmentManager(), "no internet");
                        }
                    }
                });

    }
}
