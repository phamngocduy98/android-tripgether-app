package cf.bautroixa.tripgether.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.ui.user.LoginActivity;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText mOldPasswordField;
    private EditText mNewPasswordField;
    private EditText mConfirmNewPasswordField;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        mOldPasswordField = findViewById(R.id.et_old_password);
        mNewPasswordField = findViewById(R.id.et_new_password);
        mConfirmNewPasswordField = findViewById(R.id.et_confirm_new_password);
        Button mChangePasswordButton = findViewById(R.id.btn_change_password);
        mChangePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = mOldPasswordField.getText().toString();
                String newPassword = mNewPasswordField.getText().toString();
                String confirmNewPassword = mConfirmNewPasswordField.getText().toString();
                if (oldPassword .equals("") || newPassword.equals("")
                        ||  confirmNewPassword .equals("")) {
                    Toast.makeText(ChangePasswordActivity.this,
                            "Vui lòng điền đầy đủ các trường để có thể đổi mật khẩu", Toast.LENGTH_LONG).show();
                } else {
                    if (newPassword.equals(confirmNewPassword)) {
                        onChangePassword(oldPassword, newPassword);
                    } else {
                        Toast.makeText(ChangePasswordActivity.this,
                                "Xác nhận mật khẩu không trùng khớp", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void onChangePassword(String oldPassword, final String newPassword) {
        final FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            if(email!=null) {
                AuthCredential credential = EmailAuthProvider
                        .getCredential(email, oldPassword);

// Prompt the user to re-provide their sign-in credentials
                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    changePassword(user,newPassword);
                                }else{
                                    Toast.makeText(ChangePasswordActivity.this,
                                            "Mật khẩu cũ không chính xác",Toast.LENGTH_LONG).show();
                                }

                            }

                        });

            }
        }
    }

    private void changePassword(FirebaseUser user, String newPassword) {
        user.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ChangePasswordActivity.this,
                                    "Đổi mật khẩu thành công",Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    }
                });
    }


}
