package cf.bautroixa.tripgether.presenter.user;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterPresenterImpl implements RegisterPresenter {
    FirebaseAuth mAuth;
    Activity activity;
    View view;

    public RegisterPresenterImpl(Activity activity, View view) {
        this.mAuth = FirebaseAuth.getInstance();
        this.activity = activity;
        this.view = view;
    }

    @Override
    public void register(String userName, String password) {
        view.onLoading();
        Task<AuthResult> regTask = mAuth.createUserWithEmailAndPassword(userName, password);
        checkRegistered(regTask);
    }

    @Override
    public void checkRegistered(Task<AuthResult> regTask) {
        regTask.addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    view.onRegisterSuccess();
                } else {
                    view.onRegisterFailed(task.getException().getMessage());
                }
            }
        });
    }
}
