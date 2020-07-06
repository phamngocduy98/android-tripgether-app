package cf.bautroixa.maptest.presenter;

import cf.bautroixa.maptest.model.repo.objects.UserPublic;

public interface WaitingRoomItemPresenter {
    void allowJoinTrip(UserPublic userPublic);

    interface View {
        void onLoading();

        void onFailed(String reason);

        void onSuccess();
    }
}
