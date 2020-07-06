package cf.bautroixa.maptest.presenter.impl;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.objects.User;
import cf.bautroixa.maptest.presenter.PhoneVerificationPresenter;

public class PhoneVerificationPresenterImpl implements PhoneVerificationPresenter {
    Activity activity;
    View view;
    ModelManager manager;
    FirebaseAuth mAuth;
    String sentVerifyCode, phoneNumber;

    public PhoneVerificationPresenterImpl(Activity activity, View view) {
        this.activity = activity;
        this.view = view;
        this.manager = ModelManager.getInstance(activity);
        this.mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void init() {
        mAuth.getCurrentUser().unlink(PhoneAuthProvider.PROVIDER_ID)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        view.onInitDone();
                        if (!task.isSuccessful()) {
                            view.onVerificationFailed(task.getException().getMessage());
                        }
                    }
                });
    }

    @Override
    public void sendVerificationSms(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        // 60 seconds timeout
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, activity, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                sentVerifyCode = s;
                view.onVerificationSent();
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                view.onVerificationTimeout();
            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                onVerificationCompleted(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                view.onVerificationFailed(e.getLocalizedMessage());
            }
        });
    }

    @Override
    public void verifyCode(String verifyCode) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(sentVerifyCode, verifyCode);
        onVerificationCompleted(credential);
    }

    void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
        mAuth.getCurrentUser().linkWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    manager.getCurrentUser().sendUpdate(null, User.PHONE, phoneNumber);
                    view.onVerificationSuccess(manager.getCurrentUser().getName().length() == 0);
                } else {
                    view.onVerificationFailed(task.getException().getLocalizedMessage());
                }

            }
        });
    }
}
