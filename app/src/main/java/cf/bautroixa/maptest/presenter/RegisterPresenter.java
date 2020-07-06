package cf.bautroixa.maptest.presenter;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public interface RegisterPresenter {
    void register(String userName, String password);

    void checkRegistered(Task<AuthResult> regTask);

    interface View {
        void onLoading();

        void onRegisterSuccess();

        void onRegisterFailed(String message);
    }
}
