package cf.bautroixa.tripgether.presenter.user;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;

import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.core.DocumentsManager;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.ui.MainActivity;
import cf.bautroixa.tripgether.ui.user.PhoneVerificationActivity;

public class LoginPresenterImpl implements LoginPresenter {
    ModelManager manager;
    private AppCompatActivity activity;
    private View view;
    private FirebaseAuth mAuth;

    public LoginPresenterImpl(AppCompatActivity activity, View view) {
        this.mAuth = FirebaseAuth.getInstance();
        this.activity = activity;
        this.view = view;
        this.manager = ModelManager.getInstance(activity);

        mAuth.useAppLanguage();
    }

    @Override
    public void login(String username, String password) {
        Task<AuthResult> loginTask = mAuth.signInWithEmailAndPassword(username, password);
        checkAuthenticated(loginTask);
    }

    @Override
    public void loginWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        Task<AuthResult> loginTask = mAuth.signInWithCredential(credential);
        checkAuthenticated(loginTask);
    }

    @Override
    public void checkAuthenticated(Task<AuthResult> loginTask) {
        loginTask.addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    afterAuthenticated();
                } else {
                    view.onLoginFailed("sign in failed: " + task.getException().getMessage());
                }
            }
        });
    }

    @Override
    public void onResume() {
        if (mAuth.getCurrentUser() != null) afterAuthenticated();
    }

    private void afterAuthenticated() {
        manager.login(mAuth);
        // manager.getCurrentUser() may (exactly) contain empty User object when login action has't done;
        // use listenGet to make sure User received is not empty
        manager.getBaseUsersManager().requestGet(mAuth.getUid()).addOnCompleteListener(activity, new OnCompleteListener<User>() {
            @Override
            public void onComplete(@NonNull Task<User> task) {
                if (task.isSuccessful()){
                    User user = task.getResult();
                    if (user.getPhoneNumber() != null && user.getPhoneNumber().length() > 5) {
                        Intent intent = new Intent(activity, MainActivity.class);
                        view.onLoginSuccess(intent);
                    } else {
                        mAuth.getCurrentUser().unlink(PhoneAuthProvider.PROVIDER_ID).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Intent intent = new Intent(activity, PhoneVerificationActivity.class);
                                view.onLoginSuccess(intent);
                            }
                        });
                    }
                } else {
                    view.onLoginFailed(task.getException().getMessage());
                }
            }
        });
    }
}
