package cf.bautroixa.maptest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
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
                if (password.equals(confirmPassword) && password.length() >= 8) {
                    register(username, password);
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this,
                            "Xác nhận mật khẩu không trùng khớp", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(RegisterActivity.this,
                            "Mật khẩu phải có ít nhât 8 kí tự", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void register(String username, String password) {
        mAuth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this,
                            "Đăng ký tài khoản thành công", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(RegisterActivity.this,
                            "Địa chỉ email không hợp lệ", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
