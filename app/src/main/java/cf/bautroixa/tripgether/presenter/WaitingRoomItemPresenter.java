package cf.bautroixa.tripgether.presenter;

import cf.bautroixa.tripgether.model.repo.objects.UserPublic;

public interface WaitingRoomItemPresenter {
    void allowJoinTrip(UserPublic userPublic);

    interface View {
        void onLoading();

        void onFailed(String reason);

        void onSuccess();
    }
}
