package cf.bautroixa.tripgether.presenter;

public interface PhoneVerificationPresenter {
    void init();

    void sendVerificationSms(String phoneNumber);

    void verifyCode(String verifyCode);

    interface View {
        void onInitDone();

        void onVerificationSent();

        void onVerificationSuccess(boolean continueWithProfile);

        void onVerificationTimeout();

        void onVerificationFailed(String reason);
    }
}
