package cf.bautroixa.maptest.ui.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.constant.RequestCodes;
import cf.bautroixa.maptest.presenter.LoginPresenter;
import cf.bautroixa.maptest.presenter.impl.LoginPresenterImpl;
import cf.bautroixa.maptest.ui.dialogs.LoadingDialogHelper;
import cf.bautroixa.maptest.ui.theme.LoadingDialogFragment;

public class LoginActivity extends AppCompatActivity implements LoginPresenter.View {
    private static final String TAG = "LoginActivity";
    private static final String googleClientId = "703604566706-upp9g9rtcdh3adrflqcgddt4p712jh27.apps.googleusercontent.com";
    LoginPresenterImpl loginPresenter;
    private GoogleSignInClient mGoogleSignInClient;
    private EditText mUsernameField;
    private EditText mPasswordField;
    private LoadingDialogFragment loadingDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginPresenter = new LoginPresenterImpl(this, this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(googleClientId)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mUsernameField = findViewById(R.id.et_username);
        mPasswordField = findViewById(R.id.et_password);
        Button mLoginButton = findViewById(R.id.btn_login);
        Button mSignUpButton = findViewById(R.id.btn_register);
        Button mGoogleSignInButton = findViewById(R.id.btn_gg_sign_in);
        EditText mForgotPasswordText = findViewById(R.id.tv_forgotPassword);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateInput();
            }
        });
        mGoogleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RequestCodes.GOOGLE_SIGN_IN);
            }
        });
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        mForgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loginPresenter.onResume();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) loginPresenter.loginWithGoogle(account);
            } catch (ApiException e) {
                onLoginFailed("Google sign in failed" + e.getMessage());
            }
        }
    }

    void validateInput() {
        if (mUsernameField.getText().length() == 0) {
            mUsernameField.setHintTextColor(Color.RED);
            return;
        }
        if (mPasswordField.getText().length() == 0) {
            mPasswordField.setHintTextColor(Color.RED);
            return;
        }
        loginPresenter.login(mUsernameField.getText().toString(), mPasswordField.getText().toString());
    }

    @Override
    public void onLoading() {
        loadingDialogFragment = LoadingDialogHelper.create(getSupportFragmentManager());
    }

    @Override
    public void onLoginSuccess(Intent intent) {
        if (loadingDialogFragment != null) loadingDialogFragment.dismiss();
        if (intent != null) startActivity(intent);
        finish();
    }

    @Override
    public void onLoginFailed(String reason) {
        if (loadingDialogFragment != null) loadingDialogFragment.dismiss();
        Toast.makeText(this, reason, Toast.LENGTH_LONG).show();
    }
}
