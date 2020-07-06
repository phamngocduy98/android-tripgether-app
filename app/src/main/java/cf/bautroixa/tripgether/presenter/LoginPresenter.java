package cf.bautroixa.tripgether.presenter;

import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public interface LoginPresenter {
    void login(String username, String password);

    void loginWithGoogle(GoogleSignInAccount account);

    void checkAuthenticated(Task<AuthResult> loginTask);

    void onResume();

    interface View {
        void onLoading();

        void onLoginSuccess(Intent intent);

        void onLoginFailed(String reason);
    }
}
